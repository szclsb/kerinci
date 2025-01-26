package ch.szclsb.maven.plugins;

import ch.szclsb.maven.plugins.writer.EnumWriter;
import ch.szclsb.maven.plugins.writer.StructWriter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Context {
    private static final String VK_FLAGS = "VkFlags";

    public static String getFlagBitsType(String flags) {
        return flags == null || flags.isBlank() ? null : flags.replace("Flags", "FlagBits");
    }

    public record Declaration(
            List<String> typeChain,
            String javaType,
            String javaLayout,
            int bytes
    ) {
        public boolean isFlag() {
            return typeChain.contains(VK_FLAGS);
        }

        public boolean isHandle() {
            return typeChain.getLast().endsWith("_T");
        }
    }

    private static final Set<String> cursorKinds = Set.of(
            LibcCursor.KIND_FUNCTION,
            LibcCursor.KIND_STRUCT,
            LibcCursor.KIND_ENUM
    );

    private final Map<String, LibcCursor> declarations;
    private final Map<String, String> typedefs;
    private final Map<String, Integer> structSizes;
    private final Set<String> enumNames;
    private final StructWriter structWriter;
    private final EnumWriter enumWriter;

    public Context(LibcCursor translationUnit, StructWriter structWriter, EnumWriter enumWriter) {
        this.declarations = new HashMap<>();
        this.typedefs = new HashMap<>();
//        this.handles = new HashMap<>();
        for (var declCursor : translationUnit.getChildren()) {
            if (cursorKinds.contains(declCursor.getKind())) {
                declarations.put(declCursor.getSpelling(), declCursor);
            } else if (LibcCursor.KIND_TYPEDEF.equals(declCursor.getKind())) {
                // type definitions
                declCursor.getChildren().stream()
                        .filter(c -> LibcCursor.KIND_TYPEREF.equals(c.getKind()))
                        .findFirst()
                        .ifPresent(a -> {
                            var typeRef = a.getSpelling();
                            if (typeRef.startsWith("struct ")) {
                                var structType = typeRef.substring(7);
                                addTypeDef(declCursor.getSpelling(), structType);
                            } else {
                                addTypeDef(declCursor.getSpelling(), typeRef);
                            }
                        });
            }
        }
        this.structSizes = new HashMap<>();
        this.enumNames = new HashSet<>();
        this.structWriter = structWriter;
        this.enumWriter = enumWriter;
    }

    private void addTypeDef(String ref, String type) {
        if (!ref.equals(type)) {
            typedefs.put(ref, type);
        }
    }


    public Stream<LibcCursor> getDeclarations() {
        return declarations.values().stream();
    }

    public Stream<LibcCursor> getDeclarations(String cursorKind) {
        return getDeclarations().filter(cursor -> cursorKind.equals(cursor.getKind()));
    }

    /**
     * @return
     * @throws IOException
     */
    public synchronized Declaration declare(String typeName) throws IOException {
        var typeChain = new LinkedList<String>();
        var tn = typeName;
        while (tn != null) {
            typeChain.add(tn);
            tn = typedefs.get(tn);
        }

        var actualType = typeChain.getLast();

        if (typeChain.contains(VK_FLAGS)) {
            var flagBitsType = getFlagBitsType(typeName);
            declare(flagBitsType);  // declare flag bit enums
        }

        var cursor = declarations.get(actualType);
        if (cursor == null) {
            return switch (actualType) {
                case "bool", "VkBool32" -> new Declaration(typeChain, "bool", "JAVA_BOOLEAN", 1);
                case "int32_t", "uint32_t" -> new Declaration(typeChain, "int", "JAVA_INT", 4);
                case "int64_t", "uint64_t" -> new Declaration(typeChain, "long", "JAVA_LONG", 4);
                default -> null;
            };
        }
        if (LibcCursor.KIND_ENUM.equals(cursor.getKind())) {
            if (!enumNames.contains(typeName)) {
                enumWriter.write(typeName, cursor);
                enumNames.add(typeName);
            }
            return new Declaration(typeChain, typeName, "JAVA_INT", 4);
        } else if (LibcCursor.KIND_STRUCT.equals(cursor.getKind())) {
            var structSize = structSizes.get(typeName);
            if (structSize == null) {
                structSize = structWriter.write(typeName, cursor, this);
                structSizes.put(typeName, structSize);
            }
            return new Declaration(typeChain, typeName, typeName + ".LAYOUT", structSize);
        }
        return null;
    }
}
