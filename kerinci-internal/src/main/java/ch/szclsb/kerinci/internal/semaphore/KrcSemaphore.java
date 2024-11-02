package ch.szclsb.kerinci.internal.semaphore;

import ch.szclsb.kerinci.internal.KrcDevice;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;

public class KrcSemaphore implements AutoCloseable {
    public record CreateInfo(
            int flags
    ) {
    }

    private final KrcDevice device;
    private final MemorySegment vkSemaphore;

    protected KrcSemaphore(final KrcDevice device, MemorySegment vkSemaphore) {
        this.device = device;
        this.vkSemaphore = vkSemaphore;
    }

    protected KrcDevice getDevice() {
        return device;
    }

    protected MemorySegment getVkSemaphore() {
        return vkSemaphore;
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroySemaphore(device.getLogical(), vkSemaphore, MemorySegment.NULL);
    }
}
