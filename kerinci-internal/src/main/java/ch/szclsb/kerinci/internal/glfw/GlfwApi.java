package ch.szclsb.kerinci.internal.glfw;

import ch.szclsb.kerinci.internal.vulkan.VulkanApi;

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

    public KrcWindow createWindow(VulkanApi vk, int width, int height, String name) {
        return new KrcWindow(vk, width, height, name);
    }

    public void pollEvents() {
        krc_glfwPollEvents();
    }

    @Override
    public void close() throws Exception {
        krc_glfwTerminate();
    }
}
