package ch.szclsb.kerinci.internal.extent;

import ch.szclsb.kerinci.api.VkExtent2D;
import ch.szclsb.kerinci.api.VkExtent3D;

import java.lang.foreign.MemorySegment;

public class KrcExtentFactory {
    private KrcExtentFactory() {
    }

    public static KrcExtent2D read2D(MemorySegment segment) {
        var width = VkExtent2D.width$get(segment);
        var height = VkExtent2D.height$get(segment);
        return new KrcExtent2D(width, height);
    }

    public static KrcExtent3D read3D(MemorySegment segment) {
        var width = VkExtent3D.width$get(segment);
        var height = VkExtent3D.height$get(segment);
        var depth = VkExtent3D.depth$get(segment);
        return new KrcExtent3D(width, height, depth);
    }

    public static void write2D(MemorySegment segment, KrcExtent2D extent2D) {
        VkExtent2D.width$set(segment, extent2D.width());
        VkExtent2D.height$set(segment, extent2D.height());
    }

    public static void write3D(MemorySegment segment, KrcExtent3D extent3D) {
        VkExtent3D.width$set(segment, extent3D.width());
        VkExtent3D.height$set(segment, extent3D.height());
        VkExtent3D.depth$set(segment, extent3D.depth());
    }
}
