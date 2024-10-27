package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.HashSet;

import static ch.szclsb.kerinci.api.api_h.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkGetPhysicalDeviceQueueFamilyProperties;
import static ch.szclsb.kerinci.internal.Utils.checkFlags;

public class VulkanApi implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(VulkanApi.class);

    private final Arena arena;
    private final MemorySegment instance;
    private final MemorySegment physicalDevice;

    public VulkanApi(String applicationName) {
        this(applicationName, false);
    }

    public VulkanApi(String applicationName, boolean debug) {
        this.arena = Arena.ofConfined();
        this.instance = initVulkan(applicationName, debug);
        this.physicalDevice = pickPhysicalDevice();
    }

    private MemorySegment initVulkan(String applicationName, boolean debug) {
        logger.info("debug={}", debug);

        var engineName = arena.allocateUtf8String("Kerinci");
        var appName = arena.allocateUtf8String(applicationName);

        var app = arena.allocate(VkApplicationInfo.$LAYOUT());
        VkApplicationInfo.sType$set(app, VK_STRUCTURE_TYPE_APPLICATION_INFO());
        VkApplicationInfo.pApplicationName$set(app, appName);
        VkApplicationInfo.pEngineName$set(app, engineName);
        VkApplicationInfo.apiVersion$set(app, VK_API_VERSION_1_1());

        var requiredExtensions = getRequiredExtensions(debug);
        var instanceCreateInfo = arena.allocate(VkInstanceCreateInfo.$LAYOUT());
        VkInstanceCreateInfo.sType$set(instanceCreateInfo, VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO());
        VkInstanceCreateInfo.pApplicationInfo$set(instanceCreateInfo, app);
        VkInstanceCreateInfo.enabledExtensionCount$set(instanceCreateInfo, requiredExtensions.size());
        VkInstanceCreateInfo.ppEnabledExtensionNames$set(instanceCreateInfo, requiredExtensions.data());
        VkInstanceCreateInfo.pNext$set(instanceCreateInfo, MemorySegment.NULL);

        var pInstance = arena.allocate(VkInstance);
        if (krc_vkCreateInstance(instanceCreateInfo, MemorySegment.NULL, pInstance) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create instance");
        }
        var instance = pInstance.get(VkInstance, 0);
        logger.debug("Created vulkan instance @{}", instance.address());
        return instance;
    }

    private NativeArray getRequiredExtensions(boolean debug) {
        var requiredExtensions = new HashSet<String>();
        var pGlfwExtensionCount = arena.allocate(uint32_t);
        var ppGlfwExtensions = krc_glfwGetRequiredInstanceExtensions(pGlfwExtensionCount);
        var glfwExtensionCount = pGlfwExtensionCount.get(uint32_t, 0);

        for (int i = 0; i < glfwExtensionCount; ++i) {
            requiredExtensions.add(ppGlfwExtensions.get(C_POINTER, i * C_POINTER.byteSize()).getUtf8String(0));
        }
        if (debug) {
            requiredExtensions.add(VK_EXT_DEBUG_UTILS_EXTENSION_NAME().getUtf8String(0));
        }

        var requiredExtensionsCopy = new ArrayList<>(requiredExtensions);
        var pInstanceExtensionCount = arena.allocate(uint32_t);
        krc_vkEnumerateInstanceExtensionProperties(MemorySegment.NULL, pInstanceExtensionCount, MemorySegment.NULL);
        var instanceExtensionCount = pInstanceExtensionCount.get(uint32_t, 0);
        var ppInstanceExtensions = arena.allocate(MemoryLayout.sequenceLayout(instanceExtensionCount, VkExtensionProperties.$LAYOUT()));
        krc_vkEnumerateInstanceExtensionProperties(MemorySegment.NULL, pInstanceExtensionCount, ppInstanceExtensions);
        logger.info("available extensions:");
        for (int i = 0; i < instanceExtensionCount; ++i) {
            var extensionProperty = ppInstanceExtensions.asSlice(i * VkExtensionProperties.sizeof(), VkExtensionProperties.$LAYOUT());
            var extensionName = VkExtensionProperties.extensionName$slice(extensionProperty).getUtf8String(0);
            if (requiredExtensions.remove(extensionName)) {
                logger.info("* {}", extensionName);
            } else {
                logger.info("  {}", extensionName);
            }
        }
        if (!requiredExtensions.isEmpty()) {
            throw new RuntimeException("Missing required extensions: " + String.join(", ", requiredExtensions));
        }

        var ppRequiredExtensions = arena.allocate(MemoryLayout.sequenceLayout(requiredExtensionsCopy.size(), C_POINTER));
        for (int i = 0; i < requiredExtensionsCopy.size(); ++i) {
            var extensionName = arena.allocateUtf8String(requiredExtensionsCopy.get(i));
            ppRequiredExtensions.set(C_POINTER, i * C_POINTER.byteSize(), extensionName);
        }
        return new NativeArray(ppRequiredExtensions, requiredExtensionsCopy.size());
    }

    private MemorySegment pickPhysicalDevice() {
        var pPhysicalDevicesCount = arena.allocate(uint32_t);
        krc_vkEnumeratePhysicalDevices(instance, pPhysicalDevicesCount, MemorySegment.NULL);
        var physicalDevicesCount = pPhysicalDevicesCount.get(uint32_t, 0);
        var pPhysicalDevices = arena.allocate(MemoryLayout.sequenceLayout(physicalDevicesCount, VkPhysicalDevice));
        krc_vkEnumeratePhysicalDevices(instance, pPhysicalDevicesCount, pPhysicalDevices);

        var suitablePhysicalDevice = MemorySegment.NULL;
        logger.info("physical devices:");
        var physicalDeviceProperties = arena.allocate(VkPhysicalDeviceProperties.$LAYOUT());
        for (var i = 0; i < physicalDevicesCount; ++i) {
            var physicalDevice = pPhysicalDevices.get(VkPhysicalDevice, i * VkPhysicalDevice.byteSize());
            krc_vkGetPhysicalDeviceProperties(physicalDevice, physicalDeviceProperties);
            var deviceName = VkPhysicalDeviceProperties.deviceName$slice(physicalDeviceProperties).getUtf8String(0);

            if (isDeviceSuitable(physicalDevice)) {
                suitablePhysicalDevice = physicalDevice;
                logger.info("* {}", deviceName);
            } else {
                logger.info("  {}", deviceName);
            }
        }
        if (suitablePhysicalDevice.address() == 0) {
            throw new RuntimeException("No suitable physical device found");
        }
        logger.debug("selected physical device @{}", suitablePhysicalDevice.address());
        return suitablePhysicalDevice;
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
