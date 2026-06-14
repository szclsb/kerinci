package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

// move string to templates?
public class FtlTemplateMethodStructFieldPrimitive implements FtlTemplateMethodStructField {
    @Override
    public String getterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return """
                    public %s get_%s() {
                        return pSegment.get(%s, %d);
                    }""".formatted(javaType, fieldName, memoryLayout, fieldOffset);
    }

    @Override
    public String setterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return """
                    public void set_%s(%s value) {
                        pSegment.set(%s, %d, value);
                    }""".formatted(fieldName, javaType, memoryLayout, fieldOffset);
    }

    @Override
    public String builderMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return """
                public Builder set_%1$s(%2$s value) {
                    instance.set_%1$s(value);
                    return this;
                }""".formatted(fieldName, javaType);
    }
}
