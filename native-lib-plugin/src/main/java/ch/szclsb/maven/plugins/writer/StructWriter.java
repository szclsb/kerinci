package ch.szclsb.maven.plugins.writer;

import ch.szclsb.maven.plugins.Context;
import ch.szclsb.maven.plugins.LibcCursor;
import ch.szclsb.maven.plugins.LibcType;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StructWriter extends FileWriter {
    public record StructField(
            String name,
            String layout,
            int bytes
    ) {
    }

    private final String generatedPackage;

    public StructWriter(Log logger, Path dir, String generatedPackage) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
    }

    private StructField declare(String name, LibcCursor typeCursor, Context context) throws IOException {
        var typeName = typeCursor.getSpelling();
        return switch (typeName) {
            case "uint32_t" -> new StructField(name, "JAVA_INT", 4);
            case "uint64_t" -> new StructField(name, "JAVA_LONG", 8);
            default -> {
                var decl = context.declare(typeName);
                yield switch (decl.kind()) {
                    case LibcCursor.KIND_ENUM -> new StructField(name, "JAVA_INT", 4);
                    case LibcCursor.KIND_STRUCT -> new StructField(name, typeName + ".LAYOUT", decl.bytes());
                    case "XXX" -> new StructField(name, "ADDRESS", 8);  //TODO typeref and function pointer
                    default -> throw new IllegalArgumentException("Unexpected declaration: " + decl.kind());
                };
            }
        };
    }

    public int write(LibcCursor structCursor, Context context) throws IOException {
        var className = structCursor.getSpelling();
        logger.info("-- declaring struct: " + className);
        var fields = new ArrayList<StructField>();
        for (var fieldCursor : structCursor.getChildren()) {
            if (LibcCursor.KIND_FIELD.equals(fieldCursor.getKind())) {
                var fieldName = fieldCursor.getSpelling();
                logger.debug("---- resolving field: %s".formatted(fieldName));
                fields.add(switch (fieldCursor.getType().getKind()) {
                    case LibcType.KIND_ELABORATED -> declare(fieldName, fieldCursor.getChildren().getFirst(), context);
                    case LibcType.KIND_POINTER, LibcType.KIND_ARRAY -> new StructField(fieldName, "ADDRESS", 8);
                    case LibcType.KIND_INT -> new StructField(fieldName, "JAVA_INT", 4);
                    case LibcType.KIND_FLOAT -> new StructField(fieldName, "JAVA_FLOAT", 4);
                    default ->
                            throw new IllegalArgumentException("Unexpected field type king: " + fieldCursor.getType().getKind());
                });
            }
        }

        var bytes = new AtomicInteger(0);
        writeFile(className, writer -> {
            writer.write("""
                    // GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
                    package %s;
                    
                    import ch.szclsb.kerinci.internal.Struct;
                    
                    import java.lang.foreign.MemoryLayout;
                    import java.lang.foreign.MemorySegment;
                    import java.lang.foreign.StructLayout;
                    
                    import static java.lang.foreign.ValueLayout.ADDRESS;
                    import static java.lang.foreign.ValueLayout.JAVA_INT;
                    import static java.lang.foreign.ValueLayout.JAVA_FLOAT;
                    
                    public class %s implements Struct {
                        public static final StructLayout LAYOUT = MemoryLayout.structLayout(
                    """.formatted(generatedPackage, className));
            var it = fields.iterator();
            while (it.hasNext()) {
                var field = it.next();
                var offset = bytes.get();
                var padding = offset % 8;
                if (field.bytes >= 8 && (padding > 0)) {  // todo verify padding
                    writer.write("""
                                    MemoryLayout.paddingLayout(%d),
                            """.formatted(padding));
                    offset += padding;
                }
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

            // FIXME getter + setter

            writer.write("""
                    }
                    """);
        });
        return bytes.get();
    }
}
