package ch.szclsb.kerinci.internal.commands;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.HasValue;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.QueueFamilyIndices;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_3.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static ch.szclsb.kerinci.api.api_h_3.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyCommandPool;

public class KrcCommandPool extends AbstractKrcHandle {
    public enum Flag implements HasValue {
        CREATE_TRANSIENT_BIT(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT()),
        CREATE_RESET_COMMAND_BUFFER_BIT(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT());

        private final int value;
        Flag(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public record CreateInfo(
            QueueFamilyIndices indices,
            Flag ...flags
    ) {}

    protected KrcCommandPool(KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyCommandPool(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
