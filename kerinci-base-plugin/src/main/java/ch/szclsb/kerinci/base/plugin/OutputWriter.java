package ch.szclsb.kerinci.base.plugin;

import java.io.BufferedWriter;
import java.io.IOException;

public interface OutputWriter {
    void write(BufferedWriter writer) throws IOException;
}
