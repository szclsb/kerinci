package ch.szclsb.kerinci.internal.fence;

import ch.szclsb.kerinci.api.VkFenceCreateInfo;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.KrcArray;
import ch.szclsb.kerinci.internal.commands.KrcCommandPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateFence;
import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcFenceFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcFenceFactory.class);

    private KrcFenceFactory() {
    }

    private static MemorySegment allocateCreateInfo(Arena arena) {
        var createInfoSegment = arena.allocate(VkFenceCreateInfo.$LAYOUT());
        VkFenceCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_FENCE_CREATE_INFO());
        return createInfoSegment;
    }

    private static KrcFence allocate(KrcDevice device, MemorySegment createInfoSegment, MemorySegment handle) {
        if (krc_vkCreateFence(device.getLogical(), createInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create fence");
        }
        var fence = handle.get(VkFence, 0);
        logger.debug("Created fence {}", printAddress(fence));
        return new KrcFence(device, fence);
    }

    public static KrcFence createFence(KrcDevice device, KrcFence.CreateInfo fenceCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            VkFenceCreateInfo.flags$set(createInfoSegment, fenceCreateInfo.flags());
            var handle = arena.allocate(VkFence);
            return allocate(device, createInfoSegment, handle);
        }
    }

    public static KrcArray<KrcFence, KrcDevice> createFences(KrcDevice device, int count, KrcFence.CreateInfo fenceCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            VkFenceCreateInfo.flags$set(createInfoSegment, fenceCreateInfo.flags());
            return new KrcArray<>(count, VkFence, (handle, i) ->
                    allocate(device, createInfoSegment, handle), device);
        }
    }

    public static KrcArray<KrcFence, KrcDevice> createFences(KrcDevice device, List<KrcFence.CreateInfo> fenceCreateInfos) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = arena.allocate(VkFenceCreateInfo.$LAYOUT());
            return new KrcArray<>(fenceCreateInfos.size(), VkFence, (handle, i) -> {
                VkFenceCreateInfo.flags$set(createInfoSegment, fenceCreateInfos.get(i).flags());
                return allocate(device, createInfoSegment, handle);
            }, device);
        }
    }
}
