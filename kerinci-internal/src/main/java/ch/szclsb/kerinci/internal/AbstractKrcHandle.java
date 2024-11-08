package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

@Deprecated
public abstract class AbstractKrcHandle implements AutoCloseable {
    protected final KrcDevice device;
    protected final MemorySegment vkHandle;

    public AbstractKrcHandle(KrcDevice device, MemorySegment vkHandle) {
        this.device = device;
        this.vkHandle = vkHandle;
    }

    public KrcDevice getDevice() {
        return device;
    }

    public MemorySegment getVkHandle() {
        return vkHandle.asReadOnly();
    }
}
