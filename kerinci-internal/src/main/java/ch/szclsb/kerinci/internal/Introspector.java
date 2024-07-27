package ch.szclsb.kerinci.internal;

import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Properties;

public final class Introspector {
    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup loader = SymbolLookup.loaderLookup();

    private Introspector() {}

    static {
        try (var is = Introspector.class.getClassLoader().getResourceAsStream("driver.properties")) {
            var properties = new Properties();
            properties.load(is);
            var library = (String) properties.get("native-dll");
//            var vulkanHeader = (String) properties.get("vulkan-header");
//            var glmHeader = (String) properties.get("glm-header");
            System.load(library);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MemorySegment loadSymbol(String name) {
        return loader.find(name).orElseThrow(() -> new UnsatisfiedLinkError("unable to find symbol " + name));
    }

    public static MethodHandle loadMethod(String name, MemoryLayout returnLayout, MemoryLayout... argsLayout) {
        var symbol = loadSymbol(name);
        var descriptor = returnLayout != null
                ? FunctionDescriptor.of(returnLayout, argsLayout)
                : FunctionDescriptor.ofVoid(argsLayout);
        return linker.downcallHandle(symbol, descriptor);
    }
}
