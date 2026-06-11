package ch.szclsb.kerinci.base.plugin.libc.writer.struct;

import ch.szclsb.kerinci.base.plugin.libc.LibcContext;

import java.io.IOException;
import java.io.Writer;

public class EnumStructField extends AbstractStructField {
    public EnumStructField(String fieldName, String memoryLayout, long bytes, String javaType) {
        super(fieldName, memoryLayout, bytes, javaType);
    }

    public EnumStructField(String fieldName, LibcContext.Declaration declaration) {
        super(fieldName, declaration);
    }

    @Override
    public void writeFieldGetter(Writer structWriter, long fieldOffset) throws IOException {
        structWriter.write("""
                
                    public %s %s() {
                        return %s.ofValue(pSegment.get(%s, %d));
                    }
                """.formatted(javaType, getFieldMethodGet(), javaType, memoryLayout, fieldOffset));
    }

    @Override
    public void writeFieldSetter(Writer structWriter, long fieldOffset) throws IOException {
        structWriter.write("""
                
                    public void %s(%s value) {
                        pSegment.set(%s, %d, value.getValue());
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
