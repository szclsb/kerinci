package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;

// move string to templates?
public class FtlTemplateMethodStructFieldEnum implements FtlTemplateMethodStructField {
    @Override
    public String readField(StructTemplateDefinition.Field field) {
        return "%s.ofValue(pSegment.get(%s, %d))".formatted(
                field.definition().javaType(),
                field.definition().memoryLayout(),
                field.offset()
        );
    }

    @Override
    public String writeField(StructTemplateDefinition.Field field, String varName) {
        return "pSegment.set(%s, %d, %s.getValue())".formatted(
                field.definition().memoryLayout(),
                field.offset(),
                varName
        );
    }
}
