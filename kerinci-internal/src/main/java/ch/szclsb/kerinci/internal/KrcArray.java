package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import java.util.Arrays;
import java.util.stream.Stream;

import static ch.szclsb.kerinci.internal.Utils.forEachSlice;

// ensures native segments are adjacent
public sealed class KrcArray<T> implements AutoCloseable permits KrcArrayExtended {
    private final MemorySegment handles;
    private final T[] data;

    public KrcArray(int length, MemoryLayout memoryLayout, Allocator allocator, Creator<T> creator) {
        this.handles = allocator.apply(MemoryLayout.sequenceLayout(length, memoryLayout));
        this.data = (T[]) new Object[length];
        forEachSlice(memoryLayout, handles, (slice, i) -> data[i] = creator.apply(slice, i));
    }

    protected KrcArray(KrcArray<T> other) {
        this.handles = other.handles;
        this.data = other.data;
    }

    public MemorySegment getPointer() {
        return handles.asReadOnly();
    }

    public int length() {
        return data.length;
    }

    public T get(final int index) {
        return data[index];
    }

    public Stream<T> stream() {
        return Arrays.stream(data);
    }

    @Override
    public void close() throws Exception {
        // todo improve
        for (var i = 0; i < data.length; ++i) {
            if (data[i] instanceof AutoCloseable ac) {
                ac.close();
            }
        }
    }
}
