package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

@FunctionalInterface
public interface Allocator {
    MemorySegment apply(MemoryLayout layout);
}
