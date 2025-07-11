package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.base.api.Runtime;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;

public class InternalRuntime implements Runtime {
    private static final SymbolLookup LOADER = SymbolLookup.loaderLookup();

    public InternalRuntime() {
        System.loadLibrary("D:/Projects/kerinci/kerinci-internal/target-native/Debug/Kerinci.dll");  //FIXME
    }

    public MemorySegment loadSymbol(String name) {
        return LOADER.find(name).orElseThrow(() -> new UnsatisfiedLinkError("unable to find symbol " + name));
    }
}
