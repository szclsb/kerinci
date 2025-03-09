package ch.szclsb.kerinci.internal;

import org.reflections.Reflections;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Runtime {
    private Runtime () {}

    private static final SymbolLookup LOADER = SymbolLookup.loaderLookup();

    static {
        System.loadLibrary("D:/Projects/kerinci/kerinci-internal/target-native/Debug/Kerinci.dll");  //FIXME
    }

    public static MemorySegment loadSymbol(String name) {
        return LOADER.find(name).orElseThrow(() -> new UnsatisfiedLinkError("unable to find symbol " + name));
    }

    private static final Map<Class<?>, Map<Integer, ?>> typeConstants = initHasValueTypes();
    private static Map<Class<?>, Map<Integer, ?>> initHasValueTypes() {
        var result = new HashMap<Class<?>, Map<Integer, ?>>();
        var reflections = new Reflections("ch.szclsb.kerinci.api");
        reflections.getSubTypesOf(HasValue.class).forEach(nativeEnum -> {
            if (nativeEnum.isEnum()) {
                var constants = new TreeMap<Integer, Object>();
                for (var c : nativeEnum.getEnumConstants()) {
                    constants.putIfAbsent(c.getValue(), c);
                }
                result.put(nativeEnum, constants);
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T extends HasValue> T getConstOfValue(int value, Class<T> enumClass) {
        return (T) typeConstants.get(enumClass).get(value);
    }
}
