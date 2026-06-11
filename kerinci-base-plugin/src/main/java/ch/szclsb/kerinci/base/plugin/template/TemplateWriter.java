package ch.szclsb.kerinci.base.plugin.template;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface TemplateWriter<T extends FileModel<?>> {
    void render(T model, Writer writer) throws IOException;
}
