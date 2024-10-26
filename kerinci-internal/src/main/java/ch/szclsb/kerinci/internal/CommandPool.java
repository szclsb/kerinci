package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkCommandPoolCreateInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.api.api_h_1.*;
import static ch.szclsb.kerinci.api.api_h_3.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static ch.szclsb.kerinci.api.api_h_3.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkCreateCommandPool;
import static ch.szclsb.kerinci.api.api_h_6.krc_vkDestroyCommandPool;

public class CommandPool implements AutoCloseable {
    private final Arena arena;
    private final Device device;
    private final MemorySegment commandPool;

    public CommandPool(Device device, QueueFamilyIndices indices) {
        this.arena = Arena.ofConfined();
        this.device = device;
        this.commandPool = init(indices);
    }

    private MemorySegment init(QueueFamilyIndices indices) {
        var commandPoolCreateInfo = arena.allocate(VkCommandPoolCreateInfo.$LAYOUT());
        VkCommandPoolCreateInfo.sType$set(commandPoolCreateInfo, VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO());
        VkCommandPoolCreateInfo.queueFamilyIndex$set(commandPoolCreateInfo, indices.graphicsFamily);
        VkCommandPoolCreateInfo.flags$set(commandPoolCreateInfo, VK_COMMAND_POOL_CREATE_TRANSIENT_BIT() | VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT());

        var pCommandPool = arena.allocate(VkCommandPool);
        if (krc_vkCreateCommandPool(device.getLogical(), commandPoolCreateInfo, MemorySegment.NULL, pCommandPool) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create command pool");
        }
        return pCommandPool.get(VkCommandPool, 0);
    }

    protected MemorySegment getCommandPool() {
        return commandPool;
    }

    @Override
    public void close() throws Exception {
        krc_vkDestroyCommandPool(device.getLogical(), commandPool, MemorySegment.NULL);
        arena.close();
    }
}
