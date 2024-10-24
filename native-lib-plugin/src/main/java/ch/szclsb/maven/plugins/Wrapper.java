package ch.szclsb.maven.plugins;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Collection;

public class Wrapper {
    @Parameter(property = "name")
    private String name;
    @Parameter(property = "defines")
    private Collection<String> defines;
    @Parameter(property = "includes")
    private Collection<String> includes;
    @Parameter(property = "headerFiles")
    private Collection<String> headerFiles;
    @Parameter(property = "functionPrefix")
    private String functionPrefix;
    @Parameter(property = "functionListPath", defaultValue = "nativeFunctions.txt")
    private String functionListPath;
    @Parameter(property = "excludes")
    private Collection<String> excludes;

    public String getName() {
        return name;
    }

    public Collection<String> getDefines() {
        return defines;
    }

    public Collection<String> getIncludes() {
        return includes;
    }

    public Collection<String> getHeaderFiles() {
        return headerFiles;
    }

    public String getFunctionPrefix() {
        return functionPrefix;
    }

    public String getFunctionListPath() {
        return functionListPath;
    }

    public Collection<String> getExcludes() {
        return excludes;
    }
}
