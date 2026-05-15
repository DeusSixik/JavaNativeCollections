package net.sixik.javastructg.structs.arrays;

public final class NativeShortSlice {

    private static final int ELEMENT_SHIFT = 1;

    private final NativeShortArray array;
    private final long baseAddress;
    private final int length;

    NativeShortSlice(NativeShortArray array, int startIndex, int length) {
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

    public short get(int index) {
        return NativeArray.unsafe.getShort(addressAt(index));
    }

    public void set(int index, short value) {
        NativeArray.unsafe.putShort(addressAt(index), value);
    }

    public void copyFrom(short[] source) {
        copyFrom(source, 0, 0, source.length);
    }

    public void copyFrom(short[] source, int sourceIndex, int destinationIndex, int elementCount) {
        NativeArray.unsafe.copyMemory(
                source,
                net.sixik.javastructg.structs.NativeUtils.shortArrayBaseOffset() + (((long) sourceIndex) << ELEMENT_SHIFT),
                null,
                addressAt(destinationIndex),
                ((long) elementCount) << ELEMENT_SHIFT
        );
    }

    public void copyTo(short[] destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, short[] destination, int destinationIndex, int elementCount) {
        NativeArray.unsafe.copyMemory(
                null,
                addressAt(sourceIndex),
                destination,
                net.sixik.javastructg.structs.NativeUtils.shortArrayBaseOffset() + (((long) destinationIndex) << ELEMENT_SHIFT),
                ((long) elementCount) << ELEMENT_SHIFT
        );
    }

    public void copyTo(NativeShortSlice destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, NativeShortSlice destination, int destinationIndex, int elementCount) {
        array.copyNativeMemory(addressAt(sourceIndex), destination.addressAt(destinationIndex), ((long) elementCount) << ELEMENT_SHIFT);
    }

    public void fill(short value) {
        fill(0, length, value);
    }

    public void fill(int startIndex, int elementCount, short value) {
        if (elementCount == 0) {
            return;
        }

        long startAddress = addressAt(startIndex);
        NativeArray.unsafe.putShort(startAddress, value);

        int copied = 1;
        while (copied < elementCount) {
            int chunk = Math.min(copied, elementCount - copied);
            array.copyNativeMemory(startAddress, startAddress + (((long) copied) << ELEMENT_SHIFT), ((long) chunk) << ELEMENT_SHIFT);
            copied += chunk;
        }
    }

    public NativeShortCursor cursor() {
        return new NativeShortCursor(array, (int) ((baseAddress - array.ptr()) >> ELEMENT_SHIFT), length);
    }
}
