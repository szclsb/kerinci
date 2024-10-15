#pragma once

//#ifdef KERINCI_EXPORTS
#define KERINCI_API  __declspec(dllexport)   // export DLL information
//#else
//#define KERINCI_API  __declspec(dllimport)   // import DLL information
//#endif

#define GLFW_INCLUDE_VULKAN
#include "GLFW/glfw3.h"

KERINCI_API void krc_glfwInit();
KERINCI_API void krc_glfwTerminate();
KERINCI_API GLFWvkproc krc_glfwGetInstanceProcAddress(VkInstance instance, const char* procname);
KERINCI_API GLFWwindow* krc_glfwCreateWindow(int width, int height, const char* title, GLFWmonitor* monitor, GLFWwindow* share);
KERINCI_API void krc_glfwDestroyWindow(GLFWwindow* handle);
KERINCI_API VkResult krc_glfwCreateWindowSurface(VkInstance instance, GLFWwindow* handle, const VkAllocationCallbacks* allocator, VkSurfaceKHR* pSurface);
KERINCI_API void krc_glfwWindowHint(int hint, int value);
KERINCI_API int krc_glfwWindowShouldClose(GLFWwindow* handle);
KERINCI_API void* krc_glfwGetWindowUserPointer(GLFWwindow* handle);
KERINCI_API void krc_glfwSetWindowUserPointer(GLFWwindow* handle, void* pointer);
KERINCI_API void krc_glfwSetFramebufferSizeCallback(GLFWwindow* handle, GLFWframebuffersizefun cbfun);
KERINCI_API const char** krc_glfwGetRequiredInstanceExtensions(uint32_t* count);
KERINCI_API void krc_glfwPollEvents();
KERINCI_API void krc_glfwWaitEvents();
KERINCI_API int krc_glfwGetKey(GLFWwindow* handle, int key);
KERINCI_API int krc_glfwGetMouseButton(GLFWwindow* handle, int button);

KERINCI_API VkResult krc_vkCreateInstance(const VkInstanceCreateInfo* pCreateInfo, const VkAllocationCallbacks* pAllocator, VkInstance* pInstance);
KERINCI_API void krc_vkDestroyInstance(VkInstance instance, const VkAllocationCallbacks* pAllocator);

KERINCI_API void krc_vkDestroySurfaceKHR(VkInstance instance, VkSurfaceKHR surface, const VkAllocationCallbacks* pAllocator);

//        KERINCI_API PFN_vkVoidFunction vkGetInstanceProcAddr(VkInstance instance, const char* pName);
//        KERINCI_API void vkEnumerateInstanceLayerProperties();
//        KERINCI_API void vkEnumerateInstanceExtensionProperties();
//
//        KERINCI_API void vkEnumeratePhysicalDevices();
//        KERINCI_API void vkGetPhysicalDeviceProperties();
//        KERINCI_API void vkGetPhysicalDeviceFormatProperties();
//        KERINCI_API void vkGetPhysicalDeviceMemoryProperties();
//        KERINCI_API void vkGetPhysicalDeviceQueueFamilyProperties();
//        KERINCI_API void vkGetPhysicalDeviceSurfaceSupportKHR();
//        KERINCI_API void vkGetPhysicalDeviceSurfaceCapabilitiesKHR();
//        KERINCI_API void vkGetPhysicalDeviceSurfaceFormatsKHR();
//        KERINCI_API void vkGetPhysicalDeviceSurfacePresentModesKHR();
//
//        KERINCI_API void vkCreateDevice();
//        KERINCI_API void vkGetDeviceQueue();
//        KERINCI_API void vkDeviceWaitIdle();
//        KERINCI_API void vkEnumerateDeviceExtensionProperties();
//
//        KERINCI_API VkResult vkAllocateMemory(VkDevice device, const VkMemoryAllocateInfo* pAllocateInfo, const VkAllocationCallbacks* pAllocator, VkDeviceMemory* pMemory);
//        KERINCI_API void vkFreeMemory(VkDevice device, VkDeviceMemory memory, const VkAllocationCallbacks* pAllocator);
//        KERINCI_API void vkMapMemory(VkDevice device, VkDeviceMemory memory, VkDeviceSize offset, VkDeviceSize size, VkMemoryMapFlags flags, void** ppData);
//        KERINCI_API void vkUnmapMemory(VkDevice device, VkDeviceMemory memory);
//
//        KERINCI_API void vkCreateCommandPool();
//        KERINCI_API void vkAllocateCommandBuffers();
//        KERINCI_API void vkFreeCommandBuffers();
//        KERINCI_API void vkBeginCommandBuffer();
//        KERINCI_API void vkEndCommandBuffer();
//
//        KERINCI_API void vkCmdBeginRenderPass();
//        KERINCI_API void vkCmdEndRenderPass();
//        KERINCI_API void vkCmdSetViewport();
//        KERINCI_API void vkCmdSetScissor();
//
//        KERINCI_API void vkQueueSubmit();
//        KERINCI_API void vkQueueWaitIdle();
//
//        KERINCI_API void vkCreateBuffer();
//        KERINCI_API void vkBindBufferMemory();
//        KERINCI_API void vkDestroyBuffer();
//
//        KERINCI_API void vkCreateImage();
//        KERINCI_API void vkCmdCopyBufferToImage();
//        KERINCI_API void vkBindImageMemory();
//        KERINCI_API void vkGetImageMemoryRequirements();
