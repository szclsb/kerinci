package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;

import java.lang.foreign.MemorySegment;

public interface HandleConstructor<T extends AbstractKrcHandle> {
    T apply(KrcDevice device, MemorySegment vkHandle, Runnable destructor);
}
