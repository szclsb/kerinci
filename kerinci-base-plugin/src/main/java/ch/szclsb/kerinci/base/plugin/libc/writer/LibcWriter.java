package ch.szclsb.kerinci.base.plugin.libc.writer;

import ch.szclsb.kerinci.base.plugin.FileWriter;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.mapper.LibcParser;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;

public class LibcWriter<T> extends FileWriter {
    private final String generatedPackage;
    private final LibcParser<T> libcParser;
    private final TemplateWriter<T> templateWriter;

    public LibcWriter(Log logger, Path dir,
                      String generatedPackage,
                      LibcParser<T> libcParser,
                      TemplateWriter<T> templateWriter) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        this.libcParser = libcParser;
        this.templateWriter = templateWriter;
    }

    public void write(String className, LibcCursor libcCursor) throws IOException {
        var model = libcParser.parse(className, libcCursor);
        writeFile(className, writer -> templateWriter.render(generatedPackage, className, model, writer));
    }
}
