package ch.szclsb.kerinci.internal.memory;

import ch.szclsb.kerinci.internal.Flag;

public enum KrcMemoryHeapFlags implements Flag {
    DEVICE_LOCAL_BIT(0x00000001),
    MULTI_INSTANCE_BIT(0x00000002),
    MULTI_INSTANCE_BIT_KHR(MULTI_INSTANCE_BIT),
    FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

    private final int value;

    KrcMemoryHeapFlags(final int value) {
        this.value = value;
    }

    KrcMemoryHeapFlags(final KrcMemoryHeapFlags flag) {
        this.value = flag.value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
