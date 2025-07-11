package ch.szclsb.kerinci.base.api;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

@FunctionalInterface
public interface Allocator {
    MemorySegment apply(MemoryLayout layout);
}
