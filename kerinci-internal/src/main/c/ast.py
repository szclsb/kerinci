import os
import shutil
import json
import argparse
import clang.cindex

def is_literal(cursor):
    return cursor.kind == clang.cindex.CursorKind.INTEGER_LITERAL

def parse_type(type):
    if type.kind == clang.cindex.TypeKind.INVALID:
        return None
    return {
        "kind": str(type.kind),
        "const": type.is_const_qualified(),
        "ref": parse_type(type.get_pointee()),
    }

def parse(cursor):
    children = list()
    for childCursor in cursor.get_children():
        children.append(parse(childCursor))
    return {
        "kind": str(cursor.kind),
        "spelling": next(cursor.get_tokens()).spelling if is_literal(cursor) else cursor.spelling,
        "type":  parse_type(cursor.type),
        "children": children
    }

def main():
    parser = argparse.ArgumentParser(prog='generate ast from header sources')
    parser.add_argument('-t', '--target', help='target directory to write the definition file')
    parser.add_argument('-s', '--sources', help='path of source header files to scan', nargs='+')

    args = parser.parse_args()
    print('ast.py ' + str(args))

    index = clang.cindex.Index.create()

    #todo macro handling
    for source in args.sources:
        name = source[source.rfind('/')+1:] + '.ast.json'
        tmpIlmPath = os.path.join(args.target, 'ast.json.tmp')
        tmpIlmFile = open(tmpIlmPath, 'w')

        translationUnit = index.parse(source)
        print('Translation unit:', translationUnit.spelling)
        declaration = parse(translationUnit.cursor)

        tmpIlmFile.write(json.dumps(declaration, indent=2))
        tmpIlmFile.close()
        shutil.move(tmpIlmPath, os.path.join(args.target, name))
        print('ast: ', name)

if __name__ == '__main__':
    main()
