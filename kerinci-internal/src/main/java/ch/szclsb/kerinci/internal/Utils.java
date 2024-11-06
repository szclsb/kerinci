package ch.szclsb.kerinci.internal;

import ch.szclsb.kerinci.api.VkRenderPassCreateInfo;
import ch.szclsb.kerinci.api.VkSubpassDescription;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static ch.szclsb.kerinci.api.api_h_6.VK_FALSE;
import static ch.szclsb.kerinci.api.api_h_6.VK_TRUE;

public class Utils {
    public static boolean checkFlags(int value, int flags) {
        return (value & flags) == flags;
    }

    public static String printAddress(MemorySegment segment) {
        return STR."0x\{Long.toHexString(segment.address())}";
    }

    public static void forEachSlice(MemoryLayout layout, MemorySegment segment, BiConsumer<MemorySegment, Integer> function) {
        var length = segment.byteSize() / layout.byteSize();
        for (var i = 0; i < length; ++i) {
            function.accept(segment.asSlice(i * layout.byteSize(), layout), i);
        }
    }

    public static <T> void writeArrayPointer(T[] array, MemoryLayout layout, MemorySegment segment,
                                             Allocator allocator,
                                             BiConsumer<MemorySegment, T> writer,
                                             BiConsumer<MemorySegment, Integer> countSetter,
                                             BiConsumer<MemorySegment, MemorySegment> pSetter) {
        if (array == null) {
            countSetter.accept(segment, 0);
            pSetter.accept(segment, MemorySegment.NULL);
        } else {
            var count = array.length;
            countSetter.accept(segment, count);
            var pSegment = allocator.apply(MemoryLayout.sequenceLayout(count, layout));
            forEachSlice(layout, pSegment, (slice, i) -> writer.accept(slice, array[i]));
            pSetter.accept(segment, pSegment);
        }
    }

    public static <T> void writePointer(T t, MemoryLayout layout, MemorySegment segment,
                                        Allocator allocator,
                                        BiConsumer<MemorySegment, T> writer,
                                        BiConsumer<MemorySegment, MemorySegment> pSetter) {
        if (t == null) {
            pSetter.accept(segment, MemorySegment.NULL);
        } else {
            var pSegment = allocator.apply(layout);
            writer.accept(pSegment, t);
            pSetter.accept(segment, pSegment);
        }
    }

    public static int toVkBool(boolean value) {
        return value ? VK_TRUE() : VK_FALSE();
    }

//    public static OptionalInt and(HasValue... flags) {
//        return Arrays.stream(flags)
//                .mapToInt(HasValue::getValue)
//                .reduce((a, b) -> a & b);
//    }

    public static <T extends HasValue> int or(T[] flags) {
        return Arrays.stream(flags)
                .mapToInt(HasValue::getValue)
                .reduce(0, (a, b) -> a | b);
    }

    public static <T> T reduceUnique(T t1, T t2) {
        if (t1 == null) {
            return t2;
        }
        if (t2 == null) {
            return t1;
        }
        if (t1.equals(t2)) {
            return t1;
        }
        throw new RuntimeException("items are not unique");
    }
}
