package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;

// move string to templates?
public class FtlTemplateMethodStructFieldElaborated implements FtlTemplateMethodStructField {
    @Override
    public String getterMethod(StructTemplateDefinition.Field field) {
        return "new %s(pSegment.asSlice(%d, %s))".formatted(
                field.definition().javaType(),
                field.offset(),
                field.definition().memoryLayout()
        );
    }

    @Override
    public String setterMethod(StructTemplateDefinition.Field field, String varName) {
        return "throw new UnsupportedOperationException()";  // ignore or copy memory
    }
}
