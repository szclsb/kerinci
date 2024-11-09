package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkAttachmentDescription;
import ch.szclsb.kerinci.internal.Allocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.internal.Utils.from;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KrcAttachmentDescription extends AbstractStruct {
    private int flags;
    private KrcFormat format;
    private int samples;
    private int loadOp;
    private int storeOp;
    private int stencilLoadOp;
    private int stencilStoreOp;
    private int initialLayout;
    private int finalLayout;

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkAttachmentDescription.flags$set(pStruct, flags);
        VkAttachmentDescription.format$set(pStruct, format.getValue());
        VkAttachmentDescription.samples$set(pStruct, samples);
        VkAttachmentDescription.loadOp$set(pStruct, loadOp);
        VkAttachmentDescription.storeOp$set(pStruct, storeOp);
        VkAttachmentDescription.stencilLoadOp$set(pStruct, stencilLoadOp);
        VkAttachmentDescription.stencilStoreOp$set(pStruct, stencilStoreOp);
        VkAttachmentDescription.initialLayout$set(pStruct, initialLayout);
        VkAttachmentDescription.finalLayout$set(pStruct, finalLayout);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        flags = VkAttachmentDescription.flags$get(pStruct);
        format = from(VkAttachmentDescription.format$get(pStruct), KrcFormat.class);
        samples = VkAttachmentDescription.samples$get(pStruct);
        loadOp = VkAttachmentDescription.loadOp$get(pStruct);
        storeOp = VkAttachmentDescription.storeOp$get(pStruct);
        stencilLoadOp = VkAttachmentDescription.stencilLoadOp$get(pStruct);
        stencilStoreOp = VkAttachmentDescription.stencilStoreOp$get(pStruct);
        initialLayout = VkAttachmentDescription.initialLayout$get(pStruct);
        finalLayout = VkAttachmentDescription.finalLayout$get(pStruct);
    }
}
