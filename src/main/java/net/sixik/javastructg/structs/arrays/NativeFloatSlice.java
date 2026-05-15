package net.sixik.javastructg.structs.arrays;

public final class NativeFloatSlice {

    private static final int ELEMENT_SHIFT = 2;

    private final NativeFloatArray array;
    private final long baseAddress;
    private final int length;

    NativeFloatSlice(NativeFloatArray array, int startIndex, int length) {
        this.array = array;
        this.baseAddress = array.addressAt(startIndex);
        this.length = length;
    }

    public int length() {
        return length;
    }

    public long ptr() {
        return baseAddress;
    }

    public long addressAt(int index) {
        return baseAddress + (((long) index) << ELEMENT_SHIFT);
    }

    public float get(int index) {
        return NativeArray.unsafe.getFloat(addressAt(index));
    }

    public void set(int index, float value) {
        NativeArray.unsafe.putFloat(addressAt(index), value);
    }

    public void copyFrom(float[] source) {
        copyFrom(source, 0, 0, source.length);
    }

    public void copyFrom(float[] source, int sourceIndex, int destinationIndex, int elementCount) {
        NativeArray.unsafe.copyMemory(
                source,
                net.sixik.javastructg.structs.NativeUtils.floatArrayBaseOffset() + (((long) sourceIndex) << ELEMENT_SHIFT),
                null,
                addressAt(destinationIndex),
                ((long) elementCount) << ELEMENT_SHIFT
        );
    }

    public void copyTo(float[] destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, float[] destination, int destinationIndex, int elementCount) {
        NativeArray.unsafe.copyMemory(
                null,
                addressAt(sourceIndex),
                destination,
                net.sixik.javastructg.structs.NativeUtils.floatArrayBaseOffset() + (((long) destinationIndex) << ELEMENT_SHIFT),
                ((long) elementCount) << ELEMENT_SHIFT
        );
    }

    public void copyTo(NativeFloatSlice destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, NativeFloatSlice destination, int destinationIndex, int elementCount) {
        array.copyNativeMemory(addressAt(sourceIndex), destination.addressAt(destinationIndex), ((long) elementCount) << ELEMENT_SHIFT);
    }

    public void fill(float value) {
        fill(0, length, value);
    }

    public void fill(int startIndex, int elementCount, float value) {
        if (elementCount == 0) {
            return;
        }

        long startAddress = addressAt(startIndex);
        NativeArray.unsafe.putFloat(startAddress, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            array.copyNativeMemory(startAddress, startAddress + (((long) copied) << ELEMENT_SHIFT), ((long) chunk) << ELEMENT_SHIFT);
            copied += chunk;
        }
    }

    public NativeFloatCursor cursor() {
        return new NativeFloatCursor(array, (int) ((baseAddress - array.ptr()) >> ELEMENT_SHIFT), length);
    }
}
