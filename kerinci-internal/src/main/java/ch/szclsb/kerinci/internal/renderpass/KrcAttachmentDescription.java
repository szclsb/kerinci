package ch.szclsb.kerinci.internal.renderpass;

import ch.szclsb.kerinci.api.VkAttachmentDescription;

import java.lang.foreign.MemorySegment;

public record KrcAttachmentDescription(
        int flags,
        int format,
        int samples,
        int loadOp,
        long storeOp,
        int stencilLoadOp,
        long stencilStoreOp,
        int initialLayout,
        long finalLayout
) {
}
