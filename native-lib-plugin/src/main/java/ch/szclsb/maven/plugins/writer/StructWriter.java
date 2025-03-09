package ch.szclsb.maven.plugins.writer;

import ch.szclsb.maven.plugins.Context;
import ch.szclsb.maven.plugins.LibcCursor;
import ch.szclsb.maven.plugins.LibcType;
import ch.szclsb.maven.plugins.writer.convertor.EnumConvertor;
import ch.szclsb.maven.plugins.writer.convertor.FieldConverter;
import ch.szclsb.maven.plugins.writer.convertor.PointerConvertor;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StructWriter extends FileWriter {
    private static final String STRUCTURE_TYPE_FIELD = "sType";
    private final FieldConverter defaultFieldConverter = new FieldConverter();
    private final FieldConverter enumFieldConverter = new EnumConvertor();
    private final FieldConverter refFieldConvertor = new PointerConvertor();

    public record StructField(
            String name,
            String layout,
            FieldConverter converter,
            long bytes,
            String javaType
    ) {
    }

    private final String generatedPackage;
    private final boolean enableBuilder;

    public StructWriter(Log logger, Path dir, String generatedPackage, boolean enableBuilder) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        this.enableBuilder = enableBuilder;
    }

    private StructField declare(String name, LibcCursor typeCursor, Context context) throws IOException {
        var typeName = typeCursor.getSpelling();
        var decl = context.declare(typeName);
        if (decl == null) {
            return new StructField(name, "UNDEFINED", defaultFieldConverter, 8, "MemoryLayout");  //TODO typeref and function pointer;
        }
        if (decl.isPointer()) {
            return new StructField(name, "ADDRESS", refFieldConvertor, 8, decl.javaType());
        }
        return new StructField(name, decl.javaLayout(), LibcCursor.KIND_ENUM.equals(decl.cursorKind()) ? enumFieldConverter : defaultFieldConverter, decl.bytes(), decl.javaType());
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
                fields.add(switch (fieldCursor.getType().getKind()) {
                    case LibcType.KIND_ELABORATED -> declare(fieldName, fieldCursor.getChildren().getFirst(), context);
                    case LibcType.KIND_POINTER, LibcType.KIND_ARRAY ->
                            new StructField(fieldName, "ADDRESS", defaultFieldConverter, 8, "MemorySegment");
                    case LibcType.KIND_INT -> new StructField(fieldName, "JAVA_INT", defaultFieldConverter, 4, "int");
                    case LibcType.KIND_FLOAT ->
                            new StructField(fieldName, "JAVA_FLOAT", defaultFieldConverter, 4, "float");
                    default ->
                            throw new IllegalArgumentException("Unexpected field type king: " + fieldCursor.getType().getKind());
                });
            }
        }

        var bytes = new AtomicLong(0);
        writeFile(className, writer -> {
            writer.write("""
                    // GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
                    package %s;
                    
                    import ch.szclsb.kerinci.internal.Struct;
                    
                    import java.lang.foreign.MemoryLayout;
                    import java.lang.foreign.MemorySegment;
                    import java.lang.foreign.SegmentAllocator;
                    import java.lang.foreign.StructLayout;
                    
                    import static java.lang.foreign.ValueLayout.ADDRESS;
                    import static java.lang.foreign.ValueLayout.JAVA_BOOLEAN;
                    import static java.lang.foreign.ValueLayout.JAVA_LONG;
                    import static java.lang.foreign.ValueLayout.JAVA_INT;
                    import static java.lang.foreign.ValueLayout.JAVA_FLOAT;
                    
                    public class %s implements Struct {
                        public static final StructLayout LAYOUT = MemoryLayout.structLayout(
                    """.formatted(generatedPackage, className));
            var offsets = new HashMap<String, Long>();
            var it = fields.iterator();
            while (it.hasNext()) {
                var field = it.next();
                var offset = bytes.get();
                var m = offset % field.bytes;
                if (m > 0) {
                    var padding = field.bytes - m;
                    writer.write("""
                                    MemoryLayout.paddingLayout(%d),
                            """.formatted(padding));
                    offset += padding;
                }
                offsets.put(field.name, offset);
                writer.write("""
                                %s.withName("%s")%s
                        """.formatted(field.layout, field.name, it.hasNext() ? "," : ""));
                bytes.set(offset + field.bytes);
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
                var fieldOffset = offsets.get(field.name);
                var firstChar = field.name.charAt(0);
                var fieldName = Character.toUpperCase(firstChar) + field.name.substring(1);
                var getterAccessor = field.converter.getterAccessor(field);
                writer.write("""
                        
                            public %s get%s() {
                                var value = pSegment.get(%s, %d);
                                return %s;
                            }
                        """.formatted(field.javaType, fieldName, field.layout(), fieldOffset, getterAccessor));
                var setterAccessor = field.converter.setterAccessor(field);
                writer.write("""
                        
                            public void set%s(%s value) {
                                pSegment.set(%s, %d, %s);
                            }
                        """.formatted(fieldName, field.javaType, field.layout(), fieldOffset, setterAccessor));
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
                if (fields.stream().anyMatch(field -> STRUCTURE_TYPE_FIELD.equals(field.name()))) {
                    // set sType if present
                    var sb = new StringBuilder();
                    for (var c : className.substring(2).toCharArray()) {
                        if (Character.isUpperCase(c)) {
                            sb.append("_");
                        }
                        sb.append(Character.toUpperCase(c));
                    }
                    var sType = "ch.szclsb.kerinci.api.VkStructureType.VK_STRUCTURE_TYPE" + sb;
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
                    if (!STRUCTURE_TYPE_FIELD.equals(field.name())) {
                        var firstChar = field.name.charAt(0);
                        var fieldName = Character.toUpperCase(firstChar) + field.name.substring(1);
                        writer.write("""
                                
                                        public Builder set%1$s(%2$s value) {
                                            instance.set%1$s(value);
                                            return this;
                                        }
                                """.formatted(fieldName, field.javaType));
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
