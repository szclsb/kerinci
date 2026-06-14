package ch.szclsb.kerinci.base.plugin.template;

import ch.szclsb.kerinci.base.plugin.libc.LibcContext;

import java.util.List;
import java.util.OptionalLong;

public record StructTemplateDefinition(
        List<StructTemplateDefinition.Field> fields,
        boolean enableBuilder
) implements TemplateDefinition {
    public enum FieldType {
        PRIMITIVE,
        ENUM,
        BITMASK,
        ELABORATED
    }

    public record Field(
            FieldDefinition definition,
            long offset,
            long padding
    ) {
        public long memoryLength() {
            return definition.bytes + padding;
        }
    }

    public record FieldDefinition(
            FieldType dType,
            String name,
            String javaType,
            String memoryLayout,
            long bytes) {
        public static FieldDefinition ofInt(String name) {
            return new FieldDefinition(FieldType.PRIMITIVE, name, "int", "JAVA_INT", 4);
        }
        public static FieldDefinition ofFloat(String name) {
            return new FieldDefinition(FieldType.PRIMITIVE, name, "float", "JAVA_FLOAT", 4);
        }
        public static FieldDefinition ofAddress(String name) {
            return new FieldDefinition(FieldType.PRIMITIVE, name, "MemorySegment", "ADDRESS", 8);
        }
        public static FieldDefinition ofDecl(FieldType fieldType, String name, LibcContext.Declaration declaration) {
            return new FieldDefinition(fieldType, name, declaration.javaType(), declaration.javaLayout(), declaration.bytes());
        }
    }

    @Override
    public OptionalLong javaObjectBytes() {
        return fields.stream()
                .mapToLong(Field::memoryLength)
                .reduce(Long::sum);
    }
}

