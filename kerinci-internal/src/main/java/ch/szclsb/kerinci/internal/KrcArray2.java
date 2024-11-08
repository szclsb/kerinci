package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.stream.Stream;

// ensures native segments are adjacent
public record KrcArray2<T extends AbstractKrcHandle2>(
        MemorySegment pArray,
        T[] data
) implements AutoCloseable {
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
