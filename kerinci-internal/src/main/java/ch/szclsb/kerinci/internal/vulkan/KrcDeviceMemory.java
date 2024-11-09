package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkMemoryAllocateInfo;
import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkAllocateMemory;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkFreeMemory;

public class KrcDeviceMemory extends AbstractKrcHandle {
    private KrcDeviceMemory(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public static class AllocateInfo extends AbstractCreateInfo<KrcDeviceMemory> {
        private final long allocationSize;
        private final int memoryTypeIndex;

        public AllocateInfo(long allocationSize, int memoryTypeIndex) {
            super(KrcDeviceMemory.class, KrcDeviceMemory::new, VkDeviceMemory);
            this.allocationSize = allocationSize;
            this.memoryTypeIndex = memoryTypeIndex;
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var allocateInfoSegment = allocator.apply(VkMemoryAllocateInfo.$LAYOUT());
            VkMemoryAllocateInfo.sType$set(allocateInfoSegment, VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO());
            return allocateInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional) {
            VkMemoryAllocateInfo.allocationSize$set(pCreateInfo, allocationSize);
            VkMemoryAllocateInfo.memoryTypeIndex$set(pCreateInfo, memoryTypeIndex);
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkAllocateMemory(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) == VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkFreeMemory(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }
}
