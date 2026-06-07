package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.base.api.BitMask;
import ch.szclsb.kerinci.base.api.Flag;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class KerinciBitMask<F extends Flag> implements BitMask<F> {
    private final AtomicInteger mask;

    public KerinciBitMask() {
        this(0);
    }

    public KerinciBitMask(int mask) {
        this.mask = new AtomicInteger(mask);
    }

    @Override
    public int getValue() {
        return mask.get();
    }

    @Override
    public final void reset() {
        this.mask.set(0);
    }

    @Override
    public final void add(F flag) {
        int currentValue, newValue;
        do {
            currentValue = mask.get();
            newValue = INT_OR.applyAsInt(currentValue, flag.getValue());
        } while (!this.mask.compareAndSet(currentValue, newValue));
    }

    @Override
    @SafeVarargs
    public final void addAll(F... flags) {
        int currentValue, newValue;
        do {
            currentValue = mask.get();
            newValue = Arrays.stream(flags)
                    .mapToInt(F::getValue)
                    .reduce(currentValue, INT_OR);
        } while (!this.mask.compareAndSet(currentValue, newValue));
    }

    @Override
    public final boolean isSet(F flag) {
        return BitMask.checkFlags(mask.get(), flag.getValue());
    }

    @Override
    @SafeVarargs
    public final boolean areAllSet(F... flags) {
        return BitMask.checkFlags(mask.get(), Arrays.stream(flags)
                .mapToInt(F::getValue)
                .reduce(0, INT_OR));
    }

    @SafeVarargs
    public static <F extends Flag> KerinciBitMask<F> ofFlags(F... flags) {
        var bitMask = new KerinciBitMask<F>();
        bitMask.addAll(flags);
        return bitMask;
    }
}
