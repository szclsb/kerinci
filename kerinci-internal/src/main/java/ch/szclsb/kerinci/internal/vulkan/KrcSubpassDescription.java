package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkAttachmentReference;
import ch.szclsb.kerinci.api.VkSubpassDescription;
import ch.szclsb.kerinci.internal.Allocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.internal.Utils.writeArrayPointer;
import static ch.szclsb.kerinci.internal.Utils.writePointer;
import static java.lang.foreign.ValueLayout.JAVA_INT;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KrcSubpassDescription  extends AbstractStruct {
    private int flags;
    private int pipelineBindPoint;
    private KrcAttachmentReference[] inputAttachments;
    private KrcAttachmentReference[] colorAttachments;
    //            KrcAttachmentReference[] resolveAttachment,
    private KrcAttachmentReference depthStencilAttachment;
    private Integer[] preserveAttachments;

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkSubpassDescription.flags$set(pStruct, flags);
        VkSubpassDescription.pipelineBindPoint$set(pStruct, pipelineBindPoint);
        writeArrayPointer(inputAttachments,
                VkAttachmentReference.$LAYOUT(), pStruct, additional,
                (pSlice, attachment) -> attachment.write(pSlice, additional),
                VkSubpassDescription::inputAttachmentCount$set,
                VkSubpassDescription::pInputAttachments$set);
        writeArrayPointer(colorAttachments,
                VkAttachmentReference.$LAYOUT(), pStruct, additional,
                (pSlice, attachment) -> attachment.write(pSlice, additional),
                VkSubpassDescription::colorAttachmentCount$set,
                VkSubpassDescription::pColorAttachments$set);
        // resolveAttachment? Uses colorAttachmentCount as well -> subTypes?
        writePointer(depthStencilAttachment,
                VkAttachmentReference.$LAYOUT(), pStruct, additional,
                (pSlice, attachment) -> attachment.write(pSlice, additional),
                VkSubpassDescription::pDepthStencilAttachment$set);
        writeArrayPointer(preserveAttachments,
                JAVA_INT, pStruct, additional,
                (slice, i) -> slice.set(JAVA_INT, 0, i),
                VkSubpassDescription::preserveAttachmentCount$set,
                VkSubpassDescription::pPreserveAttachments$set);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        flags = VkSubpassDescription.flags$get(pStruct);
        pipelineBindPoint = VkSubpassDescription.pipelineBindPoint$get(pStruct);
        // todo arrays
    }
}
