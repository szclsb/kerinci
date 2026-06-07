package ch.szclsb.kerinci.base.plugin.writer;

import ch.szclsb.kerinci.base.plugin.libc.LibcCursor;
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

    public void write(String className, LibcCursor enumCursor) throws IOException {
        logger.info("-- declaring enum: %s (%s)".formatted(className, enumCursor.getSpelling()));

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
                    
                    import ch.szclsb.kerinci.base.api.Flag;
                    import lombok.Getter;
                    import lombok.RequiredArgsConstructor;
                    
                    import java.util.Arrays;
                    import java.util.Map;
                    import java.util.function.Function;
                    import java.util.stream.Collectors;
                    
                    @Getter
                    @RequiredArgsConstructor
                    public enum %s implements Flag {
                    """.formatted(
                    generatedPackage,
                    className
            ));
            writer.write(String.join(",\n", enumConst) + ";");
            writer.write("""
                    
                        private final int value;
                    
                        private %1$s(int value) {
                            this.value = value;
                        }
                    
                        @Override
                        public int getValue() {
                            return value;
                        }
                    
                        private static Map<Integer, %1$s> flags = Arrays.stream(%1$s.values())
                            .collect(Collectors.toMap(%1$s::getValue, Function.identity()));
                        public static %1$s ofValue(int value) {
                            return flags.get(value);
                        }
                    }
                    """.formatted(className));
        });
    }
}
