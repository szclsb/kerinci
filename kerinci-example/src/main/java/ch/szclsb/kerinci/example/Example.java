package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.internal.GlfwApi;
import ch.szclsb.kerinci.internal.VulkanApi;

import java.lang.foreign.Arena;

import static ch.szclsb.kerinci.api.api_h.C_POINTER;

public class Example {
    public static void main(String[] args) {
        try (var arena = Arena.ofShared();
             var glfw = new GlfwApi(arena);
             var vk = new VulkanApi(arena, "Kerinci", glfw)) {

            try (var window = glfw.createWindow(500, 350, "Kerinci Example")) {
                //fixme pointer handling: vkGetInstanceProcAddr: Invalid instance [VUID-vkGetInstanceProcAddr-instance-parameter]
//                var pSurface = arena.allocate(C_POINTER);
//                window.createWindowSurface(vk.getInstance(), pSurface);
//                vk.setSurface(pSurface.get(C_POINTER, 0));

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
