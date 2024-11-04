package ch.szclsb.kerinci.internal.images;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.KrcDevice;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyImage;

public class KrcImage extends AbstractKrcHandle {
    protected KrcImage(KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyImage(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
