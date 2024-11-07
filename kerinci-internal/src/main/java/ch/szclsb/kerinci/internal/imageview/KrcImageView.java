package ch.szclsb.kerinci.internal.imageview;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.KrcDevice;
import ch.szclsb.kerinci.internal.KrcFormat;
import ch.szclsb.kerinci.internal.KrcComponentMapping;
import ch.szclsb.kerinci.internal.images.KrcImage;
import ch.szclsb.kerinci.internal.images.KrcImageSubresourceRange;
import ch.szclsb.kerinci.internal.images.KrcImageType;

import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyImageView;

public class KrcImageView extends AbstractKrcHandle {
    public record CreateInfo(
            int flags,
            KrcImage image,
            KrcImageType viewType,
            KrcFormat format,
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
