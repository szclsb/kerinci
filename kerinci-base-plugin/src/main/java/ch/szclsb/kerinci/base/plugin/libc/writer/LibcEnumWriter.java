package ch.szclsb.kerinci.base.plugin.libc.writer;

import ch.szclsb.kerinci.base.plugin.FileWriter;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.mapper.LibcParser;
import ch.szclsb.kerinci.base.plugin.template.EnumFileModel;
import ch.szclsb.kerinci.base.plugin.template.FileModel;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LibcEnumWriter extends FileWriter {
    private final String generatedPackage;
    private final LibcParser<List<EnumFileModel.EnumConst>> libcParser;
    private final TemplateWriter<EnumFileModel> templateWriter;

    public LibcEnumWriter(Log logger, Path dir,
                          String generatedPackage,
                          LibcParser<List<EnumFileModel.EnumConst>> libcParser,
                          TemplateWriter<EnumFileModel> templateWriter) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        this.libcParser = libcParser;
        this.templateWriter = templateWriter;
    }

    public void write(String className, LibcCursor enumCursor) throws IOException {
        logger.info("-- declaring enum: %s (%s)".formatted(className, enumCursor.getSpelling()));

        var enumContent = libcParser.parse(enumCursor);
        var model = new EnumFileModel(generatedPackage, className, enumContent);
        writeFile(className, writer -> templateWriter.render(model, writer));
    }
}
