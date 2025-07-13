package ch.szclsb.kerinci.base.api;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

public class BitMask<F extends Flag> implements HasValue {
    private static final IntBinaryOperator INT_OR = (a, b) -> a | b;
    private static boolean checkFlags(int value, int flags) {
        return (value & flags) == flags;
    }

    private final AtomicInteger mask;

    public BitMask() {
        this(0);
    }

    public BitMask(int mask) {
        this.mask = new AtomicInteger(mask);
    }

    @Override
    public int getValue() {
        return mask.get();
    }

    public final void reset() {
        this.mask.set(0);
    }

    public final void add(F flag) {
        int currentValue, newValue;
        do {
            currentValue = mask.get();
            newValue = INT_OR.applyAsInt(currentValue, flag.getValue());
        } while (!this.mask.compareAndSet(currentValue, newValue));
    }

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

    public final boolean isSet(F flag) {
        return checkFlags(mask.get(), flag.getValue());
    }

    @SafeVarargs
    public final boolean areAllSet(F ...flags) {
        return checkFlags(mask.get(), Arrays.stream(flags)
                .mapToInt(F::getValue)
                .reduce(0, INT_OR));
    }

    @SafeVarargs
    public static <F extends Flag> BitMask<F> ofFlags(F... flags) {
        var bitMask = new BitMask<F>();
        bitMask.addAll(flags);
        return bitMask;
    }
}
