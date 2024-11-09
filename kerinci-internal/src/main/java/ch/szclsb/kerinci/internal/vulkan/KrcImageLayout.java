package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.HasValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KrcImageLayout implements HasValue {
    UNDEFINED(0),
    GENERAL(1),
    COLOR_ATTACHMENT_OPTIMAL(2),
    DEPTH_STENCIL_ATTACHMENT_OPTIMAL(3),
    DEPTH_STENCIL_READ_ONLY_OPTIMAL(4),
    SHADER_READ_ONLY_OPTIMAL(5),
    TRANSFER_SRC_OPTIMAL(6),
    TRANSFER_DST_OPTIMAL(7),
    PREINITIALIZED(8),
    DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL(1000117000),
    DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL(1000117001),
    DEPTH_ATTACHMENT_OPTIMAL(1000241000),
    DEPTH_READ_ONLY_OPTIMAL(1000241001),
    STENCIL_ATTACHMENT_OPTIMAL(1000241002),
    STENCIL_READ_ONLY_OPTIMAL(1000241003),
    READ_ONLY_OPTIMAL(1000314000),
    ATTACHMENT_OPTIMAL(1000314001),
    PRESENT_SRC_KHR(1000001002),
    VIDEO_DECODE_DST_KHR(1000024000),
    VIDEO_DECODE_SRC_KHR(1000024001),
    VIDEO_DECODE_DPB_KHR(1000024002),
    SHARED_PRESENT_KHR(1000111000),
    FRAGMENT_DENSITY_MAP_OPTIMAL_EXT(1000218000),
    FRAGMENT_SHADING_RATE_ATTACHMENT_OPTIMAL_KHR(1000164003),
    ATTACHMENT_FEEDBACK_LOOP_OPTIMAL_EXT(1000339000),
    DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL_KHR(DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL.value),
    DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL_KHR(DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL.value),
    SHADING_RATE_OPTIMAL_NV(FRAGMENT_SHADING_RATE_ATTACHMENT_OPTIMAL_KHR.value),
    DEPTH_ATTACHMENT_OPTIMAL_KHR(DEPTH_ATTACHMENT_OPTIMAL.value),
    DEPTH_READ_ONLY_OPTIMAL_KHR(DEPTH_READ_ONLY_OPTIMAL.value),
    STENCIL_ATTACHMENT_OPTIMAL_KHR(STENCIL_ATTACHMENT_OPTIMAL.value),
    STENCIL_READ_ONLY_OPTIMAL_KHR(STENCIL_READ_ONLY_OPTIMAL.value),
    READ_ONLY_OPTIMAL_KHR(READ_ONLY_OPTIMAL.value),
    ATTACHMENT_OPTIMAL_KHR(ATTACHMENT_OPTIMAL.value),
    MAX_ENUM(0x7FFFFFFF);

    private final int value;
}