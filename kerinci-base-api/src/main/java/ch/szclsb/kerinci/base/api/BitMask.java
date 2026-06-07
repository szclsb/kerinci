package ch.szclsb.kerinci.base.api;

import java.util.function.IntBinaryOperator;

public interface BitMask<F extends Flag> extends HasValue {
    IntBinaryOperator INT_OR = (a, b) -> a | b;
    static boolean checkFlags(int value, int flags) {
        return (value & flags) == flags;
    }

    void reset();

    void add(F flag);

    void addAll(F... flags);

    boolean isSet(F flag);

    boolean areAllSet(F... flags);
}
