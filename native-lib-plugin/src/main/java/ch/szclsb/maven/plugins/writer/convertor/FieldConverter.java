package ch.szclsb.maven.plugins.writer.convertor;

public class FieldConverter {
    // memory segment variable = pSegment
    // setter variable = value



    public String getterAccessor(String fieldLayout, long fieldOffset, String javaClass) {
        return "pSegment.get(%s, %d)".formatted(fieldLayout, fieldOffset);
    }

    public String setterAccessor(String fieldLayout, long fieldOffset) {
        return "pSegment.set(%s, %d, value)".formatted(fieldLayout, fieldOffset);
    }
}
