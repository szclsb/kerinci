package ch.szclsb.kerinci.internal.swapchain;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.extent.KrcExtent2D;
import ch.szclsb.kerinci.internal.Window;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;

public class KrcSwapchain extends AbstractKrcHandle {
    public record CreateInfo(
            int flags,
            Window window,
            int minImageCount,
            int imageFormat,
            int imageColorSpace,
            KrcExtent2D imageExtent,
            int imageArrayLayers,
            int imageUsage,
            int imageSharingMode,
            Integer[] pQueueFamilyIndices,
            int preTransform,
            int compositeAlpha,
            int presentMode,
            boolean clipped,
            KrcSwapchain oldSwapchain
    ) {
    }

    public KrcSwapchain(KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroySwapchainKHR(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
