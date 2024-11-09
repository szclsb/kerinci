package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.Allocator;

import java.lang.foreign.MemorySegment;

public abstract class AbstractStruct {
    protected abstract void write(MemorySegment pStruct, Allocator additional);

    protected abstract void read(MemorySegment pStruct);
}