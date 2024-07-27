package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.graphic.VulkanApi;

import java.lang.foreign.Arena;

public class Example {
    public static void main(String[] args) {
        var api = new VulkanApi();

        try (var session = Arena.ofShared()) {

            api.glfwInit();
            var title = session.allocateUtf8String("Kerinci Example");
            var window = api.glfwCreateWindow(500, 350, title);

            while (!api.glfwShouldClose(window)) {
                api.glfwPollEvents();

                //todo
            }

            api.glfwDestroyWindow(window);
            api.glfwTerminate();
        }
    }
}
