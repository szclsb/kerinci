package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

public interface HandleConstructor<T extends AbstractKrcHandle2> {
    T apply(KrcDevice device, MemorySegment vkHandle, Runnable destructor);
}
