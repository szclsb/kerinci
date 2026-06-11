package ch.szclsb.kerinci.base.plugin.libc.writer.struct;

import ch.szclsb.kerinci.base.plugin.libc.LibcContext;

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractStructField implements StructField {
    public static final String METHOD_PREFIX_GET = "get";
    public static final String METHOD_PREFIX_SET = "set";

    protected final String fieldName;
    protected final String memoryLayout;
    protected final long byteSize;
    protected final String javaType;

    public AbstractStructField(String fieldName, String memoryLayout, long byteSize, String javaType) {
        this.fieldName = fieldName;
        this.memoryLayout = memoryLayout;
        this.byteSize = byteSize;
        this.javaType = javaType;
    }

    public AbstractStructField(String fieldName, LibcContext.Declaration declaration) {
        this(fieldName, declaration.javaLayout(), declaration.bytes(), declaration.javaType());
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public long getByteSize() {
        return byteSize;
    }

    @Override
    public void writeMemoryLayoutElement(Writer structWriter, boolean hasNext) throws IOException {
        structWriter.write("""
                        %s.withName("%s")%s
                """.formatted(memoryLayout, fieldName, hasNext ? "," : ""));
    }

    public String getFieldMethod(String prefix) {
        var firstChar = fieldName.charAt(0);
        return prefix + Character.toUpperCase(firstChar) + fieldName.substring(1);
    }

    public String getFieldMethodGet() {
        return getFieldMethod(METHOD_PREFIX_GET);
    }

    public String getFieldMethodSet() {
        return getFieldMethod(METHOD_PREFIX_SET);
    }
}
