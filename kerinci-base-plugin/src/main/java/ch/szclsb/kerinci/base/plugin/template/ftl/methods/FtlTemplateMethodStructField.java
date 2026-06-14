package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;

public interface FtlTemplateMethodStructField {
    String readField(StructTemplateDefinition.Field field);

    String writeField(StructTemplateDefinition.Field field, String varName);
}
