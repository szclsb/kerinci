package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.vulkan.GlfwApi;
import ch.szclsb.kerinci.vulkan.VulkanApi;

import java.lang.foreign.Arena;

public class Example {
    public static void main(String[] args) {
        try (var arena = Arena.ofShared();
             var glfw = new GlfwApi(arena);  //fixme  unresolved symbol: glfwInit
             var vk = new VulkanApi(arena, "Kerinci", glfw)) {

            try (var window = glfw.createWindow(500, 350, "Kerinci Example")) {
                window.createWindowSurface(vk);

                while (!window.shouldClose()) {
                    glfw.pollEvents();

                    //todo game loop
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
