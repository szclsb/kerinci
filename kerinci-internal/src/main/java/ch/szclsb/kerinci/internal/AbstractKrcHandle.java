package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.internal.vulkan.KrcDevice;

import java.lang.foreign.MemorySegment;

public class AbstractKrcHandle implements AutoCloseable {
    private final KrcDevice device;
    private final MemorySegment vkHandle;
    private final Runnable destructor;

    public AbstractKrcHandle(KrcDevice device, MemorySegment vkHandle, Runnable destructor) {
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
