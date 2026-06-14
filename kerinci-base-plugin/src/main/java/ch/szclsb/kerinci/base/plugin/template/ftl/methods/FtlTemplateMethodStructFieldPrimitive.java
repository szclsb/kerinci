package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;

// move string to templates?
public class FtlTemplateMethodStructFieldPrimitive implements FtlTemplateMethodStructField {
    @Override
    public String getterMethod(StructTemplateDefinition.Field field) {
        return "pSegment.get(%s, %d)".formatted(
                field.definition().memoryLayout(),
                field.offset()
        );
    }

    @Override
    public String setterMethod(StructTemplateDefinition.Field field, String varName) {
        return "pSegment.set(%s, %d, %s)".formatted(
                field.definition().memoryLayout(),
                field.offset(),
                varName
        );
    }
}
