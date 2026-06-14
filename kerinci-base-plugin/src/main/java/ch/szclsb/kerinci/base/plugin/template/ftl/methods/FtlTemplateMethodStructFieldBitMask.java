package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;

// move string to templates?
public class FtlTemplateMethodStructFieldBitMask implements FtlTemplateMethodStructField {
    //TODO: fetch BitMask implementation name form external source

    @Override
    public String readField(StructTemplateDefinition.Field field) {
        return "new KerinciBitMask<>(pSegment.get(%s, %d))".formatted(
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
