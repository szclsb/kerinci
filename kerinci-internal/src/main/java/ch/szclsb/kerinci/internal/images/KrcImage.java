package ch.szclsb.kerinci.internal.images;

import ch.szclsb.kerinci.api.VkMemoryRequirements;
import ch.szclsb.kerinci.internal.*;
import ch.szclsb.kerinci.internal.KrcFormat;
import ch.szclsb.kerinci.internal.extent.KrcExtent3D;
import ch.szclsb.kerinci.internal.memory.KrcDeviceMemory;
import ch.szclsb.kerinci.internal.memory.KrcDeviceMemoryAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.VK_SUCCESS;
import static ch.szclsb.kerinci.api.api_h_6.*;
import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcImage extends AbstractKrcHandle {
    private static final Logger logger = LoggerFactory.getLogger(KrcImage.class);

    public record CreateInfo(
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
            KrcImageLayout initialLayout
    ) {
    }

    protected KrcImage(KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    public KrcMemoryRequirement getMemoryRequirement() {
        try(var arena = Arena.ofConfined()) {
            var pMemoryRequirement = arena.allocate(VkMemoryRequirements.$LAYOUT());
            krc_vkGetImageMemoryRequirements(device.getLogical(), vkHandle, pMemoryRequirement);
            return new KrcMemoryRequirement(
                    VkMemoryRequirements.size$get(pMemoryRequirement),
                    VkMemoryRequirements.alignment$get(pMemoryRequirement),
                    VkMemoryRequirements.memoryTypeBits$get(pMemoryRequirement)
            );
        }
    }

    public void bindMemory(KrcDeviceMemory memory) {
        if (krc_vkBindImageMemory(device.getLogical(), vkHandle, memory.getVkHandle(), 0) != VK_SUCCESS()) {
            throw new RuntimeException("failed to bind image memory!");
        }
        logger.debug("bound image {} to memory {}", printAddress(vkHandle), printAddress(memory.getVkHandle()));
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyImage(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
