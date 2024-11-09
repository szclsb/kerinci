package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkExtent3D;
import ch.szclsb.kerinci.internal.Allocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.foreign.MemorySegment;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KrcExtent3D extends AbstractStruct {
    private int width;
    private int height;
    private int depth;

    public static KrcExtent3D cap(KrcExtent3D current, KrcExtent3D min, KrcExtent3D max) {
        return new KrcExtent3D(
                Integer.max(min.width, Integer.min(max.width, current.width)),
                Integer.max(min.height, Integer.min(max.height, current.height)),
                Integer.max(min.depth, Integer.min(max.depth, current.depth))
        );
    }

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkExtent3D.width$set(pStruct, width);
        VkExtent3D.height$set(pStruct, height);
        VkExtent3D.depth$set(pStruct, depth);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        this.width = VkExtent3D.width$get(pStruct);
        this.height = VkExtent3D.height$get(pStruct);
        this.depth = VkExtent3D.depth$get(pStruct);
    }
}
