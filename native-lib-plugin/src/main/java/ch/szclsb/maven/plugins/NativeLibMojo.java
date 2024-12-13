package ch.szclsb.maven.plugins;

import ch.szclsb.maven.plugins.writer.EnumWriter;
import ch.szclsb.maven.plugins.writer.FunctionWriter;
import ch.szclsb.maven.plugins.writer.StructWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "native-lib")
public class NativeLibMojo extends AbstractCommandProcessMojo {
    @Parameter(property = "nativePath", required = true)
    private File nativePath;
    @Parameter(property = "nativeBuildPath", defaultValue = "native-build")
    private File nativeBuildPath;
    @Parameter(property = "nativeFunctionsPath", required = true)
    private File nativeFunctionsPath;
    @Parameter(property = "nativeFunctionsPrefix", defaultValue = "_")
    private String nativeFunctionsPrefix;
    @Parameter(property = "target", defaultValue = "target/generated-sources")
    private File target;

    @Parameter(property = "libs")
    private Collection<Lib> libs;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        generateNativeLib();
        generateNativeHandlers();
//        generateNativeHandlers();

        // add additional source root
        getProject().addCompileSourceRoot(target.getAbsolutePath());
    }

    private void generateNativeLib() throws MojoExecutionException {
        getLog().info("start generating native shared library");
        executeCommands(
                new CommandLine("cmake",
                        "-S", nativePath.getAbsolutePath(),
                        "-B", nativeBuildPath.getAbsolutePath(),
                        "-DKRC_MAVEN_PROJECT_DIR:STRING=" + getWorkingDirectory().getAbsolutePath(),
                        "-DKRC_FUNCTION_FILTER:STRING=" + nativeFunctionsPath.getAbsolutePath(),
                        "-DKRC_FUNCTION_PREFIX:STRING=" + nativeFunctionsPrefix,
                        nativePath.getAbsolutePath()),
                new CommandLine("cmake",
                        "--build", nativeBuildPath.getAbsolutePath())
        );
        getLog().info("finished generating native shared library");
    }

    private void generateNativeHandlers() throws MojoExecutionException {
        getLog().info("start generating native handlers");

        try {
            var nativeFunctions = new HashSet<String>();
            Files.readAllLines(nativeFunctionsPath.toPath()).stream()
                    .filter(line -> !line.startsWith("--"))
                    .forEach(nativeFunctions::add);

            var objectMapper = new ObjectMapper();
            for (var targetLib : libs.stream().collect(Collectors.groupingBy(Lib::getTargetPackage)).entrySet()) {
                var targetPackage = targetLib.getKey();
                var outputPath = target.toPath().resolve(targetPackage.replace(".", "/"));
                getLog().info("prepareOutputDirectory: " + outputPath);
                prepareDir(outputPath);

                var enumWriter = new EnumWriter(getLog(), outputPath, targetPackage);
                var structWriter = new StructWriter(getLog(), outputPath, targetPackage);
                var libFunctionWriter = new FunctionWriter(getLog(), outputPath, targetPackage, nativeFunctionsPrefix);

                for (var lib : targetLib.getValue()) {
                    getLog().info("start generating native library " + lib.getName());
                    var translationUnit = parseAst(objectMapper, lib.getHeader().toPath());
                    var context = new Context(translationUnit, structWriter, enumWriter);
                    // lib containing functions
                    var functionCursors = context.getDeclarations(LibcCursor.KIND_FUNCTION)
                            .filter(cursor -> nativeFunctions.contains(cursor.getSpelling()))
                            .toList();
                    libFunctionWriter.write(lib.getName(), functionCursors, context);
                }
            }
            getLog().info("finished generating native handlers");
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private LibcCursor parseAst(ObjectMapper objectMapper, Path headerPath) throws IOException {
        var astPath = getWorkingDirectory().toPath().resolve("%s.ast.json".formatted(headerPath.getFileName().toString()));
        try (var inputStream = new BufferedInputStream(Files.newInputStream(astPath))) {
            return objectMapper.readValue(inputStream, LibcCursor.class);
        }
    }

//    private void generateNativeHandlers() throws MojoExecutionException {
//        getLog().info("start generating native handlers");
//        for (var libDefinition : libs) {
//            getLog().info("Generating native handle for " + libDefinition.getName());
//            var args = new ArrayList<String>();
//            args.add("jextract" + (windows ? ".bat" : ""));
//            args.add("--source");
//            libDefinition.getIncludeDirs().forEach(includeDir -> {
//                args.add("--include-dir");
//                args.add(includeDir);
//            });
//            if (libDefinition.getDefineMacros() != null) {
//                libDefinition.getDefineMacros().forEach(defineMarco -> {
//                    args.add("--define-macro");
//                    args.add(defineMarco);
//                });
//            }
//            args.add("--output");
//            args.add(target);
//            args.add("--target-package");
//            args.add(libDefinition.getTargetPackage());
//            args.add("--library");
//            args.add(libDefinition.getLibrary());
//            args.add(libDefinition.getHeader());
//
//            executeCommands(new CommandLine(args.toArray(String[]::new)));
//        }
//        getLog().info("finished generating native handlers");
//    }
}
