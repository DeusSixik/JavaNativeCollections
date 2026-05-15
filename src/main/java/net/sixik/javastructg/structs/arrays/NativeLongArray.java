package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypes;

public class NativeLongArray extends NativeArray {

    public NativeLongArray(int initialCapacity) {
        super(initialCapacity, NativeTypes.LONG);
    }

    public NativeLongArray(NativeLongArray otherArray) {
        super(otherArray);
    }

    public void add(long value) {
        if(length == capacity) {
            grow();
        }
        set(length, value);
        length++;
    }

    public void set(int index, long value) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.putLong(offset, value);
    }

    public long get(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        return unsafe.getLong(offset);
    }

    @Override
    public NativeArray copy() {
        return new NativeLongArray(this);
    }
}
