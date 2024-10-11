package ch.szclsb.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class AbstractCommandProcessMojo extends AbstractMojo {
    @Parameter(property = "workingDirectory", required = true)
    private File workingDirectory;

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
}
