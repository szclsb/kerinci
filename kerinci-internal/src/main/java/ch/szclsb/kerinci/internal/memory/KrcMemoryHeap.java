package ch.szclsb.kerinci.internal.memory;

public record KrcMemoryHeap(
        long size,
        KrcMemoryHeapFlags flags
) {
}
