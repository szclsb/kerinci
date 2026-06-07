import ch.szclsb.kerinci.internal.BitMask;
import ch.szclsb.kerinci.base.api.Flag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BitMaskTest {
    public enum TestFlag implements Flag {
        FLAG_1(0b00000001),
        FLAG_2(0b00000010),
        FLAG_3(0b00000100),
        FLAG_4(0b00001000),
        FLAG_5(0b00010000),
        FLAG_6(0b00100000),
        FLAG_7(0b01000000),
        FLAG_8(0b10000000);

        private final int value;
        TestFlag(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    @Test
    public void test_mask() {
        var bitMask = new BitMask<TestFlag>();

        // empty mask
        Assertions.assertEquals(0, bitMask.getValue());
        for (var flag : TestFlag.values()) {
            Assertions.assertFalse(bitMask.isSet(flag));
        }

        // set single flag
        bitMask.add(TestFlag.FLAG_4);
        Assertions.assertEquals(0b00001000, bitMask.getValue());
        for (var flag : TestFlag.values()) {
            if (TestFlag.FLAG_4.equals(flag)) {
                Assertions.assertTrue(bitMask.isSet(flag));
            } else {
                Assertions.assertFalse(bitMask.isSet(flag));
            }
        }

        // set multiple flags
        bitMask.addAll(TestFlag.FLAG_1, TestFlag.FLAG_6);
        Assertions.assertEquals(0b00101001, bitMask.getValue());
        for (var flag : TestFlag.values()) {
            if (TestFlag.FLAG_1.equals(flag) || TestFlag.FLAG_4.equals(flag) || TestFlag.FLAG_6.equals(flag)) {
                Assertions.assertTrue(bitMask.isSet(flag));
            } else {
                Assertions.assertFalse(bitMask.isSet(flag));
            }
        }
        Assertions.assertTrue(bitMask.areAllSet(TestFlag.FLAG_1, TestFlag.FLAG_4));
        Assertions.assertTrue(bitMask.areAllSet(TestFlag.FLAG_1, TestFlag.FLAG_4, TestFlag.FLAG_6));
        Assertions.assertFalse(bitMask.areAllSet(TestFlag.FLAG_1, TestFlag.FLAG_2, TestFlag.FLAG_4));
        Assertions.assertFalse(bitMask.areAllSet(TestFlag.FLAG_2));
    }
}
