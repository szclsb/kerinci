package ch.szclsb.kerinci.internal.glfw;

import ch.szclsb.kerinci.internal.vulkan.KrcExtent2D;
import ch.szclsb.kerinci.internal.vulkan.VulkanApi;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static ch.szclsb.kerinci.api.api_h.*;
import static java.lang.foreign.ValueLayout.ADDRESS;

public class KrcWindow implements AutoCloseable {
    private final VulkanApi vk;
    private final MemorySegment handle;
    private final MemorySegment surface;
    private KrcExtent2D extent;

    KrcWindow(VulkanApi vk, int width, int height, String name) {
        this.extent = new KrcExtent2D(width, height);
        this.vk = vk;
        try(var arena = Arena.ofConfined()) {
            var windowName = arena.allocateUtf8String(name);

            this.handle = krc_glfwCreateWindow(width, height, windowName, MemorySegment.NULL, MemorySegment.NULL);

            var pVkSurfaceKHR = arena.allocate(ADDRESS);
            if (krc_glfwCreateWindowSurface(vk.getInstance(), handle, MemorySegment.NULL, pVkSurfaceKHR) != VK_SUCCESS()) {
                throw new RuntimeException("Failed to create window surface");
            }
            this.surface = pVkSurfaceKHR.get(ADDRESS, 0);
        }
    }

    public MemorySegment getHandle() {
        return handle.asReadOnly();
    }

    public MemorySegment getSurface() {
        return surface.asReadOnly();
    }

    public boolean shouldClose() {
        return krc_glfwWindowShouldClose(handle) == GLFW_TRUE();
    }

    public KrcExtent2D getExtent() {
        return extent;
    }

    //todo resizing

    @Override
    public void close() throws Exception {
        krc_vkDestroySurfaceKHR(vk.getInstance(), surface, MemorySegment.NULL);
        krc_glfwDestroyWindow(handle);
    }
}
