package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.internal.*;

import java.lang.foreign.Arena;


public class Example {
    public static void main(String[] args) {
        try (var arena = Arena.ofShared();
             var glfw = new GlfwApi();
             var vk = new VulkanApi("Kerinci", true);
             var window = glfw.createWindow(vk, 500, 350, "Kerinci Example Window")) {
            var indices = vk.findQueueFamilies(window);
            try (var device = new KrcDevice(vk, indices);
                var swapchain = new Swapchain(device, window, indices)
            ) {
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
