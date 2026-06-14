package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

// move string to templates?
public class FtlTemplateMethodStructFieldPrimitive implements FtlTemplateMethodStructField {
    @Override
    public String getterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return "pSegment.get(%s, %d)".formatted(memoryLayout, fieldOffset);
    }

    @Override
    public String setterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return "pSegment.set(%s, %d, %s)".formatted(memoryLayout, fieldOffset, fieldName);
    }
}
