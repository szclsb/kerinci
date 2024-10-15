cmake_minimum_required(VERSION 3.22)

#set(CMAKE_CXX_STANDARD 23)
set(NAME Kerinci)

project(${NAME} VERSION 0.0.1 LANGUAGES C)

#set(NAME_VULKAN ${NAME}Vulkan)
#set(NAME_GLFW ${NAME}Glfw)
#set(NAME_GLM ${NAME}Glm)

set(GLFW_BUILD_DOCS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)

add_subdirectory(external/GLFW)
#add_subdirectory(external/GLM)

find_package(Vulkan REQUIRED)
if (NOT Vulkan_FOUND)
    message(FATAL_ERROR "Could not find vulkan lib")
endif()

add_library(${NAME} SHARED api.c)
target_include_directories(${NAME} PUBLIC include
        ${Vulkan_INCLUDE_DIRS}
        external/GLFW/include
#        external/GLM
)
target_link_directories(${NAME} PUBLIC
        external/GLFW/src
)
target_link_libraries(${NAME}
        ${Vulkan_LIBRARIES}
        glfw
)
