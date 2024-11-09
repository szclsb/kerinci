package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.HasValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KrcImageTiling implements HasValue {
    OPTIMAL(0),
    LINEAR(1),
    DRM_FORMAT_MODIFIER_EXT(1000158000),
    MAX_ENUM(0x7FFFFFFF);

    private final int value;
}

