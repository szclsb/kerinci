package ch.szclsb.kerinci.base.plugin.template;

import java.io.IOException;
import java.io.Writer;

public interface TemplateWriter {
    void writeEnum(EnumFileContext context, Writer writer) throws IOException;

    //TODO struct

    //TODO API
}
