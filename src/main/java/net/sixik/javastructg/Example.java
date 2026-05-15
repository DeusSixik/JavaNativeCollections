package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeUtils;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import sun.misc.Unsafe;

public final class Example {

    private static final Unsafe UNSAFE = NativeUtils.getUnsafe();

    private Example() {
    }

    public static int[] doubleValues(int[] input) {
        NativeIntArray nativeArray = new NativeIntArray(input.length);
        try {
            long ptr = nativeArray.ptr();
            int count = input.length;

            // Fast path: bulk load once, process on raw addresses, bulk read once.
            NativeRawPrimitives.copyIntsFromArray(ptr, input);

            long current = ptr;
            for (int i = 0; i < count; i++, current += Integer.BYTES) {
                UNSAFE.putInt(current, UNSAFE.getInt(current) * 2);
            }

            int[] output = new int[count];
            NativeRawPrimitives.copyIntsToArray(ptr, output);
            return output;
        } finally {
            nativeArray.freeMemory();
        }
    }

    public static long sumFilled(int size, int value) {
        NativeIntArray nativeArray = new NativeIntArray(size);
        try {
            long ptr = nativeArray.ptr();
            NativeRawPrimitives.fillInts(ptr, size, value);
            return NativeRawPrimitives.sumInts(ptr, size);
        } finally {
            nativeArray.freeMemory();
        }
    }
}
