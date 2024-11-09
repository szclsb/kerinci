package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkFramebufferCreateInfo;
import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateFramebuffer;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyFramebuffer;
import static ch.szclsb.kerinci.internal.Utils.writeArrayPointer;

public class KrcFramebuffer extends AbstractKrcHandle {
    private KrcFramebuffer(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public static class CreateInfo extends AbstractCreateInfo<KrcFramebuffer> {
        private final int flags;
        private final KrcRenderPass renderPass;
        private final KrcArray<KrcImageView> attachments;
        private final int width;
        private final int height;
        private final int layers;

        public CreateInfo(
                int flags,
                KrcRenderPass renderPass,
                KrcArray<KrcImageView> attachments,
                int width,
                int height,
                int layers) {
            super(KrcFramebuffer.class, KrcFramebuffer::new, VkFramebuffer);
            this.flags = flags;
            this.renderPass = renderPass;
            this.attachments = attachments;
            this.width = width;
            this.height = height;
            this.layers = layers;
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var createInfoSegment = allocator.apply(VkFramebufferCreateInfo.$LAYOUT());
            VkFramebufferCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO());
            return createInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional) {
            VkFramebufferCreateInfo.flags$set(pCreateInfo, flags);
            VkFramebufferCreateInfo.renderPass$set(pCreateInfo, renderPass.getVkHandle());
            writeArrayPointer(attachments, pCreateInfo,
                    VkFramebufferCreateInfo::attachmentCount$set,
                    VkFramebufferCreateInfo::pAttachments$set);
            VkFramebufferCreateInfo.width$set(pCreateInfo, width);
            VkFramebufferCreateInfo.height$set(pCreateInfo, height);
            VkFramebufferCreateInfo.layers$set(pCreateInfo, layers);
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkCreateFramebuffer(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) == VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkDestroyFramebuffer(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }
}
