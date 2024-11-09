package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.NativeArray;

public record SwapChainSupportDetails(
        KrcSurfaceCapabilities capabilities,
        NativeArray formats, // todo
        NativeArray presentModes  // todo
) {
}
