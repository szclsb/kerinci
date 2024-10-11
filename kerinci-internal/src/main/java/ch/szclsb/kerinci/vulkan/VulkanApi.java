package ch.szclsb.kerinci.vulkan;

import java.lang.foreign.Arena;

import static ch.szclsb.kerinci.vulkan.vulkan_h.*;

public class VulkanApi implements AutoCloseable {
    private final Arena arena;

    public VulkanApi(String applicationName) {
        this.arena = Arena.ofShared();

        try (var localArena = Arena.ofConfined()) {
            var engineName = localArena.allocateUtf8String("Kerinci");
            var appName = localArena.allocateUtf8String(applicationName);

            var app = VkApplicationInfo.allocate(arena);
            VkApplicationInfo.sType$set(app, VK_STRUCTURE_TYPE_APPLICATION_INFO());
            VkApplicationInfo.pApplicationName$set(app, appName);
            VkApplicationInfo.pEngineName$set(app, engineName);
            VkApplicationInfo.apiVersion$set(app, VK_API_VERSION_1_1());

            var instance = VkInstanceCreateInfo.allocate(arena);
            VkInstanceCreateInfo.sType$set(instance, VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO());
            VkInstanceCreateInfo.pApplicationInfo$set(instance, app);
        }
    }

    @Override
    public void close() throws Exception {
        this.arena.close();
    }
}
