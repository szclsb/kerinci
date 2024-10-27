package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

public record NativeArray(
        MemorySegment data,
        int size
) {
}
