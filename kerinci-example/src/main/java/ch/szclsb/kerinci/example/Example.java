package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.internal.GlfwApi;
import ch.szclsb.kerinci.internal.VulkanApi;

import java.lang.foreign.Arena;

public class Example {
    public static void main(String[] args) {
        try (var arena = Arena.ofShared();
             var glfw = new GlfwApi(arena);
             var vk = new VulkanApi(arena, "Kerinci", glfw)) {

            try (var window = glfw.createWindow(500, 350, "Kerinci Example")) {
                //fixme pointer handling: vkGetInstanceProcAddr: Invalid instance [VUID-vkGetInstanceProcAddr-instance-parameter]
                window.createWindowSurface(vk.getInstance(), vk.getSurface());

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
