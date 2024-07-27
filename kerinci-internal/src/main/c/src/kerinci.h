#pragma once

#ifdef KERINCI_EXPORTS
#define KERINCI_API  __declspec(dllexport)   // export DLL information
#else
#define KERINCI_API  __declspec(dllimport)   // import DLL information
#endif

#define GLFW_INCLUDE_VULKAN
#include "GLFW/glfw3.h"
#include <vulkan/vulkan.h>
#include <string>

extern "C" {
    KERINCI_API bool kerinciVkCreateInstance(const char* appName, VkInstance* instance);

    KERINCI_API void kerinciVkDestroySurface(VkInstance* instance, VkSurfaceKHR *surface);
    KERINCI_API void kerinciVkDestroyInstance(VkInstance* instance);

    KERINCI_API void kerinciGlfwInit();
    KERINCI_API GLFWwindow* kerinciGlfwCreateWindow(int width, int height, const char* title);
    KERINCI_API bool kerinciGlfwCreateWindowSurface(VkInstance instance, GLFWwindow* window, VkSurfaceKHR *surface);
    KERINCI_API bool kerinciGlfwWindowShouldClose(GLFWwindow* window);
    KERINCI_API void kerinciGlfwPollEvents();
    KERINCI_API void kerinciGlfwDestroyWindow(GLFWwindow* window);
    KERINCI_API void kerinciGlfwTerminate();
}
