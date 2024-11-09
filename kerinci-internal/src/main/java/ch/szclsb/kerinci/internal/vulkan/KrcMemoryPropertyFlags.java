package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.Flag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KrcMemoryPropertyFlags implements Flag {
    DEVICE_LOCAL_BIT(0x00000001),
    HOST_VISIBLE_BIT(0x00000002),
    HOST_COHERENT_BIT(0x00000004),
    HOST_CACHED_BIT(0x00000008),
    LAZILY_ALLOCATED_BIT(0x00000010),
    PROTECTED_BIT(0x00000020),
    DEVICE_COHERENT_BIT_AMD(0x00000040),
    DEVICE_UNCACHED_BIT_AMD(0x00000080),
    RDMA_CAPABLE_BIT_NV(0x00000100),
    FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

    private final int value;
}
