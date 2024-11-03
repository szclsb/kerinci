package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.OptionalInt;

public class Utils {
    public static boolean checkFlags(int value, int flags) {
        return (value & flags) == flags;
    }

    public static String printAddress(MemorySegment segment) {
        return STR."0x\{Long.toHexString(segment.address())}";
    }

    public static OptionalInt and(HasValue... flags) {
        return Arrays.stream(flags)
                .mapToInt(HasValue::getValue)
                .reduce((a, b) -> a & b);
    }

    public static OptionalInt or(HasValue... flags) {
        return Arrays.stream(flags)
                .mapToInt(HasValue::getValue)
                .reduce((a, b) -> a | b);
    }

    public static int and(int defaultValue, HasValue... flags) {
        return and(flags).orElse(defaultValue);
    }

    public static int or(int defaultValue, HasValue... flags) {
        return or(flags).orElse(defaultValue);
    }
}
