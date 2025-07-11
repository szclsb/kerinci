package ch.szclsb.kerinci.base.plugin.writer.struct;

import java.io.IOException;
import java.io.Writer;

public interface StructField {
    String getFieldName();

    long getByteSize();

    void writeMemoryLayoutElement(Writer structWriter, boolean hasNext) throws IOException;

    void writeFieldGetter(Writer structWriter, long fieldOffset) throws IOException;

    void writeFieldSetter(Writer structWriter, long fieldOffset) throws IOException;

    void writeBuilderMethod(Writer structWriter) throws IOException;
}
