package ch.szclsb.maven.plugins.writer.convertor;

import ch.szclsb.maven.plugins.writer.StructWriter;

public class PointerConvertor extends FieldConverter {
    @Override
    public String getterAccessor(StructWriter.StructField field) {
        return "new %s(%s)".formatted(field.javaType(), super.getterAccessor(field));
    }

    @Override
    public String setterAccessor(StructWriter.StructField field) {
        return "%s.getSegment()".formatted(super.setterAccessor(field));
    }
}
