package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

@Deprecated
public record NativeArray (
        MemorySegment data,
        int size
) {
}
