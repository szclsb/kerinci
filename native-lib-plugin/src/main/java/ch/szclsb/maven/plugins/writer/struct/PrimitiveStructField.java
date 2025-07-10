package ch.szclsb.maven.plugins.writer.struct;

import ch.szclsb.maven.plugins.Context;

import java.io.IOException;
import java.io.Writer;

public class PrimitiveStructField extends AbstractStructField {
    public PrimitiveStructField(String fieldName, String memoryLayout, long byteSize, String javaType) {
        super(fieldName, memoryLayout, byteSize, javaType);
    }

    public PrimitiveStructField(String fieldName, Context.Declaration declaration) {
        super(fieldName, declaration);
    }

    @Override
    public void writeFieldGetter(Writer structWriter, long fieldOffset) throws IOException {
        structWriter.write("""
                
                    public %s %s() {
                        return pSegment.get(%s, %d);
                    }
                """.formatted(javaType, getFieldMethodGet(), memoryLayout, fieldOffset));
    }

    @Override
    public void writeFieldSetter(Writer structWriter, long fieldOffset) throws IOException {
        structWriter.write("""
                
                    public void %s(%s value) {
                        pSegment.set(%s, %d, value);
                    }
                """.formatted(getFieldMethodSet(), javaType, memoryLayout, fieldOffset));
    }

    @Override
    public void writeBuilderMethod(Writer structWriter) throws IOException {
        structWriter.write("""
                
                        public Builder %1$s(%2$s value) {
                            instance.%1$s(value);
                            return this;
                        }
                """.formatted(getFieldMethodSet(), javaType));
    }
}
