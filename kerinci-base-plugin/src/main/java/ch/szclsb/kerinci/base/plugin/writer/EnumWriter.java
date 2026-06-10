package ch.szclsb.kerinci.base.plugin.writer;

import ch.szclsb.kerinci.base.plugin.libc.LibcCursor;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.WriterOutput;
import gg.jte.resolve.ResourceCodeResolver;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class EnumWriter extends FileWriter {
    private final TemplateEngine templateEngine;
    private final String generatedPackage;

    public EnumWriter(Log logger, Path dir, String generatedPackage) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        var codeResolver = new ResourceCodeResolver("templates/jte");
        this.templateEngine = TemplateEngine.create(codeResolver, ContentType.Plain);
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
        var context = new EnumFileContext(generatedPackage, className, enumConst);

        writeFile(className, writer -> {
            TemplateOutput output = new WriterOutput(writer);
            templateEngine.render("enum.jte", context, output);
        });
    }
}
