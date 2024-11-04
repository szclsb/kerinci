package ch.szclsb.kerinci.internal.framebuffer;

import ch.szclsb.kerinci.api.VkFramebufferCreateInfo;
import ch.szclsb.kerinci.internal.KrcDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.function.Function;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateFramebuffer;
import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcFramebufferFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcFramebufferFactory.class);

    private static MemorySegment allocateCreateInfo(Function<MemoryLayout, MemorySegment> allocate) {
        var createInfoSegment = allocate.apply(VkFramebufferCreateInfo.$LAYOUT());
        VkFramebufferCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO());
        return createInfoSegment;
    }

    private static void setFramebufferCreateInfo(MemorySegment segment, KrcFramebuffer.CreateInfo framebufferCreateInfo) {
        VkFramebufferCreateInfo.flags$set(segment, framebufferCreateInfo.flags());
        VkFramebufferCreateInfo.renderPass$set(segment, framebufferCreateInfo.renderPass().getVkHandle());
        VkFramebufferCreateInfo.attachmentCount$set(segment, framebufferCreateInfo.attachments().length());
        VkFramebufferCreateInfo.pAttachments$set(segment, framebufferCreateInfo.attachments().getPointer());
        VkFramebufferCreateInfo.width$set(segment, framebufferCreateInfo.width());
        VkFramebufferCreateInfo.height$set(segment, framebufferCreateInfo.height());
        VkFramebufferCreateInfo.layers$set(segment, framebufferCreateInfo.layers());
    }

    private static KrcFramebuffer create(KrcDevice device, MemorySegment createInfoSegment, MemorySegment handle) {
        if (krc_vkCreateFramebuffer(device.getLogical(), createInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create framebuffer");
        }
        var framebuffer = handle.get(VkFramebuffer, 0);
        logger.debug("Created framebuffer {}", printAddress(framebuffer));
        return new KrcFramebuffer(device, framebuffer);
    }
}
