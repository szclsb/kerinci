package ch.szclsb.maven.plugins.writer.convertor;

import ch.szclsb.maven.plugins.writer.StructWriter;

public class FieldConverter {
    public String getterAccessor(StructWriter.StructField field) {
        return "value";
    }

    public String setterAccessor(StructWriter.StructField field) {
        return "value";
    }
}
