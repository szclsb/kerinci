package ch.szclsb.maven.plugins;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class Lib {
    @Parameter(property = "name", required = true)
    private String name;
    @Parameter(property = "header", required = true)
    private File header;
    @Parameter(property = "defineMacros")
    private Collection<String> defineMacros;
    @Parameter(property = "targetPackage", required = true)
    private String targetPackage;

    public String getName() {
        return name;
    }

    public File getHeader() {
        return header;
    }

    public Collection<String> getDefineMacros() {
        return defineMacros;
    }

    public String getTargetPackage() {
        return targetPackage;
    }
}
