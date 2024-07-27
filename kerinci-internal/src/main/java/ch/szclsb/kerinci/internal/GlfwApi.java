package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

public class GlfwApi implements AutoCloseable {
    private final MethodHandle nativeGlfwInit;
    private final MethodHandle nativeGlfwCreateWindow;
    //    private final MethodHandle glfwSetWindowUserPointer;
    private final MethodHandle nativeGlfwWindowShouldClose;
    private final MethodHandle nativeGlfwPollEvents;
    private final MethodHandle nativeGlfwDestroyWindow;
    private final MethodHandle nativeGlfwTerminate;

    public GlfwApi() {
        this.nativeGlfwInit = Introspector.loadMethod("kerinciGlfwInit", null);
        this.nativeGlfwCreateWindow = Introspector.loadMethod("kerinciGlfwCreateWindow", ADDRESS, JAVA_INT, JAVA_INT, ADDRESS);
        this.nativeGlfwWindowShouldClose = Introspector.loadMethod("kerinciGlfwWindowShouldClose", JAVA_BOOLEAN, ADDRESS);
//            this.glfwCreateWindowSurface = LINKER.downcallHandle(loadSymbol("glfwCreateWindowSurface"), FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
        this.nativeGlfwPollEvents = Introspector.loadMethod("kerinciGlfwPollEvents", null);
        this.nativeGlfwDestroyWindow = Introspector.loadMethod("kerinciGlfwDestroyWindow", null, ADDRESS);
        this.nativeGlfwTerminate = Introspector.loadMethod("kerinciGlfwTerminate", null);
        init();
    }

    private void init() {
        try {
            this.nativeGlfwInit.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public MemorySegment createWindow(int width, int height, MemorySegment title) {
        try {
            return (MemorySegment) this.nativeGlfwCreateWindow.invoke(width, height, title);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldClose(MemorySegment window) {
        try {
            return (boolean) this.nativeGlfwWindowShouldClose.invoke(window);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void pollEvents() {
        try {
            this.nativeGlfwPollEvents.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void destroyWindow(MemorySegment window) {
        try {
            this.nativeGlfwDestroyWindow.invoke(window);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.nativeGlfwTerminate.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
