package ch.szclsb.kerinci.internal.images;

import ch.szclsb.kerinci.api.*;
import ch.szclsb.kerinci.internal.Allocator;
import ch.szclsb.kerinci.internal.KrcArray;
import ch.szclsb.kerinci.internal.KrcDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateImageView;
import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcImageViewFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcImageViewFactory.class);

    private KrcImageViewFactory() {
    }

    private static MemorySegment allocateCreateInfo(Arena arena) {
        var createInfoSegment = arena.allocate(VkImageViewCreateInfo.$LAYOUT());
        VkImageViewCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO());
        return createInfoSegment;
    }

    private static void setImageViewCreateInfo(MemorySegment segment, KrcImageView.CreateInfo imageViewCreateInfo) {
        VkImageViewCreateInfo.flags$set(segment, imageViewCreateInfo.flags());
        VkImageViewCreateInfo.image$set(segment, imageViewCreateInfo.image().getVkHandle());
        VkImageViewCreateInfo.viewType$set(segment, imageViewCreateInfo.viewType().getValue());
        VkImageViewCreateInfo.format$set(segment, imageViewCreateInfo.format());
        if (imageViewCreateInfo.componentMapping() != null) {
            setComponentMapping(VkImageViewCreateInfo.components$slice(segment), imageViewCreateInfo.componentMapping());
        }
        if (imageViewCreateInfo.subresourceRange() != null) {
            setSubresourceRange(VkImageViewCreateInfo.subresourceRange$slice(segment), imageViewCreateInfo.subresourceRange());
        }
    }

    private static void setComponentMapping(MemorySegment segment, KrcComponentMapping componentMapping) {
        VkComponentMapping.r$set(segment, componentMapping.r());
        VkComponentMapping.g$set(segment, componentMapping.g());
        VkComponentMapping.b$set(segment, componentMapping.b());
        VkComponentMapping.a$set(segment, componentMapping.a());
    }

    private static void setSubresourceRange(MemorySegment segment, KrcImageSubresourceRange imageSubresourceRange) {
        VkImageSubresourceRange.aspectMask$set(segment, imageSubresourceRange.aspectMask());
        VkImageSubresourceRange.baseMipLevel$set(segment, imageSubresourceRange.baseMipLevel());
        VkImageSubresourceRange.levelCount$set(segment, imageSubresourceRange.levelCount());
        VkImageSubresourceRange.baseArrayLayer$set(segment, imageSubresourceRange.baseArrayLayer());
        VkImageSubresourceRange.layerCount$set(segment, imageSubresourceRange.layerCount());
    }

    private static KrcImageView create(KrcDevice device, MemorySegment createInfoSegment, MemorySegment handle) {
        if (krc_vkCreateImageView(device.getLogical(), createInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create image view");
        }
        var imageView = handle.get(VkImageView, 0);
        logger.debug("Created image view {}", printAddress(imageView));
        return new KrcImageView(device, imageView);
    }

    public static KrcImageView createImageView(KrcDevice device, KrcImageView.CreateInfo imageViewCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            setImageViewCreateInfo(createInfoSegment, imageViewCreateInfo);
            var handle = arena.allocate(VkImageView);
            return create(device, createInfoSegment, handle);
        }
    }

    public static KrcArray<KrcImageView> createImageViews(Allocator arrayAllocator, KrcDevice device, int count, KrcImageView.CreateInfo imageViewCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            setImageViewCreateInfo(createInfoSegment, imageViewCreateInfo);
            return new KrcArray<>(count, VkImageView, arrayAllocator, (handle, i) ->
                    create(device, createInfoSegment, handle));
        }
    }

    public static KrcArray<KrcImageView> createImageViews(Allocator arrayAllocator, KrcDevice device, List<KrcImageView.CreateInfo> imageViewCreateInfos) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            return new KrcArray<>(imageViewCreateInfos.size(), VkImageView, arrayAllocator, (handle, i) -> {
                setImageViewCreateInfo(createInfoSegment, imageViewCreateInfos.get(i));
                return create(device, createInfoSegment, handle);
            });
        }
    }
}
