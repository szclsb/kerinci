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
    private MemorySegment instance;
    private MemorySegment surface;

    public VulkanApi(Arena arena, String applicationName, GlfwApi glfwApi) {
        this.arena = arena;
        this.glfwApi = glfwApi;

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

            var pInstance = arena.allocate(C_POINTER);

            if (krc_vkCreateInstance(instanceCreateInfo, MemorySegment.NULL, pInstance) != VK_SUCCESS()) {
                throw new RuntimeException("Failed to create instance");
            }
            instance = pInstance.get(C_POINTER, 0);
        }
    }

    public MemorySegment getInstance() {
        return instance;
    }

    public void setSurface(MemorySegment surface) {
        this.surface = surface;
    }

    @Override
    public void close() throws Exception {
//        krc_vkDestroySurfaceKHR(instance, surface, MemorySegment.NULL);
        krc_vkDestroyInstance(instance, MemorySegment.NULL);
    }
}
