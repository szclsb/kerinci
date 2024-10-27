package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.internal.CommandPool;
import ch.szclsb.kerinci.internal.Device;
import ch.szclsb.kerinci.internal.GlfwApi;
import ch.szclsb.kerinci.internal.VulkanApi;

public class Example {
    public static void main(String[] args) {
        try (var glfw = new GlfwApi();
             var vk = new VulkanApi("Kerinci", true);
             var window = glfw.createWindow(vk, 500, 350, "Kerinci Example Window")) {
            var indices = vk.findQueueFamilies(window);
            try (var device = new Device(vk, indices);
                 var commandPool = new CommandPool(device, indices)) {

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
