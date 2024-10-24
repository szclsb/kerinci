package ch.szclsb.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mojo(name = "native-lib")
public class NativeLibMojo extends AbstractCommandProcessMojo {
    @Parameter(property = "wrappers")
    private Collection<Wrapper> wrappers;

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
        generateWrappers();
        generateNativeLib();
        generateNativeHandlers();
    }

    private void generateWrappers() throws MojoExecutionException {
        getLog().info("start generating wrappers");
        var cwd = Paths.get(getWorkingDirectory().toURI());
        var nativeWorkingDir = cwd.resolve(nativePath);
        var pattern = Pattern.compile(".*\\s(\\w+)\\(([a-zA-Z0-9_\\r\\n\\t\\f\\v \\,\\*]*)\\);");
        for (var wrapper : wrappers) {
            getLog().info("Generating wrapper for " + wrapper.getName());
            try {
                var functions = new HashSet<String>();
                try (var stream = Files.lines(cwd.resolve(wrapper.getFunctionListPath()))) {
                    stream.filter(line -> !line.isEmpty()).forEach(functions::add);
                }
                var headerFile = nativeWorkingDir.resolve("include/" + wrapper.getName() + ".h");
                var sourceFile = nativeWorkingDir.resolve(wrapper.getName() + ".c");

                var tmpHeaderFile = Files.createTempFile(cwd, null, null);
                var tmpSourceFile = Files.createTempFile(cwd, null, null);
                try (var headerWriter = Files.newBufferedWriter(tmpHeaderFile, StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE);
                     var sourceWriter = Files.newBufferedWriter(tmpSourceFile, StandardOpenOption.WRITE,
                             StandardOpenOption.CREATE)) {
                    headerWriter.write("""
                            #pragma once
                            
                            """);
                    for (var define : wrapper.getDefines()) {
                        headerWriter.write("""
                                #define %s
                                """.formatted(define));
                    }
                    for (var define : wrapper.getIncludes()) {
                        headerWriter.write("""
                                #include %s
                                """.formatted(define));
                    }
                    headerWriter.newLine();

                    sourceWriter.write("""
                            #include \"%s.h\"
                            
                            """.formatted(wrapper.getName()));

                    for (var sourceHeader : wrapper.getHeaderFiles()) {
                        try (var scanner = new Scanner(Paths.get(sourceHeader))) {
                            var matches = scanner.findAll(pattern).toList();
                            for (var match : matches) {
                                var function = match.group(1);
                                if (functions.contains(function)) {
                                    var definition = match.group(0);

                                    var headerFunctionDefinition = definition.replace(function, wrapper.getFunctionPrefix() + function);
                                    headerFunctionDefinition = headerFunctionDefinition.replace("(void);", "();");
                                    for (var exclusion : wrapper.getExcludes()) {
                                        headerFunctionDefinition = headerFunctionDefinition.replaceAll(exclusion, "");
                                    }
                                    headerFunctionDefinition = headerFunctionDefinition.trim();
                                    headerWriter.write("""
                                            __declspec(dllexport) %s
                                            """.formatted(headerFunctionDefinition));

                                    var argLines = match.group(2);
                                    var args = Arrays.stream(argLines.split(","))
                                            .map(str -> {
                                                var i = str.lastIndexOf(' ');
                                                return str.substring(i + 1);
                                            })
                                            .filter(str -> !str.isEmpty() && !str.equals("void"))
                                            .collect(Collectors.joining(", "));
                                    var sourceFunctionDefinition = headerFunctionDefinition.replace(";", """
                                            {
                                                %s%s(%s);
                                            }
                                            """.formatted(headerFunctionDefinition.startsWith("void") ? "" : "return ", function, args));
                                    sourceWriter.write(sourceFunctionDefinition);

                                }
                            }
                        }
                    }
                    headerWriter.flush();
                    sourceWriter.flush();

                    Files.move(tmpHeaderFile, headerFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    Files.move(tmpSourceFile, sourceFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } finally {
                    Files.deleteIfExists(tmpHeaderFile);
                    Files.deleteIfExists(tmpSourceFile);
                }
            } catch (Exception e) {
                getLog().error(e);
            }
        }
        getLog().info("finished generating wrappers");
    }

    private void generateNativeLib() throws MojoExecutionException {
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
    }

    private void generateNativeHandlers() throws MojoExecutionException {
        getLog().info("start generating native handlers");
        for (var libDefinition : libs) {
            getLog().info("Generating native handle for " + libDefinition.getName());
            var args = new ArrayList<String>();
            args.add("jextract" + (windows ? ".bat" : ""));
            args.add("--source");
            libDefinition.getIncludeDirs().forEach(includeDir -> {
                args.add("--include-dir");
                args.add(includeDir);
            });
            if (libDefinition.getDefineMacros() != null) {
                libDefinition.getDefineMacros().forEach(defineMarco -> {
                    args.add("--define-macro");
                    args.add(defineMarco);
                });
            }
            args.add("--output");
            args.add(target);
            args.add("--target-package");
            args.add(libDefinition.getTargetPackage());
            args.add("--library");
            args.add(libDefinition.getLibrary());
            args.add(libDefinition.getHeader());

            executeCommands(new CommandLine(args.toArray(String[]::new)));
        }
        getLog().info("finished generating native handlers");
    }
}
