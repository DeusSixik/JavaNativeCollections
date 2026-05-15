import net.sixik.javastructg.Example;
import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeCharArray;
import net.sixik.javastructg.structs.arrays.NativeDoubleArray;
import net.sixik.javastructg.structs.arrays.NativeFloatArray;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeLongArray;
import net.sixik.javastructg.structs.arrays.NativeShortArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NativeRawPrimitivesTest {

    @Test
    public void testIntRawCopyFillAndSum() {
        NativeIntArray array = new NativeIntArray(4);
        try {
            NativeRawPrimitives.copyIntsFromArray(array.ptr(), new int[]{1, 2, 3, 4});

            int[] copied = new int[4];
            NativeRawPrimitives.copyIntsToArray(array.ptr(), copied);
            assertArrayEquals(new int[]{1, 2, 3, 4}, copied);
            assertEquals(10L, NativeRawPrimitives.sumInts(array.ptr(), 4));

            NativeRawPrimitives.fillInts(array.ptr(), 4, 6);
            assertEquals(24L, NativeRawPrimitives.sumInts(array.ptr(), 4));
        } finally {
            array.freeMemory();
        }
    }

    @Test
    public void testFloatRawCopyAndSum() {
        NativeFloatArray array = new NativeFloatArray(3);
        try {
            NativeRawPrimitives.copyFloatsFromArray(array.ptr(), new float[]{1.25f, 2.5f, 3.75f});

            float[] copied = new float[3];
            NativeRawPrimitives.copyFloatsToArray(array.ptr(), copied);
            assertArrayEquals(new float[]{1.25f, 2.5f, 3.75f}, copied);
            assertEquals(7.5d, NativeRawPrimitives.sumFloats(array.ptr(), 3));
        } finally {
            array.freeMemory();
        }
    }

    @Test
    public void testShortRawCopyAndFill() {
        NativeShortArray array = new NativeShortArray(5);
        try {
            NativeRawPrimitives.copyShortsFromArray(array.ptr(), new short[]{1, 2, 3, 4, 5});
            NativeRawPrimitives.fillShorts(array.ptr(), 5, (short) 9);

            short[] copied = new short[5];
            NativeRawPrimitives.copyShortsToArray(array.ptr(), copied);
            assertArrayEquals(new short[]{9, 9, 9, 9, 9}, copied);
            assertEquals(45L, NativeRawPrimitives.sumShorts(array.ptr(), 5));
        } finally {
            array.freeMemory();
        }
    }

    @Test
    public void testLongRawCopyFillAndSum() {
        NativeLongArray array = new NativeLongArray(4);
        try {
            NativeRawPrimitives.copyLongsFromArray(array.ptr(), new long[]{10L, 20L, 30L, 40L});
            assertEquals(100L, NativeRawPrimitives.sumLongs(array.ptr(), 4));

            NativeRawPrimitives.fillLongs(array.ptr(), 4, 7L);
            long[] copied = new long[4];
            NativeRawPrimitives.copyLongsToArray(array.ptr(), copied);

            assertArrayEquals(new long[]{7L, 7L, 7L, 7L}, copied);
            assertEquals(28L, NativeRawPrimitives.sumLongs(array.ptr(), 4));
        } finally {
            array.freeMemory();
        }
    }

    @Test
    public void testDoubleRawCopyFillAndSum() {
        NativeDoubleArray array = new NativeDoubleArray(3);
        try {
            NativeRawPrimitives.copyDoublesFromArray(array.ptr(), new double[]{1.5d, 2.5d, 3.5d});
            assertEquals(7.5d, NativeRawPrimitives.sumDoubles(array.ptr(), 3));

            NativeRawPrimitives.fillDoubles(array.ptr(), 3, 4.25d);
            double[] copied = new double[3];
            NativeRawPrimitives.copyDoublesToArray(array.ptr(), copied);

            assertArrayEquals(new double[]{4.25d, 4.25d, 4.25d}, copied);
            assertEquals(12.75d, NativeRawPrimitives.sumDoubles(array.ptr(), 3));
        } finally {
            array.freeMemory();
        }
    }

    @Test
    public void testByteRawCopyFillAndSum() {
        NativeByteArray array = new NativeByteArray(4);
        try {
            NativeRawPrimitives.copyBytesFromArray(array.ptr(), new byte[]{1, 2, 3, 4});
            assertEquals(10L, NativeRawPrimitives.sumBytes(array.ptr(), 4));

            NativeRawPrimitives.fillBytes(array.ptr(), 4, (byte) 5);
            byte[] copied = new byte[4];
            NativeRawPrimitives.copyBytesToArray(array.ptr(), copied);

            assertArrayEquals(new byte[]{5, 5, 5, 5}, copied);
            assertEquals(20L, NativeRawPrimitives.sumBytes(array.ptr(), 4));
        } finally {
            array.freeMemory();
        }
    }

    @Test
    public void testCharRawCopyFillAndSum() {
        NativeCharArray array = new NativeCharArray(3);
        try {
            NativeRawPrimitives.copyCharsFromArray(array.ptr(), new char[]{'A', 'Ж', 'Ω'});
            assertEquals('A' + 'Ж' + 'Ω', NativeRawPrimitives.sumChars(array.ptr(), 3));

            NativeRawPrimitives.fillChars(array.ptr(), 3, 'Z');
            char[] copied = new char[3];
            NativeRawPrimitives.copyCharsToArray(array.ptr(), copied);

            assertArrayEquals(new char[]{'Z', 'Z', 'Z'}, copied);
            assertEquals(3L * 'Z', NativeRawPrimitives.sumChars(array.ptr(), 3));
        } finally {
            array.freeMemory();
        }
    }

    @Test
    public void testExampleUsesRecommendedPattern() {
        assertArrayEquals(new int[]{4, 8, 12}, Example.doubleValues(new int[]{2, 4, 6}));
        assertEquals(35L, Example.sumFilled(5, 7));
    }
}
