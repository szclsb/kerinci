package ch.szclsb.kerinci.base.plugin.template.ftl;

import ch.szclsb.kerinci.base.plugin.template.EnumFileContext;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class FtlWriterImpl implements TemplateWriter {
    private final Configuration configuration;

    public FtlWriterImpl() {
        this.configuration = configure();
    }

    public static Configuration configure() {
        try {
            var cfg = new Configuration(Configuration.VERSION_2_3_34);
            // TODO improve
            cfg.setDirectoryForTemplateLoading(Path.of("kerinci-base-plugin/src/main/resources/templates/freemarker").toFile());
            cfg.setDefaultEncoding("UTF-8");
            return cfg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void run(FtlRunnable runnable) throws IOException {
        try{
            runnable.run();
        } catch (TemplateException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeEnum(EnumFileContext model, Writer writer) throws IOException {
        run(() -> {
            var template = configuration.getTemplate("enum.ftl");
            template.process(model, writer);
        });
    }
}
