package ch.szclsb.kerinci.base.plugin.writer;

import ch.szclsb.kerinci.base.plugin.libc.LibcCursor;
import ch.szclsb.kerinci.base.plugin.template.EnumFileContext;
import freemarker.core.OutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class EnumWriter extends FileWriter {
    private final String generatedPackage;

    public EnumWriter(Log logger, Path dir, String generatedPackage) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
    }

    public void write(String className, LibcCursor enumCursor) throws IOException {
        logger.info("-- declaring enum: %s (%s)".formatted(className, enumCursor.getSpelling()));

        var enumConst = new ArrayList<EnumFileContext.EnumConst>();

        for (var enumValue : enumCursor.getChildren()) {
            if (LibcCursor.KIND_ENUM_CONST.equals(enumValue.getKind())) {
                var valueName = enumValue.getSpelling();
                var value = enumValue.getChildren().stream()
                        .filter(c -> LibcCursor.KIND_INT_LITERAL.equals(c.getKind()))
                        .findFirst()
                        .map(LibcCursor::getSpelling)
                        .or(() -> enumValue.getChildren().stream()
                                .filter(c -> LibcCursor.KIND_REF_EXPR.equals(c.getKind()))
                                .findFirst()
                                .map(c -> c.getSpelling() + ".value"))
                        .orElse("");
                enumConst.add(new EnumFileContext.EnumConst(valueName, value));
            }
        }

        var cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setDirectoryForTemplateLoading(Path.of("kerinci-base-plugin/src/main/resources/templates/freemarker").toFile());
        cfg.setDefaultEncoding("UTF-8");

        var template = cfg.getTemplate("enum.ftl");
        var model = new EnumFileContext(generatedPackage, className, enumConst);

        writeFile(className, writer -> {
            try{
                template.process(model, writer);
            } catch (TemplateException e) {
                throw new IOException(e);
            }
        });
    }
}
