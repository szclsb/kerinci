package ch.szclsb.kerinci.base.plugin.template.ftl;

import ch.szclsb.kerinci.base.plugin.template.EnumFileModel;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;

public class FtlTemplateHandler {
    public static final String templatePath = "templates/freemarker";
    public static final String templateFileNameEnum = "enum.ftl";

    private final Configuration configuration;

    public FtlTemplateHandler() {
        this.configuration = configure();
    }

    public static Configuration configure() {
        try {
            var cfg = new Configuration(Configuration.VERSION_2_3_34);
            cfg.setTemplateLoader(new ClassTemplateLoader(FtlTemplateHandler.class.getClassLoader(), templatePath));
            cfg.setDefaultEncoding("UTF-8");
            return cfg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void wrapTemplateException(FtlRunnable runnable) throws IOException {
        try{
            runnable.run();
        } catch (TemplateException e) {
            throw new IOException(e);
        }
    }

    public void writeEnum(EnumFileModel model, Writer writer) throws IOException {
        wrapTemplateException(() -> {
            var template = configuration.getTemplate(templateFileNameEnum);
            template.process(model, writer);
        });
    }
}
