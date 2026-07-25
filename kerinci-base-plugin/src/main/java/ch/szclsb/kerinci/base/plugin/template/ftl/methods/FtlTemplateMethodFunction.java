package ch.szclsb.kerinci.base.plugin.template.ftl.methods;


import freemarker.template.TemplateModelException;

public interface FtlTemplateMethodFunction<T, R> {
    R apply(T firstArg, FtlTemplateMethodExtraArgParser extraArgParser) throws TemplateModelException;
}
