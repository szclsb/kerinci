package ch.szclsb.kerinci.internal.fence;

import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.KrcArray;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;

public class KrcFence implements AutoCloseable {
    public record CreateInfo(
            int flags
    ) {
    }

    private final KrcDevice device;
    private final MemorySegment vkFence;

    protected KrcFence(final KrcDevice device, MemorySegment vkFence) {
        this.device = device;
        this.vkFence = vkFence;
    }

    public static void reset(KrcArray<KrcFence, KrcDevice> fences) {
        krc_vkResetFences(fences.getAttachment().getLogical(), fences.length(), fences.getPointer());
    }

    public void reset() {
        krc_vkResetFences(device.getLogical(), 1, vkFence);
    }

    public static void waitFor(KrcArray<KrcFence, KrcDevice> fences, boolean waitAll, long timeout) {
        krc_vkWaitForFences(fences.getAttachment().getLogical(), fences.length(), fences.getPointer(),
                waitAll ? VK_TRUE() : VK_FALSE(), timeout);
    }

    public void waitFor(long timeout) {
        krc_vkWaitForFences(device.getLogical(), 1, vkFence, VK_TRUE(), timeout);
    }

    protected KrcDevice getDevice() {
        return device;
    }

    protected MemorySegment getVkFence() {
        return vkFence;
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyFence(device.getLogical(), vkFence, MemorySegment.NULL);
    }
}
