package ch.szclsb.kerinci.base.plugin.libc.mapper;

import ch.szclsb.kerinci.base.plugin.libc.LibcContext;
import ch.szclsb.kerinci.base.plugin.libc.ast.LibcCursor;

import java.io.IOException;

public interface LibcObjectParser<T> {
    T parse(String className, LibcCursor libcCursor, LibcContext context) throws IOException;
}
