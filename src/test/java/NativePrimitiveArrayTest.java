import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.structs.NativeTypes;
import net.sixik.javastructg.structs.arrays.NativeBooleanArray;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeCharArray;
import net.sixik.javastructg.structs.arrays.NativeDoubleArray;
import net.sixik.javastructg.structs.arrays.NativeFloatArray;
import net.sixik.javastructg.structs.arrays.NativeFloatCursor;
import net.sixik.javastructg.structs.arrays.NativeFloatSlice;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeIntCursor;
import net.sixik.javastructg.structs.arrays.NativeIntSlice;
import net.sixik.javastructg.structs.arrays.NativeLongArray;
import net.sixik.javastructg.structs.arrays.NativeArray;
import net.sixik.javastructg.structs.arrays.NativeShortArray;
import net.sixik.javastructg.structs.arrays.NativeShortCursor;
import net.sixik.javastructg.structs.arrays.NativeShortSlice;
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
        assertEquals(0L, array.ptr() % NativeArray.MEMORY_ALIGNMENT);
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
    public void testIntArrayBulkCopyRoundTrip() {
        NativeIntArray array = track(new NativeIntArray(4));
        int[] expected = new int[]{7, -1, 42, Integer.MIN_VALUE};

        array.copyFrom(expected);

        int[] actual = new int[expected.length];
        array.copyTo(actual);

        assertEquals(expected.length, array.size());
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testIntCursorSequentialWriteAndRead() {
        NativeIntArray array = track(new NativeIntArray(4));
        int[] expected = new int[]{11, 22, 33, 44};

        NativeIntCursor writeCursor = array.writeCursor(expected.length);
        for (int value : expected) {
            writeCursor.put(value);
        }

        NativeIntCursor readCursor = array.cursor();
        int[] actual = new int[expected.length];
        int index = 0;
        while (readCursor.hasRemaining()) {
            actual[index++] = readCursor.get();
        }

        assertEquals(expected.length, array.size());
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testIntArrayRangeCopyFromHeapSlice() {
        NativeIntArray array = track(new NativeIntArray(5));

        array.copyFrom(new int[]{5, 10, 15, 20, 25}, 1, 0, 3);

        int[] actual = new int[3];
        array.copyTo(actual);

        assertArrayEquals(new int[]{10, 15, 20}, actual);
    }

    @Test
    public void testIntSliceWritesIntoSubRange() {
        NativeIntArray array = track(new NativeIntArray(6));
        array.fill(0);

        NativeIntSlice slice = array.writeSlice(2, 3);
        slice.set(0, 7);
        slice.set(1, 8);
        slice.set(2, 9);

        int[] actual = new int[6];
        array.copyTo(actual);

        assertArrayEquals(new int[]{0, 0, 7, 8, 9, 0}, actual);
    }

    @Test
    public void testIntArrayNativeToNativeCopyBetweenArrays() {
        NativeIntArray source = track(new NativeIntArray(5));
        NativeIntArray destination = track(new NativeIntArray(5));
        source.copyFrom(new int[]{1, 2, 3, 4, 5});

        source.copyTo(1, destination, 0, 3);

        int[] actual = new int[3];
        destination.copyTo(actual);

        assertArrayEquals(new int[]{2, 3, 4}, actual);
    }

    @Test
    public void testFloatArrayBulkCopyRoundTrip() {
        NativeFloatArray array = track(new NativeFloatArray(4));
        float[] expected = new float[]{1.5f, -2.25f, 0.0f, 99.75f};

        array.copyFrom(expected);

        float[] actual = new float[expected.length];
        array.copyTo(actual);

        assertEquals(expected.length, array.size());
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testFloatCursorSeekFromMiddle() {
        NativeFloatArray array = track(new NativeFloatArray(4));
        array.copyFrom(new float[]{1.0f, 2.0f, 3.0f, 4.0f});

        NativeFloatCursor cursor = array.cursorFrom(1);
        assertEquals(3, cursor.limit());
        assertEquals(2.0f, cursor.get());

        cursor.seek(1);
        assertEquals(3.0f, cursor.get());
        assertEquals(4.0f, cursor.get());
    }

    @Test
    public void testFloatArrayFillRange() {
        NativeFloatArray array = track(new NativeFloatArray(5));
        array.copyFrom(new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f});

        array.fill(1, 3, 9.5f);

        float[] actual = new float[5];
        array.copyTo(actual);

        assertArrayEquals(new float[]{1.0f, 9.5f, 9.5f, 9.5f, 5.0f}, actual);
    }

    @Test
    public void testFloatSliceCanCopyHeapRange() {
        NativeFloatArray array = track(new NativeFloatArray(6));
        NativeFloatSlice slice = array.writeSlice(1, 3);

        slice.copyFrom(new float[]{2.5f, 5.0f, 7.5f});

        float[] actual = new float[3];
        slice.copyTo(actual);

        assertArrayEquals(new float[]{2.5f, 5.0f, 7.5f}, actual);
    }

    @Test
    public void testShortArrayBulkCopyRoundTrip() {
        NativeShortArray array = track(new NativeShortArray(4));
        short[] expected = new short[]{Short.MIN_VALUE, -7, 12, Short.MAX_VALUE};

        array.copyFrom(expected);

        short[] actual = new short[expected.length];
        array.copyTo(actual);

        assertEquals(expected.length, array.size());
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testShortCursorCanOverwriteRange() {
        NativeShortArray array = track(new NativeShortArray(4));
        array.copyFrom(new short[]{1, 2, 3, 4});

        NativeShortCursor cursor = array.writeCursor(1, 2);
        cursor.put((short) 20);
        cursor.put((short) 30);

        short[] actual = new short[4];
        array.copyTo(actual);

        assertArrayEquals(new short[]{1, 20, 30, 4}, actual);
    }

    @Test
    public void testShortArrayCopyToAnotherNativeArray() {
        NativeShortArray source = track(new NativeShortArray(4));
        NativeShortArray destination = track(new NativeShortArray(4));
        source.copyFrom(new short[]{3, 6, 9, 12});

        source.copyTo(1, destination, 0, 2);

        short[] actual = new short[2];
        destination.copyTo(actual);

        assertArrayEquals(new short[]{6, 9}, actual);
    }

    @Test
    public void testShortTailSliceRead() {
        NativeShortArray array = track(new NativeShortArray(5));
        array.copyFrom(new short[]{1, 2, 3, 4, 5});

        NativeShortSlice slice = array.tailSlice(2);
        short[] actual = new short[3];
        slice.copyTo(actual);

        assertArrayEquals(new short[]{3, 4, 5}, actual);
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
