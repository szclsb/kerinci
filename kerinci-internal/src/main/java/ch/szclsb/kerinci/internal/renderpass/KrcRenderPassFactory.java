package ch.szclsb.kerinci.internal.renderpass;

import ch.szclsb.kerinci.api.*;
import ch.szclsb.kerinci.internal.Allocator;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.attachment.KrcAttachmentDescription;
import ch.szclsb.kerinci.internal.attachment.KrcAttachmentReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateRenderPass;
import static ch.szclsb.kerinci.internal.Utils.*;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class KrcRenderPassFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcRenderPassFactory.class);

    private KrcRenderPassFactory() {
    }

    private static MemorySegment allocateCreateInfo(Allocator allocator) {
        var createInfoSegment = allocator.apply(VkRenderPassCreateInfo.$LAYOUT());
        VkRenderPassCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO());
        return createInfoSegment;
    }

    private static void setRenderPassCreateInfo(Allocator allocator,
                                                MemorySegment segment,
                                                KrcRenderPass.CreateInfo imageViewCreateInfo) {
        VkRenderPassCreateInfo.flags$set(segment, imageViewCreateInfo.flags());
        writeArrayPointer(imageViewCreateInfo.attachments(),
                VkAttachmentDescription.$LAYOUT(), segment, allocator,
                KrcRenderPassFactory::setAttachmentDescription,
                VkRenderPassCreateInfo::attachmentCount$set,
                VkRenderPassCreateInfo::pAttachments$set);
        writeArrayPointer(imageViewCreateInfo.subpasses(),
                VkSubpassDescription.$LAYOUT(), segment, allocator,
                (slice, subpassDescription) -> setSubpassDescription(allocator, slice, subpassDescription),
                VkRenderPassCreateInfo::subpassCount$set,
                VkRenderPassCreateInfo::pSubpasses$set);
        writeArrayPointer(imageViewCreateInfo.dependencies(),
                VkSubpassDependency.$LAYOUT(), segment, allocator,
                KrcRenderPassFactory::setSubpassDependency,
                VkRenderPassCreateInfo::dependencyCount$set,
                VkRenderPassCreateInfo::pDependencies$set);
    }

    private static void setAttachmentDescription(MemorySegment segment, KrcAttachmentDescription description) {
        VkAttachmentDescription.flags$set(segment, description.flags());
        VkAttachmentDescription.format$set(segment, description.format().getValue());
        VkAttachmentDescription.samples$set(segment, description.samples());
        VkAttachmentDescription.loadOp$set(segment, description.loadOp());
        VkAttachmentDescription.storeOp$set(segment, description.storeOp());
        VkAttachmentDescription.stencilLoadOp$set(segment, description.stencilLoadOp());
        VkAttachmentDescription.stencilStoreOp$set(segment, description.stencilStoreOp());
        VkAttachmentDescription.initialLayout$set(segment, description.initialLayout());
        VkAttachmentDescription.finalLayout$set(segment, description.finalLayout());
    }

    private static void setSubpassDescription(Allocator allocator, MemorySegment segment, KrcRenderPass.SubpassDescription subpassDescription) {
        VkSubpassDescription.flags$set(segment, subpassDescription.flag());
        VkSubpassDescription.pipelineBindPoint$set(segment, subpassDescription.pipelineBindPoint());
        writeArrayPointer(subpassDescription.inputAttachments(),
                VkAttachmentReference.$LAYOUT(), segment, allocator,
                KrcRenderPassFactory::setAttachmentReference,
                VkSubpassDescription::inputAttachmentCount$set,
                VkSubpassDescription::pInputAttachments$set);
        writeArrayPointer(subpassDescription.colorAttachments(),
                VkAttachmentReference.$LAYOUT(), segment, allocator,
                KrcRenderPassFactory::setAttachmentReference,
                VkSubpassDescription::colorAttachmentCount$set,
                VkSubpassDescription::pColorAttachments$set);
        // resolveAttachment? Uses colorAttachmentCount as well -> subTypes?
        writePointer(subpassDescription.depthStencilAttachment(),
                VkAttachmentReference.$LAYOUT(), segment, allocator,
                KrcRenderPassFactory::setAttachmentReference,
                VkSubpassDescription::pDepthStencilAttachment$set);
        writeArrayPointer(subpassDescription.preserveAttachments(),
                JAVA_INT, segment, allocator,
                (slice, i) -> slice.set(JAVA_INT, 0, i),
                VkSubpassDescription::preserveAttachmentCount$set,
                VkSubpassDescription::pPreserveAttachments$set);

    }

    private static void setSubpassDependency(MemorySegment segment, KrcRenderPass.SubpassDependency subpassDependency) {
        VkSubpassDependency.srcSubpass$set(segment, subpassDependency.srcSubpass());
        VkSubpassDependency.dstSubpass$set(segment, subpassDependency.dstSubpass());
        VkSubpassDependency.srcStageMask$set(segment, subpassDependency.srcStageMask());
        VkSubpassDependency.dstStageMask$set(segment, subpassDependency.dstStageMask());
        VkSubpassDependency.srcAccessMask$set(segment, subpassDependency.srcAccessMask());
        VkSubpassDependency.dstAccessMask$set(segment, subpassDependency.dstAccessMask());
        VkSubpassDependency.dependencyFlags$set(segment, subpassDependency.dependencyFlags());
    }

    private static void setAttachmentReference(MemorySegment segment, KrcAttachmentReference attachmentReference) {
        VkAttachmentReference.attachment$set(segment, attachmentReference.attachment());
        VkAttachmentReference.layout$set(segment, attachmentReference.layout());
    }

    private static KrcRenderPass create(KrcDevice device, MemorySegment createInfoSegment, MemorySegment handle) {
        if (krc_vkCreateRenderPass(device.getLogical(), createInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create render pass");
        }
        var renderPass = handle.get(VkRenderPass, 0);
        logger.debug("Created render pass {}", printAddress(renderPass));
        return new KrcRenderPass(device, renderPass);
    }

    public static KrcRenderPass createRenderPass(KrcDevice device, KrcRenderPass.CreateInfo renderPassCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena::allocate);
            setRenderPassCreateInfo(arena::allocate, createInfoSegment, renderPassCreateInfo);
            var handle = arena.allocate(VkRenderPass);
            return create(device, createInfoSegment, handle);
        }
    }
}
