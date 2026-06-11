package ch.szclsb.kerinci.base.plugin.template;

import java.io.IOException;
import java.io.Writer;

@FunctionalInterface
public interface TemplateWriter<T> {
    void render(String packageName, String className, T definition, Writer writer) throws IOException;
}
