cmake_minimum_required(VERSION 3.22)

set(CMAKE_CXX_STANDARD 23)
set(NAME Kerinci)

project(${NAME} VERSION 0.0.1)

set(NAME_VULKAN ${NAME}Vulkan)
set(NAME_GLFW ${NAME}Glfw)
set(NAME_GLM ${NAME}Glm)

set(GLFW_BUILD_DOCS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)

add_subdirectory(external/GLFW)
add_subdirectory(external/GLM)

find_package(Vulkan REQUIRED)
if (NOT Vulkan_FOUND)
    message(FATAL_ERROR "Could not find vulkan lib")
endif()

# Vulkan
add_library(${NAME_VULKAN} SHARED main.cpp)
target_include_directories(${NAME_VULKAN} PUBLIC
        ${Vulkan_INCLUDE_DIRS}
)
target_link_libraries(${NAME_VULKAN}
        ${Vulkan_LIBRARIES}
)

#GLFW
add_library(${NAME_GLFW} SHARED main.cpp)
target_include_directories(${NAME_GLFW} PUBLIC
        external/GLFW/include
)
target_link_directories(${NAME_GLFW} PUBLIC
        external/GLFW/src
)
target_link_libraries(${NAME_GLFW}
        glfw
)

#GLM
add_library(${NAME_GLM} SHARED main.cpp)
target_include_directories(${NAME_GLM} PUBLIC
        external/GLM
)
