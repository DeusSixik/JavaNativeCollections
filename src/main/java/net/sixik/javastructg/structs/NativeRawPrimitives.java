package net.sixik.javastructg.structs;

import sun.misc.Unsafe;

public final class NativeRawPrimitives {

    private static final Unsafe UNSAFE = NativeUtils.getUnsafe();

    private NativeRawPrimitives() {
    }

    public static void copyIntsFromArray(long destinationAddress, int[] source) {
        copyIntsFromArray(destinationAddress, source, 0, source.length);
    }

    public static void copyIntsFromArray(long destinationAddress, int[] source, int sourceIndex, int elementCount) {
        UNSAFE.copyMemory(
                source,
                NativeUtils.intArrayBaseOffset() + (((long) sourceIndex) << 2),
                null,
                destinationAddress,
                ((long) elementCount) << 2
        );
    }

    public static void copyIntsToArray(long sourceAddress, int[] destination) {
        copyIntsToArray(sourceAddress, destination, 0, destination.length);
    }

    public static void copyIntsToArray(long sourceAddress, int[] destination, int destinationIndex, int elementCount) {
        UNSAFE.copyMemory(
                null,
                sourceAddress,
                destination,
                NativeUtils.intArrayBaseOffset() + (((long) destinationIndex) << 2),
                ((long) elementCount) << 2
        );
    }

    public static void fillInts(long address, int elementCount, int value) {
        if (elementCount == 0) {
            return;
        }

        UNSAFE.putInt(address, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            UNSAFE.copyMemory(address, address + (((long) copied) << 2), ((long) chunk) << 2);
            copied += chunk;
        }
    }

    public static long sumInts(long address, int elementCount) {
        long sum = 0L;
        long current = address;
        for (int i = 0; i < elementCount; i++, current += Integer.BYTES) {
            sum += UNSAFE.getInt(current);
        }
        return sum;
    }

    public static void copyFloatsFromArray(long destinationAddress, float[] source) {
        copyFloatsFromArray(destinationAddress, source, 0, source.length);
    }

    public static void copyFloatsFromArray(long destinationAddress, float[] source, int sourceIndex, int elementCount) {
        UNSAFE.copyMemory(
                source,
                NativeUtils.floatArrayBaseOffset() + (((long) sourceIndex) << 2),
                null,
                destinationAddress,
                ((long) elementCount) << 2
        );
    }

    public static void copyFloatsToArray(long sourceAddress, float[] destination) {
        copyFloatsToArray(sourceAddress, destination, 0, destination.length);
    }

    public static void copyFloatsToArray(long sourceAddress, float[] destination, int destinationIndex, int elementCount) {
        UNSAFE.copyMemory(
                null,
                sourceAddress,
                destination,
                NativeUtils.floatArrayBaseOffset() + (((long) destinationIndex) << 2),
                ((long) elementCount) << 2
        );
    }

    public static void fillFloats(long address, int elementCount, float value) {
        if (elementCount == 0) {
            return;
        }

        UNSAFE.putFloat(address, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            UNSAFE.copyMemory(address, address + (((long) copied) << 2), ((long) chunk) << 2);
            copied += chunk;
        }
    }

    public static double sumFloats(long address, int elementCount) {
        double sum = 0.0d;
        long current = address;
        for (int i = 0; i < elementCount; i++, current += Float.BYTES) {
            sum += UNSAFE.getFloat(current);
        }
        return sum;
    }

    public static void copyShortsFromArray(long destinationAddress, short[] source) {
        copyShortsFromArray(destinationAddress, source, 0, source.length);
    }

    public static void copyShortsFromArray(long destinationAddress, short[] source, int sourceIndex, int elementCount) {
        UNSAFE.copyMemory(
                source,
                NativeUtils.shortArrayBaseOffset() + (((long) sourceIndex) << 1),
                null,
                destinationAddress,
                ((long) elementCount) << 1
        );
    }

    public static void copyShortsToArray(long sourceAddress, short[] destination) {
        copyShortsToArray(sourceAddress, destination, 0, destination.length);
    }

    public static void copyShortsToArray(long sourceAddress, short[] destination, int destinationIndex, int elementCount) {
        UNSAFE.copyMemory(
                null,
                sourceAddress,
                destination,
                NativeUtils.shortArrayBaseOffset() + (((long) destinationIndex) << 1),
                ((long) elementCount) << 1
        );
    }

    public static void fillShorts(long address, int elementCount, short value) {
        if (elementCount == 0) {
            return;
        }

        UNSAFE.putShort(address, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            UNSAFE.copyMemory(address, address + (((long) copied) << 1), ((long) chunk) << 1);
            copied += chunk;
        }
    }

    public static long sumShorts(long address, int elementCount) {
        long sum = 0L;
        long current = address;
        for (int i = 0; i < elementCount; i++, current += Short.BYTES) {
            sum += UNSAFE.getShort(current);
        }
        return sum;
    }

    public static void copyLongsFromArray(long destinationAddress, long[] source) {
        copyLongsFromArray(destinationAddress, source, 0, source.length);
    }

    public static void copyLongsFromArray(long destinationAddress, long[] source, int sourceIndex, int elementCount) {
        UNSAFE.copyMemory(
                source,
                UNSAFE.arrayBaseOffset(long[].class) + (((long) sourceIndex) << 3),
                null,
                destinationAddress,
                ((long) elementCount) << 3
        );
    }

    public static void copyLongsToArray(long sourceAddress, long[] destination) {
        copyLongsToArray(sourceAddress, destination, 0, destination.length);
    }

    public static void copyLongsToArray(long sourceAddress, long[] destination, int destinationIndex, int elementCount) {
        UNSAFE.copyMemory(
                null,
                sourceAddress,
                destination,
                UNSAFE.arrayBaseOffset(long[].class) + (((long) destinationIndex) << 3),
                ((long) elementCount) << 3
        );
    }

    public static void fillLongs(long address, int elementCount, long value) {
        if (elementCount == 0) {
            return;
        }

        UNSAFE.putLong(address, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            UNSAFE.copyMemory(address, address + (((long) copied) << 3), ((long) chunk) << 3);
            copied += chunk;
        }
    }

    public static long sumLongs(long address, int elementCount) {
        long sum = 0L;
        long current = address;
        for (int i = 0; i < elementCount; i++, current += Long.BYTES) {
            sum += UNSAFE.getLong(current);
        }
        return sum;
    }

    public static void copyDoublesFromArray(long destinationAddress, double[] source) {
        copyDoublesFromArray(destinationAddress, source, 0, source.length);
    }

    public static void copyDoublesFromArray(long destinationAddress, double[] source, int sourceIndex, int elementCount) {
        UNSAFE.copyMemory(
                source,
                UNSAFE.arrayBaseOffset(double[].class) + (((long) sourceIndex) << 3),
                null,
                destinationAddress,
                ((long) elementCount) << 3
        );
    }

    public static void copyDoublesToArray(long sourceAddress, double[] destination) {
        copyDoublesToArray(sourceAddress, destination, 0, destination.length);
    }

    public static void copyDoublesToArray(long sourceAddress, double[] destination, int destinationIndex, int elementCount) {
        UNSAFE.copyMemory(
                null,
                sourceAddress,
                destination,
                UNSAFE.arrayBaseOffset(double[].class) + (((long) destinationIndex) << 3),
                ((long) elementCount) << 3
        );
    }

    public static void fillDoubles(long address, int elementCount, double value) {
        if (elementCount == 0) {
            return;
        }

        UNSAFE.putDouble(address, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            UNSAFE.copyMemory(address, address + (((long) copied) << 3), ((long) chunk) << 3);
            copied += chunk;
        }
    }

    public static double sumDoubles(long address, int elementCount) {
        double sum = 0.0d;
        long current = address;
        for (int i = 0; i < elementCount; i++, current += Double.BYTES) {
            sum += UNSAFE.getDouble(current);
        }
        return sum;
    }

    public static void copyBytesFromArray(long destinationAddress, byte[] source) {
        copyBytesFromArray(destinationAddress, source, 0, source.length);
    }

    public static void copyBytesFromArray(long destinationAddress, byte[] source, int sourceIndex, int elementCount) {
        UNSAFE.copyMemory(
                source,
                UNSAFE.arrayBaseOffset(byte[].class) + sourceIndex,
                null,
                destinationAddress,
                elementCount
        );
    }

    public static void copyBytesToArray(long sourceAddress, byte[] destination) {
        copyBytesToArray(sourceAddress, destination, 0, destination.length);
    }

    public static void copyBytesToArray(long sourceAddress, byte[] destination, int destinationIndex, int elementCount) {
        UNSAFE.copyMemory(
                null,
                sourceAddress,
                destination,
                UNSAFE.arrayBaseOffset(byte[].class) + destinationIndex,
                elementCount
        );
    }

    public static void fillBytes(long address, int elementCount, byte value) {
        UNSAFE.setMemory(address, elementCount, value);
    }

    public static long sumBytes(long address, int elementCount) {
        long sum = 0L;
        long current = address;
        for (int i = 0; i < elementCount; i++, current += Byte.BYTES) {
            sum += UNSAFE.getByte(current);
        }
        return sum;
    }

    public static void copyCharsFromArray(long destinationAddress, char[] source) {
        copyCharsFromArray(destinationAddress, source, 0, source.length);
    }

    public static void copyCharsFromArray(long destinationAddress, char[] source, int sourceIndex, int elementCount) {
        UNSAFE.copyMemory(
                source,
                UNSAFE.arrayBaseOffset(char[].class) + (((long) sourceIndex) << 1),
                null,
                destinationAddress,
                ((long) elementCount) << 1
        );
    }

    public static void copyCharsToArray(long sourceAddress, char[] destination) {
        copyCharsToArray(sourceAddress, destination, 0, destination.length);
    }

    public static void copyCharsToArray(long sourceAddress, char[] destination, int destinationIndex, int elementCount) {
        UNSAFE.copyMemory(
                null,
                sourceAddress,
                destination,
                UNSAFE.arrayBaseOffset(char[].class) + (((long) destinationIndex) << 1),
                ((long) elementCount) << 1
        );
    }

    public static void fillChars(long address, int elementCount, char value) {
        if (elementCount == 0) {
            return;
        }

        UNSAFE.putChar(address, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            UNSAFE.copyMemory(address, address + (((long) copied) << 1), ((long) chunk) << 1);
            copied += chunk;
        }
    }

    public static long sumChars(long address, int elementCount) {
        long sum = 0L;
        long current = address;
        for (int i = 0; i < elementCount; i++, current += Character.BYTES) {
            sum += UNSAFE.getChar(current);
        }
        return sum;
    }
}
