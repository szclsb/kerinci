package ch.szclsb.kerinci.base.api;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;

public class BitMask<F extends Flag> implements HasValue {
    private static final IntBinaryOperator INT_OR = (a, b) -> a | b;
    private static boolean checkFlags(int value, int flags) {
        return  (value & flags) == flags;
    }

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
        return checkFlags(mask, flag.getValue());
    }

    @SafeVarargs
    public final boolean areAllSet(F ...flags) {
        return checkFlags(mask, Arrays.stream(flags)
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
