package ch.szclsb.kerinci.base.plugin.writer;

import ch.szclsb.kerinci.base.plugin.Context;
import ch.szclsb.kerinci.base.plugin.libc.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.LibcType;
import ch.szclsb.kerinci.base.plugin.writer.struct.*;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StructWriter extends FileWriter {
    private static final String STRUCTURE_TYPE_FIELD = "sType";

    private final String generatedPackage;
    private final boolean enableBuilder;

    public StructWriter(Log logger, Path dir, String generatedPackage, boolean enableBuilder) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        this.enableBuilder = enableBuilder;
    }

    private StructField declare(String fieldName, LibcCursor typeCursor, Context context) throws IOException {
        var typeName = typeCursor.getSpelling();
        var decl = context.declare(typeName);
        if (decl == null || decl.isPointer()) {
            return null;  //TODO typeref and function pointer;
        }
        if (decl.isFlag()) {
            return new BitMaskStructField(fieldName, decl);
        }
        if (LibcCursor.KIND_ENUM.equals(decl.cursorKind())) {
            return new EnumStructField(fieldName, decl);
        }
        if (LibcCursor.KIND_STRUCT.equals(decl.cursorKind())) {
            return new ElaboratedStructField(fieldName, decl);
        }
        return new PrimitiveStructField(fieldName, decl);
    }

    /**
     * @param className
     * @param structCursor
     * @param context
     * @return
     * @throws IOException
     */
    public long write(String className, LibcCursor structCursor, Context context) throws IOException {
        logger.info("-- declaring struct: %s (%s)".formatted(className, structCursor.getSpelling()));
        var fields = new ArrayList<StructField>();
        for (var fieldCursor : structCursor.getChildren()) {
            if (LibcCursor.KIND_FIELD.equals(fieldCursor.getKind())) {
                var fieldName = fieldCursor.getSpelling();
                logger.debug("---- resolving field: %s".formatted(fieldName));
                var field = switch (fieldCursor.getType().getKind()) {
                    case LibcType.KIND_ELABORATED -> declare(fieldName, fieldCursor.getChildren().getFirst(), context);
                    case LibcType.KIND_POINTER, LibcType.KIND_ARRAY ->
                            new PrimitiveStructField(fieldName, "ADDRESS", 8, "MemorySegment");
                    case LibcType.KIND_INT -> new PrimitiveStructField(fieldName, "JAVA_INT", 4, "int");
                    case LibcType.KIND_FLOAT -> new PrimitiveStructField(fieldName, "JAVA_FLOAT", 4, "float");
                    default ->
                            throw new IllegalArgumentException("Unexpected field type kind: " + fieldCursor.getType().getKind());
                };
                if (field == null) {
                    logger.warn("---- ignoring field %s, because resolved declaration is null".formatted(fieldName));
                } else {
                    fields.add(field);
                }
            }
        }

        var bytes = new AtomicLong(0);
        writeFile(className, writer -> {
            //TODO: fetch BitMask implementation name form external source
            writer.write("""
                    // GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
                    package %s;
                    
                    import ch.szclsb.kerinci.base.api.BitMask;
                    import ch.szclsb.kerinci.base.api.ForeignObject;
                    
                    import ch.szclsb.kerinci.internal.KerinciBitMask;
                    
                    import java.lang.foreign.MemoryLayout;
                    import java.lang.foreign.MemorySegment;
                    import java.lang.foreign.SegmentAllocator;
                    import java.lang.foreign.StructLayout;
                    
                    import static java.lang.foreign.ValueLayout.ADDRESS;
                    import static java.lang.foreign.ValueLayout.JAVA_BOOLEAN;
                    import static java.lang.foreign.ValueLayout.JAVA_LONG;
                    import static java.lang.foreign.ValueLayout.JAVA_INT;
                    import static java.lang.foreign.ValueLayout.JAVA_FLOAT;
                    
                    public class %s implements ForeignObject {
                        public static final StructLayout LAYOUT = MemoryLayout.structLayout(
                    """.formatted(generatedPackage, className));
            var offsets = new HashMap<StructField, Long>();
            var it = fields.iterator();
            while (it.hasNext()) {
                var field = it.next();
                var offset = bytes.get();
                var m = offset % field.getByteSize();
                if (m > 0) {
                    var padding = field.getByteSize() - m;
                    writer.write("""
                                    MemoryLayout.paddingLayout(%d),
                            """.formatted(padding));
                    offset += padding;
                }
                offsets.put(field, offset);
                field.writeMemoryLayoutElement(writer, it.hasNext());
                bytes.set(offset + field.getByteSize());
            }

            writer.write("""
                        ).withName("%1$s");
                    
                        private final MemorySegment pSegment;
                        private final int index;
                    
                        public %1$s(MemorySegment pSegment) {
                            this(pSegment, 0);
                        }
                    
                        public %1$s(MemorySegment pSegment, int index) {
                            this.pSegment = pSegment;
                            this.index = index;
                        }
                    
                        @Override
                        public MemorySegment getSegment() {
                            return pSegment.asReadOnly();
                        }
                    
                    """.formatted(className));

            // getter and setter
            for (var field : fields) {
                var fieldOffset = offsets.get(field);
                field.writeFieldGetter(writer, fieldOffset);
                field.writeFieldSetter(writer, fieldOffset);
            }

            if (enableBuilder) {
                writer.write("""
                        
                            public static Builder builder(SegmentAllocator allocator) {
                                return new Builder(allocator);
                            };
                        
                            public static class Builder {
                                private final %1$s instance;
                        
                                private Builder(SegmentAllocator allocator) {
                                    this.instance = new %1$s(allocator.allocate(LAYOUT));
                        """.formatted(className));
                if (fields.stream().anyMatch(field -> STRUCTURE_TYPE_FIELD.equals(field.getFieldName()))) {
                    // set sType if present
                    var sb = new StringBuilder();
                    var charArray = className.substring(2).toCharArray();
                    for (var i = 0; i < charArray.length; i++) {
                        var c = charArray[i];
                        var cp = i - 1 < 0 ? null : charArray[i - 1];
                        if (Character.isUpperCase(c) && cp != null && Character.isLowerCase(cp)) {
                            sb.append("_");
                        }
                        sb.append(Character.toUpperCase(c));
                    }
                    var sType = "ch.szclsb.kerinci.api.VkStructureType.VK_STRUCTURE_TYPE_" + sb;
                    writer.write("""
                                        this.instance.setSType(%s);
                            """.formatted(sType));
                }
                writer.write("""
                                }
                        
                                public %s build() {
                                    return instance;
                                }
                        """.formatted(className));

                for (var field : fields) {
                    if (!STRUCTURE_TYPE_FIELD.equals(field.getFieldName())) {
                        field.writeBuilderMethod(writer);
                    }
                }
                writer.write("""
                            }
                        """);
            }

            writer.write("""
                    }
                    """);
        });
        return bytes.get();
    }
}
