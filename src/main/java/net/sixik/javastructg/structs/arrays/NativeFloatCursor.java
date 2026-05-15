package net.sixik.javastructg.structs.arrays;

public final class NativeFloatCursor {

    private final NativeFloatArray array;
    private final int startIndex;
    private final int limit;

    private long currentAddress;
    private int position;

    NativeFloatCursor(NativeFloatArray array, int startIndex, int elementCount) {
        this.array = array;
        this.startIndex = startIndex;
        this.limit = elementCount;
        rewind();
    }

    public boolean hasRemaining() {
        return position < limit;
    }

    public int remaining() {
        return limit - position;
    }

    public int position() {
        return position;
    }

    public int limit() {
        return limit;
    }

    public long address() {
        return currentAddress;
    }

    public void rewind() {
        this.position = 0;
        this.currentAddress = array.addressAt(startIndex);
    }

    public void seek(int index) {
        this.position = index;
        this.currentAddress = array.addressAt(startIndex + index);
    }

    public float get() {
        float value = NativeArray.unsafe.getFloat(currentAddress);
        currentAddress += Float.BYTES;
        position++;
        return value;
    }

    public void put(float value) {
        NativeArray.unsafe.putFloat(currentAddress, value);
        currentAddress += Float.BYTES;
        position++;
    }
}
