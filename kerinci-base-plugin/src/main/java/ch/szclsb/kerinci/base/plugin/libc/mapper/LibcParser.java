package ch.szclsb.kerinci.base.plugin.libc.mapper;

import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;

public interface LibcParser<T> {
    T parse(LibcCursor libcCursor);
}
