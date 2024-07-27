package ch.szclsb.kerinci.graphic;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Properties;

import static java.lang.foreign.ValueLayout.*;

public class VulkanApi {
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOADER = SymbolLookup.loaderLookup();

    private final MethodHandle nativeGlfwInit;
    private final MethodHandle nativeGlfwCreateWindow;
//    private final MethodHandle glfwSetWindowUserPointer;
    private final MethodHandle nativeGlfwWindowShouldClose;
    private final MethodHandle nativeGlfwPollEvents;
    private final MethodHandle nativeGlfwDestroyWindow;
    private final MethodHandle nativeGlfwTerminate;

    private static MemorySegment loadSymbol(String name) {
        return LOADER.find(name).orElseThrow(() -> new UnsatisfiedLinkError("unable to find symbol " + name));
    }

    public VulkanApi() {
        try (var is = getClass().getClassLoader().getResourceAsStream("driver.properties")) {
            var properties = new Properties();
            properties.load(is);
            var library = (String) properties.get("native-dll");
//            var vulkanHeader = (String) properties.get("vulkan-header");
//            var glmHeader = (String) properties.get("glm-header");
            System.load(library);

            //this.invokeNative = LINKER.downcallHandle(loadSymbol("invoke"), FunctionDescriptor.ofVoid(JAVA_INT, JAVA_BOOLEAN));
            this.nativeGlfwInit = LINKER.downcallHandle(loadSymbol("kerinciGlfwInit"), FunctionDescriptor.ofVoid());
            this.nativeGlfwCreateWindow = LINKER.downcallHandle(loadSymbol("kerinciGlfwCreateWindow"), FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, ADDRESS));
//            this.glfwSetWindowUserPointer = LINKER.downcallHandle(loadSymbol("glfwSetWindowUserPointer"), FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
            this.nativeGlfwWindowShouldClose = LINKER.downcallHandle(loadSymbol("kerinciGlfwWindowShouldClose"), FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS));
//            this.glfwCreateWindowSurface = LINKER.downcallHandle(loadSymbol("glfwCreateWindowSurface"), FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
            this.nativeGlfwPollEvents = LINKER.downcallHandle(loadSymbol("kerinciGlfwPollEvents"), FunctionDescriptor.ofVoid());
            this.nativeGlfwDestroyWindow = LINKER.downcallHandle(loadSymbol("kerinciGlfwDestroyWindow"), FunctionDescriptor.ofVoid(ADDRESS));
            this.nativeGlfwTerminate = LINKER.downcallHandle(loadSymbol("kerinciGlfwTerminate"), FunctionDescriptor.ofVoid());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void glfwInit() {
        try {
            this.nativeGlfwInit.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public MemorySegment glfwCreateWindow(int width, int height, MemorySegment title) {
        try {
            return (MemorySegment) this.nativeGlfwCreateWindow.invoke(width, height, title);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean glfwShouldClose(MemorySegment window) {
        try {
            return (boolean) this.nativeGlfwWindowShouldClose.invoke(window);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void glfwPollEvents() {
        try {
            this.nativeGlfwPollEvents.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public void glfwDestroyWindow(MemorySegment window) {
        try {
            this.nativeGlfwDestroyWindow.invoke(window);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void glfwTerminate() {
        try {
            this.nativeGlfwTerminate.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
