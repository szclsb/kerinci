package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkSurfaceCapabilitiesKHR;
import ch.szclsb.kerinci.api.VkSurfaceFormatKHR;
import ch.szclsb.kerinci.internal.extent.KrcExtent2D;
import ch.szclsb.kerinci.internal.extent.KrcExtent3D;
import ch.szclsb.kerinci.internal.extent.KrcExtentFactory;
import ch.szclsb.kerinci.internal.images.*;
import ch.szclsb.kerinci.internal.imageview.KrcImageView;
import ch.szclsb.kerinci.internal.imageview.KrcImageViewFactory;
import ch.szclsb.kerinci.internal.attachment.KrcAttachmentDescription;
import ch.szclsb.kerinci.internal.attachment.KrcAttachmentReference;
import ch.szclsb.kerinci.internal.memory.KrcDeviceMemory;
import ch.szclsb.kerinci.internal.memory.KrcDeviceMemoryAllocator;
import ch.szclsb.kerinci.internal.memory.KrcMemoryPropertyFlags;
import ch.szclsb.kerinci.internal.renderpass.KrcRenderPass;
import ch.szclsb.kerinci.internal.renderpass.KrcRenderPassFactory;
import ch.szclsb.kerinci.internal.swapchain.KrcSwapchain;
import ch.szclsb.kerinci.internal.swapchain.KrcSwapchainFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_2.VK_FORMAT_B8G8R8A8_UNORM;
import static ch.szclsb.kerinci.api.api_h_4.*;
import static ch.szclsb.kerinci.api.api_h_6.VK_SUBPASS_EXTERNAL;
import static ch.szclsb.kerinci.internal.Utils.from;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Swapchain implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Swapchain.class);

    private final Arena arena;

    private KrcDevice device;
    private KrcSwapchain swapchain;
    //    private MemorySegment oldSwapChain;
//
    private KrcExtent2D swapchainExtend;
    private KrcFormat swapChainImageFormat;
    private KrcFormat depthFormat;
    //
//    private NativeArray swapChainFramebuffers;
    private KrcRenderPass renderPass;

    //    private NativeArray depthImageMemorys;
    private KrcArray<KrcImage> swapChainImages;
    private KrcArray<KrcImageView> swapChainImageViews;
    private KrcArray<KrcImage> depthImages;
    private KrcArray<KrcDeviceMemory> depthImageMemory;
    private KrcArray<KrcImageView> depthImageViews;

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
        this.swapchain = createSwapChain(device, window, indices);
        this.swapChainImages = swapchain.getSwapChainImages(arena::allocate);
        this.swapChainImageViews = createImageViews();
        this.depthImages = createDepthImages();
        this.depthImageMemory = bindDepthImages();
        this.depthImageViews = createDepthImageViews();
        this.renderPass = createRenderPass();
//        createFramebuffers();
//        createSyncObjects();
    }

//    private int findDepthFormat() {
//        return device.getVk().findSupportedFormat(Set.of(
//                VK_FORMAT_D32_SFLOAT(), VK_FORMAT_D32_SFLOAT_S8_UINT(), VK_FORMAT_D24_UNORM_S8_UINT()
//        ), VK_IMAGE_TILING_OPTIMAL(), VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT());
//    }

    private KrcSwapchain createSwapChain(KrcDevice device, Window window, QueueFamilyIndices indices) {
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

        this.swapchainExtend = KrcExtent2D.cap(
                window.getExtent(),
                KrcExtentFactory.read2D(VkSurfaceCapabilitiesKHR.minImageExtent$slice(details.capabilities())),
                KrcExtentFactory.read2D(VkSurfaceCapabilitiesKHR.maxImageExtent$slice(details.capabilities()))
        );

        var imageCount = VkSurfaceCapabilitiesKHR.minImageCount$get(details.capabilities()) + 1;
        var maxImageCount = VkSurfaceCapabilitiesKHR.maxImageCount$get(details.capabilities());
        if (maxImageCount > 0 && imageCount > maxImageCount) {
            imageCount = maxImageCount;
        }

        this.swapChainImageFormat = from(VkSurfaceFormatKHR.format$get(format), KrcFormat.class);
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
                exclusive ? null : new Integer[]{indices.graphicsFamily, indices.presentFamily},
                VkSurfaceCapabilitiesKHR.currentTransform$get(details.capabilities()),
                VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR(),
                presentMode,
                true,
                null
        );
        // todo old swapchain

        return KrcSwapchainFactory.createSwapchain(device, swapChainCreateInfo);
    }

    private KrcArray<KrcImageView> createImageViews() {
        var subresource = new KrcImageSubresourceRange(
                VK_IMAGE_ASPECT_COLOR_BIT(),
                0,
                1,
                0,
                1
        );
        return KrcImageViewFactory.createImageViewArray(arena::allocate, device, swapChainImages.stream()
                .map(image -> new KrcImageView.CreateInfo(
                        0,
                        image,
                        KrcImageType.TYPE_2D,
                        swapChainImageFormat,
                        null,
                        subresource
                ))
                .toList());
    }


    private KrcArray<KrcImage> createDepthImages() {
        this.depthFormat = device.getVk().findSupportedFormat(new KrcFormat[]{
                KrcFormat.D32_SFLOAT,
                KrcFormat.D32_SFLOAT_S8_UINT,
                KrcFormat.D24_UNORM_S8_UINT
        }, KrcImageTiling.OPTIMAL, KrcFormatFeatureFlag.DEPTH_STENCIL_ATTACHMENT_BIT);
        var imageCount = swapChainImages.length();
        var imageCreateInfo = new KrcImage.CreateInfo(
                0,
                KrcImageType.TYPE_2D,
                depthFormat,
                new KrcExtent3D(
                        swapchainExtend.width(),
                        swapchainExtend.height(),
                        1
                ),
                1,
                1,
                new KrcSampleCountFlag[]{
                        KrcSampleCountFlag.BIT_1
                },
                KrcImageTiling.OPTIMAL,
                new KrcImageUsageFlags[]{
                        KrcImageUsageFlags.DEPTH_STENCIL_ATTACHMENT_BIT
                },
                KrcSharingMode.EXCLUSIVE,
                null,
                KrcImageLayout.UNDEFINED
        );
        return KrcImageFactory.createImageArray(arena::allocate, device, imageCount, imageCreateInfo);
    }

    private KrcArray<KrcDeviceMemory> bindDepthImages() {
        var memoryArray = KrcDeviceMemoryAllocator.allocateArray(arena::allocate, device, depthImages.stream()
                .map(image -> {
                    var requirements = image.getMemoryRequirement();
                    return new KrcDeviceMemory.AllocateInfo(
                            requirements.size(),
                            device.getVk().findMemoryType(requirements.memoryTypeBits(),
                                    KrcMemoryPropertyFlags.DEVICE_LOCAL_BIT)
                    );
                })
                .toList());
        for (var i = 0; i < depthImages.length(); i++) {
            var image = depthImages.get(i);
            var memory = memoryArray.get(i);
            image.bindMemory(memory);
        }
        return memoryArray;
    }

    private KrcArray<KrcImageView> createDepthImageViews() {
        var subresource = new KrcImageSubresourceRange(
                VK_IMAGE_ASPECT_DEPTH_BIT(),
                0,
                1,
                0,
                1
        );
        return KrcImageViewFactory.createImageViewArray(arena::allocate, device, depthImages.stream()
                .map(image -> new KrcImageView.CreateInfo(
                        0,
                        image,
                        KrcImageType.TYPE_2D,
                        depthFormat,
                        null,
                        subresource
                ))
                .toList());
    }


    private KrcRenderPass createRenderPass() {
        var colorAttachment = new KrcAttachmentDescription(
                0,
                swapChainImageFormat,
                VK_SAMPLE_COUNT_1_BIT(),
                VK_ATTACHMENT_LOAD_OP_CLEAR(),
                VK_ATTACHMENT_STORE_OP_STORE(),
                VK_ATTACHMENT_LOAD_OP_DONT_CARE(),
                VK_ATTACHMENT_STORE_OP_DONT_CARE(),
                VK_IMAGE_LAYOUT_UNDEFINED(),
                VK_IMAGE_LAYOUT_PRESENT_SRC_KHR()
        );
        var depthAttachment = new KrcAttachmentDescription(
                0,
                depthFormat,
                VK_SAMPLE_COUNT_1_BIT(),
                VK_ATTACHMENT_LOAD_OP_CLEAR(),
                VK_ATTACHMENT_STORE_OP_DONT_CARE(),
                VK_ATTACHMENT_LOAD_OP_DONT_CARE(),
                VK_ATTACHMENT_STORE_OP_DONT_CARE(),
                VK_IMAGE_LAYOUT_UNDEFINED(),
                VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL()
        );
        var subpass = new KrcRenderPass.SubpassDescription(
                0,
                VK_PIPELINE_BIND_POINT_GRAPHICS(),
                null,
                new KrcAttachmentReference[]{
                        new KrcAttachmentReference(
                                0,
                                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL()
                        )
                },
                new KrcAttachmentReference(
                        1,
                        VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL()
                ),
                null
        );
        var dependency = new KrcRenderPass.SubpassDependency(
                0,
                VK_SUBPASS_EXTERNAL(),
                VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT() | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT(),
                0,
                0,
                VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT() | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT(),
                VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT() | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT()
        );
        return KrcRenderPassFactory.createRenderPass(device, new KrcRenderPass.CreateInfo(
                0,
                new KrcAttachmentDescription[]{
                        colorAttachment,
                        depthAttachment
                },
                new KrcRenderPass.SubpassDescription[]{
                        subpass
                },
                new KrcRenderPass.SubpassDependency[]{
                        dependency
                }
        ));
    }

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
        renderPass.close();
        depthImageViews.close();
        depthImageMemory.close();
        depthImages.close();
        swapChainImageViews.close();
        swapchain.close();
        arena.close();
    }
}
