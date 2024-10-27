import os
import shutil
import argparse
import re

def main():
    parser = argparse.ArgumentParser(prog='generate glfw/vulkan dll spec')
    parser.add_argument('-t', '--target', help='target directory to write the source file, header file will be written to $target/include')
    parser.add_argument('-f', '--function', help='path to file containing function names as filter')
    parser.add_argument('-s', '--sources', help='path of source header files to scan', nargs='+')
    parser.add_argument('-p', '--prefix', help='wrapper function prefix')
    parser.add_argument('-e', '--exclusions', help='keywords to exclude form generated functions', nargs='*')

    args = parser.parse_args()
    print('api.py ' + str(args))

    regex = re.compile('.*\s(\w+)\(([a-zA-Z0-9_\r\n\t\f\v \,\*]*)\);')

    functions = set()
    functionsFile = open(os.path.join(args.function), 'r')
    for line in functionsFile:
        function = line.strip()
        if function and not function.startswith('--'):
            print('-- ' + function)
            functions.add(function)

    tmpHeaderPath = os.path.join(args.target, 'api.h.tmp')
    tmpSourcePath = os.path.join(args.target, 'api.c.tmp')

    tmpHeaderFile = open(tmpHeaderPath, 'w')
    tmpSourceFile = open(tmpSourcePath, 'w')

    tmpHeaderFile.write("""
#pragma once

#define GLFW_INCLUDE_VULKAN
#include "GLFW/glfw3.h"

""")
    tmpSourceFile.write("""
#include "api.h"

""")

    for source in args.sources:
        print('scanning ' + source)
        sourceFile = open(source, 'r')
        for match in regex.finditer(sourceFile.read(), re.MULTILINE):
            function = match.group(1)
            if function in functions:
                headerFunctionDefinition = match.group(0).replace(function, args.prefix + function).replace('(void)', '()')
                for exclusion in args.exclusions:
                    headerFunctionDefinition = headerFunctionDefinition.replace(exclusion, '')
                headerFunctionDefinition = headerFunctionDefinition.strip()
                tmpHeaderFile.write(f"""
__declspec(dllexport) {headerFunctionDefinition}""")
                params = filter(filterArg, map(extractArg, match.group(2).split(",")))
                tmpSourceFile.write(headerFunctionDefinition.replace(";", f"""{{
    {'' if headerFunctionDefinition.startswith('void') else 'return '}{function}({', '.join(params)});
}}
"""))

    tmpHeaderFile.write(f"""
__declspec(dllexport) VkResult {args.prefix}createDebugUtilsMessengerEXT(
    VkInstance instance,
    const VkDebugUtilsMessengerCreateInfoEXT *pCreateInfo,
    const VkAllocationCallbacks *pAllocator,
    VkDebugUtilsMessengerEXT *pDebugMessenger);""")
    tmpSourceFile.write(f"""
VkResult {args.prefix}createDebugUtilsMessengerEXT(
    VkInstance instance,
    const VkDebugUtilsMessengerCreateInfoEXT *pCreateInfo,
    const VkAllocationCallbacks *pAllocator,
    VkDebugUtilsMessengerEXT *pDebugMessenger) {{
        PFN_vkCreateDebugUtilsMessengerEXT func = (PFN_vkCreateDebugUtilsMessengerEXT)vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT");
        if (func != NULL) {{
            return func(instance, pCreateInfo, pAllocator, pDebugMessenger);
        }} else {{
            return VK_ERROR_EXTENSION_NOT_PRESENT;
        }}
}}""")

    tmpHeaderFile.write(f"""
__declspec(dllexport) void {args.prefix}destroyDebugUtilsMessengerEXT(
    VkInstance instance,
    VkDebugUtilsMessengerEXT debugMessenger,
    const VkAllocationCallbacks *pAllocator);""")
    tmpSourceFile.write(f"""
void {args.prefix}destroyDebugUtilsMessengerEXT(
    VkInstance instance,
    VkDebugUtilsMessengerEXT debugMessenger,
    const VkAllocationCallbacks *pAllocator) {{
        PFN_vkDestroyDebugUtilsMessengerEXT func = (PFN_vkDestroyDebugUtilsMessengerEXT)vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT");
        if (func != NULL) {{
            func(instance, debugMessenger, pAllocator);
        }}
}}""")

    tmpHeaderFile.close()
    tmpSourceFile.close()

    shutil.move(tmpHeaderPath, os.path.join(args.target, 'include', 'api.h'))
    shutil.move(tmpSourcePath, os.path.join(args.target, 'api.c'))

def filterArg(arg):
    return arg != 'void' and arg

def extractArg(arg):
    i = arg.rfind(' ')
    return arg[i+1:]

if __name__ == '__main__':
    main()
