package ch.szclsb.maven.plugins;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.ArrayList;
import java.util.Collection;

public class Lib {
    @Parameter(property = "name", required = true)
    private String name;
    @Parameter(property = "includeDir", required = true)
    private Collection<String> includeDirs;
    @Parameter(property = "header", required = true)
    private String header;
    @Parameter(property = "defineMacros")
    private Collection<String> defineMacros;
    @Parameter(property = "targetPackage", required = true)
    private String targetPackage;
    @Parameter(property = "library", required = true)
    private String library;

    public String getName() {
        return name;
    }

    public Collection<String> getIncludeDirs() {
        return includeDirs;
    }

    public String getHeader() {
        return header;
    }

    public Collection<String> getDefineMacros() {
        return defineMacros;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public String getLibrary() {
        return library;
    }
}
