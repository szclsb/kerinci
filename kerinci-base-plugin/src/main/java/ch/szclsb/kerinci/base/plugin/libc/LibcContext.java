package ch.szclsb.kerinci.base.plugin.libc;

import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcType;
import ch.szclsb.kerinci.base.plugin.libc.writer.LibcObjectWriter;
import ch.szclsb.kerinci.base.plugin.template.EnumTemplateDefinition;
import ch.szclsb.kerinci.base.plugin.template.StructTemplateDefinition;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class LibcContext {
    private static final String VK_FLAGS = "VkFlags";

    public static String getFlagBitsType(String flags) {
        return flags == null || flags.isBlank() ? null : flags.replace("Flags", "FlagBits");
    }

    public record TypeRef(String name, int pointer) {
        public TypeRef(String name) {
            this(name, 0);
        }
    }

    public record Declaration(
            String cursorKind,
            List<TypeRef> typeChain,
            String javaType,
            String javaLayout,
            long bytes
    ) {
        public boolean isFlag() {
            return typeChain.stream().anyMatch(typeRef -> typeRef.name().contains(VK_FLAGS));
        }

        public boolean isPointer() {
            return typeChain.stream()
                    .mapToInt(TypeRef::pointer)
                    .sum() > 0;
        }

//        public boolean isHandle() {
//            return typeChain.getLast().endsWith("_T");
//        }
    }

    private static final Set<String> cursorKinds = Set.of(
            LibcCursor.KIND_FUNCTION,
            LibcCursor.KIND_STRUCT,
            LibcCursor.KIND_ENUM
    );

    private final Map<String, LibcCursor> declarations;
    private final Map<String, TypeRef> typedefs;
    private final Map<String, Long> structSizes;
    private final Set<String> enumNames;
    private final LibcObjectWriter<StructTemplateDefinition> structWriter;
    private final LibcObjectWriter<EnumTemplateDefinition> enumWriter;

    public LibcContext(LibcCursor translationUnit,
                       LibcObjectWriter<StructTemplateDefinition> structWriter,
                       LibcObjectWriter<EnumTemplateDefinition> enumWriter) {
        this.declarations = new HashMap<>();
        this.typedefs = new HashMap<>();
//        this.handles = new HashMap<>();
        for (var declCursor : translationUnit.getChildren()) {
            if (cursorKinds.contains(declCursor.getKind())) {
                declarations.put(declCursor.getSpelling(), declCursor);
            } else if (LibcCursor.KIND_TYPEDEF.equals(declCursor.getKind())) {
                // type definitions
                var pointer = new AtomicInteger(0);
                var refType = declCursor.getUnderlyingTypedefType();
                while (LibcType.KIND_POINTER.equals(refType.getKind())) {
                    pointer.getAndIncrement();
                    refType = refType.getRef();
                }

                if (LibcType.KIND_ELABORATED.equals(refType.getKind())) {
                    declCursor.getChildren().stream()
                            .filter(c -> LibcCursor.KIND_TYPEREF.equals(c.getKind()))
                            .findFirst()
                            .ifPresent(a -> {
                                var typeRef = a.getSpelling();
                                if (typeRef.startsWith("struct ")) {
                                    var structType = typeRef.substring(7);
                                    addTypeDef(declCursor.getSpelling(), structType, pointer.get());
                                } else {
                                    addTypeDef(declCursor.getSpelling(), typeRef, pointer.get());
                                }
                            });
                }
            }
        }
        this.structSizes = new HashMap<>();
        this.enumNames = new HashSet<>();
        this.structWriter = structWriter;
        this.enumWriter = enumWriter;
    }

    private void addTypeDef(String ref, String type, int pointer) {
        if (!ref.equals(type)) {
            typedefs.put(ref, new TypeRef(type, pointer));
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
        var typeChain = new LinkedList<TypeRef>();
        var tn = new TypeRef(typeName);
        while (tn != null) {
            typeChain.add(tn);
            tn = typedefs.get(tn.name());
        }

        var actualType = typeChain.getLast();

        // handle VkFlag as bit mask using int
        if (typeChain.stream().anyMatch(typeRef -> typeRef.name().contains(VK_FLAGS))) {
            var flagBitsType = getFlagBitsType(typeName);
            if (declare(flagBitsType) == null) {  // declare flag bit enums
                return null;  // abort if corresponding bits are not present
            }
            return new Declaration(null, typeChain, flagBitsType, "JAVA_INT", 4);  // use flagBitsType instead of actualType
        }

        var cursor = declarations.get(actualType.name());
        if (cursor == null) {
            return switch (actualType.name()) {
                case "bool", "VkBool32" -> new Declaration(null, typeChain, "bool", "JAVA_BOOLEAN", 1);
                case "int32_t", "uint32_t" -> new Declaration(null, typeChain, "int", "JAVA_INT", 4);
                case "int64_t", "uint64_t" -> new Declaration(null, typeChain, "long", "JAVA_LONG", 8);
                default -> null;
            };
        }
        if (LibcCursor.KIND_ENUM.equals(cursor.getKind())) {
            if (!enumNames.contains(typeName)) {
                enumWriter.write(typeName, cursor, this);
                enumNames.add(typeName);
            }
            return new Declaration(LibcCursor.KIND_ENUM, typeChain, typeName, "JAVA_INT", 4);
        } else if (LibcCursor.KIND_STRUCT.equals(cursor.getKind())) {
            var structSize = structSizes.get(typeName);
            if (structSize == null) {
                structSize = structWriter.write(typeName, cursor, this).orElse(0);
                structSizes.put(typeName, structSize);
            }
            return new Declaration(LibcCursor.KIND_STRUCT, typeChain, typeName, typeName + ".LAYOUT", structSize);
        }
        return null;
    }
}
