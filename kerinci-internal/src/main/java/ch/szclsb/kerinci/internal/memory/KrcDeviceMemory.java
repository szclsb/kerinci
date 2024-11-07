package ch.szclsb.kerinci.internal.memory;

import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkFreeMemory;

public class KrcDeviceMemory extends AbstractKrcHandle {
    public record AllocateInfo(
            long allocationSize,
            int memoryTypeIndex
    ) {
    }

    KrcDeviceMemory(KrcDevice device, MemorySegment memorySegment) {
        super(device, memorySegment);
    }

    @Override
    public void close() throws Exception {
        krc_vkFreeMemory(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
