package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.HasValue;

public enum KrcColorSpace implements HasValue {
    SRGB_NONLINEAR_KHR(0),
    DISPLAY_P3_NONLINEAR_EXT(1000104001),
    EXTENDED_SRGB_LINEAR_EXT(1000104002),
    DISPLAY_P3_LINEAR_EXT(1000104003),
    DCI_P3_NONLINEAR_EXT(1000104004),
    BT709_LINEAR_EXT(1000104005),
    BT709_NONLINEAR_EXT(1000104006),
    BT2020_LINEAR_EXT(1000104007),
    HDR10_ST2084_EXT(1000104008),
    DOLBYVISION_EXT(1000104009),
    HDR10_HLG_EXT(1000104010),
    ADOBERGB_LINEAR_EXT(1000104011),
    ADOBERGB_NONLINEAR_EXT(1000104012),
    PASS_THROUGH_EXT(1000104013),
    EXTENDED_SRGB_NONLINEAR_EXT(1000104014),
    DISPLAY_NATIVE_AMD(1000213000),
    RGB_NONLINEAR_KHR(SRGB_NONLINEAR_KHR),
    DCI_P3_LINEAR_EXT(DISPLAY_P3_LINEAR_EXT),
    MAX_ENUM_KHR(0x7FFFFFFF);

    private final int value;

    KrcColorSpace(int value) {
        this.value = value;
    }

    KrcColorSpace(KrcColorSpace colorSpace) {
        this.value = colorSpace.value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
