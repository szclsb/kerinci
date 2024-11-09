package ch.szclsb.kerinci.internal.vulkan;

public record KrcMemoryRequirement(
        long size,
        long alignment,
        int memoryTypeBits
) {
}
