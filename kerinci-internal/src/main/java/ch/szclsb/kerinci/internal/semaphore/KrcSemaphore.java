package ch.szclsb.kerinci.internal.semaphore;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.KrcDevice;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;

public class KrcSemaphore extends AbstractKrcHandle {
    public record CreateInfo(
            int flags  // future use
    ) {
    }

    protected KrcSemaphore(KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroySemaphore(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
