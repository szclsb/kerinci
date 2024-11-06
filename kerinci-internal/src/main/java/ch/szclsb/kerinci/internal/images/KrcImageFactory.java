package ch.szclsb.kerinci.internal.images;

import ch.szclsb.kerinci.internal.Allocator;
import ch.szclsb.kerinci.internal.KrcArray;
import ch.szclsb.kerinci.internal.swapchain.KrcSwapchain;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.VkImage;
import static ch.szclsb.kerinci.api.api_h_1.uint32_t;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkGetSwapchainImagesKHR;

public class KrcImageFactory {
    private KrcImageFactory() {}

    public static KrcArray<KrcImage> getSwapChainImages(Allocator allocator, KrcSwapchain swapchain) {
        var pCount = allocator.apply(uint32_t);
        krc_vkGetSwapchainImagesKHR(swapchain.getDevice().getLogical(), swapchain.getVkHandle(), pCount, MemorySegment.NULL);
        var count = pCount.get(uint32_t, 0);
        return new KrcArray<>(count, VkImage, allocator,
                segment -> krc_vkGetSwapchainImagesKHR(swapchain.getDevice().getLogical(), swapchain.getVkHandle(), pCount, segment),
                (slice, _) -> new KrcImage(swapchain.getDevice(), slice.get(VkImage, 0)));
    }
}
