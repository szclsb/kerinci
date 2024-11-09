package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.NativeArray;

import java.lang.foreign.MemorySegment;

public record SwapChainSupportDetails(
        MemorySegment capabilities,
        NativeArray formats,
        NativeArray presentModes
) {
}
