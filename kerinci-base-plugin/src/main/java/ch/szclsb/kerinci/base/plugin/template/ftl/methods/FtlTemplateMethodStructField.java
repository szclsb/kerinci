package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;

public interface FtlTemplateMethodStructField {
    String getterMethod(StructTemplateDefinition.Field field);

    String setterMethod(StructTemplateDefinition.Field field, String varName);
}
