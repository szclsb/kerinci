package ch.szclsb.maven.plugins.writer.convertor;

public class PointerConvertor extends FieldConverter {
    @Override
    public String getterAccessor(String fieldLayout, long fieldOffset, String javaClass) {
        return "new %s(pSegment.asSlice(%d, %s))".formatted(javaClass, fieldOffset, fieldLayout);
    }

    @Override
    public String setterAccessor(String fieldLayout, long fieldOffset) {
        return "pSegment.set(%s, %d, value.getSegment())".formatted(fieldLayout, fieldOffset);
    }
}
