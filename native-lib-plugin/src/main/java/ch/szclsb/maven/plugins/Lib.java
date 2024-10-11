package ch.szclsb.maven.plugins;

import org.apache.maven.plugins.annotations.Parameter;

public class Lib {
    @Parameter(property = "name", required = true)
    private String name;
    @Parameter(property = "nativePath", required = true)
    private String nativePath;
    @Parameter(property = "includeDir", required = true)
    private String includeDir;
    @Parameter(property = "header", required = true)
    private String header;
    @Parameter(property = "targetPackage", required = true)
    private String targetPackage;
    @Parameter(property = "library", required = true)
    private String library;

    public String getName() {
        return name;
    }

    public String getNativePath() {
        return nativePath;
    }

    public String getIncludeDir() {
        return includeDir;
    }

    public String getHeader() {
        return header;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public String getLibrary() {
        return library;
    }
}
