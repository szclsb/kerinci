package ch.szclsb.kerinci.internal.vulkan;

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
}
