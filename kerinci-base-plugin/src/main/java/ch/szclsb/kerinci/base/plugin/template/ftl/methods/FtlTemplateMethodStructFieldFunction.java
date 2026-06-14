package ch.szclsb.kerinci.base.plugin.template.ftl.methods;


import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;
import freemarker.template.TemplateModelException;

public interface FtlTemplateMethodStructFieldFunction<T> {
    T apply(StructTemplateDefinition.Field field, FtlTemplateMethodStructField templateMethodStructField, FtlTemplateMethodExtraArgParser extraArgParser) throws TemplateModelException;
}
