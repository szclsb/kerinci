package ch.szclsb.kerinci.vulkan;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static ch.szclsb.kerinci.glfw.glfw3_h.*;

public class GlfwApi implements AutoCloseable {
    private final Arena arena;

    public GlfwApi(Arena arena) {
        this.arena = arena;
        initGlfw();
    }

    private void initGlfw() {
        glfwInit();
        glfwWindowHint(GLFW_CLIENT_API(), GLFW_NO_API());  // Disable OpenGl
        glfwWindowHint(GLFW_RESIZABLE(), GLFW_TRUE());
    }

    public Window createWindow(int width, int height, String name) {
        return new Window(width, height, name);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    @Override
    public void close() throws Exception {
        glfwTerminate();
    }
}
