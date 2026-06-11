package ch.szclsb.kerinci.base.plugin.libc.writer;

import ch.szclsb.kerinci.base.plugin.libc.LibcContext;
import ch.szclsb.kerinci.base.plugin.FileWriter;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.mapper.LibcLibraryParser;
import ch.szclsb.kerinci.base.plugin.template.FunctionsTemplateDefinition;
import ch.szclsb.kerinci.base.plugin.template.TemplateWriter;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LibcLibraryWriter extends FileWriter {
    private final String generatedPackage;
    private final LibcLibraryParser functionsParser;
    private final TemplateWriter<FunctionsTemplateDefinition> templateWriter;

    public LibcLibraryWriter(Log logger, Path dir, String generatedPackage,
                             LibcLibraryParser functionsParser,
                             TemplateWriter<FunctionsTemplateDefinition> templateWriter) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        this.functionsParser = functionsParser;
        this.templateWriter = templateWriter;
    }

    public void write(String libName, List<LibcCursor> functionCursors, LibcContext context) throws IOException {
        var libDefinition = functionsParser.parse(functionCursors, context);
        writeFile(libName, writer -> templateWriter.render(generatedPackage, libName, libDefinition, writer));
    }
}
