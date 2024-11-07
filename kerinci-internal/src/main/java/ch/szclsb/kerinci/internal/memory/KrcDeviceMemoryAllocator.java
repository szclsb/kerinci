package ch.szclsb.kerinci.internal.memory;

import ch.szclsb.kerinci.api.VkMemoryAllocateInfo;
import ch.szclsb.kerinci.internal.Allocator;
import ch.szclsb.kerinci.internal.KrcArray;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.imageview.KrcImageViewFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkAllocateMemory;
import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcDeviceMemoryAllocator {
    private static final Logger logger = LoggerFactory.getLogger(KrcDeviceMemoryAllocator.class);

    private KrcDeviceMemoryAllocator() {
    }

    private static MemorySegment allocateCreateInfo(Allocator allocator) {
        var allocateInfoSegment = allocator.apply(VkMemoryAllocateInfo.$LAYOUT());
        VkMemoryAllocateInfo.sType$set(allocateInfoSegment, VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO());
        return allocateInfoSegment;
    }

    private static void setAllocateInfo(MemorySegment segment, KrcDeviceMemory.AllocateInfo allocateInfo) {
        VkMemoryAllocateInfo.allocationSize$set(segment, allocateInfo.allocationSize());
        VkMemoryAllocateInfo.memoryTypeIndex$set(segment, allocateInfo.memoryTypeIndex());
    }

    private static KrcDeviceMemory allocateDeviceMemory(KrcDevice device, MemorySegment allocateInfoSegment, MemorySegment handle) {
        if (krc_vkAllocateMemory(device.getLogical(), allocateInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to allocate image memory!");
        }
        var memory = handle.get(VkDeviceMemory, 0);
        logger.debug("allocated device memory {}",  printAddress(memory));
        return new KrcDeviceMemory(device, memory);
    }

    public static KrcDeviceMemory allocate(KrcDevice device, KrcDeviceMemory.AllocateInfo allocateInfo) {
        try (var arena = Arena.ofConfined()) {
            var allocateInfoSegment = allocateCreateInfo(arena::allocate);
            setAllocateInfo(allocateInfoSegment, allocateInfo);
            var handle = arena.allocate(VkDeviceMemory);
            return allocateDeviceMemory(device, allocateInfoSegment, handle);
        }
    }

    public static KrcArray<KrcDeviceMemory> allocateArray(Allocator arrayAllocator, KrcDevice device, int count, KrcDeviceMemory.AllocateInfo allocateInfo) {
        try (var arena = Arena.ofConfined()) {
            var allocateInfoSegment = allocateCreateInfo(arena::allocate);
            setAllocateInfo(allocateInfoSegment, allocateInfo);
            return new KrcArray<>(count, VkDeviceMemory, arrayAllocator, (handle, _) ->
                    allocateDeviceMemory(device, allocateInfoSegment, handle));
        }
    }

    public static KrcArray<KrcDeviceMemory> allocateArray(Allocator arrayAllocator, KrcDevice device, List<KrcDeviceMemory.AllocateInfo> allocateInfos) {
        try (var arena = Arena.ofConfined()) {
            var allocateInfoSegment = allocateCreateInfo(arena::allocate);
            return new KrcArray<>(allocateInfos.size(), VkDeviceMemory, arrayAllocator, (handle, i) -> {
                setAllocateInfo(allocateInfoSegment, allocateInfos.get(i));
                return allocateDeviceMemory(device, allocateInfoSegment, handle);
            });
        }
    }
}
