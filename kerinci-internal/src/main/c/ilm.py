import os
import shutil
import json
import argparse
import re
import clang.cindex

def is_valid_type(t):
    '''used to check if a cursor has a type'''
    return t.kind != clang.cindex.TypeKind.INVALID

def parseType(cursor):
    for typeCursor in cursor.get_children():
        if typeCursor.kind == clang.cindex.CursorKind.TYPE_REF:
            return {
                "type": typeCursor.spelling,
                "pointer": cursor.type.kind == clang.cindex.TypeKind.POINTER
            }

def parse(cursor):
    '''print ast into definition file'''
    if cursor.kind == clang.cindex.CursorKind.TRANSLATION_UNIT:
        delarations = list()
        for childCursor in cursor.get_children():
            decl = parse(childCursor)
            if not decl is None:
                delarations.append(decl)
        return {
            "file": cursor.spelling,
            "declarations": delarations
        }
    elif cursor.kind == clang.cindex.CursorKind.ENUM_DECL:
        values = dict()
        for childCursor in cursor.get_children():
            # CursorKind.ENUM_CONSTANT_DECL
            values[childCursor.spelling] = childCursor.enum_value
        return {
            "kind": "enum",
            "name": cursor.spelling,
            "values": values
        }
    elif cursor.kind == clang.cindex.CursorKind.STRUCT_DECL:
        fields = dict()
        for fieldCursor in cursor.get_children():
            if fieldCursor.kind == clang.cindex.CursorKind.FIELD_DECL:
                fields[fieldCursor.spelling] = parseType(fieldCursor)
        return {
            "kind": "struct",
            "name": cursor.spelling,
            "fields": fields
        }
    elif cursor.kind == clang.cindex.CursorKind.FUNCTION_DECL:
        params = dict()
        returnType = None
        for paramCursor in cursor.get_children():
            if paramCursor.kind == clang.cindex.CursorKind.TYPE_REF:
                returnType = parseType(paramCursor)
            elif paramCursor.kind == clang.cindex.CursorKind.PARM_DECL:
                params[paramCursor.spelling] = parseType(paramCursor)
        return {
            "kind": "function",
            "name": cursor.spelling,
            "returnType": returnType,
            "params": params
        }
    return None

def main():
    parser = argparse.ArgumentParser(prog='generate glfw/vulkan dll spec')
    parser.add_argument('-t', '--target', help='target directory to write the definition file')
    parser.add_argument('-f', '--function', help='path to file containing function names as filter')
    parser.add_argument('-s', '--sources', help='path of source header files to scan', nargs='+')
    parser.add_argument('-p', '--prefix', help='wrapper function prefix')
    parser.add_argument('-e', '--exclusions', help='keywords to exclude form generated functions', nargs='*')

    args = parser.parse_args()
    print('ilm.py ' + str(args))

    index = clang.cindex.Index.create()

    # functions = set()
    # functionsFile = open(os.path.join(args.function), 'r')
    # for line in functionsFile:
    #     function = line.strip()
    #     if function and not function.startswith('--'):
    #         print('-- ' + function)
    #         functions.add(function)

    #todo macro handling
    i = 1  # todo source name
    for source in args.sources:
        tmpIlmPath = os.path.join(args.target, 'ilm.json.tmp')
        tmpIlmFile = open(tmpIlmPath, 'w')

        translationUnit = index.parse(source)
        print('Translation unit:', translationUnit.spelling)
        declaration = parse(translationUnit.cursor)

        tmpIlmFile.write(json.dumps(declaration, indent=2))
        tmpIlmFile.close()
        shutil.move(tmpIlmPath, os.path.join(args.target, f'ilm{i}.json'))

if __name__ == '__main__':
    main()
