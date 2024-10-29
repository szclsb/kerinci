package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkExtent2D;
import ch.szclsb.kerinci.api.VkSurfaceCapabilitiesKHR;
import ch.szclsb.kerinci.api.VkSurfaceFormatKHR;
import ch.szclsb.kerinci.api.VkSwapchainCreateInfoKHR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Set;

import static ch.szclsb.kerinci.api.api_h_2.VK_FORMAT_B8G8R8A8_UNORM;
import static ch.szclsb.kerinci.api.api_h_4.*;
import static ch.szclsb.kerinci.api.api_h_4.VK_PRESENT_MODE_MAILBOX_KHR;
import static ch.szclsb.kerinci.api.api_h_6.*;
import static ch.szclsb.kerinci.internal.Utils.printAddress;
import static java.lang.foreign.MemoryLayout.sequenceLayout;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class SwapChain implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SwapChain.class);

    private final Arena arena;

    private Device device;
    private MemorySegment swapChain;
    private MemorySegment oldSwapChain;

    private int swapChainImageFormat;
    private int swapChainDepthFormat;
    private MemorySegment swapChainExtent;
    private MemorySegment windowExtent;

    private NativeArray swapChainFramebuffers;
    private MemorySegment renderPass;

    private NativeArray depthImages;
    private NativeArray depthImageMemorys;
    private NativeArray depthImageViews;
    private NativeArray swapChainImages;
    private NativeArray swapChainImageViews;

    private NativeArray imageAvailableSemaphores;
    private NativeArray renderFinishedSemaphores;
    private NativeArray inFlightFences;
    private NativeArray imagesInFlight;

    private int currentFrame = 0;

    private MemorySegment pImageIndex;

    public SwapChain(Device device, Window window, QueueFamilyIndices indices) {
        this.arena = Arena.ofConfined();
        this.device = device;
        init(device, window, indices);
    }

//    public SwapChain(Device device, Window window, QueueFamilyIndices indices, SwapChain old) {
//
//    }

    private void init(Device device, Window window, QueueFamilyIndices indices) {
        createSwapChain(device, window, indices);
        createImageViews();
        createRenderPass();
        createDepthResources();
        createFramebuffers();
        createSyncObjects();
    }

    private int findDepthFormat() {
        return device.getVk().findSupportedFormat(Set.of(
                VK_FORMAT_D32_SFLOAT(), VK_FORMAT_D32_SFLOAT_S8_UINT(), VK_FORMAT_D24_UNORM_S8_UINT()
        ), VK_IMAGE_TILING_OPTIMAL(), VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT());
    }

    private void createSwapChain(Device device, Window window, QueueFamilyIndices indices) {
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
            if (details.formats().data().get(JAVA_INT, i) == VK_PRESENT_MODE_MAILBOX_KHR()) {
                presentMode = VK_PRESENT_MODE_MAILBOX_KHR();
                logger.info("Present mode: Mailbox");
            }
        }

        var currentExtend = window.getExtent();
        var minImageExtendWidth = VkExtent2D.width$get(VkSurfaceCapabilitiesKHR.minImageExtent$slice(details.capabilities()));
        var minImageExtendHeight = VkExtent2D.height$get(VkSurfaceCapabilitiesKHR.minImageExtent$slice(details.capabilities()));
        var maxImageExtendWidth = VkExtent2D.width$get(VkSurfaceCapabilitiesKHR.maxImageExtent$slice(details.capabilities()));
        var maxImageExtendHeight = VkExtent2D.height$get(VkSurfaceCapabilitiesKHR.maxImageExtent$slice(details.capabilities()));

        var imageCount = VkSurfaceCapabilitiesKHR.minImageCount$get(details.capabilities()) + 1;
        var maxImageCount = VkSurfaceCapabilitiesKHR.maxImageCount$get(details.capabilities());
        if (maxImageCount > 0 && imageCount > maxImageCount) {
            imageCount = maxImageCount;
        }

        this.swapChainImageFormat = VkSurfaceFormatKHR.format$get(format);
        this.swapChainExtent = arena.allocate(VkExtent2D.$LAYOUT());
        VkExtent2D.width$set(swapChainExtent, Integer.max(minImageExtendWidth, Integer.max(maxImageExtendWidth,
                VkExtent2D.width$get(currentExtend))));
        VkExtent2D.height$set(swapChainExtent, Integer.max(minImageExtendHeight, Integer.max(maxImageExtendHeight,
                VkExtent2D.height$get(currentExtend))));

        var swapChainCreateInfo = arena.allocate(VkSwapchainCreateInfoKHR.$LAYOUT());
        VkSwapchainCreateInfoKHR.sType$set(swapChainCreateInfo, VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR());
        VkSwapchainCreateInfoKHR.surface$set(swapChainCreateInfo, window.getSurface());
        VkSwapchainCreateInfoKHR.minImageCount$set(swapChainCreateInfo, imageCount);
        VkSwapchainCreateInfoKHR.imageFormat$set(swapChainCreateInfo, VkSurfaceFormatKHR.format$get(format));
        VkSwapchainCreateInfoKHR.imageColorSpace$set(swapChainCreateInfo, VkSurfaceFormatKHR.colorSpace$get(format));
        var swapChainCreateInfoExtent = VkSwapchainCreateInfoKHR.imageExtent$slice(swapChainCreateInfo);
        VkExtent2D.width$set(swapChainCreateInfoExtent, VkExtent2D.width$get(swapChainExtent));
        VkExtent2D.height$set(swapChainCreateInfoExtent, VkExtent2D.height$get(swapChainExtent));
        VkSwapchainCreateInfoKHR.imageArrayLayers$set(swapChainCreateInfo, 1);
        VkSwapchainCreateInfoKHR.imageUsage$set(swapChainCreateInfo, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT());

        var pQueueFamilyIndices = arena.allocate(MemoryLayout.sequenceLayout(2, JAVA_INT));
        pQueueFamilyIndices.set(JAVA_INT, 0, indices.graphicsFamily);
        pQueueFamilyIndices.set(JAVA_INT, 1, indices.presentFamily);
        if (indices.graphicsFamily != indices.presentFamily) {
            VkSwapchainCreateInfoKHR.imageSharingMode$set(swapChainCreateInfo, VK_SHARING_MODE_CONCURRENT());
            VkSwapchainCreateInfoKHR.queueFamilyIndexCount$set(swapChainCreateInfo, 2);
            VkSwapchainCreateInfoKHR.pQueueFamilyIndices$set(swapChainCreateInfo, pQueueFamilyIndices);
        } else {
            VkSwapchainCreateInfoKHR.imageSharingMode$set(swapChainCreateInfo, VK_SHARING_MODE_EXCLUSIVE());
            VkSwapchainCreateInfoKHR.queueFamilyIndexCount$set(swapChainCreateInfo, 0);
            VkSwapchainCreateInfoKHR.pQueueFamilyIndices$set(swapChainCreateInfo, MemorySegment.NULL);
        }

        VkSwapchainCreateInfoKHR.preTransform$set(swapChainCreateInfo, VkSurfaceCapabilitiesKHR.currentTransform$get(details.capabilities()));
        VkSwapchainCreateInfoKHR.compositeAlpha$set(swapChainCreateInfo, VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR());
        VkSwapchainCreateInfoKHR.presentMode$set(swapChainCreateInfo, presentMode);
        VkSwapchainCreateInfoKHR.clipped$set(swapChainCreateInfo, VK_TRUE());

        // todo old swapchain

        var pSwapChain = arena.allocate(VkSwapchainKHR);
        if (krc_vkCreateSwapchainKHR(device.getLogical(), swapChainCreateInfo, MemorySegment.NULL, pSwapChain) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create swapchain");
        }
        this.swapChain = pSwapChain.get(VkSwapchainKHR, 0);
        logger.debug("created swapchain {}", printAddress(swapChain));
    }

    private void createImageViews() {
        // we only specified a minimum number of images in the swap chain, so the implementation is
        // allowed to create a swap chain with more. That's why we'll first query the final number of
        // images with vkGetSwapchainImagesKHR, then resize the container and finally call it again to
        // retrieve the handles.
        var pEffectiveImageCount = arena.allocate(JAVA_INT);
        krc_vkGetSwapchainImagesKHR(device.getLogical(), swapChain, pEffectiveImageCount, MemorySegment.NULL);
        var effectiveImageCount = pEffectiveImageCount.get(JAVA_INT, 0);
        var pSwapChainImages = arena.allocate(sequenceLayout(effectiveImageCount, VkImage));
        krc_vkGetSwapchainImagesKHR(device.getLogical(), swapChain, pEffectiveImageCount, pSwapChainImages);
        this.swapChainImages = new NativeArray(pSwapChainImages, effectiveImageCount);

        this.pImageIndex = arena.allocate(uint32_t);
    }

    private void createRenderPass() {

    }

    private void createDepthResources() {

    }

    private void createFramebuffers() {

    }

    private void createSyncObjects() {

    }

    public boolean acquireNextImage(int imageIndex) {
        pImageIndex.set(JAVA_INT, 0, imageIndex);
        krc_vkWaitForFences(
                device.getLogical(),
                1,
                inFlightFences.data().asSlice(currentFrame, VkFence),
                VK_TRUE(),
                Long.MAX_VALUE);
        return krc_vkAcquireNextImageKHR(
                device.getLogical(),
                swapChain,
                Long.MAX_VALUE,
                imageAvailableSemaphores.data().asSlice(currentFrame, VkSemaphore),
                MemorySegment.NULL,
                pImageIndex
        ) == VK_TRUE();
    }


    @Override
    public void close() throws Exception {
        arena.close();
    }
}
