package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.internal.*;
import ch.szclsb.kerinci.internal.commands.KrcCommandPool;
import ch.szclsb.kerinci.internal.commands.KrcCommandPoolFactory;
import ch.szclsb.kerinci.internal.fence.KrcFence;
import ch.szclsb.kerinci.internal.fence.KrcFenceFactory;
import ch.szclsb.kerinci.internal.semaphore.KrcSemaphore;
import ch.szclsb.kerinci.internal.semaphore.KrcSemaphoreFactory;

import static ch.szclsb.kerinci.internal.Utils.or;


public class Example {
    public static void main(String[] args) {
        try (var glfw = new GlfwApi();
             var vk = new VulkanApi("Kerinci", true);
             var window = glfw.createWindow(vk, 500, 350, "Kerinci Example Window")) {
            var indices = vk.findQueueFamilies(window);
            try (var device = new KrcDevice(vk, indices);
                 var commandPool = KrcCommandPoolFactory.createCommandPool(device, new KrcCommandPool.CreateInfo(
                         indices, or(0,
                         KrcCommandPool.Flag.CREATE_RESET_COMMAND_BUFFER_BIT,
                         KrcCommandPool.Flag.CREATE_TRANSIENT_BIT
                 )));
                 var fences = KrcFenceFactory.createFences(device, 2, new KrcFence.CreateInfo(
                         or(0, KrcFence.Flag.CREATE_SIGNALED_BIT)
                 ));
                 var semaphores = KrcSemaphoreFactory.createSemaphores(device, 2,
                         new KrcSemaphore.CreateInfo(0))) {

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
