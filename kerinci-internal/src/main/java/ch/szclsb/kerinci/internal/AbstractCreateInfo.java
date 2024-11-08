package ch.szclsb.kerinci.internal;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;

public abstract class AbstractCreateInfo<T extends AbstractKrcHandle2> {
    private final Class<T> tClass;
    private final HandleConstructor<T> constructor;
    private final AddressLayout layout;

    public AbstractCreateInfo(Class<T> tClass, HandleConstructor<T> constructor, AddressLayout layout) {
        this.layout = layout;
        this.tClass = tClass;
        this.constructor = constructor;
    }

    public AddressLayout getLayout() {
        return layout;
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
