import os
import shutil
import json
import argparse

def getType(cursor):
    #todo improve
    typeKind = cursor['type']['kind']
    const = cursor['type']['const']
    if typeKind == "TypeKind.INT":
        return "const int" if const else "int"
    elif typeKind == "TypeKind.FLOAT":
        return "const float" if const else "float"
    elif typeKind == "TypeKind.VOID":
        return "void"
    elif typeKind == "TypeKind.ELABORATED":
        return cursor['children'][0]['spelling']
    elif typeKind == "TypeKind.POINTER":
        const = cursor['type']['ref']['const']
        refTypeKind = cursor['type']['ref']['kind']
        if refTypeKind == "TypeKind.ELABORATED":
            return cursor['children'][0]['spelling']+"*"
        elif refTypeKind == "TypeKind.CHAR_S":
            return "const char*" if const else "char*"
        elif refTypeKind == "TypeKind.INT":
            return "const int*" if const else "int*"
        elif refTypeKind == "TypeKind.FLOAT":
            return "const float*" if const else "float*"
        elif refTypeKind == "TypeKind.VOID":
            return "void*"
    return ""

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
                returnType = "void"
                params = list()
                for childNode in node['children']:
                    if childNode['kind'] == "CursorKind.TYPE_REF":
                        returnType = childNode['spelling']
                    elif childNode['kind'] == "CursorKind.PARM_DECL":
                        params.append(f"{getType(childNode)} {childNode['spelling']}")
                external_function = args.prefix + node['spelling']
                tmpHeaderFile.write(f"""
__declspec(dllexport) {returnType} {external_function}({', '.join(params)});""")

    tmpHeaderFile.close()
    tmpSourceFile.close()

    shutil.move(tmpHeaderPath, os.path.join(args.target, 'include', 'api.h'))
    shutil.move(tmpSourcePath, os.path.join(args.target, 'api.c'))

if __name__ == '__main__':
    main()
