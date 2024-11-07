package ch.szclsb.kerinci.internal.images;

import ch.szclsb.kerinci.api.VkImageCreateInfo;
import ch.szclsb.kerinci.internal.Allocator;
import ch.szclsb.kerinci.internal.KrcArray;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.extent.KrcExtentFactory;
import ch.szclsb.kerinci.internal.swapchain.KrcSwapchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.*;
import static ch.szclsb.kerinci.internal.Utils.*;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class KrcImageFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcImageFactory.class);

    private KrcImageFactory() {}

    private static MemorySegment allocateCreateInfo(Allocator allocator) {
        var createInfoSegment = allocator.apply(VkImageCreateInfo.$LAYOUT());
        VkImageCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO());
        return createInfoSegment;
    }

    private static void setImageCreateInfo(Allocator allocator, MemorySegment segment, KrcImage.CreateInfo imageCreateInfo) {
        VkImageCreateInfo.flags$set(segment, imageCreateInfo.flags());
        VkImageCreateInfo.imageType$set(segment, imageCreateInfo.type().getValue());
        VkImageCreateInfo.format$set(segment, imageCreateInfo.format().getValue());
        KrcExtentFactory.write3D(VkImageCreateInfo.extent$slice(segment), imageCreateInfo.extent());
        VkImageCreateInfo.mipLevels$set(segment, imageCreateInfo.mipLevels());
        VkImageCreateInfo.arrayLayers$set(segment, imageCreateInfo.arrayLayers());
        VkImageCreateInfo.samples$set(segment, or(imageCreateInfo.samples()));
        VkImageCreateInfo.tiling$set(segment, imageCreateInfo.tiling().getValue());
        VkImageCreateInfo.usage$set(segment, or(imageCreateInfo.usage()));
        VkImageCreateInfo.sharingMode$set(segment, imageCreateInfo.sharingMode().getValue());
        writeArrayPointer(imageCreateInfo.queueFamilyIndex(),
                JAVA_INT, segment, allocator,
                (slice, i) -> slice.set(JAVA_INT, 0, i),
                VkImageCreateInfo::queueFamilyIndexCount$set,
                VkImageCreateInfo::pQueueFamilyIndices$set);
        VkImageCreateInfo.initialLayout$set(segment, imageCreateInfo.initialLayout().getValue());
    }

    private static KrcImage create(KrcDevice device, MemorySegment createInfoSegment, MemorySegment handle) {
        if (krc_vkCreateImage(device.getLogical(), createInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create image");
        }
        var image = handle.get(VkImage, 0);
        logger.debug("Created image {}", printAddress(image));
        return new KrcImage(device, image);
    }

    public static KrcImage createImage(KrcDevice device, KrcImage.CreateInfo imageViewCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena::allocate);
            setImageCreateInfo(arena::allocate, createInfoSegment, imageViewCreateInfo);
            var handle = arena.allocate(VkImage);
            return create(device, createInfoSegment, handle);
        }
    }

    public static KrcArray<KrcImage> createImageArray(Allocator arrayAllocator, KrcDevice device, int count, KrcImage.CreateInfo imageViewCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena::allocate);
            setImageCreateInfo(arena::allocate, createInfoSegment, imageViewCreateInfo);
            return new KrcArray<>(count, VkImage, arrayAllocator, (handle, _) ->
                    create(device, createInfoSegment, handle));
        }
    }

    public static KrcArray<KrcImage> createImageArray(Allocator arrayAllocator, KrcDevice device, List<KrcImage.CreateInfo> imageViewCreateInfos) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena::allocate);
            return new KrcArray<>(imageViewCreateInfos.size(), VkImage, arrayAllocator, (handle, i) -> {
                setImageCreateInfo(arena::allocate, createInfoSegment, imageViewCreateInfos.get(i));
                return create(device, createInfoSegment, handle);
            });
        }
    }

    public static KrcArray<KrcImage> getSwapChainImages(Allocator allocator, KrcSwapchain swapchain) {
        var pCount = allocator.apply(uint32_t);
        krc_vkGetSwapchainImagesKHR(swapchain.getDevice().getLogical(), swapchain.getVkHandle(), pCount, MemorySegment.NULL);
        var count = pCount.get(uint32_t, 0);
        return new KrcArray<>(count, VkImage, allocator,
                segment -> krc_vkGetSwapchainImagesKHR(swapchain.getDevice().getLogical(), swapchain.getVkHandle(), pCount, segment),
                (slice, _) -> new KrcImage(swapchain.getDevice(), slice.get(VkImage, 0)));
    }
}
