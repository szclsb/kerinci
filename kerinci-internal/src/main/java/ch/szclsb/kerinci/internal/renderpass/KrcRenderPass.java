package ch.szclsb.kerinci.internal.renderpass;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.KrcDevice;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyRenderPass;

public class KrcRenderPass extends AbstractKrcHandle {
    public record SubpassDescription(
            int flag,
            int pipelineBindPoint,
            KrcAttachmentReference[] inputAttachments,
            KrcAttachmentReference[] colorAttachments,
//            KrcAttachmentReference[] resolveAttachment,
            KrcAttachmentReference depthStencilAttachment,
            Integer[] preserveAttachments
    ) {
    }

    public record SubpassDependency(
        int srcSubpass,
        int dstSubpass,
        int srcStageMask,
        int dstStageMask,
        int srcAccessMask,
        int dstAccessMask,
        int dependencyFlags
    ) {}

    public record CreateInfo(
            int flags,
            KrcAttachmentDescription[] attachments,
            SubpassDescription[] subpasses,
            SubpassDependency[] dependencies
    ) {}

    protected KrcRenderPass(final KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyRenderPass(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
