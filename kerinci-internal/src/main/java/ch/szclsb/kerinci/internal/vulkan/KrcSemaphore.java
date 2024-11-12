package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkSemaphoreCreateInfo;
import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;

public class KrcSemaphore extends AbstractKrcHandle {
    private KrcSemaphore(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public static class CreateInfo extends AbstractCreateInfo<KrcSemaphore> {
        private final int flags;

        public CreateInfo(int flags) {
            super(KrcSemaphore.class, KrcSemaphore::new);
            this.flags = flags;
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var createInfoSegment = allocator.apply(VkSemaphoreCreateInfo.$LAYOUT());
            VkSemaphoreCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO());
            return createInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional) {
            VkSemaphoreCreateInfo.flags$set(pCreateInfo, flags);
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkCreateSemaphore(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) == VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkDestroySemaphore(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }
}
