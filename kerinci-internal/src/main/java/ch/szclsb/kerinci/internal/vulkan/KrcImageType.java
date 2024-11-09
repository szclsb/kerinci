package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.HasValue;

public enum KrcImageType implements HasValue {
    TYPE_1D(0),
    TYPE_2D(1),
    TYPE_3D(2),
    TYPE_CUBE(3),
    TYPE_1D_ARRAY(4),
    TYPE_2D_ARRAY(5),
    TYPE_CUBE_ARRAY(6),
    TYPE_MAX_ENUM(2147483647);

    private final int value;

    KrcImageType(final int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
