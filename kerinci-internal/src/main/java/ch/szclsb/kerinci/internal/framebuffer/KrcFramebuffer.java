package ch.szclsb.kerinci.internal.framebuffer;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.KrcArray;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.images.KrcImageView;
import ch.szclsb.kerinci.internal.renderpass.KrcRenderPass;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyFramebuffer;

public class KrcFramebuffer extends AbstractKrcHandle {
    public record CreateInfo(
            int flags,
            KrcRenderPass renderPass,
            KrcArray<KrcImageView> attachments,
            int width,
            int height,
            int layers
    ) {}

    protected KrcFramebuffer(KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyFramebuffer(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
