package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkExtent2D;
import ch.szclsb.kerinci.api.VkSurfaceCapabilitiesKHR;
import ch.szclsb.kerinci.api.VkSurfaceFormatKHR;
import ch.szclsb.kerinci.internal.extent.KrcExtent2D;
import ch.szclsb.kerinci.internal.extent.KrcExtentFactory;
import ch.szclsb.kerinci.internal.swapchain.KrcSwapchain;
import ch.szclsb.kerinci.internal.swapchain.KrcSwapchainFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_2.VK_FORMAT_B8G8R8A8_UNORM;
import static ch.szclsb.kerinci.api.api_h_4.*;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Swapchain implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Swapchain.class);

    private final Arena arena;

    private KrcDevice device;
    private KrcSwapchain swapchain;
//    private MemorySegment oldSwapChain;
//
//    private int swapChainImageFormat;
//    private int swapChainDepthFormat;
//    private MemorySegment extentSegment;
//    private MemorySegment windowExtent;
//
//    private NativeArray swapChainFramebuffers;
//    private MemorySegment renderPass;
//
//    private NativeArray depthImages;
//    private NativeArray depthImageMemorys;
//    private NativeArray depthImageViews;
//    private NativeArray swapChainImages;
//    private NativeArray swapChainImageViews;
//
//    private NativeArray imageAvailableSemaphores;
//    private NativeArray renderFinishedSemaphores;
//    private NativeArray inFlightFences;
//    private NativeArray imagesInFlight;
//
//    private int currentFrame = 0;
//
//    private MemorySegment pImageIndex;
//
    public Swapchain(KrcDevice device, Window window, QueueFamilyIndices indices) {
        this.arena = Arena.ofConfined();
        this.device = device;
        init(device, window, indices);
    }
//
////    public SwapChain(Device device, Window window, QueueFamilyIndices indices, SwapChain old) {
////
////    }

    private void init(KrcDevice device, Window window, QueueFamilyIndices indices) {
        createSwapChain(device, window, indices);
//        createImageViews();
//        createRenderPass();
//        createDepthResources();
//        createFramebuffers();
//        createSyncObjects();
    }

//    private int findDepthFormat() {
//        return device.getVk().findSupportedFormat(Set.of(
//                VK_FORMAT_D32_SFLOAT(), VK_FORMAT_D32_SFLOAT_S8_UINT(), VK_FORMAT_D24_UNORM_S8_UINT()
//        ), VK_IMAGE_TILING_OPTIMAL(), VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT());
//    }

    private void createSwapChain(KrcDevice device, Window window, QueueFamilyIndices indices) {
        var details = window.querySwapChainSupport();

        var format = MemorySegment.NULL;
        for (var i = 0; i < details.formats().size() && format.address() == 0; i++) {
            if (VkSurfaceFormatKHR.format$get(details.formats().data(), i) == VK_FORMAT_B8G8R8A8_UNORM()
                    && VkSurfaceFormatKHR.colorSpace$get(details.formats().data(), i) == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR()) {
                format = details.formats().data().asSlice(i * VkSurfaceFormatKHR.sizeof(), VkSurfaceFormatKHR.$LAYOUT());
            }
        }

        var presentMode = VK_PRESENT_MODE_FIFO_KHR();  // V-Sync
        for (var i = 0; i < details.presentModes().size() && presentMode == VK_PRESENT_MODE_FIFO_KHR(); i++) {
            if (details.presentModes().data().get(JAVA_INT, i * JAVA_INT.byteSize()) == VK_PRESENT_MODE_MAILBOX_KHR()) {
                presentMode = VK_PRESENT_MODE_MAILBOX_KHR();
                logger.info("Present mode: Mailbox");
            }
        }

        var swapchainExtend = KrcExtent2D.cap(
                window.getExtent(),
                KrcExtentFactory.read2D(VkSurfaceCapabilitiesKHR.minImageExtent$slice(details.capabilities())),
                KrcExtentFactory.read2D(VkSurfaceCapabilitiesKHR.maxImageExtent$slice(details.capabilities()))
        );

        var imageCount = VkSurfaceCapabilitiesKHR.minImageCount$get(details.capabilities()) + 1;
        var maxImageCount = VkSurfaceCapabilitiesKHR.maxImageCount$get(details.capabilities());
        if (maxImageCount > 0 && imageCount > maxImageCount) {
            imageCount = maxImageCount;
        }

//        this.swapChainImageFormat = VkSurfaceFormatKHR.format$get(format);
//        this.extentSegment = arena.allocate(VkExtent2D.$LAYOUT());
//        KrcExtentFactory.write2D(swapchainExtend, extentSegment);

        var exclusive = indices.graphicsFamily == indices.presentFamily;
        var swapChainCreateInfo = new KrcSwapchain.CreateInfo(
                0,
                window,
                imageCount,
                VkSurfaceFormatKHR.format$get(format),
                VkSurfaceFormatKHR.colorSpace$get(format),
                swapchainExtend,
                1,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT(),
                exclusive ? VK_SHARING_MODE_EXCLUSIVE() : VK_SHARING_MODE_CONCURRENT(),
                exclusive ? null : new Integer[] {indices.graphicsFamily, indices.presentFamily},
                VkSurfaceCapabilitiesKHR.currentTransform$get(details.capabilities()),
                VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR(),
                presentMode,
                true,
                null
        );
        // todo old swapchain

        this.swapchain = KrcSwapchainFactory.createSwapchain(device, swapChainCreateInfo);
    }

//    private void createImageViews() {
//        // we only specified a minimum number of images in the swap chain, so the implementation is
//        // allowed to create a swap chain with more. That's why we'll first query the final number of
//        // images with vkGetSwapchainImagesKHR, then resize the container and finally call it again to
//        // retrieve the handles.
//        var pEffectiveImageCount = arena.allocate(JAVA_INT);
//        krc_vkGetSwapchainImagesKHR(device.getLogical(), swapChain, pEffectiveImageCount, MemorySegment.NULL);
//        var effectiveImageCount = pEffectiveImageCount.get(JAVA_INT, 0);
//        var pSwapChainImages = arena.allocate(sequenceLayout(effectiveImageCount, VkImage));
//        krc_vkGetSwapchainImagesKHR(device.getLogical(), swapChain, pEffectiveImageCount, pSwapChainImages);
//        this.swapChainImages = new NativeArray(pSwapChainImages, effectiveImageCount);
//
//        this.pImageIndex = arena.allocate(uint32_t);
//    }
//
//    private void createRenderPass() {
//
//    }
//
//    private void createDepthResources() {
//
//    }
//
//    private void createFramebuffers() {
//
//    }
//
//    private void createSyncObjects() {
//
//    }
//
//    protected boolean acquireNextImage(int imageIndex) {
//        pImageIndex.set(JAVA_INT, 0, imageIndex);
//        krc_vkWaitForFences(
//                device.getLogical(),
//                1,
//                inFlightFences.data().asSlice(currentFrame, VkFence),
//                VK_TRUE(),
//                Long.MAX_VALUE);
//        return krc_vkAcquireNextImageKHR(
//                device.getLogical(),
//                swapChain,
//                Long.MAX_VALUE,
//                imageAvailableSemaphores.data().asSlice(currentFrame, VkSemaphore),
//                MemorySegment.NULL,
//                pImageIndex
//        ) == VK_TRUE();
//    }
//
////    protected boolean submitCommandBuffer() {
////
////    }


    @Override
    public void close() throws Exception {
        swapchain.close();
        arena.close();
    }
}
