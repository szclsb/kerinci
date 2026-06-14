package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

public interface FtlTemplateMethodStructField {
    String getterMethod(String javaType, String memoryLayout, long fieldOffset);

    String setterMethod(String javaType, String memoryLayout, long fieldOffset, String varName);
}
