package ch.szclsb.kerinci.base.plugin.libc.writer;

import ch.szclsb.kerinci.base.plugin.FileWriter;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.mapper.LibcParser;
import ch.szclsb.kerinci.base.plugin.template.EnumConst;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LibcEnumWriter extends FileWriter {
    private final String generatedPackage;
    private final LibcParser<List<EnumConst>> libcParser;
    private final TemplateWriter<List<EnumConst>> templateWriter;

    public LibcEnumWriter(Log logger, Path dir,
                          String generatedPackage,
                          LibcParser<List<EnumConst>> libcParser,
                          TemplateWriter<List<EnumConst>> templateWriter) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        this.libcParser = libcParser;
        this.templateWriter = templateWriter;
    }

    public void write(String className, LibcCursor enumCursor) throws IOException {
        logger.info("-- declaring enum: %s (%s)".formatted(className, enumCursor.getSpelling()));
        var model = libcParser.parse(enumCursor);
        writeFile(className, writer -> templateWriter.render(generatedPackage, className, model, writer));
    }
}
