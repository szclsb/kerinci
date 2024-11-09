package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.HasValue;

public enum KrcPresentMode implements HasValue {
    IMMEDIATE_KHR(0),
    MAILBOX_KHR(1),
    FIFO_KHR(2),
    FIFO_RELAXED_KHR(3),
    SHARED_DEMAND_REFRESH_KHR(1000111000),
    SHARED_CONTINUOUS_REFRESH_KHR(1000111001),
    MAX_ENUM_KHR(0x7FFFFFFF);

    private final int value;

    KrcPresentMode(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
