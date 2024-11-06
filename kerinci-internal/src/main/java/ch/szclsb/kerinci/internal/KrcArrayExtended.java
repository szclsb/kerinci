package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemoryLayout;
import java.util.function.Function;

// ensures native segments are adjacent
public final class KrcArrayExtended<T, E> extends KrcArray<T> {
    private final E extension;

    public KrcArrayExtended(int length, MemoryLayout memoryLayout,
                            Allocator allocator, Slicer<T> creator,
                            Function<T, E> extensionFunction) {
        super(length, memoryLayout, allocator, creator);
        this.extension = applyExtensionFunction(extensionFunction);
    }

    public KrcArrayExtended(KrcArray<T> base,
                            Function<T, E> extensionFunction) {
        super(base);
        this.extension = applyExtensionFunction(extensionFunction);
    }

    private E applyExtensionFunction(Function<T, E> extension) {
        return stream()
                .map(extension)
                .reduce(Utils::reduceUnique)
                .orElse(null);
    }

    public E getExtension() {
        return extension;
    }
}
