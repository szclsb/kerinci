package ch.szclsb.kerinci.internal.commands;

import ch.szclsb.kerinci.api.VkCommandPoolCreateInfo;
import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateCommandPool;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyCommandPool;
import static ch.szclsb.kerinci.internal.Utils.or;

public class KrcCommandPool extends AbstractKrcHandle2 {
    private KrcCommandPool(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public enum CreateFlag implements Flag {
        CREATE_TRANSIENT_BIT(1),
        CREATE_RESET_COMMAND_BUFFER_BIT(2);

        private final int value;
        CreateFlag(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public class CreateInfo extends AbstractCreateInfo<KrcCommandPool> {
        private final QueueFamilyIndices indices;
        private final int flags;

        public CreateInfo(QueueFamilyIndices indices, CreateFlag ...flags) {
            super(KrcCommandPool.class, KrcCommandPool::new);
            this.indices = indices;
            this.flags = or(flags);
        }

        @Override
        protected AddressLayout layout() {
            return VkCommandPool;
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var createInfoSegment = allocator.apply(VkCommandPoolCreateInfo.$LAYOUT());
            VkCommandPoolCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO());
            return createInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo) {
            VkCommandPoolCreateInfo.queueFamilyIndex$set(pCreateInfo, indices.getGraphicsFamily());
            VkCommandPoolCreateInfo.flags$set(pCreateInfo, flags);
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkCreateCommandPool(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) != VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkDestroyCommandPool(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }
}
