package ch.szclsb.kerinci.base.plugin.writer;

import ch.szclsb.kerinci.base.plugin.libc.LibcCursor;
import ch.szclsb.kerinci.base.plugin.template.EnumFileContext;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class EnumWriter extends FileWriter {
    private final TemplateWriter<EnumFileContext> templateWriter;
    private final String generatedPackage;

    public EnumWriter(Log logger, Path dir, TemplateWriter<EnumFileContext> templateWriter, String generatedPackage) {
        super(logger, dir);
        this.templateWriter = templateWriter;
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

        var model = new EnumFileContext(generatedPackage, className, enumConst);
        writeFile(className, writer -> templateWriter.render(model, writer));
    }
}
