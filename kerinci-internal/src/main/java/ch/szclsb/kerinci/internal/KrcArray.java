package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.stream.Stream;

// todo improve creation, factory-wise and swapchain-wise
public class KrcArray<T extends AbstractKrcHandle> implements AutoCloseable {
    private final MemorySegment pArray;
    private final T[] data;

    public KrcArray(MemorySegment pArray, T[] data) {
        this.pArray = pArray;
        this.data = data;
    }

    protected KrcArray(KrcArray<T> other) {
        this.pArray = other.pArray;
        this.data = other.data;
    }

    public MemorySegment getpArray() {
        return pArray.asReadOnly();
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
        for (var i : data) {
            i.close();
        }
    }
}
