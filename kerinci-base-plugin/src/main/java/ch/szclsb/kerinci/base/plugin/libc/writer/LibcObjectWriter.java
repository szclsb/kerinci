package ch.szclsb.kerinci.base.plugin.libc.writer;

import ch.szclsb.kerinci.base.plugin.FileWriter;
import ch.szclsb.kerinci.base.plugin.libc.LibcContext;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.mapper.LibcObjectParser;
import ch.szclsb.kerinci.base.plugin.template.TemplateDefinition;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.OptionalLong;

public class LibcObjectWriter<T extends TemplateDefinition> extends FileWriter {
    private final String generatedPackage;
    private final LibcObjectParser<T> libcParser;
    private final TemplateWriter<T> templateWriter;

    public LibcObjectWriter(Log logger, Path dir,
                            String generatedPackage,
                            LibcObjectParser<T> libcParser,
                            TemplateWriter<T> templateWriter) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        this.libcParser = libcParser;
        this.templateWriter = templateWriter;
    }

    public OptionalLong write(String className, LibcCursor libcCursor, LibcContext context) throws IOException {
        var objectDefinition = libcParser.parse(className, libcCursor, context);
        writeFile(className, writer -> templateWriter.render(generatedPackage, className, objectDefinition, writer));
        return objectDefinition.javaObjectBytes();
    }
}
