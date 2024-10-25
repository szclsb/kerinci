package ch.szclsb.kerinci.internal;


import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h.*;

public class Window implements AutoCloseable {
    private final VulkanApi vk;
    private final MemorySegment handle;
    private final MemorySegment surface;

    Window(VulkanApi vk, int width, int height, String name) {
        this.vk = vk;
        try (var localArena = Arena.ofConfined()) {
            var windowName = localArena.allocateUtf8String(name);

            this.handle = krc_glfwCreateWindow(width, height, windowName, MemorySegment.NULL, MemorySegment.NULL);

            var pVkSurfaceKHR = localArena.allocate(C_POINTER);
            if(krc_glfwCreateWindowSurface(vk.getInstance(), handle, MemorySegment.NULL, pVkSurfaceKHR) != VK_SUCCESS()) {
                throw new RuntimeException("Failed to create window surface");
            }
            this.surface = pVkSurfaceKHR.get(C_POINTER, 0);
        }
    }

    protected MemorySegment getHandle() {
        return handle;
    }

    protected MemorySegment getSurface() {
        return surface;
    }

    public boolean shouldClose() {
        return krc_glfwWindowShouldClose(handle) == GLFW_TRUE();
    }

    //todo resizing

    @Override
    public void close() throws Exception {
        krc_vkDestroySurfaceKHR(vk.getInstance(), surface, MemorySegment.NULL);
        krc_glfwDestroyWindow(handle);
    }
}
