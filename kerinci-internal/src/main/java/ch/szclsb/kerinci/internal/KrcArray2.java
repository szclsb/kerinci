package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.stream.Stream;

// todo improve creation, factory-wise and swapchain-wise
public class KrcArray2<T extends AbstractKrcHandle2> implements AutoCloseable {
    private final MemorySegment pArray;
    private final T[] data;

    public KrcArray2(MemorySegment pArray, T[] data) {
        this.pArray = pArray;
        this.data = data;
    }

    protected KrcArray2(KrcArray2<T> other) {
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
