#include "kerinci.h"
#include <string>

void kerinciGlfwInit() {
        glfwInit();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);  // Disable OpenGL
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
}

GLFWwindow* kerinciGlfwCreateWindow(int width, int height, const char* title) {
    return glfwCreateWindow(width, height, title, nullptr, nullptr);
}

bool kerinciGlfwCreateWindowSurface(VkInstance instance, GLFWwindow* window, VkSurfaceKHR *surface) {
    return glfwCreateWindowSurface(instance, window, nullptr, surface) == VK_SUCCESS;
}

bool kerinciGlfwWindowShouldClose(GLFWwindow* window) {
    return glfwWindowShouldClose(window);
}

void kerinciGlfwPollEvents() {
    glfwPollEvents();
}

void kerinciGlfwDestroyWindow(GLFWwindow* window) {
    glfwDestroyWindow(window);
}

void kerinciGlfwTerminate() {
    glfwTerminate();
}

