package ch.szclsb.kerinci.internal;


import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h.*;

public class Window implements AutoCloseable {
    private final MemorySegment handle;
    private final MemorySegment vkInstance;
    private final MemorySegment surface;

    Window(MemorySegment vkInstance, int width, int height, String name) {
        this.vkInstance = vkInstance;
        try (var localArena = Arena.ofConfined()) {
            var windowName = localArena.allocateUtf8String(name);

            this.handle = krc_glfwCreateWindow(width, height, windowName, MemorySegment.NULL, MemorySegment.NULL);

            var pVkSurfaceKHR = localArena.allocate(C_POINTER);
            if(krc_glfwCreateWindowSurface(vkInstance, handle, MemorySegment.NULL, pVkSurfaceKHR) != VK_SUCCESS()) {
                throw new RuntimeException("Failed to create window surface");
            }
            this.surface = pVkSurfaceKHR.get(C_POINTER, 0);
        }
    }

    public boolean shouldClose() {
        return krc_glfwWindowShouldClose(handle) == GLFW_TRUE();
    }

    //todo resizing

    @Override
    public void close() throws Exception {
        krc_vkDestroySurfaceKHR(vkInstance, surface, MemorySegment.NULL);
        krc_glfwDestroyWindow(handle);
    }
}
