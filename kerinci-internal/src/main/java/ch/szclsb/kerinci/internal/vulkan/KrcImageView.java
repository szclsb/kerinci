package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkImageViewCreateInfo;
import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateImageView;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyImageView;
import static ch.szclsb.kerinci.internal.vulkan.KrcFactory.setComponentMapping;
import static ch.szclsb.kerinci.internal.vulkan.KrcFactory.setSubresourceRange;

public class KrcImageView extends AbstractKrcHandle {
    private KrcImageView(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public static class CreateInfo extends AbstractCreateInfo<KrcImageView> {
        private final int flags;
        private final KrcImage image;
        private final KrcImageType viewType;
        private final KrcFormat format;
        private final KrcComponentMapping componentMapping;
        private final KrcImageSubresourceRange subresourceRange;

        public CreateInfo(int flags,
                          KrcImage image,
                          KrcImageType viewType,
                          KrcFormat format,
                          KrcComponentMapping componentMapping,
                          KrcImageSubresourceRange subresourceRange) {
            super(KrcImageView.class, KrcImageView::new, VkImageView);
            this.flags = flags;
            this.image = image;
            this.viewType = viewType;
            this.format = format;
            this.componentMapping = componentMapping;
            this.subresourceRange = subresourceRange;
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var createInfoSegment = allocator.apply(VkImageViewCreateInfo.$LAYOUT());
            VkImageViewCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO());
            return createInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional) {
            VkImageViewCreateInfo.flags$set(pCreateInfo, flags);
            VkImageViewCreateInfo.image$set(pCreateInfo, image.getVkHandle());
            VkImageViewCreateInfo.viewType$set(pCreateInfo, viewType.getValue());
            VkImageViewCreateInfo.format$set(pCreateInfo, format.getValue());
            if (componentMapping != null) {
                setComponentMapping(VkImageViewCreateInfo.components$slice(pCreateInfo), componentMapping);
            }
            if (subresourceRange != null) {
                setSubresourceRange(VkImageViewCreateInfo.subresourceRange$slice(pCreateInfo), subresourceRange);
            }
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkCreateImageView(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) == VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkDestroyImageView(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }
}
