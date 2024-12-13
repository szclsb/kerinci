package ch.szclsb.maven.plugins;

import ch.szclsb.maven.plugins.writer.EnumWriter;
import ch.szclsb.maven.plugins.writer.StructWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Context {
    public record Declaration(
            String kind,
            int bytes
    ) {

    }

    private static final Set<String> cursorKinds = Set.of(
            LibcCursor.KIND_FUNCTION,
            LibcCursor.KIND_STRUCT,
            LibcCursor.KIND_ENUM
    );

    private final Map<String, LibcCursor> declarations;
    private final Map<String, Integer> structSizes;
    private final Set<String> enumNames;
    private final StructWriter structWriter;
    private final EnumWriter enumWriter;

    public Context(LibcCursor translationUnit, StructWriter structWriter, EnumWriter enumWriter) {
        this.declarations = translationUnit.getChildren().stream()
                .filter(cursor -> cursorKinds.contains(cursor.getKind()))
                .collect(Collectors.toMap(LibcCursor::getSpelling, Function.identity()));
        this.structSizes = new HashMap<>();
        this.enumNames = new HashSet<>();
        this.structWriter = structWriter;
        this.enumWriter = enumWriter;
    }

    public Stream<LibcCursor> getDeclarations() {
        return declarations.values().stream();
    }

    public Stream<LibcCursor> getDeclarations(String cursorKind) {
        return getDeclarations().filter(cursor -> cursorKind.equals(cursor.getKind()));
    }

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    public synchronized Declaration declare(String name) throws IOException {
        var cursor = declarations.get(name);
        if (cursor == null) {
            return new Declaration("XXX", 0);  //FIXME typedefs and function pointer
        }
        if (LibcCursor.KIND_ENUM.equals(cursor.getKind())) {
            if (!enumNames.contains(name)) {
                enumWriter.write(cursor);
                enumNames.add(name);
            }
            return new Declaration(LibcCursor.KIND_ENUM, 4);
        } else if (LibcCursor.KIND_STRUCT.equals(cursor.getKind())) {
            if (structSizes.containsKey(name)) {
                return new Declaration(LibcCursor.KIND_STRUCT, structSizes.get(name));
            }
            var structSize = structWriter.write(cursor, this);
            structSizes.put(name, structSize);
            return new Declaration(LibcCursor.KIND_STRUCT, structSize);
        }
        throw new IllegalArgumentException("Unknown kind: " + cursor.getKind());
    }
}
