package net.sixik.javastructg.structs.arrays;

public final class NativeIntSlice {

    private static final int ELEMENT_SHIFT = 2;

    private final NativeIntArray array;
    private final long baseAddress;
    private final int length;

    NativeIntSlice(NativeIntArray array, int startIndex, int length) {
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

    public int get(int index) {
        return NativeArray.unsafe.getInt(addressAt(index));
    }

    public void set(int index, int value) {
        NativeArray.unsafe.putInt(addressAt(index), value);
    }

    public void copyFrom(int[] source) {
        copyFrom(source, 0, 0, source.length);
    }

    public void copyFrom(int[] source, int sourceIndex, int destinationIndex, int elementCount) {
        NativeArray.unsafe.copyMemory(
                source,
                net.sixik.javastructg.structs.NativeUtils.intArrayBaseOffset() + (((long) sourceIndex) << ELEMENT_SHIFT),
                null,
                addressAt(destinationIndex),
                ((long) elementCount) << ELEMENT_SHIFT
        );
    }

    public void copyTo(int[] destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, int[] destination, int destinationIndex, int elementCount) {
        NativeArray.unsafe.copyMemory(
                null,
                addressAt(sourceIndex),
                destination,
                net.sixik.javastructg.structs.NativeUtils.intArrayBaseOffset() + (((long) destinationIndex) << ELEMENT_SHIFT),
                ((long) elementCount) << ELEMENT_SHIFT
        );
    }

    public void copyTo(NativeIntSlice destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, NativeIntSlice destination, int destinationIndex, int elementCount) {
        array.copyNativeMemory(addressAt(sourceIndex), destination.addressAt(destinationIndex), ((long) elementCount) << ELEMENT_SHIFT);
    }

    public void fill(int value) {
        fill(0, length, value);
    }

    public void fill(int startIndex, int elementCount, int value) {
        if (elementCount == 0) {
            return;
        }

        long startAddress = addressAt(startIndex);
        NativeArray.unsafe.putInt(startAddress, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            array.copyNativeMemory(startAddress, startAddress + (((long) copied) << ELEMENT_SHIFT), ((long) chunk) << ELEMENT_SHIFT);
            copied += chunk;
        }
    }

    public NativeIntCursor cursor() {
        return new NativeIntCursor(array, (int) ((baseAddress - array.ptr()) >> ELEMENT_SHIFT), length);
    }
}
