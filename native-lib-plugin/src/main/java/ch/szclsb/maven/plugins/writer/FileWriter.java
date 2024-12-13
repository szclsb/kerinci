package ch.szclsb.maven.plugins.writer;

import ch.szclsb.maven.plugins.OutputWriter;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public abstract class FileWriter {
    protected final Log logger;
    private final Path dir;

    public FileWriter(Log logger, Path dir) {
        this.logger = logger;
        this.dir = dir;
    }

    protected void writeFile(String name, OutputWriter outputWriter) throws IOException {
        var file = dir.resolve(name + ".java");
        var tmpFile = dir.resolve(name + ".tmp");
        try (var writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(tmpFile,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE)))) {
            outputWriter.write(writer);
            Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
//            logger.info(String.format("Wrote file %s", file));
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }
}
