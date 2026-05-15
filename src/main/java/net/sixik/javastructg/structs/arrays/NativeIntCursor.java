package net.sixik.javastructg.structs.arrays;

public final class NativeIntCursor {

    private final NativeIntArray array;
    private final int startIndex;
    private final int limit;

    private long currentAddress;
    private int position;

    NativeIntCursor(NativeIntArray array, int startIndex, int elementCount) {
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

    public int get() {
        int value = NativeArray.unsafe.getInt(currentAddress);
        currentAddress += Integer.BYTES;
        position++;
        return value;
    }

    public void put(int value) {
        NativeArray.unsafe.putInt(currentAddress, value);
        currentAddress += Integer.BYTES;
        position++;
    }
}
