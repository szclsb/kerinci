package ch.szclsb.kerinci.internal.images;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.HasValue;
import ch.szclsb.kerinci.internal.KrcDevice;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_2.*;
import static ch.szclsb.kerinci.api.api_h_3.VK_IMAGE_VIEW_TYPE_CUBE_ARRAY;
import static ch.szclsb.kerinci.api.api_h_3.VK_IMAGE_VIEW_TYPE_MAX_ENUM;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyImageView;

public class KrcImageView extends AbstractKrcHandle {
    enum Type implements HasValue {
        TYPE_1D(VK_IMAGE_VIEW_TYPE_1D()),
        TYPE_2D(VK_IMAGE_VIEW_TYPE_2D()),
        TYPE_3D(VK_IMAGE_VIEW_TYPE_3D()),
        TYPE_CUBE(VK_IMAGE_VIEW_TYPE_CUBE()),
        TYPE_1D_ARRAY(VK_IMAGE_VIEW_TYPE_1D_ARRAY()),
        TYPE_2D_ARRAY(VK_IMAGE_VIEW_TYPE_2D_ARRAY()),
        TYPE_CUBE_ARRAY(VK_IMAGE_VIEW_TYPE_CUBE_ARRAY()),
        TYPE_MAX_ENUM(VK_IMAGE_VIEW_TYPE_MAX_ENUM());

        private final int value;

        Type(final int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public record CreateInfo(
            int flags,
            KrcImage image,
            Type viewType,
            int format,
            KrcComponentMapping componentMapping,
            KrcImageSubresourceRange subresourceRange
    ) {
    }

    protected KrcImageView(final KrcDevice device, MemorySegment vkHandle) {
        super(device, vkHandle);
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyImageView(device.getLogical(), vkHandle, MemorySegment.NULL);
    }
}
