package net.sixik.javastructg.structs;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class NativeUtils {

    private static final Unsafe unsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static Unsafe getUnsafe() {
        return unsafe;
    }
}
