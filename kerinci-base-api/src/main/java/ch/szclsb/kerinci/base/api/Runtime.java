package ch.szclsb.kerinci.base.api;

import java.lang.foreign.MemorySegment;

public interface Runtime {
    MemorySegment loadSymbol(String name);
}
