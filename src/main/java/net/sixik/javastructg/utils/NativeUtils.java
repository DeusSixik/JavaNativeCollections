package net.sixik.javastructg.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class NativeUtils {

    private static final int PHI_C32 = 0x9e3779b9;
    private static final long PHI_C64 = 0x9e3779b97f4a7c15L;

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

    public static int mix(int value) {
        int x = value;
        x ^= (x >>> 16);
        x *= 0x7feb352d;
        x ^= (x >>> 15);
        x *= 0x846ca68b;
        x ^= (x >>> 16);
        return x;
    }

    public static int nextPowerOfTwo(int value) {
        int n = value - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return n + 1;
    }

    public static long mix(long z) {
        z = (z ^ (z >>> 32)) * 0x4cd6944c5cc20b6dL;
        z = (z ^ (z >>> 29)) * 0xfc12c5b19d3259e9L;
        return z ^ (z >>> 32);
    }

    public static long nextPowerOfTwo(long value) {
        long n = value - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        n |= n >>> 32;
        return n + 1;
    }

    public static int mix(float key) {
        return mix(Float.floatToIntBits(key));
    }

    public static long mix(double key) {
        return mix(Double.doubleToLongBits(key));
    }

    public static int mix(byte key) {
        return key * PHI_C32;
    }

    public static int mix(short key) {
        return mixPhi(key);
    }

    public static int mix(char key) {
        return mixPhi(key);
    }

    public static int mixPhi(byte k) {
        final int h = k * PHI_C32;
        return h ^ (h >>> 16);
    }

    public static int mixPhi(char k) {
        final int h = k * PHI_C32;
        return h ^ (h >>> 16);
    }

    public static int mixPhi(short k) {
        final int h = k * PHI_C32;
        return h ^ (h >>> 16);
    }

    public static int mixPhi(int k) {
        final int h = k * PHI_C32;
        return h ^ (h >>> 16);
    }

    public static int mixPhi(float k) {
        final int h = Float.floatToIntBits(k) * PHI_C32;
        return h ^ (h >>> 16);
    }

    public static int mixPhi(double k) {
        final long h = Double.doubleToLongBits(k) * PHI_C64;
        return (int) (h ^ (h >>> 32));
    }

    public static int mixPhi(long k) {
        final long h = k * PHI_C64;
        return (int) (h ^ (h >>> 32));
    }

    public static int mixPhi(Object k) {
        final int h = (k == null ? 0 : k.hashCode() * PHI_C32);
        return h ^ (h >>> 16);
    }

    public static long hashCombine(long seed, long hash) /* boost::hash_combine */ {
        return seed ^ (hash + PHI_C64 + (seed << 6) + (seed >>> 2));
    }
}
