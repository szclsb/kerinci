package ch.szclsb.kerinci.vulkan;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.vulkan.vulkan_h.*;

public class VulkanApi implements AutoCloseable {
    private final Arena arena;
    private final GlfwApi glfwApi;
    private MemorySegment instance;
    private MemorySegment surface;


    public VulkanApi(Arena arena, String applicationName, GlfwApi glfwApi) {
        this.arena = arena;
        this.glfwApi = glfwApi;

        initVulkan(applicationName);
        surface = arena.allocate(VkSurfaceKHR);
    }

    private void initVulkan(String applicationName) {
        try (var localArena = Arena.ofConfined()) {
            var engineName = localArena.allocateUtf8String("Kerinci");
            var appName = localArena.allocateUtf8String(applicationName);

            var app = VkApplicationInfo.allocate(arena);
            VkApplicationInfo.sType$set(app,  VK_STRUCTURE_TYPE_APPLICATION_INFO());
            VkApplicationInfo.pApplicationName$set(app, appName);
            VkApplicationInfo.pEngineName$set(app, engineName);
            VkApplicationInfo.apiVersion$set(app, VK_API_VERSION_1_1());

            this.instance = VkInstanceCreateInfo.allocate(arena);
            VkInstanceCreateInfo.sType$set(instance, VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO());
            VkInstanceCreateInfo.pApplicationInfo$set(instance, app);
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

    }
}
