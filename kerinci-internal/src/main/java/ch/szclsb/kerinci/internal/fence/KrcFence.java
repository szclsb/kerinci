package ch.szclsb.kerinci.internal.fence;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.HasValue;
import ch.szclsb.kerinci.internal.KrcArrayExtended;
import ch.szclsb.kerinci.internal.KrcDevice;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_3.VK_FENCE_CREATE_SIGNALED_BIT;
import static ch.szclsb.kerinci.api.api_h_6.*;

public class KrcFence extends AbstractKrcHandle {
    public enum Flag implements HasValue {
        CREATE_SIGNALED_BIT(VK_FENCE_CREATE_SIGNALED_BIT()),
        CREATE_FLAG_BITS_MAX_ENUM(VK_FENCE_CREATE_FLAG_BITS_MAX_ENUM());

        private final int value;

        Flag(final int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public record CreateInfo(
            int flags
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
