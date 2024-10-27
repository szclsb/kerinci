package ch.szclsb.kerinci.internal;

import java.lang.foreign.MemorySegment;

public class Utils {
    public static boolean checkFlags(int value, int flags) {
        return (value & flags) == flags;
    }

    public static String printAddress(MemorySegment segment) {
        return STR."0x\{Long.toHexString(segment.address())}";
    }
}
