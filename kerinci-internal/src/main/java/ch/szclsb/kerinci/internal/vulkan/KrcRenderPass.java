package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkAttachmentDescription;
import ch.szclsb.kerinci.api.VkRenderPassCreateInfo;
import ch.szclsb.kerinci.api.VkSubpassDependency;
import ch.szclsb.kerinci.api.VkSubpassDescription;
import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateRenderPass;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyRenderPass;
import static ch.szclsb.kerinci.internal.Utils.writeArrayPointer;

public class KrcRenderPass extends AbstractKrcHandle {
    private KrcRenderPass(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public static class CreateInfo extends AbstractCreateInfo<KrcRenderPass> {
        private final int flags;
        private final KrcAttachmentDescription[] attachments;
        private final KrcSubpassDescription[] subpasses;
        private final KrcSubpassDependency[] dependencies;

        public CreateInfo(
                int flags,
                KrcAttachmentDescription[] attachments,
                KrcSubpassDescription[] subpasses,
                KrcSubpassDependency[] dependencies) {
            super(KrcRenderPass.class, KrcRenderPass::new);
            this.flags = flags;
            this.attachments = attachments;
            this.subpasses = subpasses;
            this.dependencies = dependencies;
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var createInfoSegment = allocator.apply(VkRenderPassCreateInfo.$LAYOUT());
            VkRenderPassCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO());
            return createInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional) {
            VkRenderPassCreateInfo.flags$set(pCreateInfo, flags);
            writeArrayPointer(attachments,
                    VkAttachmentDescription.$LAYOUT(), pCreateInfo, additional,
                    (pSlice, attachment) -> attachment.write(pSlice, additional),
                    VkRenderPassCreateInfo::attachmentCount$set,
                    VkRenderPassCreateInfo::pAttachments$set);
            writeArrayPointer(subpasses,
                    VkSubpassDescription.$LAYOUT(), pCreateInfo, additional,
                    (pSlice, subpassDescription) -> subpassDescription.write(pSlice, additional),
                    VkRenderPassCreateInfo::subpassCount$set,
                    VkRenderPassCreateInfo::pSubpasses$set);
            writeArrayPointer(dependencies,
                    VkSubpassDependency.$LAYOUT(), pCreateInfo, additional,
                    (pSlice, subpassDependency) -> subpassDependency.write(pSlice, additional),
                    VkRenderPassCreateInfo::dependencyCount$set,
                    VkRenderPassCreateInfo::pDependencies$set);
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkCreateRenderPass(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) == VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkDestroyRenderPass(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }
}
