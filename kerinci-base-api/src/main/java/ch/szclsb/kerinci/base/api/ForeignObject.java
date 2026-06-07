package ch.szclsb.kerinci.base.api;

import java.lang.foreign.MemorySegment;

public interface ForeignObject {
    MemorySegment getSegment();
}
