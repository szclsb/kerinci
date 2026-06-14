package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

// move string to templates?
public class FtlTemplateMethodStructFieldElaborated implements FtlTemplateMethodStructField {
    @Override
    public String getterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return """
                    public %s get_%s() {
                        return new %s(pSegment.asSlice(%d, %s));
                    }""".formatted(javaType, fieldName, javaType, fieldOffset, memoryLayout);
    }

    @Override
    public String setterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return "";  // ignore or copy memory
    }

    @Override
    public String builderMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return "";  // ignore or copy memory
    }
}
