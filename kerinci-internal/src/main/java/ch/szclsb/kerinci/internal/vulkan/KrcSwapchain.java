package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkSwapchainCreateInfoKHR;
import ch.szclsb.kerinci.internal.*;
import ch.szclsb.kerinci.internal.glfw.KrcWindow;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;
import static ch.szclsb.kerinci.internal.Utils.*;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class KrcSwapchain extends AbstractKrcHandle {
    private KrcSwapchain(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public static class CreateInfo extends AbstractCreateInfo<KrcSwapchain> {
        private final int flags;
        private final KrcWindow window;
        private final int minImageCount;
        private final int imageFormat;
        private final int imageColorSpace;
        private final KrcExtent2D imageExtent;
        private final int imageArrayLayers;
        private final int imageUsage;
        private final int imageSharingMode;
        private final Integer[] pQueueFamilyIndices;
        private final int preTransform;
        private final int compositeAlpha;
        private final int presentMode;
        private final boolean clipped;
        private final KrcSwapchain oldSwapchain;

        public CreateInfo(
                int flags,
                KrcWindow window,
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
            super(KrcSwapchain.class, KrcSwapchain::new);
            this.flags = flags;
            this.window = window;
            this.minImageCount = minImageCount;
            this.imageFormat = imageFormat;
            this.imageColorSpace = imageColorSpace;
            this.imageExtent = imageExtent;
            this.imageArrayLayers = imageArrayLayers;
            this.imageUsage = imageUsage;
            this.imageSharingMode = imageSharingMode;
            this.pQueueFamilyIndices = pQueueFamilyIndices;
            this.preTransform = preTransform;
            this.compositeAlpha = compositeAlpha;
            this.presentMode = presentMode;
            this.clipped = clipped;
            this.oldSwapchain = oldSwapchain;
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var createInfoSegment = allocator.apply(VkSwapchainCreateInfoKHR.$LAYOUT());
            VkSwapchainCreateInfoKHR.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR());
            return createInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional) {
            VkSwapchainCreateInfoKHR.flags$set(pCreateInfo, flags);
            VkSwapchainCreateInfoKHR.surface$set(pCreateInfo, window.getSurface());
            VkSwapchainCreateInfoKHR.minImageCount$set(pCreateInfo, minImageCount);
            VkSwapchainCreateInfoKHR.imageFormat$set(pCreateInfo, imageFormat);
            VkSwapchainCreateInfoKHR.imageColorSpace$set(pCreateInfo, imageColorSpace);

            imageExtent.write(VkSwapchainCreateInfoKHR.imageExtent$slice(pCreateInfo), additional);
            VkSwapchainCreateInfoKHR.imageArrayLayers$set(pCreateInfo, imageArrayLayers);
            VkSwapchainCreateInfoKHR.imageUsage$set(pCreateInfo, imageUsage);
            VkSwapchainCreateInfoKHR.imageSharingMode$set(pCreateInfo, imageSharingMode);
            writeArrayPointer(pQueueFamilyIndices, JAVA_INT, pCreateInfo, additional,
                    (slice, i) -> slice.set(JAVA_INT, 0, i),
                    VkSwapchainCreateInfoKHR::queueFamilyIndexCount$set,
                    VkSwapchainCreateInfoKHR::pQueueFamilyIndices$set);
            VkSwapchainCreateInfoKHR.preTransform$set(pCreateInfo, preTransform);
            VkSwapchainCreateInfoKHR.compositeAlpha$set(pCreateInfo, compositeAlpha);
            VkSwapchainCreateInfoKHR.presentMode$set(pCreateInfo, presentMode);
            VkSwapchainCreateInfoKHR.clipped$set(pCreateInfo, toVkBool(clipped));
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkCreateSwapchainKHR(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) == VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkDestroySwapchainKHR(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }

    public KrcArray<KrcImage> getSwapChainImages(Allocator arrayAllocator) {
        try(var arena = Arena.ofConfined()) {
            var pCount = arena.allocate(uint32_t);
            krc_vkGetSwapchainImagesKHR(getDevice().getLogical(), getVkHandle(), pCount, MemorySegment.NULL);
            var count = pCount.get(uint32_t, 0);
            var pArray = arrayAllocator.apply(MemoryLayout.sequenceLayout(count, VkImage));
            krc_vkGetSwapchainImagesKHR(getDevice().getLogical(), getVkHandle(), pCount, pArray);
            var data = new KrcImage[count];
            forEachSlice(VkImage, pArray, (slice, i) ->
                    data[i] = new KrcImage(getDevice(), slice.get(VkImage, 0), () -> {}));  // dont construct by create info images are managed by swapchain
            return new KrcArray<>(pArray.asReadOnly(), data);
        }

    }
}
