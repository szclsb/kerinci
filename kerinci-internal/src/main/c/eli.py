import os
import shutil
import json
import argparse

def getType(type, ref_cursor):
    name = "???"
    typeKind = type['kind']
    if typeKind == "TypeKind.INT":
        name = "int"
    elif typeKind == "TypeKind.FLOAT":
        name = "float"
    elif typeKind == "TypeKind.CHAR_S":
        name = "char"
    elif typeKind == "TypeKind.VOID":
        name = "void"
    elif typeKind == "TypeKind.ELABORATED":
        name = ref_cursor['spelling']
    elif typeKind == "TypeKind.POINTER":
        name = getType(type['ref'], ref_cursor) + "*"
    return f"{'const ' if type['const'] else ''}{name}"

def main():
    parser = argparse.ArgumentParser(prog='generate ast from header sources')
    parser.add_argument('-t', '--target', help='target directory to write the definition file')
    parser.add_argument('-s', '--sources', help='path of source header files to scan', nargs='+')
    parser.add_argument('-f', '--function', help='path to file containing function names as filter')
    parser.add_argument('-p', '--prefix', help='wrapper function prefix')

    args = parser.parse_args()
    print('eli.py ' + str(args))

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
        ast = None
        with open(source, 'r') as sourceFile:
            ast = json.load(sourceFile)
        for node in ast['children']:
            if node['kind'] == "CursorKind.FUNCTION_DECL" and node['spelling'] in functions:
                function_return_type_node = None
                function_params = list()
                function_args = list()
                for childNode in node['children']:
                    if childNode['kind'] == "CursorKind.TYPE_REF":
                        function_return_type_node = childNode
                    elif childNode['kind'] == "CursorKind.PARM_DECL":
                        function_params.append(f"{getType(childNode['type'], childNode['children'][0] if len(childNode['children']) > 0 else None)} {childNode['spelling']}")
                        function_args.append(childNode['spelling'])
                function_return_type = getType(node['result_type'], function_return_type_node)
                function_name = node['spelling']
                external_function_name = args.prefix + function_name
                tmpHeaderFile.write(f"""
__declspec(dllexport) {function_return_type} {external_function_name}({', '.join(function_params)});""")
                tmpSourceFile.write(f"""
{function_return_type} {external_function_name}({', '.join(function_params)}) {{
    {"" if function_return_type == "void" else "return "}{function_name}({', '.join(function_args)});
}}""")

    tmpHeaderFile.close()
    tmpSourceFile.close()

    shutil.move(tmpHeaderPath, os.path.join(args.target, 'include', 'api.h'))
    shutil.move(tmpSourcePath, os.path.join(args.target, 'api.c'))

if __name__ == '__main__':
    main()
