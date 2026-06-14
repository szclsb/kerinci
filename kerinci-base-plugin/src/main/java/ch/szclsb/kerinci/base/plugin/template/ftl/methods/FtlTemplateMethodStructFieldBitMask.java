package ch.szclsb.kerinci.base.plugin.template.ftl.methods;

// move string to templates?
public class FtlTemplateMethodStructFieldBitMask implements FtlTemplateMethodStructField {
    //TODO: fetch BitMask implementation name form external source

    @Override
    public String getterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return """
                public BitMask<%s> get_%s() {
                    return new KerinciBitMask<>(pSegment.get(%s, %d));
                }""".formatted(javaType, fieldName, memoryLayout, fieldOffset);
    }

    @Override
    public String setterMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return """
                public void set_%s(BitMask<%s> mask) {
                    pSegment.set(%s, %d, mask.getValue());
                }""".formatted(fieldName, javaType, memoryLayout, fieldOffset);
    }

    @Override
    public String builderMethod(String javaType, String memoryLayout, long fieldOffset, String fieldName) {
        return """
                public Builder set_%1$s(BitMask<%2$s> mask) {
                    instance.set_%1$s(mask);
                    return this;
                }""".formatted(fieldName, javaType);
    }
}
