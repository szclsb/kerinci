package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

// move string to templates?
public class FtlTemplateMethodStructFieldEnum implements FtlTemplateMethodStructField {
    @Override
    public String getterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return "%s.ofValue(pSegment.get(%s, %d))"
                .formatted(javaType, memoryLayout, fieldOffset);
    }

    @Override
    public String setterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return "pSegment.set(%s, %d, value.getValue())".formatted(memoryLayout, fieldOffset);
    }
}
