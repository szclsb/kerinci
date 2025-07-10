package ch.szclsb.maven.plugins.writer.struct;

import ch.szclsb.maven.plugins.Context;

import java.io.IOException;
import java.io.Writer;

public class ElaboratedStructField extends AbstractStructField {
    public ElaboratedStructField(String fieldName, String memoryLayout, long byteSize, String javaType) {
        super(fieldName, memoryLayout, byteSize, javaType);
    }

    public ElaboratedStructField(String fieldName, Context.Declaration declaration) {
        super(fieldName, declaration);
    }

    @Override
    public void writeFieldGetter(Writer structWriter, long fieldOffset) throws IOException {
        // TODO use JEP 502: Stable Values if available
        structWriter.write("""
                
                    public %s %s() {
                        return new %s(pSegment.asSlice(%d, %s));
                    }
                """.formatted(javaType, getFieldMethodGet(), javaType, fieldOffset, memoryLayout));
    }

    @Override
    public void writeFieldSetter(Writer structWriter, long fieldOffset) throws IOException {
        // ignore or copy memory
    }

    @Override
    public void writeBuilderMethod(Writer structWriter) throws IOException {
        // TODO separate builder handle
    }
}
