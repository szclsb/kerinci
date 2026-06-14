package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

// move string to templates?
public class FtlTemplateMethodStructFieldBitMask implements FtlTemplateMethodStructField {
    //TODO: fetch BitMask implementation name form external source

    @Override
    public String getterMethod(String javaType, String memoryLayout, long fieldOffset) {
        return "new KerinciBitMask<>(pSegment.get(%s, %d))"
                .formatted(memoryLayout, fieldOffset);
    }

    @Override
    public String setterMethod(String javaType, String memoryLayout, long fieldOffset, String varName) {
        return "pSegment.set(%s, %d, %s.getValue())"
                .formatted(memoryLayout, fieldOffset, varName);
    }
}
