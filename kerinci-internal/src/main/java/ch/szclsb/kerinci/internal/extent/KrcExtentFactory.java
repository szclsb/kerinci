package ch.szclsb.kerinci.internal.extent;

import ch.szclsb.kerinci.api.VkExtent2D;

import java.lang.foreign.MemorySegment;

public class KrcExtentFactory {
    private KrcExtentFactory() {
    }

    public static KrcExtent2D read2D(MemorySegment segment) {
        var width = VkExtent2D.width$get(segment);
        var height = VkExtent2D.height$get(segment);
        return new KrcExtent2D(width, height);
    }

    public static void write2D(KrcExtent2D extent2D, MemorySegment segment) {
        VkExtent2D.width$set(segment, extent2D.width());
        VkExtent2D.height$set(segment, extent2D.height());
    }
}
