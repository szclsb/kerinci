package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkComponentMapping;
import ch.szclsb.kerinci.internal.Allocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.foreign.MemorySegment;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KrcComponentMapping extends AbstractStruct {
    private int r;
    private int g;
    private int b;
    private int a;

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkComponentMapping.r$set(pStruct, r);
        VkComponentMapping.g$set(pStruct, g);
        VkComponentMapping.b$set(pStruct, b);
        VkComponentMapping.a$set(pStruct, a);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        this.r = VkComponentMapping.r$get(pStruct);
        this.g = VkComponentMapping.g$get(pStruct);
        this.b = VkComponentMapping.b$get(pStruct);
        this.a = VkComponentMapping.a$get(pStruct);
    }
}
