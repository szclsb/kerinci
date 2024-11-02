package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.*;

public class KrcFence implements AutoCloseable {
    private final Device device;
    private final MemorySegment vkFence;

    protected KrcFence(final Device device, MemorySegment vkFence) {
        this.device = device;
        this.vkFence = vkFence;
    }

    public static void reset(KrcArray<KrcFence, Device> fences) {
        krc_vkResetFences(fences.getAttachment().getLogical(), fences.length(), fences.getPointer());
    }

    public void reset() {
        krc_vkResetFences(device.getLogical(), 1, vkFence);
    }

    public static void waitFor(KrcArray<KrcFence, Device> fences, boolean waitAll, long timeout) {
        krc_vkWaitForFences(fences.getAttachment().getLogical(), fences.length(), fences.getPointer(),
                waitAll ? VK_TRUE() : VK_FALSE(), timeout);
    }

    public void waitFor(long timeout) {
        krc_vkWaitForFences(device.getLogical(), 1, vkFence, VK_TRUE(), timeout);
    }

    protected Device getDevice() {
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
