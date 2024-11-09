package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkFenceCreateInfo;
import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;
import static ch.szclsb.kerinci.internal.Utils.or;

public class KrcFence extends AbstractKrcHandle {
    private KrcFence(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public enum CreateFlag implements Flag {
        CREATE_SIGNALED_BIT(1),
        CREATE_FLAG_BITS_MAX_ENUM(2147483647);

        private final int value;

        CreateFlag(final int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public static class CreateInfo extends AbstractCreateInfo<KrcFence> {
        private final int flags;

        public CreateInfo(CreateFlag ...flags) {
            super(KrcFence.class, KrcFence::new, VkFence);
            this.flags = or(flags);
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var createInfoSegment = allocator.apply(VkFenceCreateInfo.$LAYOUT());
            VkFenceCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_FENCE_CREATE_INFO());
            return createInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional) {
            VkFenceCreateInfo.flags$set(pCreateInfo, flags);
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkCreateFence(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) == VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkDestroyFence(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }

    public static void reset(KrcArrayExtended<KrcFence, KrcDevice> fences) {
        krc_vkResetFences(fences.getExtension().getLogical(), fences.length(), fences.getpArray());
    }

    public void reset() {
        krc_vkResetFences(getDevice().getLogical(), 1, getVkHandle());
    }

    public static void waitFor(KrcArrayExtended<KrcFence, KrcDevice> fences, boolean waitAll, long timeout) {
        krc_vkWaitForFences(fences.getExtension().getLogical(), fences.length(), fences.getpArray(),
                waitAll ? VK_TRUE() : VK_FALSE(), timeout);
    }

    public void waitFor(long timeout) {
        krc_vkWaitForFences(getDevice().getLogical(), 1, getVkHandle(), VK_TRUE(), timeout);
    }
}
