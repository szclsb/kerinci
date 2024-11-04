package ch.szclsb.kerinci.internal.swapchain;

import ch.szclsb.kerinci.api.VkSwapchainCreateInfoKHR;
import ch.szclsb.kerinci.internal.Allocator;
import ch.szclsb.kerinci.internal.KrcDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static ch.szclsb.kerinci.api.api_h_1.VK_SUCCESS;
import static ch.szclsb.kerinci.api.api_h_4.VkSwapchainKHR;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateSwapchainKHR;
import static ch.szclsb.kerinci.internal.Utils.*;
import static ch.szclsb.kerinci.internal.extent.KrcExtentFactory.write2D;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class KrcSwapchainFactory  {
    private static final Logger logger = LoggerFactory.getLogger(KrcSwapchainFactory.class);

    private KrcSwapchainFactory() {}

    private static MemorySegment allocateCreateInfo(Arena arena) {
        var createInfoSegment = arena.allocate(VkSwapchainCreateInfoKHR.$LAYOUT());
        VkSwapchainCreateInfoKHR.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR());
        return createInfoSegment;
    }

    private static void setSwaphainCreateInfo(Allocator allocator, MemorySegment segment, KrcSwapchain.CreateInfo createInfo) {
        VkSwapchainCreateInfoKHR.flags$set(segment, createInfo.flags());
        VkSwapchainCreateInfoKHR.surface$set(segment, createInfo.window().getSurface());
        VkSwapchainCreateInfoKHR.minImageCount$set(segment, createInfo.minImageCount());
        VkSwapchainCreateInfoKHR.imageFormat$set(segment, createInfo.imageFormat());
        VkSwapchainCreateInfoKHR.imageColorSpace$set(segment, createInfo.imageColorSpace());
        write2D(createInfo.imageExtent(), VkSwapchainCreateInfoKHR.imageExtent$slice(segment));
        VkSwapchainCreateInfoKHR.imageArrayLayers$set(segment, createInfo.imageArrayLayers());
        VkSwapchainCreateInfoKHR.imageUsage$set(segment, createInfo.imageUsage());
        VkSwapchainCreateInfoKHR.imageSharingMode$set(segment, createInfo.imageSharingMode());
        writeArrayPointer(createInfo.pQueueFamilyIndices(), JAVA_INT, segment, allocator,
                (slice, i) -> slice.set(JAVA_INT, 0, i),
                VkSwapchainCreateInfoKHR::queueFamilyIndexCount$set,
                VkSwapchainCreateInfoKHR::pQueueFamilyIndices$set);
        VkSwapchainCreateInfoKHR.preTransform$set(segment, createInfo.preTransform());
        VkSwapchainCreateInfoKHR.compositeAlpha$set(segment, createInfo.compositeAlpha());
        VkSwapchainCreateInfoKHR.presentMode$set(segment, createInfo.presentMode());
        VkSwapchainCreateInfoKHR.clipped$set(segment, toVkBool(createInfo.clipped()));
    }

    private static KrcSwapchain allocate(KrcDevice device, MemorySegment createInfoSegment, MemorySegment handle) {
        if (krc_vkCreateSwapchainKHR(device.getLogical(), createInfoSegment, MemorySegment.NULL, handle) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create swapchain");
        }
        var semaphore = handle.get(VkSwapchainKHR, 0);
        logger.debug("Created swapchain {}", printAddress(semaphore));
        return new KrcSwapchain(device, semaphore);
    }

    public static KrcSwapchain createSwapchain(KrcDevice device, KrcSwapchain.CreateInfo swapchainCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            setSwaphainCreateInfo(arena::allocate, createInfoSegment, swapchainCreateInfo);
            var handle = arena.allocate(VkSwapchainKHR);
            return allocate(device, createInfoSegment, handle);
        }
    }

}
