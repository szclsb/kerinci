package ch.szclsb.kerinci.base.plugin.libc.mapper;

import ch.szclsb.kerinci.base.plugin.libc.LibcContext;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.template.FunctionsTemplateDefinition;

import java.io.IOException;
import java.util.List;

public interface LibcLibraryParser {
    FunctionsTemplateDefinition parse(List<LibcCursor> functionCursors, LibcContext context) throws IOException;
}
