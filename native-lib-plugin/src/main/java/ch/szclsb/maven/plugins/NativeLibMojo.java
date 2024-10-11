package ch.szclsb.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Collection;

@Mojo(name = "native-lib")
public class NativeLibMojo extends AbstractCommandProcessMojo {
    @Parameter(property = "windows")
    private boolean windows;
    @Parameter(property = "nativeBuildPath", defaultValue = "native-build")
    private String nativeBuildPath;
    @Parameter(property = "target", defaultValue = "target/generated-sources")
    private String target;

    @Parameter(property = "libs")
    private Collection<Lib> libs;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("start generating native handlers");

        for (var libDefinition : libs) {
            getLog().info("Generating native handle for " + libDefinition.getName());
            executeCommands(
                    new CommandLine("cmake",
                            "-S", libDefinition.getNativePath(),
                            "-B", nativeBuildPath + "/" + libDefinition.getName(),
                            "."),
                    new CommandLine("cmake",
                            "--build", nativeBuildPath + "/" + libDefinition.getName()),
                    new CommandLine("jextract" + (windows ? ".bat" : ""),
                            "--include-dir", libDefinition.getIncludeDir(),
                            "--output", target,
                            "--target-package", libDefinition.getTargetPackage(),
                            "--library", libDefinition.getLibrary(),
                            "--source",
                            libDefinition.getHeader())
            );
            getLog().info("Generated native handle");
        }

        getLog().info("finished generating native handlers");
    }
}
