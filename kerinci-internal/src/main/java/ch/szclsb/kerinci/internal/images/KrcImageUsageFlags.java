package ch.szclsb.kerinci.internal.images;

import ch.szclsb.kerinci.internal.Flag;

public enum KrcImageUsageFlags implements Flag {
    TRANSFER_SRC_BIT(0x00000001),
    TRANSFER_DST_BIT(0x00000002),
    SAMPLED_BIT(0x00000004),
    STORAGE_BIT(0x00000008),
    COLOR_ATTACHMENT_BIT(0x00000010),
    DEPTH_STENCIL_ATTACHMENT_BIT(0x00000020),
    TRANSIENT_ATTACHMENT_BIT(0x00000040),
    INPUT_ATTACHMENT_BIT(0x00000080),
    VIDEO_DECODE_DST_BIT_KHR(0x00000400),
    VIDEO_DECODE_SRC_BIT_KHR(0x00000800),
    VIDEO_DECODE_DPB_BIT_KHR(0x00001000),
    FRAGMENT_DENSITY_MAP_BIT_EXT(0x00000200),
    FRAGMENT_SHADING_RATE_ATTACHMENT_BIT_KHR(0x00000100),
    //        VIDEO_ENCODE_DST_BIT_KHR = 0x00002000,
//        VIDEO_ENCODE_SRC_BIT_KHR = 0x00004000,
//        VIDEO_ENCODE_DPB_BIT_KHR = 0x00008000,
    ATTACHMENT_FEEDBACK_LOOP_BIT_EXT(0x00080000),
    INVOCATION_MASK_BIT_HUAWEI(0x00040000),
    SAMPLE_WEIGHT_BIT_QCOM(0x00100000),
    SAMPLE_BLOCK_MATCH_BIT_QCOM(0x00200000),
    SHADING_RATE_IMAGE_BIT_NV(0x00000100),
    FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

    private final int value;

    KrcImageUsageFlags(final int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}