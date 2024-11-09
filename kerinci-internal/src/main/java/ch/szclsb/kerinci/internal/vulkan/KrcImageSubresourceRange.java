package ch.szclsb.kerinci.internal.vulkan;

public record KrcImageSubresourceRange(
        int aspectMask,
        int baseMipLevel,
        int levelCount,
        int baseArrayLayer,
        int layerCount
) {
}
