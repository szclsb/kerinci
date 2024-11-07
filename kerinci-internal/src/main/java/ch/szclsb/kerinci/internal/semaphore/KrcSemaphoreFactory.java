package ch.szclsb.kerinci.internal.semaphore;

import ch.szclsb.kerinci.api.VkSemaphoreCreateInfo;
import ch.szclsb.kerinci.internal.Allocator;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.KrcArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateSemaphore;
import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcSemaphoreFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcSemaphoreFactory.class);

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
        var semaphore = handle.get(VkSemaphore, 0);
        logger.debug("Created semaphore {}", printAddress(semaphore));
        return new KrcSemaphore(device, semaphore);
    }

    public static KrcSemaphore createSemaphore(KrcDevice device, KrcSemaphore.CreateInfo semaphoreCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            VkSemaphoreCreateInfo.flags$set(createInfoSegment, semaphoreCreateInfo.flags());
            var handle = arena.allocate(VkSemaphore);
            return allocate(device, createInfoSegment, handle);
        }
    }

    public static KrcArray<KrcSemaphore> createSemaphoreArray(Allocator arrayAllocator, KrcDevice device,
                                                              int count, KrcSemaphore.CreateInfo semaphoreCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            VkSemaphoreCreateInfo.flags$set(createInfoSegment, semaphoreCreateInfo.flags());
            return new KrcArray<>(count, VkSemaphore, arrayAllocator, (handle, i) ->
                    allocate(device, createInfoSegment, handle));
        }
    }

    public static KrcArray<KrcSemaphore> createSemaphoreArray(Allocator arrayAllocator, KrcDevice device,
                                                              List<KrcSemaphore.CreateInfo> semaphoreCreateInfos) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            return new KrcArray<>(semaphoreCreateInfos.size(), VkSemaphore, arrayAllocator, (handle, i) -> {
                VkSemaphoreCreateInfo.flags$set(createInfoSegment, semaphoreCreateInfos.get(i).flags());
                return allocate(device, createInfoSegment, handle);
            });
        }
    }
}
