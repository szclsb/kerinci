package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.Flag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KrcSampleCountFlag implements Flag {
    BIT_1(0x00000001),
    BIT_2(0x00000002),
    BIT_4(0x00000004),
    BIT_8(0x00000008),
    BIT_16(0x00000010),
    BIT_32(0x00000020),
    BIT_64(0x00000040),
    BITS_MAX_ENUM(0x7FFFFFFF);

    private final int value;
}

