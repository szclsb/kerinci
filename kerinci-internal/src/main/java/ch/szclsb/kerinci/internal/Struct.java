package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

public interface Struct {
    MemorySegment getSegment();
}
