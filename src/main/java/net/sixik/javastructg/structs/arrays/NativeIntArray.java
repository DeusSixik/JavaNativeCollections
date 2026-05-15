package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypes;

public class NativeIntArray extends NativeArray {

    public NativeIntArray(int initialCapacity) {
        super(initialCapacity, NativeTypes.INT);
    }

    public NativeIntArray(NativeIntArray otherArray) {
        super(otherArray);
    }

    public void add(int value) {
        if(length == capacity) {
            grow();
        }
        set(length, value);
        length++;
    }

    public void set(int index, int value) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.putInt(offset, value);
    }

    public int get(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        return unsafe.getInt(offset);
    }

    @Override
    public NativeArray copy() {
        return new NativeIntArray(this);
    }
}
