cmake_minimum_required(VERSION 3.22)

set(CMAKE_CXX_STANDARD 23)
set(NAME Kerinci)

project(${NAME} VERSION 0.0.1)

set(GLFW_BUILD_DOCS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)

add_subdirectory(external/GLFW)
add_subdirectory(external/GLM)
add_subdirectory(external/tinyobjloader)

find_package(Vulkan REQUIRED)
if (NOT Vulkan_FOUND)
    message(FATAL_ERROR "Could not find vulkan lib")
endif()

add_library(${PROJECT_NAME} SHARED main.cpp)

target_include_directories(${PROJECT_NAME} PUBLIC
        ${Vulkan_INCLUDE_DIRS}
        external/GLFW/include
        external/GLM
        external/tinyobjloader
)
target_link_directories(${PROJECT_NAME} PUBLIC
        external/GLFW/src
)
target_link_libraries(${PROJECT_NAME}
        ${Vulkan_LIBRARIES}
        glfw
)
