package ch.szclsb.kerinci.internal.fence;

import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;

public class KrcFence extends AbstractKrcHandle {
    public enum CreateFlag implements Flag {
        CREATE_SIGNALED_BIT(1),
        CREATE_FLAG_BITS_MAX_ENUM(2147483647);

        private final int value;

        CreateFlag(final int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public record CreateInfo(
            CreateFlag ...flags
    ) {
    }

    protected KrcFence(final KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    public static void reset(KrcArrayExtended<KrcFence, KrcDevice> fences) {
        krc_vkResetFences(fences.getExtension().getLogical(), fences.length(), fences.getPointer());
    }

    public void reset() {
        krc_vkResetFences(device.getLogical(), 1, vkHandle);
    }

    public static void waitFor(KrcArrayExtended<KrcFence, KrcDevice> fences, boolean waitAll, long timeout) {
        krc_vkWaitForFences(fences.getExtension().getLogical(), fences.length(), fences.getPointer(),
                waitAll ? VK_TRUE() : VK_FALSE(), timeout);
    }

    public void waitFor(long timeout) {
        krc_vkWaitForFences(device.getLogical(), 1, vkHandle, VK_TRUE(), timeout);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyFence(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
