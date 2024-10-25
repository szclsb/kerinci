package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkDeviceCreateInfo;
import ch.szclsb.kerinci.api.VkDeviceQueueCreateInfo;
import ch.szclsb.kerinci.api.VkPhysicalDeviceFeatures;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;

import static ch.szclsb.kerinci.api.api_h.C_POINTER;
import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.*;

public class Device implements AutoCloseable {
    private final Arena arena;
    private final VulkanApi vk;
//    private final MemorySegment logical;
//    private final MemorySegment graphicQueue;
//    private final MemorySegment presentQueue;

    public Device(VulkanApi vk, QueueFamilyIndices indices) {
        this.arena = Arena.ofConfined();
        this.vk = vk;

//        this.logical = initLogical(indices, List.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME()));
//        this.graphicQueue = initDeviceQueue(indices.graphicsFamily);
//        this.presentQueue = initDeviceQueue(indices.presentFamily);
    }

//    private MemorySegment initLogical(QueueFamilyIndices indices, List<MemorySegment> extensionNames) {
//        var queueCreateInfos = arena.allocate(MemoryLayout.sequenceLayout(2, VkDeviceQueueCreateInfo.$LAYOUT()));
//        var pQueuePriority = arena.allocate(ValueLayout.JAVA_FLOAT);
//        pQueuePriority.set(ValueLayout.JAVA_FLOAT, 0, 1.0f);
//
//        VkDeviceQueueCreateInfo.sType$set(queueCreateInfos, 0, VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO());
//        VkDeviceQueueCreateInfo.queueFamilyIndex$set(queueCreateInfos, 0, indices.graphicsFamily);
//        VkDeviceQueueCreateInfo.queueCount$set(queueCreateInfos, 0, 1);
//        VkDeviceQueueCreateInfo.pQueuePriorities$set(queueCreateInfos, 0, pQueuePriority);
//
//        VkDeviceQueueCreateInfo.sType$set(queueCreateInfos, 1, VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO());
//        VkDeviceQueueCreateInfo.queueFamilyIndex$set(queueCreateInfos, 1, indices.presentFamily);
//        VkDeviceQueueCreateInfo.queueCount$set(queueCreateInfos, 1, 1);
//        VkDeviceQueueCreateInfo.pQueuePriorities$set(queueCreateInfos, 1, pQueuePriority);
//
//        var physicalDeviceFeatures = arena.allocate(VkPhysicalDeviceFeatures.$LAYOUT());
//        VkPhysicalDeviceFeatures.samplerAnisotropy$set(physicalDeviceFeatures, VK_TRUE());
//
//        var ppEnabledExtensionNames = arena.allocate(MemoryLayout.sequenceLayout(extensionNames.size(), C_POINTER));
//        for (var i = 0; i < extensionNames.size(); ++i) {
//            ppEnabledExtensionNames.set(C_POINTER, i * C_POINTER.byteSize(), MemorySegment.ofAddress(extensionNames.get(i).address()));
//        }
//
//        var deviceCreateInfo = arena.allocate(VkDeviceCreateInfo.$LAYOUT());
//        VkDeviceCreateInfo.sType$set(deviceCreateInfo, VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO());
//        VkDeviceCreateInfo.queueCreateInfoCount$set(deviceCreateInfo, 2);
//        VkDeviceCreateInfo.pQueueCreateInfos$set(deviceCreateInfo, queueCreateInfos);
//        VkDeviceCreateInfo.pEnabledFeatures$set(deviceCreateInfo, physicalDeviceFeatures);
//        VkDeviceCreateInfo.enabledExtensionCount$set(deviceCreateInfo, extensionNames.size());
//        VkDeviceCreateInfo.ppEnabledExtensionNames$set(deviceCreateInfo, ppEnabledExtensionNames);
//
//        VkDeviceCreateInfo.enabledLayerCount$set(deviceCreateInfo, 0);
//
//        var pLogicalDevice = arena.allocate(VkDevice);
//        if (krc_vkCreateDevice(vk.getPhysicalDevice(), deviceCreateInfo, MemorySegment.NULL, pLogicalDevice) != VK_SUCCESS()) {
//            throw new RuntimeException("Failed to create logical device");
//        }
//        return pLogicalDevice.get(VkDevice, 0);
//    }

//    private MemorySegment initDeviceQueue(int queueFamilyIndex) {
//        var pPresentQueue = arena.allocate(VkQueue);
//        krc_vkGetDeviceQueue(logical, queueFamilyIndex, 0, pPresentQueue);
//        return pPresentQueue.get(VkQueue, 0);
//    }

//    protected MemorySegment getLogical() {
//        return logical;
//    }
//
//    protected MemorySegment getGraphicQueue() {
//        return graphicQueue;
//    }
//
//    protected MemorySegment getPresentQueue() {
//        return presentQueue;
//    }

    @Override
    public void close() throws Exception {
//        krc_vkDestroyDevice(logical, MemorySegment.NULL);
        arena.close();
    }
}
