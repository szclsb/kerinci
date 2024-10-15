package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkApplicationInfo;
import ch.szclsb.kerinci.api.VkInstanceCreateInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static ch.szclsb.kerinci.api.api_h.*;

public class VulkanApi implements AutoCloseable {
    private final Arena arena;
    private final GlfwApi glfwApi;
    private final MemorySegment instance;
    private final MemorySegment surface;

    public VulkanApi(Arena arena, String applicationName, GlfwApi glfwApi) {
        this.arena = arena;
        this.glfwApi = glfwApi;

        this.instance = arena.allocate(VkInstance);
        this.surface = arena.allocate(VkSurfaceKHR);
        initVulkan(applicationName);
    }

    private void initVulkan(String applicationName) {
        try (var localArena = Arena.ofConfined()) {
            var engineName = localArena.allocateUtf8String("Kerinci");
            var appName = localArena.allocateUtf8String(applicationName);

            var app = VkApplicationInfo.allocate(arena);
            VkApplicationInfo.sType$set(app,VK_STRUCTURE_TYPE_APPLICATION_INFO());
            VkApplicationInfo.pApplicationName$set(app, appName);
            VkApplicationInfo.pEngineName$set(app, engineName);
            VkApplicationInfo.apiVersion$set(app, VK_API_VERSION_1_1());

            var instanceCreateInfo = VkInstanceCreateInfo.allocate(arena);
            VkInstanceCreateInfo.sType$set(instanceCreateInfo, VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO());
            VkInstanceCreateInfo.pApplicationInfo$set(instanceCreateInfo, app);

            if (krc_vkCreateInstance(instanceCreateInfo, MemorySegment.NULL, instance) != VK_SUCCESS()) {
                throw new RuntimeException("Failed to create instance");
            }
        }
    }

    public MemorySegment getInstance() {
        return instance;
    }

    public MemorySegment getSurface() {
        return surface;
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroySurfaceKHR(instance, surface, MemorySegment.NULL);
        krc_vkDestroyInstance(instance, MemorySegment.NULL);
    }
}
