package ch.szclsb.kerinci.vulkan;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.glfw.glfw3_h.*;

public class Window implements AutoCloseable {
    private final MemorySegment pointer;

    Window(int width, int height, String name) {
        try (var localArena = Arena.ofConfined()) {
            var windowName = localArena.allocateUtf8String(name);

            this.pointer = glfwCreateWindow(width, height, windowName, MemorySegment.NULL, MemorySegment.NULL);
        }
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(pointer) == GLFW_TRUE();
    }

    public void createWindowSurface(VulkanApi vk) {
        if(glfwCreateWindowSurface(vk.getInstance(), pointer, MemorySegment.NULL, vk.getSurface()) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create window surface");
        }
    }

    //todo resizing

    @Override
    public void close() throws Exception {
        glfwDestroyWindow(pointer);
    }
}
