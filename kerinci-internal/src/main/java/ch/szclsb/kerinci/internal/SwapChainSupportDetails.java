package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

public record SwapChainSupportDetails(
        MemorySegment capabilities,
        NativeArray formats,
        NativeArray presentModes
) {
}
