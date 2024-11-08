package ch.szclsb.kerinci.internal.renderpass;

public record KrcSubpassDescription(
        int flag,
        int pipelineBindPoint,
        KrcAttachmentReference[] inputAttachments,
        KrcAttachmentReference[] colorAttachments,
//            KrcAttachmentReference[] resolveAttachment,
        KrcAttachmentReference depthStencilAttachment,
        Integer[] preserveAttachments
) {
}
