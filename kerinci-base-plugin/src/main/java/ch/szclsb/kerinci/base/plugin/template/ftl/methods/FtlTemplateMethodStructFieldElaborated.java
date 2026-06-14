package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

// move string to templates?
public class FtlTemplateMethodStructFieldElaborated implements FtlTemplateMethodStructField {
    @Override
    public String getterMethod(String javaType, String memoryLayout, long fieldOffset) {
        return "new %s(pSegment.asSlice(%d, %s))"
                .formatted(javaType, fieldOffset, memoryLayout);
    }

    @Override
    public String setterMethod(String javaType, String memoryLayout, long fieldOffset, String varName) {
        return "throw new UnsupportedOperationException()";  // ignore or copy memory
    }
}
