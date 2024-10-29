package ch.szclsb.kerinci.internal;


import ch.szclsb.kerinci.api.VkExtent2D;
import ch.szclsb.kerinci.api.VkSurfaceCapabilitiesKHR;
import ch.szclsb.kerinci.api.VkSurfaceFormatKHR;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h.*;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Window implements AutoCloseable {
    private Arena arena;
    private final VulkanApi vk;
    private final MemorySegment handle;
    private final MemorySegment surface;
    private int width;
    private int height;

    Window(VulkanApi vk, int width, int height, String name) {
        this.arena = Arena.ofConfined();
        this.height = height;
        this.width = width;
        this.vk = vk;
        var windowName = arena.allocateUtf8String(name);

        this.handle = krc_glfwCreateWindow(width, height, windowName, MemorySegment.NULL, MemorySegment.NULL);

        var pVkSurfaceKHR = arena.allocate(C_POINTER);
        if (krc_glfwCreateWindowSurface(vk.getInstance(), handle, MemorySegment.NULL, pVkSurfaceKHR) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create window surface");
        }
        this.surface = pVkSurfaceKHR.get(C_POINTER, 0);
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

    protected MemorySegment getExtent() {
        var extent = arena.allocate(VkExtent2D.$LAYOUT());
        VkExtent2D.height$set(extent, height);
        VkExtent2D.width$set(extent, width);
        return extent;
    }

    protected SwapChainSupportDetails querySwapChainSupport() {
        var capabilities = arena.allocate(VkSurfaceCapabilitiesKHR.$LAYOUT());
        krc_vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vk.getPhysicalDevice(), surface, capabilities);

        var pFormatCount = arena.allocate(uint32_t);
        krc_vkGetPhysicalDeviceSurfaceFormatsKHR(vk.getPhysicalDevice(), surface, pFormatCount, MemorySegment.NULL);
        var formatCount = pFormatCount.get(uint32_t, 0);
        var pFormats = arena.allocate(MemoryLayout.sequenceLayout(formatCount, VkSurfaceFormatKHR.$LAYOUT()));
        krc_vkGetPhysicalDeviceSurfaceFormatsKHR(vk.getPhysicalDevice(), surface, pFormatCount, pFormats);

        var pPresentModeCount = arena.allocate(uint32_t);
        vkGetPhysicalDeviceSurfacePresentModesKHR(vk.getPhysicalDevice(), surface, pPresentModeCount, MemorySegment.NULL);
        var presentModeCount = pPresentModeCount.get(uint32_t, 0);
        var pPresentModes = arena.allocate(MemoryLayout.sequenceLayout(presentModeCount, JAVA_INT));
        vkGetPhysicalDeviceSurfacePresentModesKHR(vk.getPhysicalDevice(), surface, pPresentModeCount, pPresentModes);

        return new SwapChainSupportDetails(
                capabilities,
                new NativeArray(pFormats, formatCount),
                new NativeArray(pPresentModes, presentModeCount)
        );
    }

    //todo resizing

    @Override
    public void close() throws Exception {
        krc_vkDestroySurfaceKHR(vk.getInstance(), surface, MemorySegment.NULL);
        krc_glfwDestroyWindow(handle);
        arena.close();
    }
}
