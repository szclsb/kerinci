package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkExtent2D;
import ch.szclsb.kerinci.internal.Allocator;

import java.lang.foreign.MemorySegment;

public class KrcExtent2D extends AbstractStruct {
    private int width;
    private int height;

    public KrcExtent2D() {
    }

    public KrcExtent2D (int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static KrcExtent2D cap(KrcExtent2D current, KrcExtent2D min, KrcExtent2D max) {
        return new KrcExtent2D(
                Integer.max(min.width, Integer.min(max.width, current.width)),
                Integer.max(min.height, Integer.min(max.height, current.height))
        );
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkExtent2D.width$set(pStruct, width);
        VkExtent2D.height$set(pStruct, height);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        this.width = VkExtent2D.width$get(pStruct);
        this.height = VkExtent2D.height$get(pStruct);
    }
}
