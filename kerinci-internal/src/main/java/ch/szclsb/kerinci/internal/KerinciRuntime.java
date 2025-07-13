package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.base.api.Runtime;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class KerinciRuntime implements Runtime {
    private static final SymbolLookup loader = SymbolLookup.loaderLookup();
    private static final Linker linker = Linker.nativeLinker();

    private final Arena memorySession;

    public KerinciRuntime() {
        System.loadLibrary("D:/Projects/kerinci/kerinci-internal/target-native/Debug/Kerinci.dll");  //FIXME
        this.memorySession = Arena.ofShared();
    }

    public MethodHandle linkMethod(String name, FunctionDescriptor functionDescriptor) {
        var symbol = loader.find(name).orElseThrow(() -> new UnsatisfiedLinkError("unable to find symbol " + name));
        return linker.downcallHandle(symbol, functionDescriptor);
    }

    @Override
    public void close() throws Exception {
        memorySession.close();
    }
}
