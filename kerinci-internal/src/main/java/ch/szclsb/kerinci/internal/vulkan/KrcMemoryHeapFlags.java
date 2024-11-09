package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.Flag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KrcMemoryHeapFlags implements Flag {
    DEVICE_LOCAL_BIT(0x00000001),
    MULTI_INSTANCE_BIT(0x00000002),
    MULTI_INSTANCE_BIT_KHR(MULTI_INSTANCE_BIT.value),
    FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

    private final int value;
}
