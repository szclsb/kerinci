package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkImageCreateInfo;
import ch.szclsb.kerinci.api.VkMemoryRequirements;
import ch.szclsb.kerinci.internal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.VK_SUCCESS;
import static ch.szclsb.kerinci.api.api_h_6.*;
import static ch.szclsb.kerinci.internal.Utils.*;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class KrcImage extends AbstractKrcHandle {
    private static final Logger logger = LoggerFactory.getLogger(KrcImage.class);

    // todo swapchain image
    public KrcImage(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        super(device, vkHandle, destructor);
    }

    public static class CreateInfo extends AbstractCreateInfo<KrcImage> {
        private final int flags;
        private final KrcImageType type;
        private final KrcFormat format;
        private final KrcExtent3D extent;
        private final int mipLevels;
        private final int arrayLayers;
        private final KrcSampleCountFlag[] samples;
        private final KrcImageTiling tiling;
        private final KrcImageUsageFlags[] usage;
        private final KrcSharingMode sharingMode;
        private final Integer[] queueFamilyIndex;
        private final KrcImageLayout initialLayout;

        public CreateInfo(
                int flags,
                KrcImageType type,
                KrcFormat format,
                KrcExtent3D extent,
                int mipLevels,
                int arrayLayers,
                KrcSampleCountFlag[] samples,
                KrcImageTiling tiling,
                KrcImageUsageFlags[] usage,
                KrcSharingMode sharingMode,
                Integer[] queueFamilyIndex,
                KrcImageLayout initialLayout) {
            super(KrcImage.class, KrcImage::new, VkImage);
            this.flags = flags;
            this.type = type;
            this.format = format;
            this.extent = extent;
            this.mipLevels = mipLevels;
            this.arrayLayers = arrayLayers;
            this.samples = samples;
            this.tiling = tiling;
            this.usage = usage;
            this.sharingMode = sharingMode;
            this.queueFamilyIndex = queueFamilyIndex;
            this.initialLayout = initialLayout;
        }

        @Override
        protected MemorySegment allocateCreateInfo(Allocator allocator) {
            var createInfoSegment = allocator.apply(VkImageCreateInfo.$LAYOUT());
            VkImageCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO());
            return createInfoSegment;
        }

        @Override
        protected void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional) {
            VkImageCreateInfo.flags$set(pCreateInfo, flags);
            VkImageCreateInfo.imageType$set(pCreateInfo, type.getValue());
            VkImageCreateInfo.format$set(pCreateInfo, format.getValue());
            KrcExtentFactory.write3D(VkImageCreateInfo.extent$slice(pCreateInfo), extent);
            VkImageCreateInfo.mipLevels$set(pCreateInfo, mipLevels);
            VkImageCreateInfo.arrayLayers$set(pCreateInfo, arrayLayers);
            VkImageCreateInfo.samples$set(pCreateInfo, or(samples));
            VkImageCreateInfo.tiling$set(pCreateInfo, tiling.getValue());
            VkImageCreateInfo.usage$set(pCreateInfo, or(usage));
            VkImageCreateInfo.sharingMode$set(pCreateInfo, sharingMode.getValue());
            writeArrayPointer(queueFamilyIndex,
                    JAVA_INT, pCreateInfo, additional,
                    (slice, i) -> slice.set(JAVA_INT, 0, i),
                    VkImageCreateInfo::queueFamilyIndexCount$set,
                    VkImageCreateInfo::pQueueFamilyIndices$set);
            VkImageCreateInfo.initialLayout$set(pCreateInfo, initialLayout.getValue());
        }

        @Override
        protected boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle) {
            return krc_vkCreateImage(device.getLogical(), pCreateInfo, MemorySegment.NULL, pHandle) == VK_SUCCESS();
        }

        @Override
        protected void destroy(KrcDevice device, MemorySegment vkHandle) {
            krc_vkDestroyImage(device.getLogical(), vkHandle, MemorySegment.NULL);
        }
    }

    public KrcMemoryRequirement getMemoryRequirement() {
        try(var arena = Arena.ofConfined()) {
            var pMemoryRequirement = arena.allocate(VkMemoryRequirements.$LAYOUT());
            krc_vkGetImageMemoryRequirements(getDevice().getLogical(), getVkHandle(), pMemoryRequirement);
            return new KrcMemoryRequirement(
                    VkMemoryRequirements.size$get(pMemoryRequirement),
                    VkMemoryRequirements.alignment$get(pMemoryRequirement),
                    VkMemoryRequirements.memoryTypeBits$get(pMemoryRequirement)
            );
        }
    }

    public void bindMemory(KrcDeviceMemory memory) {
        if (krc_vkBindImageMemory(getDevice().getLogical(), getVkHandle(), memory.getVkHandle(), 0) != VK_SUCCESS()) {
            throw new RuntimeException("failed to bind image memory!");
        }
        logger.debug("bound image {} to memory {}", printAddress(getVkHandle()), printAddress(memory.getVkHandle()));
    }
}
