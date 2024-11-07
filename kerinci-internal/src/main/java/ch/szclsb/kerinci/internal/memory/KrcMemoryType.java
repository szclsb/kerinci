package ch.szclsb.kerinci.internal.memory;

public record KrcMemoryType(
        KrcMemoryPropertyFlags[] propertyFlags,
        int heapIndex
) {
}
