package ch.szclsb.kerinci.internal.commands;

import ch.szclsb.kerinci.api.VkCommandPoolCreateInfo;
import ch.szclsb.kerinci.internal.KrcDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_6.*;
import static ch.szclsb.kerinci.internal.Utils.or;
import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcCommandPoolFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcCommandPoolFactory.class);

    private static MemorySegment allocateCreateInfo(Arena arena) {
        var createInfoSegment = arena.allocate(VkCommandPoolCreateInfo.$LAYOUT());
        VkCommandPoolCreateInfo.sType$set(createInfoSegment, VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO());
        return createInfoSegment;
    }

    private static KrcCommandPool create(KrcDevice device, MemorySegment pCreateInfoSegment, MemorySegment pCommandPool) {
        if (krc_vkCreateCommandPool(device.getLogical(), pCreateInfoSegment, MemorySegment.NULL, pCommandPool) != VK_SUCCESS()) {
            throw new RuntimeException("failed to create command pool");
        }
        var commandPool = pCommandPool.get(VkCommandPool, 0);
        logger.debug("Created command pool {}", printAddress(commandPool));
        return new KrcCommandPool(device, commandPool);
    }

    public static KrcCommandPool createCommandPool(KrcDevice device, KrcCommandPool.CreateInfo commandPoolCreateInfo) {
        try (var arena = Arena.ofConfined()) {
            var createInfoSegment = allocateCreateInfo(arena);
            VkCommandPoolCreateInfo.queueFamilyIndex$set(createInfoSegment, commandPoolCreateInfo.indices().getGraphicsFamily());
            VkCommandPoolCreateInfo.flags$set(createInfoSegment, or(commandPoolCreateInfo.flags()));
            var handle = arena.allocate(VkCommandPool);
            return create(device, createInfoSegment, handle);
        }
    }
}
