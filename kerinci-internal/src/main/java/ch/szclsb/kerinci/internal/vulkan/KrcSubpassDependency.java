package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkSubpassDependency;
import ch.szclsb.kerinci.internal.Allocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.foreign.MemorySegment;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KrcSubpassDependency extends AbstractStruct {
    private int dependencyFlags;
    private int srcSubpass;
    private int srcStageMask;
    private int srcAccessMask;
    private int dstSubpass;
    private int dstStageMask;
    private int dstAccessMask;

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkSubpassDependency.srcSubpass$set(pStruct, srcSubpass);
        VkSubpassDependency.dstSubpass$set(pStruct, dstSubpass);
        VkSubpassDependency.srcStageMask$set(pStruct, srcStageMask);
        VkSubpassDependency.dstStageMask$set(pStruct, dstStageMask);
        VkSubpassDependency.srcAccessMask$set(pStruct, srcAccessMask);
        VkSubpassDependency.dstAccessMask$set(pStruct, dstAccessMask);
        VkSubpassDependency.dependencyFlags$set(pStruct, dependencyFlags);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        srcSubpass = VkSubpassDependency.srcSubpass$get(pStruct);
        dstSubpass = VkSubpassDependency.dstSubpass$get(pStruct);
        srcStageMask = VkSubpassDependency.srcStageMask$get(pStruct);
        dstStageMask = VkSubpassDependency.dstStageMask$get(pStruct);
        srcAccessMask = VkSubpassDependency.srcAccessMask$get(pStruct);
        dstAccessMask = VkSubpassDependency.dstAccessMask$get(pStruct);
        dependencyFlags = VkSubpassDependency.dependencyFlags$get(pStruct);
    }
}
