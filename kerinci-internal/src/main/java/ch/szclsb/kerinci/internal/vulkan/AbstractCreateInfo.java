package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.internal.AbstractKrcHandle;
import ch.szclsb.kerinci.internal.Allocator;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;

public abstract class AbstractCreateInfo<T extends AbstractKrcHandle> {
    private final Class<T> tClass;
    private final HandleConstructor<T> constructor;

    public AbstractCreateInfo(Class<T> tClass, HandleConstructor<T> constructor) {
        this.tClass = tClass;
        this.constructor = constructor;
    }

    public Class<T> gettClass() {
        return tClass;
    }

    protected HandleConstructor<T> getConstructor() {
        return constructor;
    }

    protected abstract MemorySegment allocateCreateInfo(Allocator allocator);

    protected abstract void writeCreateInfo(MemorySegment pCreateInfo, Allocator additional);

    protected abstract boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle);

    protected abstract void destroy(KrcDevice device, MemorySegment vkHandle);
}
