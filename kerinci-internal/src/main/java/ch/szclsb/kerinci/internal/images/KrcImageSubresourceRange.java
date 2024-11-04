package ch.szclsb.kerinci.internal.images;

public record KrcImageSubresourceRange(
        int aspectMask,
        int baseMipLevel,
        int levelCount,
        int baseArrayLayer,
        int layerCount
) {
}
