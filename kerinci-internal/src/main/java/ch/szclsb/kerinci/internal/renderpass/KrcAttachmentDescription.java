package ch.szclsb.kerinci.internal.renderpass;

public record KrcAttachmentDescription(
        int flags,
        int format,
        int samples,
        int loadOp,
        int storeOp,
        int stencilLoadOp,
        int stencilStoreOp,
        int initialLayout,
        int finalLayout
) {
}
