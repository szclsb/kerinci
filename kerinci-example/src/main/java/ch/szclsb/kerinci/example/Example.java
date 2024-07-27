package ch.szclsb.kerinci.example;

import ch.szclsb.kerinci.internal.GlfwApi;

import java.lang.foreign.Arena;

public class Example {
    public static void main(String[] args) {
        try (var session = Arena.ofShared(); var glfw = new GlfwApi()) {
            var title = session.allocateUtf8String("Kerinci Example");
            var window = glfw.createWindow(500, 350, title);

            while (!glfw.shouldClose(window)) {
                glfw.pollEvents();

                //todo game loop
            }

            glfw.destroyWindow(window);
        }
    }
}
