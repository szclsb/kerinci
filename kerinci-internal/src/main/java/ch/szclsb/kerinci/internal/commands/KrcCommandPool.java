package ch.szclsb.kerinci.internal.commands;

import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyCommandPool;

public class KrcCommandPool extends AbstractKrcHandle {
    public enum CreateFlag implements Flag {
        CREATE_TRANSIENT_BIT(1),
        CREATE_RESET_COMMAND_BUFFER_BIT(2);

        private final int value;
        CreateFlag(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public record CreateInfo(
            QueueFamilyIndices indices,
            CreateFlag ...flags
    ) {}

    protected KrcCommandPool(KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyCommandPool(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
