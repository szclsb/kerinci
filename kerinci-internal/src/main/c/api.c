#include "kerinci/api.h"

void krc_glfwInit() {
    glfwInit();
}

void krc_glfwTerminate() {
    glfwTerminate();
}

GLFWvkproc krc_glfwGetInstanceProcAddress(VkInstance instance, const char* procname) {
    return glfwGetInstanceProcAddress(instance, procname);
}

GLFWwindow* krc_glfwCreateWindow(int width, int height, const char* title, GLFWmonitor* monitor, GLFWwindow* share) {
    return glfwCreateWindow(width, height, title, monitor, share);
}

void krc_glfwDestroyWindow(GLFWwindow* handle) {
    glfwDestroyWindow(handle);
}

VkResult krc_glfwCreateWindowSurface(VkInstance instance, GLFWwindow* handle, const VkAllocationCallbacks* allocator, VkSurfaceKHR* pSurface) {
    glfwCreateWindowSurface(instance, handle, allocator, pSurface);
}

void krc_glfwWindowHint(int hint, int value) {
    glfwWindowHint(hint, value);
}

int krc_glfwWindowShouldClose(GLFWwindow* handle) {
    return glfwWindowShouldClose(handle);
}

void* krc_glfwGetWindowUserPointer(GLFWwindow* handle) {
    return glfwGetWindowUserPointer(handle);
}

void krc_glfwSetWindowUserPointer(GLFWwindow* handle, void* pointer) {
    return glfwSetWindowUserPointer(handle, pointer);
}

void krc_glfwSetFramebufferSizeCallback(GLFWwindow* handle, GLFWframebuffersizefun cbfun) {
    glfwSetFramebufferSizeCallback(handle, cbfun);
}

const char** krc_glfwGetRequiredInstanceExtensions(uint32_t* count) {
    return glfwGetRequiredInstanceExtensions(count);
}

void krc_glfwPollEvents() {
    glfwPollEvents();
}

void krc_glfwWaitEvents() {
    glfwWaitEvents();
}

int krc_glfwGetKey(GLFWwindow* handle, int key) {
    return glfwGetKey(handle, key);
}

int krc_glfwGetMouseButton(GLFWwindow* handle, int button) {
    return glfwGetMouseButton(handle, button);
}

VkResult krc_vkCreateInstance(const VkInstanceCreateInfo* pCreateInfo, const VkAllocationCallbacks* pAllocator, VkInstance* pInstance) {
    return vkCreateInstance(pCreateInfo, pAllocator, pInstance);
}

void krc_vkDestroyInstance(VkInstance instance, const VkAllocationCallbacks* pAllocator) {
    return vkDestroyInstance(instance, pAllocator);
}

void krc_vkDestroySurfaceKHR(VkInstance instance, VkSurfaceKHR surface, const VkAllocationCallbacks* pAllocator) {
    vkDestroySurfaceKHR(instance, surface, pAllocator);
}
