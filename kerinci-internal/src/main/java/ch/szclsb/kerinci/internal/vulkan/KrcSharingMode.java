package ch.szclsb.kerinci.internal.vulkan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KrcSharingMode {
    EXCLUSIVE(0),
    CONCURRENT(1),
    MAX_ENUM(0x7FFFFFFF);

    private final int value;
}
