package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkInstanceCreateInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static ch.szclsb.kerinci.api.api_h.*;

public class Window implements AutoCloseable {
    private final MemorySegment pointer;

    Window(int width, int height, String name) {
        try (var localArena = Arena.ofConfined()) {
            var windowName = localArena.allocateUtf8String(name);

            this.pointer = krc_glfwCreateWindow(width, height, windowName, MemorySegment.NULL, MemorySegment.NULL);
        }
    }

    public boolean shouldClose() {
        return krc_glfwWindowShouldClose(pointer) == GLFW_TRUE();
    }

    public void createWindowSurface(MemorySegment vkInstance, MemorySegment pVkSurfaceKHR) {
        if(krc_glfwCreateWindowSurface(vkInstance, pointer, MemorySegment.NULL, pVkSurfaceKHR) != VK_SUCCESS()) {
            throw new RuntimeException("Failed to create window surface");
        }
    }

    //todo resizing

    @Override
    public void close() throws Exception {
        krc_glfwDestroyWindow(pointer);
    }
}
