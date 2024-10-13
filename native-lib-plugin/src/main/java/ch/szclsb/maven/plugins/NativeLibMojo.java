package ch.szclsb.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.ArrayList;
import java.util.Collection;

@Mojo(name = "native-lib")
public class NativeLibMojo extends AbstractCommandProcessMojo {
    @Parameter(property = "windows")
    private boolean windows;
    @Parameter(property = "nativePath", required = true)
    private String nativePath;
    @Parameter(property = "nativeBuildPath", defaultValue = "native-build")
    private String nativeBuildPath;
    @Parameter(property = "target", defaultValue = "target/generated-sources")
    private String target;

    @Parameter(property = "libs")
    private Collection<Lib> libs;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("start generating native shared library");
        executeCommands(
                new CommandLine("cmake",
                        "-S", nativePath,
                        "-B", nativeBuildPath,
                        "."),
                new CommandLine("cmake",
                        "--build", nativeBuildPath)
        );
        getLog().info("finished generating native shared library");

        getLog().info("start generating native handlers");
        for (var libDefinition : libs) {
            getLog().info("Generating native handle for " + libDefinition.getName());
            var args = new ArrayList<String>();
            args.add("jextract" + (windows ? ".bat" : ""));
            args.add("--source");
            libDefinition.getIncludeDirs().forEach(includeDir -> {
                args.add("--include-dir"); args.add(includeDir);
            });
            if (libDefinition.getDefineMacros() != null) {
                libDefinition.getDefineMacros().forEach(defineMarco -> {
                    args.add("--define-macro"); args.add(defineMarco);
                });
            }
            args.add("--output"); args.add(target);
            args.add("--target-package"); args.add(libDefinition.getTargetPackage());
            args.add("--library"); args.add(libDefinition.getLibrary());
            args.add(libDefinition.getHeader());

            executeCommands(new CommandLine(args.toArray(String[]::new)));
            getLog().info("Generated native handle");
        }
        getLog().info("finished generating native handlers");
    }
}
