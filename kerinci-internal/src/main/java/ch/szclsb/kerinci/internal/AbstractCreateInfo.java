package ch.szclsb.kerinci.internal;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;

public abstract class AbstractCreateInfo<T extends AbstractKrcHandle2> {
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

    protected abstract AddressLayout layout();

    protected abstract MemorySegment allocateCreateInfo(Allocator allocator);

    protected abstract void writeCreateInfo(MemorySegment pCreateInfo);

    protected abstract boolean create(KrcDevice device, MemorySegment pCreateInfo, MemorySegment pHandle);

    protected abstract void destroy(KrcDevice device, MemorySegment vkHandle);
}
