package ch.szclsb.kerinci.base.plugin.template.ftl;

import freemarker.template.TemplateException;

import java.io.IOException;

@FunctionalInterface
public interface FtlRunnable {
    void run() throws TemplateException, IOException;
}
