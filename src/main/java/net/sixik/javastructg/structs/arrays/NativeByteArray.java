package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypes;

public class NativeByteArray extends NativeArray {

    public NativeByteArray(int initialCapacity) {
        super(initialCapacity, NativeTypes.BYTE);
    }

    public NativeByteArray(NativeByteArray otherArray) {
        super(otherArray);
    }

    public void add(byte value) {
        if(length == capacity) {
            grow();
        }
        set(length, value);
        length++;
    }

    public void set(int index, byte value) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.putByte(offset, value);
    }

    public byte get(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        return unsafe.getByte(offset);
    }

    @Override
    public NativeArray copy() {
        return new NativeByteArray(this);
    }
}
