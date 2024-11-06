package ch.szclsb.kerinci.internal;

public enum KrcImageTiling implements HasValue {
    OPTIMAL(0),
    LINEAR(1),
    DRM_FORMAT_MODIFIER_EXT(1000158000),
    MAX_ENUM(0x7FFFFFFF);

    private int value;
    KrcImageTiling(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}

