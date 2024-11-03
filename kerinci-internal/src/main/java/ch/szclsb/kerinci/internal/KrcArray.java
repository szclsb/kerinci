package ch.szclsb.kerinci.internal;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Stream;

// ensures native segments are adjacent
public class KrcArray<T, A> implements AutoCloseable {
    private final Arena arena;
    private final MemorySegment handles;
    private final T[] data;
    private final A attachment;

    public KrcArray(int length, AddressLayout addressLayout, BiFunction<MemorySegment, Integer, T> creator, A attachment) {
        this.arena = Arena.ofConfined();
        this.handles = arena.allocate(MemoryLayout.sequenceLayout(length, addressLayout));
        this.data = (T[]) new Object[length];
        for (var i = 0; i < length; ++i) {
            data[i] = creator.apply(handles.asSlice(i * addressLayout.byteSize(), addressLayout), i);
        }
        this.attachment = attachment;
    }

    public MemorySegment getPointer() {
        return handles.asReadOnly();
    }

    public A getAttachment() {
        return attachment;
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
        arena.close();
    }
}
