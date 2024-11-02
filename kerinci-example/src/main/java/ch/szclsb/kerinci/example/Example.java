package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.internal.*;

import static ch.szclsb.kerinci.api.api_h_3.VK_FENCE_CREATE_SIGNALED_BIT;

public class Example {
    public static void main(String[] args) {
        try (var glfw = new GlfwApi();
             var vk = new VulkanApi("Kerinci", true);
             var window = glfw.createWindow(vk, 500, 350, "Kerinci Example Window")) {
            var indices = vk.findQueueFamilies(window);
            try (var device = new Device(vk, indices);
                 var commandPool = new CommandPool(device, indices);
                 var fences = KrcFenceFactory.createFences(device, 2,
                         new KrcFenceCreateInfo(VK_FENCE_CREATE_SIGNALED_BIT()))) {

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
