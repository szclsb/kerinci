package ch.szclsb.maven.plugins.writer;

import ch.szclsb.maven.plugins.Context;
import ch.szclsb.maven.plugins.libc.LibcCursor;
import ch.szclsb.maven.plugins.libc.LibcType;
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

    public record FunctionDefinition(
            String name,
            String returnType,
            List<FunctionParam> params
    ) {
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
        var functionDefinitions = new ArrayList<FunctionDefinition>();
        for (var functionCursor : functionCursors) {
            var publicFunctionName = functionCursor.getSpelling();
            var functionName = functionPrefix + publicFunctionName;
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
            functionDefinitions.add(new FunctionDefinition(publicFunctionName, returnType, params));
        }

        writeFile(libName, writer -> {
            writer.write("""
                    // GENERATED CLASS, DO NOT MODIFY THIS CLASS: CHANGES WILL BE OVERWRITTEN
                    package %1$s;
                    
                    import java.lang.foreign.Arena;
                    import java.lang.foreign.FunctionDescriptor;
                    import java.lang.foreign.Linker;
                    import java.lang.invoke.MethodHandle;
                    
                    import java.util.Arrays;
                    import java.util.List;
                    import java.util.Set;
                    import java.util.stream.Stream;
                    
                    import ch.szclsb.kerinci.internal.Runtime;
                    
                    public class %2$s implements AutoCloseable {
                        private static final Linker LINKER = Linker.nativeLinker();
                    
                        private final Arena session;
                    """.formatted(generatedPackage, libName));
            for (var function : functionDefinitions) {
                writer.write("""
                            private final MethodHandle %sNative;
                        """.formatted(function.name()));
            }
            writer.write("""
                    
                        public %s() {
                            this.session = Arena.ofShared();
                    """.formatted(libName));
            for (var function : functionDefinitions) {
                // FIXME return args
                writer.write("""
                                this.%sNative = LINKER.downcallHandle(Runtime.loadSymbol("%s"), FunctionDescriptor.ofVoid());  // FIXME return args
                        """.formatted(function.name(), functionPrefix + function.name()));
            }

            writer.write("""
                        }
                    """);

            for (var function : functionDefinitions) {
                // FIXME return args
                writer.write("""
                        
                            public %1$s %2$s(%3$s) {
                                try {
                                    // FIXME return args
                                    %2$sNative.invoke();
                                } catch (Throwable t) {
                                    throw new RuntimeException(t);
                                }
                            }
                        """.formatted(function.returnType(), function.name(), function.params().stream()
                        .map(param -> param.javaType() + " " + param.name())
                        .collect(Collectors.joining(", "))));
            }

            writer.write("""
                    
                        @Override
                        public void close() throws Exception {
                            this.session.close();
                        }
                    
                    }""");
        });
    }
}
