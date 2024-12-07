package ch.szclsb.maven.plugins;

import java.io.BufferedWriter;
import java.io.IOException;

public interface OutputWriter {
    void write(BufferedWriter writer) throws IOException;
}
