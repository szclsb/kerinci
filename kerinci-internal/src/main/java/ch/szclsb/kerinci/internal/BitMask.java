package ch.szclsb.kerinci.internal;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;

public class BitMask<F extends Flag> implements HasValue {
    private static final IntBinaryOperator INT_OR = (a, b) -> a | b;

    private int mask;

    public BitMask() {
        this(0);
    }

    public BitMask(int mask) {
        this.mask = mask;
    }

    @Override
    public int getValue() {
        return mask;
    }

    public final void add(F flag) {
        this.mask = mask | flag.getValue();
    }

    @SafeVarargs
    public final void addAll(F... flags) {
        this.mask = Arrays.stream(flags)
                .mapToInt(F::getValue)
                .reduce(mask, INT_OR);
    }

    public final boolean isSet(F flag) {
        var f = flag.getValue();
        return (mask & f) == f;
    }

    @SafeVarargs
    public final boolean areAllSet(F ...flags) {
        var f = Arrays.stream(flags)
                .mapToInt(F::getValue)
                .reduce(0, INT_OR);
        return (mask & f) == f;
    }

    @SafeVarargs
    public static <F extends Flag> BitMask<F> ofFlags(F... flags) {
        var bitMask = new BitMask<F>();
        bitMask.addAll(flags);
        return bitMask;
    }
}
