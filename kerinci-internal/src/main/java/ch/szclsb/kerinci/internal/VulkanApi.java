package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkApplicationInfo;
import ch.szclsb.kerinci.api.VkInstanceCreateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h.*;

public class VulkanApi implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(VulkanApi.class);

    private final Arena arena;
    private MemorySegment instance;

    public VulkanApi(Arena arena, String applicationName) {
        this.arena = arena;

        initVulkan(applicationName);
    }

    private void initVulkan(String applicationName) {
        try (var localArena = Arena.ofConfined()) {
            var engineName = localArena.allocateUtf8String("Kerinci");
            var appName = localArena.allocateUtf8String(applicationName);

            var app = VkApplicationInfo.allocate(localArena);
            VkApplicationInfo.sType$set(app,VK_STRUCTURE_TYPE_APPLICATION_INFO());
            VkApplicationInfo.pApplicationName$set(app, appName);
            VkApplicationInfo.pEngineName$set(app, engineName);
            VkApplicationInfo.apiVersion$set(app, VK_API_VERSION_1_1());

            var pRequiredExtensionCount = arena.allocate(uint32_t);
            var ppRequiredExtensions = krc_glfwGetRequiredInstanceExtensions(pRequiredExtensionCount);
            var requiredExtensionCount = pRequiredExtensionCount.get(uint32_t, 0);

            logger.info("required extensions:");
            for (int i = 0; i < requiredExtensionCount; ++i) {
                var extension = ppRequiredExtensions.get(C_POINTER, i * C_POINTER.byteSize()).getUtf8String(0);
                logger.info(extension);
            }

            var instanceCreateInfo = VkInstanceCreateInfo.allocate(localArena);
            VkInstanceCreateInfo.sType$set(instanceCreateInfo, VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO());
            VkInstanceCreateInfo.pApplicationInfo$set(instanceCreateInfo, app);
            VkInstanceCreateInfo.enabledExtensionCount$set(instanceCreateInfo, requiredExtensionCount);
            VkInstanceCreateInfo.ppEnabledExtensionNames$set(instanceCreateInfo, ppRequiredExtensions);
            VkInstanceCreateInfo.pNext$set(instanceCreateInfo, MemorySegment.NULL);

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

    @Override
    public void close() throws Exception {
        krc_vkDestroyInstance(instance, MemorySegment.NULL);
    }
}
