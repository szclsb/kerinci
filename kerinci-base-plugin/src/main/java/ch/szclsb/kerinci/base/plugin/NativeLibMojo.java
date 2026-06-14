package ch.szclsb.kerinci.base.plugin;

import ch.szclsb.kerinci.base.plugin.libc.LibcContext;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.mapper.LibcEnumParser;
import ch.szclsb.kerinci.base.plugin.libc.mapper.LibcFunctionsParser;
import ch.szclsb.kerinci.base.plugin.libc.mapper.LibcStructParser;
import ch.szclsb.kerinci.base.plugin.libc.writer.LibcObjectWriter;
import ch.szclsb.kerinci.base.plugin.template.ftl.FtlTemplateFactory;
import ch.szclsb.kerinci.base.plugin.libc.writer.LibcLibraryWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Mojo(name = "generate")
public class NativeLibMojo extends AbstractCommandProcessMojo {
    @Parameter(property = "nativePath", required = true)
    private File nativePath;
    @Parameter(property = "nativeBuildPath", defaultValue = "native-build")
    private File nativeBuildPath;
    @Parameter(property = "nativeFunctionsPath", required = true)
    private File nativeFunctionsPath;
    @Parameter(property = "nativeFunctionsPrefix", defaultValue = "_")
    private String nativeFunctionsPrefix;
    @Parameter(property = "glfwSdk", required = true)
    private File glfwSdk;
    @Parameter(property = "vulkanSdk", required = true)
    private File vulkanSdk;
    @Parameter(property = "enableBuilder")
    private boolean enableBuilder;
    @Parameter(property = "defineMacros")
    private Collection<String> defineMacros;
    @Parameter(property = "targetPackage")
    private String targetPackage;
    @Parameter(property = "target", defaultValue = "target/generated-sources")
    private File target;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        generateNativeLib();
        generateNativeHandlers();

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

            var libs = List.of(
                    // todo glm
                    new Lib("GLFW", glfwSdk.toPath().resolve("include/GLFW/glfw3.h")),
                    new Lib("Vulkan", vulkanSdk.toPath().resolve("vulkan/vulkan_core.h"))
            );

            var outputPath = target.toPath().resolve(targetPackage.replace(".", "/"));
            getLog().info("prepareOutputDirectory: " + outputPath);
            prepareDir(outputPath);

            var templateHandler = new FtlTemplateFactory(getLog());
            var enumParser = new LibcEnumParser(getLog());
            var structParser = new LibcStructParser(getLog(), enableBuilder);
            var functionParser = new LibcFunctionsParser(getLog(), nativeFunctionsPrefix);
            var enumWriter = new LibcObjectWriter<>(getLog(), outputPath, targetPackage, enumParser, templateHandler.createEnumTemplateWriter());
            var structWriter = new LibcObjectWriter<>(getLog(), outputPath, targetPackage, structParser, templateHandler.createStrcutTemplateWriter());
            var libFunctionWriter = new LibcLibraryWriter(getLog(), outputPath, targetPackage, functionParser, templateHandler.createLibraryTemplateWriter());

            var objectMapper = new ObjectMapper();
            for (var lib : libs) {
                getLog().info("start generating native library " + lib.name());
                var translationUnit = parseAst(objectMapper, lib.header());
                var context = new LibcContext(translationUnit, structWriter, enumWriter);
                // lib containing functions
                var functionCursors = context.getDeclarations(LibcCursor.KIND_FUNCTION)
                        .filter(cursor -> nativeFunctions.contains(cursor.getSpelling()))
                        .toList();
                libFunctionWriter.write(lib.name(), functionCursors, context);
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
}
