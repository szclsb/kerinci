package ch.szclsb.maven.plugins.writer;

import ch.szclsb.maven.plugins.LibcCursor;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class EnumWriter extends FileWriter {
    private final String generatedPackage;

    public EnumWriter(Log logger, Path dir, String generatedPackage) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
    }

    public void write(LibcCursor enumCursor) throws IOException {
        var className = enumCursor.getSpelling();
        logger.info("-- declaring enum: " + className);

        var enumConst = new ArrayList<String>();

        for (var enumValue : enumCursor.getChildren()) {
            if (LibcCursor.KIND_ENUM_CONST.equals(enumValue.getKind())) {
                var valueName = enumValue.getSpelling();
                var value = enumValue.getChildren().stream()
                        .filter(c -> LibcCursor.KIND_INT_LITERAL.equals(c.getKind()))
                        .findFirst()
                        .map(LibcCursor::getSpelling)
                        .or(() -> enumValue.getChildren().stream()
                                .filter(c -> LibcCursor.KIND_REF_EXPR.equals(c.getKind()))
                                .findFirst()
                                .map(c -> c.getSpelling() + ".value"))
                        .orElse("");
                enumConst.add("    %s(%s)".formatted(valueName, value));
            }
        }

        writeFile(className, writer -> {
            writer.write("""
                    // GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
                    package %s;
                    
                    import ch.szclsb.kerinci.internal.HasValue;
                    import lombok.Getter;
                    import lombok.RequiredArgsConstructor;
                    
                    @Getter
                    @RequiredArgsConstructor
                    public enum %s implements HasValue {
                    """.formatted(
                    generatedPackage,
                    className
            ));
            writer.write(String.join(",\n", enumConst) + ";");
            writer.write("""
                    
                        private final int value;
                    }
                    """);
        });
    }
}
