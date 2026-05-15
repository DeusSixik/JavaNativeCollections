package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypes;

public class NativeShortArray extends NativeArray {

    public NativeShortArray(int initialCapacity) {
        super(initialCapacity, NativeTypes.SHORT);
    }

    public NativeShortArray(NativeShortArray otherArray) {
        super(otherArray);
    }

    public void add(short value) {
        if(length == capacity) {
            grow();
        }
        set(length, value);
        length++;
    }

    public void set(int index, short value) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.putShort(offset, value);
    }

    public short get(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        return unsafe.getShort(offset);
    }

    @Override
    public NativeArray copy() {
        return new NativeShortArray(this);
    }
}
