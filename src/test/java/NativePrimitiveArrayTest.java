import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.structs.NativeTypes;
import net.sixik.javastructg.structs.arrays.NativeBooleanArray;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeCharArray;
import net.sixik.javastructg.structs.arrays.NativeDoubleArray;
import net.sixik.javastructg.structs.arrays.NativeFloatArray;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeLongArray;
import net.sixik.javastructg.structs.arrays.NativeShortArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativePrimitiveArrayTest {

    private final List<NativeType> allocatedArrays = new ArrayList<>();

    @AfterEach
    public void tearDown() {
        for (NativeType nativeType : allocatedArrays) {
            nativeType.freeMemory();
        }
        allocatedArrays.clear();
    }

    @Test
    public void testIntArrayAddGrowAndRead() {
        NativeIntArray array = track(new NativeIntArray(2));

        array.add(10);
        array.add(20);
        array.add(30);

        assertEquals(3, array.size());
        assertTrue(array.capacity() >= 3);
        assertEquals(NativeTypes.INT, array.elementSizeof());
        assertEquals(10, array.get(0));
        assertEquals(20, array.get(1));
        assertEquals(30, array.get(2));
    }

    @Test
    public void testFloatArraySetAndCopyConstructor() {
        NativeFloatArray array = track(new NativeFloatArray(2));
        array.add(1.5f);
        array.add(2.5f);

        NativeFloatArray copy = track(new NativeFloatArray(array));
        array.set(0, 9.5f);

        assertEquals(NativeTypes.FLOAT, array.elementSizeof());
        assertEquals(1.5f, copy.get(0));
        assertEquals(2.5f, copy.get(1));
        assertEquals(9.5f, array.get(0));
    }

    @Test
    public void testShortArrayStoresSignedValues() {
        NativeShortArray array = track(new NativeShortArray(3));
        short[] expected = new short[]{Short.MIN_VALUE, 0, Short.MAX_VALUE};

        for (short value : expected) {
            array.add(value);
        }

        assertEquals(expected.length, array.size());
        assertEquals(NativeTypes.SHORT, array.elementSizeof());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], array.get(i));
        }
    }

    @Test
    public void testLongArrayUsesLongSizedElements() {
        NativeLongArray array = track(new NativeLongArray(2));

        array.add(Long.MIN_VALUE);
        array.add(Long.MAX_VALUE);

        assertEquals(NativeTypes.LONG, array.elementSizeof());
        assertEquals(2L * NativeTypes.LONG, array.sizeof());
        assertEquals(Long.MIN_VALUE, array.get(0));
        assertEquals(Long.MAX_VALUE, array.get(1));
    }

    @Test
    public void testDoubleArrayPreservesDoublePrecision() {
        NativeDoubleArray array = track(new NativeDoubleArray(2));
        double[] expected = new double[]{Math.PI, Math.E};

        for (double value : expected) {
            array.add(value);
        }

        assertEquals(NativeTypes.DOUBLE, array.elementSizeof());
        assertEquals(expected[0], array.get(0));
        assertEquals(expected[1], array.get(1));
    }

    @Test
    public void testByteArrayCopyConstructorCreatesIndependentCopy() {
        NativeByteArray array = track(new NativeByteArray(2));
        array.add((byte) 1);
        array.add((byte) 2);

        NativeByteArray copy = track(new NativeByteArray(array));
        array.set(0, (byte) 9);

        assertEquals((byte) 1, copy.get(0));
        assertEquals((byte) 2, copy.get(1));
        assertEquals((byte) 9, array.get(0));
    }

    @Test
    public void testBooleanArrayStoresFlagsCompactly() {
        NativeBooleanArray array = track(new NativeBooleanArray(4));
        boolean[] expected = new boolean[]{true, false, true, true};

        for (boolean value : expected) {
            array.add(value);
        }

        assertEquals(NativeTypes.BOOLEAN, array.elementSizeof());
        assertEquals(4L * NativeTypes.BOOLEAN, array.sizeof());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], array.get(i));
        }
    }

    @Test
    public void testCharArrayUsesTwoByteChars() {
        NativeCharArray array = track(new NativeCharArray(3));
        char[] expected = new char[]{'A', '\u0416', '\u03A9'};

        for (char value : expected) {
            array.add(value);
        }

        assertEquals(NativeTypes.CHAR, array.elementSizeof());
        assertEquals(3L * NativeTypes.CHAR, array.sizeof());

        char[] actual = new char[expected.length];
        for (int i = 0; i < expected.length; i++) {
            actual[i] = array.get(i);
        }
        assertArrayEquals(expected, actual);
    }

    private <T extends NativeType> T track(T array) {
        allocatedArrays.add(array);
        return array;
    }
}
