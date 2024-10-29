package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static ch.szclsb.kerinci.api.api_h.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkGetPhysicalDeviceQueueFamilyProperties;
import static ch.szclsb.kerinci.internal.Utils.checkFlags;
import static ch.szclsb.kerinci.internal.Utils.printAddress;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class VulkanApi implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(VulkanApi.class);
    private static final Logger validationLayerLogger = LoggerFactory.getLogger("Validation Layer");

    private final Arena arena;
    private final MemorySegment instance;
    private final MemorySegment debugMessenger;
    private final MemorySegment physicalDevice;

    public VulkanApi(String applicationName) {
        this(applicationName, false);
    }

    public VulkanApi(String applicationName, boolean validationLayer) {
        this.arena = Arena.ofConfined();

        this.instance = initVulkan(applicationName, validationLayer);
        this.debugMessenger = validationLayer ? initDebugMessenger() : null;
        this.physicalDevice = pickPhysicalDevice();
    }

    private MemorySegment initVulkan(String applicationName, boolean validationLayer) {
        logger.info("validationLayer={}", validationLayer);

        var engineName = arena.allocateUtf8String("Kerinci");
        var appName = arena.allocateUtf8String(applicationName);

        var app = arena.allocate(VkApplicationInfo.$LAYOUT());
        VkApplicationInfo.sType$set(app, VK_STRUCTURE_TYPE_APPLICATION_INFO());
        VkApplicationInfo.pApplicationName$set(app, appName);
        VkApplicationInfo.pEngineName$set(app, engineName);
        VkApplicationInfo.apiVersion$set(app, VK_API_VERSION_1_1());

        var enabledExtensions = getEnabledExtensions(validationLayer);
        var instanceCreateInfo = arena.allocate(VkInstanceCreateInfo.$LAYOUT());
        VkInstanceCreateInfo.sType$set(instanceCreateInfo, VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO());
        VkInstanceCreateInfo.pApplicationInfo$set(instanceCreateInfo, app);
        VkInstanceCreateInfo.enabledExtensionCount$set(instanceCreateInfo, enabledExtensions.size());
        VkInstanceCreateInfo.ppEnabledExtensionNames$set(instanceCreateInfo, enabledExtensions.data());
        if (validationLayer) {
            var validationLayerName = arena.allocateUtf8String("VK_LAYER_KHRONOS_validation");
            var ppValidationLayer = arena.allocate(MemoryLayout.sequenceLayout(1, C_POINTER));
            ppValidationLayer.set(C_POINTER, 0, MemorySegment.ofAddress(validationLayerName.address()));
            VkInstanceCreateInfo.enabledLayerCount$set(instanceCreateInfo, 1);
            VkInstanceCreateInfo.ppEnabledLayerNames$set(instanceCreateInfo, ppValidationLayer);

            var debugCreateInfo = arena.allocate(VkDebugUtilsMessengerCreateInfoEXT.$LAYOUT());
            populateDebugMessenger(debugCreateInfo);
            VkInstanceCreateInfo.pNext$set(instanceCreateInfo, debugCreateInfo);
        } else {
            VkInstanceCreateInfo.enabledLayerCount$set(instanceCreateInfo, 0);
            VkInstanceCreateInfo.pNext$set(instanceCreateInfo, MemorySegment.NULL);
        }

        var pInstance = arena.allocate(VkInstance);
        if (krc_vkCreateInstance(instanceCreateInfo, MemorySegment.NULL, pInstance) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create instance");
        }
        var instance = pInstance.get(VkInstance, 0);
        logger.debug("Created vulkan instance {}", printAddress(instance));
        return instance;
    }

    private NativeArray getEnabledExtensions(boolean debug) {
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

    private MemorySegment initDebugMessenger() {
        var debugCreateInfo = arena.allocate(VkDebugUtilsMessengerCreateInfoEXT.$LAYOUT());
        populateDebugMessenger(debugCreateInfo);
        var pDebugMessenger = arena.allocate(VkDebugUtilsMessengerEXT);
        var functionName = arena.allocateUtf8String("vkCreateDebugUtilsMessengerEXT");
        var function = PFN_vkCreateDebugUtilsMessengerEXT.ofAddress(
                krc_vkGetInstanceProcAddr(instance, functionName), arena);
        if (function.apply(instance, debugCreateInfo, MemorySegment.NULL, pDebugMessenger) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create debug messenger");
        }
        var debugMessenger = pDebugMessenger.get(VkDebugUtilsMessengerEXT, 0);
        logger.debug("Created debug messenger {}", printAddress(debugMessenger));
        return debugMessenger;
    }

    private void populateDebugMessenger(MemorySegment debugCreateInfo) {
        var debugCallback = PFN_vkDebugUtilsMessengerCallbackEXT.allocate((messageSeverity, messageTypes, pCallbackData, pUserData) -> {
            var message = VkDebugUtilsMessengerCallbackDataEXT.pMessage$get(pCallbackData).getUtf8String(0);
            if (Utils.checkFlags(messageSeverity, VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT())) {
                validationLayerLogger.error(message);
            } else if (Utils.checkFlags(messageSeverity, VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT())) {
                validationLayerLogger.warn(message);
            }

            return VK_FALSE();
        }, arena);

        VkDebugUtilsMessengerCreateInfoEXT.sType$set(debugCreateInfo, VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT());
        VkDebugUtilsMessengerCreateInfoEXT.messageSeverity$set(debugCreateInfo, VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT()
                | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT());
        VkDebugUtilsMessengerCreateInfoEXT.messageType$set(debugCreateInfo, VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT()
                | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT()
                | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT());
        VkDebugUtilsMessengerCreateInfoEXT.pfnUserCallback$set(debugCreateInfo, debugCallback);
        VkDebugUtilsMessengerCreateInfoEXT.pUserData$set(debugCreateInfo, MemorySegment.NULL);
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
        logger.debug("selected physical device {}", printAddress(suitablePhysicalDevice));
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

    protected int findSupportedFormat(Set<Integer> candidates, int tiling, int flags) {

    }

    protected MemorySegment getInstance() {
        return instance;
    }

    protected MemorySegment getPhysicalDevice() {
        return physicalDevice;
    }

    @Override
    public void close() throws Exception {
        if (debugMessenger != null) {
            var functionName = arena.allocateUtf8String("vkDestroyDebugUtilsMessengerEXT");
            var function = PFN_vkDestroyDebugUtilsMessengerEXT.ofAddress(
                    krc_vkGetInstanceProcAddr(instance, functionName), arena);
            function.apply(instance, debugMessenger, MemorySegment.NULL);
        }
        krc_vkDestroyInstance(instance, MemorySegment.NULL);
        arena.close();
    }
}
