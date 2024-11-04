package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

@FunctionalInterface
public interface Creator<T> {
    T apply(MemorySegment slice, int index);
}
