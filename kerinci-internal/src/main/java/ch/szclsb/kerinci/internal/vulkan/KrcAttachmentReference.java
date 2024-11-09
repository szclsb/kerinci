package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkAttachmentReference;
import ch.szclsb.kerinci.internal.Allocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.foreign.MemorySegment;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KrcAttachmentReference extends AbstractStruct {
    private int attachment;
    private int layout;

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkAttachmentReference.attachment$set(pStruct, attachment);
        VkAttachmentReference.layout$set(pStruct, layout);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        attachment = VkAttachmentReference.attachment$get(pStruct);
        layout = VkAttachmentReference.layout$get(pStruct);
    }
}
