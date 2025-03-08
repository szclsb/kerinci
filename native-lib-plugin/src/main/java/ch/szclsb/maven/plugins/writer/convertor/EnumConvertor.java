package ch.szclsb.maven.plugins.writer.convertor;

import ch.szclsb.maven.plugins.writer.StructWriter;

public class EnumConvertor extends FieldConverter {
    @Override
    public String getterAccessor(StructWriter.StructField field) {
        return "%s.ofValue(%s)".formatted(field.javaType(), super.getterAccessor(field));
    }

    @Override
    public String setterAccessor(StructWriter.StructField field) {
        return "%s.getValue()".formatted(super.setterAccessor(field));
    }
}
