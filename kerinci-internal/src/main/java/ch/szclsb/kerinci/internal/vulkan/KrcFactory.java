package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.*;
import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.Allocator;
import ch.szclsb.kerinci.internal.KrcArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static ch.szclsb.kerinci.internal.Utils.*;
import static ch.szclsb.kerinci.internal.Utils.writeArrayPointer;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class KrcFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcFactory.class);

    private static <T extends AbstractKrcHandle> T construct(KrcDevice device, AbstractCreateInfo<T> createInfo, MemorySegment pCreateInfo, MemorySegment pHandle) {
        if (!createInfo.create(device, pCreateInfo, pHandle)) {
            throw new RuntimeException(STR."Failed to create \{createInfo.gettClass().getSimpleName()}");
        }
        var vkHandle = pHandle.get(createInfo.getLayout(), 0);
        logger.debug(STR."Created \{createInfo.gettClass().getSimpleName()} at \{printAddress(vkHandle)}");
        Runnable destructor = () -> {
            createInfo.destroy(device, vkHandle);
            logger.debug(STR."Destroyed \{createInfo.gettClass().getSimpleName()} at \{printAddress(vkHandle)}");
        };
        return createInfo.getConstructor().apply(device, vkHandle, destructor);
    }

    public static <T extends AbstractKrcHandle> T create(KrcDevice device, AbstractCreateInfo<T> createInfo) {
        if (device == null || createInfo == null) {
            throw new IllegalArgumentException("arguments contain null");
        }

        try (var arena = Arena.ofConfined()) {
            var pCreateInfo = createInfo.allocateCreateInfo(arena::allocate);
            createInfo.writeCreateInfo(pCreateInfo, arena::allocate);
            var pHandle = arena.allocate(createInfo.getLayout());
            return construct(device, createInfo, pCreateInfo, pHandle);
        }
    }

    public static <T extends AbstractKrcHandle> KrcArray<T> createArray(Allocator arrayAllocator, KrcDevice device, int count, AbstractCreateInfo<T> createInfo) {
        if (arrayAllocator == null || device == null || count <= 0 || createInfo == null) {
            throw new IllegalArgumentException("arguments contain null or non positive sizes");
        }

        try (var arena = Arena.ofConfined()) {
            var pCreateInfo = createInfo.allocateCreateInfo(arena::allocate);
            createInfo.writeCreateInfo(pCreateInfo, arena::allocate);
            var pArray = arrayAllocator.apply(MemoryLayout.sequenceLayout(count, createInfo.getLayout()));
            var data = (T[]) new AbstractKrcHandle[count];
            forEachSlice(createInfo.getLayout(), pArray, (slice, i) ->
                    data[i] = construct(device, createInfo, pCreateInfo, slice));
            return new KrcArray<>(pArray.asReadOnly(), data);
        }
    }

    public static <T extends AbstractKrcHandle> KrcArray<T> createArray(Allocator arrayAllocator, KrcDevice device, List<? extends AbstractCreateInfo<T>> createInfos) {
        if (arrayAllocator == null || device == null || createInfos == null || createInfos.isEmpty()) {
            throw new IllegalArgumentException("arguments contain null or non positive sizes");
        }
        // todo check same layout in create Array

        try (var arena = Arena.ofConfined()) {
            var first = createInfos.getFirst();
            var layout = first.getLayout();
            var pCreateInfo = first.allocateCreateInfo(arena::allocate);
            var pArray = arrayAllocator.apply(MemoryLayout.sequenceLayout(createInfos.size(), layout));
            var data = (T[]) new AbstractKrcHandle[createInfos.size()];
            forEachSlice(layout, pArray, (slice, i) -> {
                var createInfo = createInfos.get(i);
                createInfo.writeCreateInfo(pCreateInfo, arena::allocate);  // todo improve additional allocations
                data[i] = construct(device, createInfo, pCreateInfo, slice);
            });
            return new KrcArray<>(pArray.asReadOnly(), data);
        }
    }

    public static void setComponentMapping(MemorySegment segment, KrcComponentMapping componentMapping) {
        VkComponentMapping.r$set(segment, componentMapping.r());
        VkComponentMapping.g$set(segment, componentMapping.g());
        VkComponentMapping.b$set(segment, componentMapping.b());
        VkComponentMapping.a$set(segment, componentMapping.a());
    }

    public static void setSubresourceRange(MemorySegment segment, KrcImageSubresourceRange imageSubresourceRange) {
        VkImageSubresourceRange.aspectMask$set(segment, imageSubresourceRange.aspectMask());
        VkImageSubresourceRange.baseMipLevel$set(segment, imageSubresourceRange.baseMipLevel());
        VkImageSubresourceRange.levelCount$set(segment, imageSubresourceRange.levelCount());
        VkImageSubresourceRange.baseArrayLayer$set(segment, imageSubresourceRange.baseArrayLayer());
        VkImageSubresourceRange.layerCount$set(segment, imageSubresourceRange.layerCount());
    }

    public static void setSubpassDescription(Allocator allocator, MemorySegment segment, KrcSubpassDescription subpassDescription) {
        VkSubpassDescription.flags$set(segment, subpassDescription.flag());
        VkSubpassDescription.pipelineBindPoint$set(segment, subpassDescription.pipelineBindPoint());
        writeArrayPointer(subpassDescription.inputAttachments(),
                VkAttachmentReference.$LAYOUT(), segment, allocator,
                KrcFactory::setAttachmentReference,
                VkSubpassDescription::inputAttachmentCount$set,
                VkSubpassDescription::pInputAttachments$set);
        writeArrayPointer(subpassDescription.colorAttachments(),
                VkAttachmentReference.$LAYOUT(), segment, allocator,
                KrcFactory::setAttachmentReference,
                VkSubpassDescription::colorAttachmentCount$set,
                VkSubpassDescription::pColorAttachments$set);
        // resolveAttachment? Uses colorAttachmentCount as well -> subTypes?
        writePointer(subpassDescription.depthStencilAttachment(),
                VkAttachmentReference.$LAYOUT(), segment, allocator,
                KrcFactory::setAttachmentReference,
                VkSubpassDescription::pDepthStencilAttachment$set);
        writeArrayPointer(subpassDescription.preserveAttachments(),
                JAVA_INT, segment, allocator,
                (slice, i) -> slice.set(JAVA_INT, 0, i),
                VkSubpassDescription::preserveAttachmentCount$set,
                VkSubpassDescription::pPreserveAttachments$set);
    }

    public static void setSubpassDependency(MemorySegment segment, KrcSubpassDependency subpassDependency) {
        VkSubpassDependency.srcSubpass$set(segment, subpassDependency.srcSubpass());
        VkSubpassDependency.dstSubpass$set(segment, subpassDependency.dstSubpass());
        VkSubpassDependency.srcStageMask$set(segment, subpassDependency.srcStageMask());
        VkSubpassDependency.dstStageMask$set(segment, subpassDependency.dstStageMask());
        VkSubpassDependency.srcAccessMask$set(segment, subpassDependency.srcAccessMask());
        VkSubpassDependency.dstAccessMask$set(segment, subpassDependency.dstAccessMask());
        VkSubpassDependency.dependencyFlags$set(segment, subpassDependency.dependencyFlags());
    }

    public static void setAttachmentReference(MemorySegment segment, KrcAttachmentReference attachmentReference) {
        VkAttachmentReference.attachment$set(segment, attachmentReference.attachment());
        VkAttachmentReference.layout$set(segment, attachmentReference.layout());
    }
}
