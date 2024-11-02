package ch.szclsb.kerinci.internal.semaphore;

import ch.szclsb.kerinci.api.VkSemaphoreCreateInfo;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.KrcArray;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateSemaphore;

public class KrcSemaphoreFactory {
    private KrcSemaphoreFactory() {
    }

    private static MemorySegment allocateCreateInfo(Arena arena) {
        var createInfoSegment = arena.allocate(VkSemaphoreCreateInfo.$LAYOUT());
        VkSemaphoreCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO());
        return createInfoSegment;
    }

    private static KrcSemaphore allocate(KrcDevice device, MemorySegment createInfoSegment, MemorySegment handle) {
        if (krc_vkCreateSemaphore(device.getLogical(), createInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create fence");
        }
        return new KrcSemaphore(device, handle.get(VkSemaphore, 0));
    }

    public static KrcSemaphore createSemaphore(KrcDevice device, KrcSemaphore.CreateInfo semaphoreCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            VkSemaphoreCreateInfo.flags$set(createInfoSegment, semaphoreCreateInfo.flags());
            var handle = arena.allocate(VkSemaphore);
            return allocate(device, createInfoSegment, handle);
        }
    }

    public static KrcArray<KrcSemaphore, KrcDevice> createSemaphores(KrcDevice device, int count, KrcSemaphore.CreateInfo semaphoreCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            VkSemaphoreCreateInfo.flags$set(createInfoSegment, semaphoreCreateInfo.flags());
            return new KrcArray<>(count, VkSemaphore, (handle, i) ->
                    allocate(device, createInfoSegment, handle), device);
        }
    }

    public static KrcArray<KrcSemaphore, KrcDevice> createSemaphores(KrcDevice device, List<KrcSemaphore.CreateInfo> semaphoreCreateInfos) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            return new KrcArray<>(semaphoreCreateInfos.size(), VkSemaphore, (handle, i) -> {
                VkSemaphoreCreateInfo.flags$set(createInfoSegment, semaphoreCreateInfos.get(i).flags());
                return allocate(device, createInfoSegment, handle);
            }, device);
        }
    }
}
