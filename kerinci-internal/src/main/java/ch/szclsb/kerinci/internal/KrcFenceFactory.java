package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkFenceCreateInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateFence;

public class KrcFenceFactory {
    private KrcFenceFactory() {
    }

    public static KrcFence createFence(Device device, KrcFenceCreateInfo fenceCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = arena.allocate(VkFenceCreateInfo.$LAYOUT());
            var handle = arena.allocate(VkFence);
            return allocate(device, createInfoSegment, fenceCreateInfo, handle);
        }
    }

    public static KrcArray<KrcFence, Device> createFences(Device device, int count, KrcFenceCreateInfo fenceCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = arena.allocate(VkFenceCreateInfo.$LAYOUT());
            return new KrcArray<>(count, VkFence, (handle, i) ->
                    allocate(device, createInfoSegment, fenceCreateInfo, handle), device);
        }
    }

    public static KrcArray<KrcFence, Device> createFences(Device device, List<KrcFenceCreateInfo> fenceCreateInfos) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = arena.allocate(VkFenceCreateInfo.$LAYOUT());
            return new KrcArray<>(fenceCreateInfos.size(), VkFence, (handle, i) ->
                    allocate(device, createInfoSegment, fenceCreateInfos.get(i), handle), device);
        }
    }

    private static KrcFence allocate(Device device, MemorySegment createInfoSegment, KrcFenceCreateInfo fenceCreateInfos, MemorySegment handle) {
        VkFenceCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_FENCE_CREATE_INFO());
        VkFenceCreateInfo.flags$set(createInfoSegment, fenceCreateInfos.flags());
        if (krc_vkCreateFence(device.getLogical(), createInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create fence");
        }
        return new KrcFence(device, handle.get(VkFence, 0));
    }
}
