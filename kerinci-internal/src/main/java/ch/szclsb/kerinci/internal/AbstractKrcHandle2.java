package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

public class AbstractKrcHandle2 implements AutoCloseable {
    private final KrcDevice device;
    private final MemorySegment vkHandle;
    private final Runnable destructor;

    public AbstractKrcHandle2(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
        this.device = device;
        this.vkHandle = vkHandle;
        this.destructor = destructor;
    }

    public KrcDevice getDevice() {
        return device;
    }

    public MemorySegment getVkHandle() {
        return vkHandle.asReadOnly();
    }

    @Override
    public void close() throws Exception {
        destructor.run();
    }
}
