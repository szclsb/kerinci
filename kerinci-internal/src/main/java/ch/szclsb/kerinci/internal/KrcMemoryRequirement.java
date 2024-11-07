package ch.szclsb.kerinci.internal;

public record KrcMemoryRequirement(
        long size,
        long alignment,
        int memoryTypeBits
) {
}
