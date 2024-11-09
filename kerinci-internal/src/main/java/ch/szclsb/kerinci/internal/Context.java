package ch.szclsb.kerinci.internal;

import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Context {
    public static final Context INSTANCE = new Context();

    private Map<Class<?>, Map<Integer, ?>> typeConstants = new HashMap<>();

    private Context() {
        var reflections = new Reflections("ch.szclsb.kerinci.internal");
        reflections.getSubTypesOf(HasValue.class).forEach(nativeEnum -> {
            if (nativeEnum.isEnum()) {
                var constants = new TreeMap<Integer, Object>();
                for (var c : nativeEnum.getEnumConstants()) {
                    constants.putIfAbsent(c.getValue(), c);
                }
                typeConstants.put(nativeEnum, constants);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends HasValue> T getEnum(int value, Class<T> enumClass) {
        return (T) typeConstants.get(enumClass).get(value);
    }
}
