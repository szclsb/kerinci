package ch.szclsb.kerinci.base.plugin.libc.mapper;

import ch.szclsb.kerinci.base.plugin.libc.LibcContext;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcType;
import ch.szclsb.kerinci.base.plugin.template.FunctionsTemplateDefinition;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ch.szclsb.kerinci.base.plugin.libc.LibcContext.getFlagBitsType;

public class LibcFunctionsParser implements LibcLibraryParser {
    @FunctionalInterface
    public interface ElaboratedResolver {
        String get(int level) throws IOException;
    }

    private final Log logger;
    private final String functionPrefix;

    public LibcFunctionsParser(Log logger, String functionPrefix) {
        this.logger = logger;
        this.functionPrefix = functionPrefix;
    }


    private static String getType(int level, LibcType type, ElaboratedResolver resolver) throws IOException {
        return switch (type.getKind()) {
            case LibcType.KIND_POINTER -> getType(level + 1, type.getRef(), resolver);
            case LibcType.KIND_ELABORATED -> resolver.get(level);
            case LibcType.KIND_VOID -> level > 0 ? "Object" : "void";
            case LibcType.KIND_INT -> level > 0 ? "Integer" : "int";
            case LibcType.KIND_FLOAT -> level > 0 ? "Float" : "float";
            case LibcType.KIND_CHAR -> "String";
            default -> throw new IllegalArgumentException("Unknown libc type: " + type.getKind());
        };
    }

    private static ElaboratedResolver resolver(LibcCursor cursor, LibcContext context) {
        return level -> {
            var e = cursor.getChildren().stream()
                    .filter(child -> LibcCursor.KIND_TYPEREF.equals(child.getKind()))
                    .findFirst()
                    .map(LibcCursor::getSpelling)
                    .orElse(null);
            if (e != null) {
                var decl = context.declare(e);
                if (decl != null) {
                    if (decl.isFlag()) {
                        return "BitMask<%s>".formatted(getFlagBitsType(e));
                    }
//                if (decl.isHandle()) {
//                }
                    return decl.javaType();
                }
            }
            return null;
        };
    }

    private static String getJavaArgType(LibcCursor cursor, LibcContext context) throws IOException {
        return getType(0, cursor.getType(), resolver(cursor, context));
    }

    private static String getJavaReturnType(LibcCursor functionCursor, LibcContext context) throws IOException {
        return getType(0, functionCursor.getResultType(), resolver(functionCursor, context));
    }

    @Override
    public FunctionsTemplateDefinition parse(List<LibcCursor> functionCursors, LibcContext context) throws IOException {
        var functionDefinitions = new ArrayList<FunctionsTemplateDefinition.Function>();
        for (var functionCursor : functionCursors) {
            var publicFunctionName = functionCursor.getSpelling();
            var functionName = functionPrefix + publicFunctionName;
            logger.info("-- declaring function: %s".formatted(functionName));
            var returnType = getJavaReturnType(functionCursor, context);
            var params = new ArrayList<FunctionsTemplateDefinition.Function.Param>();
            for (var childCursor : functionCursor.getChildren()) {
                if (LibcCursor.KIND_PARAMETER.equals(childCursor.getKind())) {
                    var paramName = childCursor.getSpelling();
                    logger.debug("---- resolving parameter: %s".formatted(paramName));
                    var javaType = getJavaArgType(childCursor, context);
                    if (javaType != null) {
                        params.add(new FunctionsTemplateDefinition.Function.Param(javaType, paramName));
                    } else {
                        logger.warn("---- ignoring parameter %s, because resolved java type is null".formatted(paramName));
                    }
                }
            }
            functionDefinitions.add(new FunctionsTemplateDefinition.Function(
                    functionName,
                    "%sNative".formatted(publicFunctionName),
                    returnType == null ? "Object /*FIXME*/" : returnType,
                    publicFunctionName,
                    params)
            );
        }
        return new FunctionsTemplateDefinition(functionDefinitions);
    }
}
