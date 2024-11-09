package ch.szclsb.kerinci.internal.vulkan;

public record KrcAttachmentDescription(
        int flags,
        KrcFormat format,
        int samples,
        int loadOp,
        int storeOp,
        int stencilLoadOp,
        int stencilStoreOp,
        int initialLayout,
        int finalLayout
) {
}
