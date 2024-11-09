package ch.szclsb.kerinci.internal;

import java.util.function.Function;

// ensures native segments are adjacent
public class KrcArrayExtended<T extends AbstractKrcHandle, E> extends KrcArray<T> {
    private final E extension;

    public KrcArrayExtended(KrcArray<T> base, Function<T, E> extensionFunction) {
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
