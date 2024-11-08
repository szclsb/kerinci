package ch.szclsb.kerinci.internal.renderpass;

public record KrcSubpassDependency(
        int dependencyFlags,
        int srcSubpass,
        int srcStageMask,
        int srcAccessMask,
        int dstSubpass,
        int dstStageMask,
        int dstAccessMask
) {
}
