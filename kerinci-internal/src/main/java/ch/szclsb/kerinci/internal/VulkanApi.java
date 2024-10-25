package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkGetPhysicalDeviceQueueFamilyProperties;
import static ch.szclsb.kerinci.internal.Utils.checkFlags;

public class VulkanApi implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(VulkanApi.class);

    private final Arena arena;
    private final MemorySegment instance;
    private final MemorySegment physicalDevice;

    public VulkanApi(String applicationName) {
        this.arena = Arena.ofConfined();
        this.instance = initVulkan(applicationName);
        this.physicalDevice = pickPhysicalDevice();
    }

    private MemorySegment initVulkan(String applicationName) {
        var engineName = arena.allocateUtf8String("Kerinci");
        var appName = arena.allocateUtf8String(applicationName);

        var app = arena.allocate(VkApplicationInfo.$LAYOUT());
        VkApplicationInfo.sType$set(app, VK_STRUCTURE_TYPE_APPLICATION_INFO());
        VkApplicationInfo.pApplicationName$set(app, appName);
        VkApplicationInfo.pEngineName$set(app, engineName);
        VkApplicationInfo.apiVersion$set(app, VK_API_VERSION_1_1());

        var pRequiredExtensionCount = arena.allocate(uint32_t);
        var ppRequiredExtensions = krc_glfwGetRequiredInstanceExtensions(pRequiredExtensionCount);
        var requiredExtensionCount = pRequiredExtensionCount.get(uint32_t, 0);

        logger.info("required extensions:");
        for (int i = 0; i < requiredExtensionCount; ++i) {
            var extension = ppRequiredExtensions.get(C_POINTER, i * C_POINTER.byteSize()).getUtf8String(0);
            logger.info("  {}", extension);
        }

        var instanceCreateInfo = arena.allocate(VkInstanceCreateInfo.$LAYOUT());
        VkInstanceCreateInfo.sType$set(instanceCreateInfo, VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO());
        VkInstanceCreateInfo.pApplicationInfo$set(instanceCreateInfo, app);
        VkInstanceCreateInfo.enabledExtensionCount$set(instanceCreateInfo, requiredExtensionCount);
        VkInstanceCreateInfo.ppEnabledExtensionNames$set(instanceCreateInfo, ppRequiredExtensions);
        VkInstanceCreateInfo.pNext$set(instanceCreateInfo, MemorySegment.NULL);

        var pInstance = arena.allocate(VkInstance);
        if (krc_vkCreateInstance(instanceCreateInfo, MemorySegment.NULL, pInstance) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create instance");
        }
        return pInstance.get(VkInstance, 0);
    }

    private MemorySegment pickPhysicalDevice() {
        var pPhysicalDevicesCount = arena.allocate(uint32_t);
        krc_vkEnumeratePhysicalDevices(instance, pPhysicalDevicesCount, MemorySegment.NULL);
        var physicalDevicesCount = pPhysicalDevicesCount.get(uint32_t, 0);
        var pPhysicalDevices = arena.allocate(MemoryLayout.sequenceLayout(physicalDevicesCount, VkPhysicalDevice));
        krc_vkEnumeratePhysicalDevices(instance, pPhysicalDevicesCount, pPhysicalDevices);

        int selection = -1;
        logger.info("physical devices:");
        var physicalDeviceProperties = arena.allocate(VkPhysicalDeviceProperties.$LAYOUT());
        for (var i = 0; i < physicalDevicesCount; ++i) {
            var physicalDevice = pPhysicalDevices.get(VkPhysicalDevice, i * VkPhysicalDevice.byteSize());
            krc_vkGetPhysicalDeviceProperties(physicalDevice, physicalDeviceProperties);
            var deviceName = VkPhysicalDeviceProperties.deviceName$slice(physicalDeviceProperties).getUtf8String(0);

            if (isDeviceSuitable(physicalDevice)) {
                selection = i;
                logger.info("* {}", deviceName);
            } else {
                logger.info("  {}", deviceName);
            }
        }
        if (selection < 0) {
            throw new RuntimeException("No suitable physical device found");
        }
        return pPhysicalDevices.get(VkPhysicalDevice, selection * VkPhysicalDevice.byteSize());
    }

    private boolean isDeviceSuitable(MemorySegment physicalDevice) {
        // todo check support
        return true;
    }

    public QueueFamilyIndices findQueueFamilies(Window window) {
        var indices = new QueueFamilyIndices();
        var pQueueFamilyCount = arena.allocate(uint32_t);
        krc_vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyCount, MemorySegment.NULL);
        var queueFamilyCount = pQueueFamilyCount.get(uint32_t, 0);
        var queueFamilies = arena.allocate(MemoryLayout.sequenceLayout(queueFamilyCount, VkQueueFamilyProperties.$LAYOUT()));
        krc_vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyCount, queueFamilies);

        for (var i = 0; i < queueFamilyCount && !indices.isComplete(); ++i) {
            if (VkQueueFamilyProperties.queueCount$get(queueFamilies, i) > 0
                    && checkFlags(VkQueueFamilyProperties.queueFlags$get(queueFamilies, i), VK_QUEUE_GRAPHICS_BIT())) {
                indices.graphicsFamily = i;
                indices.graphicsFamilyHasValue = true;
            }
            var presentSupport = arena.allocate(VkBool32);
            krc_vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, window.getSurface(), presentSupport);
            if (VkQueueFamilyProperties.queueCount$get(queueFamilies, i) > 0 && presentSupport.get(VkBool32, 0) == VK_TRUE()) {
                indices.presentFamily = i;
                indices.presentFamilyHasValue = true;
            }
        }

        return indices;
    }

    protected MemorySegment getInstance() {
        return instance;
    }

    protected MemorySegment getPhysicalDevice() {
        return physicalDevice;
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyInstance(instance, MemorySegment.NULL);
        arena.close();
    }
}
