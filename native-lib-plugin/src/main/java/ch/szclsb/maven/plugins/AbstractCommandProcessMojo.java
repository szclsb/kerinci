package ch.szclsb.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public abstract class AbstractCommandProcessMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    @Parameter(property = "workingDirectory", required = true)
    private File workingDirectory;

    public MavenProject getProject() {
        return project;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    protected int runCommand(String ...command) throws IOException, InterruptedException {
        getLog().info("Executing command: %s> %s"
                .formatted(workingDirectory.getAbsolutePath(), String.join(" ", command)));
        var builder = new ProcessBuilder();
        builder.command(command);
        builder.directory(workingDirectory);
        var process = builder.start();
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(line -> getLog().info(line));
        }
        try (var reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            reader.lines().forEach(line -> getLog().error(line));
        }
        return process.waitFor();
    }

    public record CommandLine(
            String ...command
    ) {}

    public void executeCommands(CommandLine ...lines) throws MojoExecutionException {
        try {
            for (var commandLine : lines) {
                var exitCode = runCommand(commandLine.command);
                if (exitCode != 0) {
                    throw new MojoExecutionException("Command finished with exit code " + exitCode);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e);
        }
    }

    public void prepareDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (var files = Files.walk(dir)) {
                files
                        .filter(path -> !dir.equals(path))
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ioe) {
                                getLog().error(ioe.getMessage(), ioe);
                            }
                        });
            }
        } else {
            Files.createDirectories(dir);
        }
    }
}
