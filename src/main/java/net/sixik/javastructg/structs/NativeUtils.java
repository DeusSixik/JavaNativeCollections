package net.sixik.javastructg.structs;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class NativeUtils {

    private static final Unsafe unsafe;
    private static final long INT_ARRAY_BASE_OFFSET;
    private static final long FLOAT_ARRAY_BASE_OFFSET;
    private static final long SHORT_ARRAY_BASE_OFFSET;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            INT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(int[].class);
            FLOAT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(float[].class);
            SHORT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(short[].class);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static Unsafe getUnsafe() {
        return unsafe;
    }

    public static long intArrayBaseOffset() {
        return INT_ARRAY_BASE_OFFSET;
    }

    public static long floatArrayBaseOffset() {
        return FLOAT_ARRAY_BASE_OFFSET;
    }

    public static long shortArrayBaseOffset() {
        return SHORT_ARRAY_BASE_OFFSET;
    }
}
