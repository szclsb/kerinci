package ch.szclsb.kerinci.internal.vulkan;

public record KrcMemoryHeap(
        long size,
        KrcMemoryHeapFlags flags
) {
}
