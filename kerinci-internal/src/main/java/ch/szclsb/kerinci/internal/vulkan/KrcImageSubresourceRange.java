package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkImageSubresourceRange;
import ch.szclsb.kerinci.internal.Allocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.foreign.MemorySegment;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KrcImageSubresourceRange extends AbstractStruct {
    private int aspectMask;
    private int baseMipLevel;
    private int levelCount;
    private int baseArrayLayer;
    private int layerCount;

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkImageSubresourceRange.aspectMask$set(pStruct, aspectMask);
        VkImageSubresourceRange.baseMipLevel$set(pStruct, baseMipLevel);
        VkImageSubresourceRange.levelCount$set(pStruct, levelCount);
        VkImageSubresourceRange.baseArrayLayer$set(pStruct, baseArrayLayer);
        VkImageSubresourceRange.layerCount$set(pStruct, layerCount);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        this.aspectMask = VkImageSubresourceRange.aspectMask$get(pStruct);
        this.baseMipLevel = VkImageSubresourceRange.baseMipLevel$get(pStruct);
        this.levelCount = VkImageSubresourceRange.levelCount$get(pStruct);
        this.baseArrayLayer = VkImageSubresourceRange.baseArrayLayer$get(pStruct);
        this.layerCount = VkImageSubresourceRange.layerCount$get(pStruct);
    }
}
