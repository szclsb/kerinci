package ch.szclsb.kerinci.internal.vulkan;

public record KrcMemoryType(
        KrcMemoryPropertyFlags[] propertyFlags,
        int heapIndex
) {
}
