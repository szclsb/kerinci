package ch.szclsb.kerinci.base.plugin.libc.mapper;

import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;

public interface LibcObjectParser<T> {
    T parse(String className, LibcCursor libcCursor);
}
