package ch.szclsb.kerinci.internal;

import static ch.szclsb.kerinci.api.api_h.*;

public class GlfwApi implements AutoCloseable {

    public GlfwApi() {
        initGlfw();
    }

    private void initGlfw() {
        krc_glfwInit();
        krc_glfwWindowHint(GLFW_CLIENT_API(), GLFW_NO_API());  // Disable OpenGl
        krc_glfwWindowHint(GLFW_RESIZABLE(), GLFW_TRUE());
    }

    public Window createWindow(VulkanApi vk, int width, int height, String name) {
        return new Window(vk, width, height, name);
    }

    public void pollEvents() {
        krc_glfwPollEvents();
    }

    @Override
    public void close() throws Exception {
        krc_glfwTerminate();
    }
}
