package ch.szclsb.maven.plugins.writer.struct;

import ch.szclsb.maven.plugins.Context;

import java.io.IOException;
import java.io.Writer;

public class BitMaskStructField extends AbstractStructField {
    public BitMaskStructField(String fieldName, String memoryLayout, long bytes, String javaType) {
        super(fieldName, memoryLayout, bytes, javaType);
    }

    public BitMaskStructField(String fieldName, Context.Declaration declaration) {
        super(fieldName, declaration);
    }

    @Override
    public void writeFieldGetter(Writer structWriter, long fieldOffset) throws IOException {
        structWriter.write("""
                
                    public BitMask<%s> %s() {
                        return new BitMask<>(pSegment.get(%s, %d));
                    }
                """.formatted(javaType, getFieldMethodGet(), memoryLayout, fieldOffset));
    }

    @Override
    public void writeFieldSetter(Writer structWriter, long fieldOffset) throws IOException {
        structWriter.write("""
                
                    public void %s(BitMask<%s> mask) {
                        pSegment.set(%s, %d, mask.getValue());
                    }
                """.formatted(getFieldMethodSet(), javaType, memoryLayout, fieldOffset));
    }

    @Override
    public void writeBuilderMethod(Writer structWriter) throws IOException {
        structWriter.write("""
                
                        public Builder %1$s(BitMask<%2$s> mask) {
                            instance.%1$s(mask);
                            return this;
                        }
                """.formatted(getFieldMethodSet(), javaType));
    }
}
