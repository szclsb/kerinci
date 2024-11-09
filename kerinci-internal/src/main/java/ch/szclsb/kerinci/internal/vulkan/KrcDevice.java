package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkDeviceCreateInfo;
import ch.szclsb.kerinci.api.VkDeviceQueueCreateInfo;
import ch.szclsb.kerinci.api.VkPhysicalDeviceFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;
import java.util.stream.Collectors;

import static ch.szclsb.kerinci.api.api_h.C_POINTER;
import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.*;
import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcDevice implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(KrcDevice.class);

    private final Arena arena;
    private final VulkanApi vk;
    private final MemorySegment logical;
    private final MemorySegment graphicQueue;
    private final MemorySegment presentQueue;

    public KrcDevice(VulkanApi vk, QueueFamilyIndices indices) {
        this.arena = Arena.ofConfined();
        this.vk = vk;

        this.logical = initLogical(indices, List.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME()));
        this.graphicQueue = initDeviceQueue(indices.graphicsFamily);
        this.presentQueue = initDeviceQueue(indices.presentFamily);
    }

    private MemorySegment initLogical(QueueFamilyIndices indices, List<MemorySegment> extensionNames) {
        var pQueuePriority = arena.allocate(ValueLayout.JAVA_FLOAT);
        pQueuePriority.set(ValueLayout.JAVA_FLOAT, 0, 1.0f);

        var uniqueQueueFamilies = indices.getUniqueFamilies();
        logger.debug("uniqueQueueFamilies: {}", uniqueQueueFamilies.stream()
                .map(i -> Integer.toString(i))
                .collect(Collectors.joining(",")));
        var queueCreateInfos = arena.allocate(MemoryLayout.sequenceLayout(uniqueQueueFamilies.size(), VkDeviceQueueCreateInfo.$LAYOUT()));

        var i = 0;
        for (var queueFamily : uniqueQueueFamilies) {
            VkDeviceQueueCreateInfo.sType$set(queueCreateInfos, i, VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO());
            VkDeviceQueueCreateInfo.queueFamilyIndex$set(queueCreateInfos, i, queueFamily);
            VkDeviceQueueCreateInfo.queueCount$set(queueCreateInfos, i, 1);
            VkDeviceQueueCreateInfo.pQueuePriorities$set(queueCreateInfos, i, pQueuePriority);
            i += 1;
        }

        var physicalDeviceFeatures = arena.allocate(VkPhysicalDeviceFeatures.$LAYOUT());
        VkPhysicalDeviceFeatures.samplerAnisotropy$set(physicalDeviceFeatures, VK_TRUE());

        logger.info("enabled device extensions:");
        var ppEnabledExtensionNames = arena.allocate(MemoryLayout.sequenceLayout(extensionNames.size(), C_POINTER));
        for (var j = 0; j < extensionNames.size(); ++j) {
            logger.info("  {}", extensionNames.get(j).getUtf8String(0));
            ppEnabledExtensionNames.set(C_POINTER, j * C_POINTER.byteSize(), MemorySegment.ofAddress(extensionNames.get(j).address()));
        }

        var deviceCreateInfo = arena.allocate(VkDeviceCreateInfo.$LAYOUT());
        VkDeviceCreateInfo.sType$set(deviceCreateInfo, VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO());
        VkDeviceCreateInfo.queueCreateInfoCount$set(deviceCreateInfo, uniqueQueueFamilies.size());
        VkDeviceCreateInfo.pQueueCreateInfos$set(deviceCreateInfo, queueCreateInfos);
        VkDeviceCreateInfo.pEnabledFeatures$set(deviceCreateInfo, physicalDeviceFeatures);
        VkDeviceCreateInfo.enabledExtensionCount$set(deviceCreateInfo, extensionNames.size());
        VkDeviceCreateInfo.ppEnabledExtensionNames$set(deviceCreateInfo, ppEnabledExtensionNames);

        VkDeviceCreateInfo.enabledLayerCount$set(deviceCreateInfo, 0);

        var pLogicalDevice = arena.allocate(VkDevice);
        if (krc_vkCreateDevice(vk.getPhysicalDevice(), deviceCreateInfo, MemorySegment.NULL, pLogicalDevice) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create logical device");
        }
        var device = pLogicalDevice.get(VkDevice, 0);
        logger.debug("Created logical device {}", printAddress(device));
        return device;
    }

    private MemorySegment initDeviceQueue(int queueFamilyIndex) {
        var pQueue = arena.allocate(VkQueue);
        krc_vkGetDeviceQueue(logical, queueFamilyIndex, 0, pQueue);
        return pQueue.get(VkQueue, 0);
    }

    public VulkanApi getVk() {
        return vk;
    }

    public MemorySegment getLogical() {
        return logical.asReadOnly();
    }

    public MemorySegment getGraphicQueue() {
        return graphicQueue.asReadOnly();
    }

    public MemorySegment getPresentQueue() {
        return presentQueue.asReadOnly();
    }

    public void waitIdle() {
        krc_vkDeviceWaitIdle(logical);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyDevice(logical, MemorySegment.NULL);
        arena.close();
    }
}
