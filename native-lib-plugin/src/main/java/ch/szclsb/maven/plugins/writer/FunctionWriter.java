package ch.szclsb.maven.plugins.writer;

import ch.szclsb.maven.plugins.Context;
import ch.szclsb.maven.plugins.LibcCursor;
import ch.szclsb.maven.plugins.LibcType;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ch.szclsb.maven.plugins.Context.getFlagBitsType;

public class FunctionWriter extends FileWriter {
    @FunctionalInterface
    public interface ElaboratedResolver {
        String get() throws IOException;
    }

    public record FunctionParam(
            String javaType,
            String name
    ) {
    }

    private final String generatedPackage;
    private final String functionPrefix;

    public FunctionWriter(Log logger, Path dir, String generatedPackage, String functionPrefix) {
        super(logger, dir);
        this.generatedPackage = generatedPackage;
        this.functionPrefix = functionPrefix;
    }

    private String getType(int level, LibcType type, ElaboratedResolver resolver) throws IOException {
        return switch (type.getKind()) {
            case LibcType.KIND_POINTER -> getType(level + 1, type.getRef(), resolver);
            case LibcType.KIND_ELABORATED -> resolver.get();
            case LibcType.KIND_VOID -> level > 0 ? "Object" : "void";
            case LibcType.KIND_INT -> level > 0 ? "Integer" : "int";
            case LibcType.KIND_FLOAT -> level > 0 ? "Float" : "float";
            case LibcType.KIND_CHAR -> "String";
            default -> throw new IllegalArgumentException("Unknown libc type: " + type.getKind());
        };
    }

    private String getJavaType(LibcCursor cursor, Context context) throws IOException {
        return getType(0, cursor.getType(), () -> {
            var e = cursor.getChildren().getFirst().getSpelling();
            var decl = context.declare(e);
            if (decl != null) {
                if (decl.isFlag()) {
                    return "Set<%s>".formatted(getFlagBitsType(e));
                }
//                if (decl.isHandle()) {
//                }
                return decl.javaType();
            }
            return null;
        });
    }

    public void write(String libName, List<LibcCursor> functionCursors, Context context) throws IOException {
        writeFile(libName, writer -> {
            writer.write("""
                    // GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
                    package %s;
                    
                    import java.util.Arrays;
                    import java.util.List;
                    import java.util.Set;
                    import java.util.stream.Stream;
                    
                    public class %s {
                    """.formatted(generatedPackage, libName));

            for (var functionCursor : functionCursors) {
                var functionName = functionPrefix + functionCursor.getSpelling();
                logger.info("-- declaring function: %s".formatted(functionName));
                var returnType = "void";  // FIXME
                var params = new ArrayList<FunctionParam>();
                for (var childCursor : functionCursor.getChildren()) {
                    if (LibcCursor.KIND_PARAMETER.equals(childCursor.getKind())) {
                        var paramName = childCursor.getSpelling();
                        logger.debug("---- resolving parameter: %s".formatted(paramName));
                        var javaType = getJavaType(childCursor, context);
                        if (javaType != null) {
                            params.add(new FunctionParam(getJavaType(childCursor, context), paramName));
                        } else {
                            logger.warn("---- ignoring parameter %s, because resolved java type is null".formatted(paramName));
                        }
                    }
                }

                // FIXME
                writer.write("""
                            public %s %s(%s) {
                                throw new UnsupportedOperationException();
                            }
                        """.formatted(returnType, functionName, params.stream()
                        .map(param -> param.javaType + " " + param.name)
                        .collect(Collectors.joining(", "))));
            }

            writer.write("""
                    }""");
        });


    }
}
