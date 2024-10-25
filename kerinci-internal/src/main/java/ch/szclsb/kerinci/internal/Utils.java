package ch.szclsb.kerinci.internal;

public class Utils {
    public static boolean checkFlags(int value, int flags) {
        return (value & flags) == flags;
    }
}
