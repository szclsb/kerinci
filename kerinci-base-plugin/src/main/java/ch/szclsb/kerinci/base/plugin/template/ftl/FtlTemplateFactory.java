package ch.szclsb.kerinci.base.plugin.template.ftl;

import ch.szclsb.kerinci.base.plugin.template.EnumTemplateDefinition;
import ch.szclsb.kerinci.base.plugin.template.FunctionsTemplateDefinition;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

public class FtlTemplateFactory {
    public static final String templatePath = "templates/freemarker";
    public static final String templateFileNameEnum = "enum.ftl";
    public static final String templateFileNameLibrary = "library.ftl";

    private final Configuration configuration;

    public FtlTemplateFactory() {
        this.configuration = configure();
    }

    public static Configuration configure() {
        try {
            var cfg = new Configuration(Configuration.VERSION_2_3_34);
            cfg.setTemplateLoader(new ClassTemplateLoader(FtlTemplateFactory.class.getClassLoader(), templatePath));
            cfg.setDefaultEncoding("UTF-8");
            return cfg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> TemplateWriter<T> createFileTemplateWriter(String templateFile) {
        return (packageName, className, definition, writer) -> {
            var fileModel = Map.of(
                    "packageName", packageName,
                    "className", className,
                    "definition", definition
            );
            try {
                var template = configuration.getTemplate(templateFile);
                template.process(fileModel, writer);
            } catch (TemplateException e) {
                throw new IOException(e);
            }
        };
    }

    public TemplateWriter<EnumTemplateDefinition> createEnumTemplateWriter() {
        return createFileTemplateWriter(templateFileNameEnum);
    }

    public TemplateWriter<FunctionsTemplateDefinition> createLibraryTemplateWriter() {
        return createFileTemplateWriter(templateFileNameLibrary);
    }
}
