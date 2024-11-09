package ch.szclsb.kerinci.internal.vulkan;

public enum KrcSharingMode {
    EXCLUSIVE(0),
    CONCURRENT(1),
    MAX_ENUM(0x7FFFFFFF);

    private int value;
    KrcSharingMode(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
