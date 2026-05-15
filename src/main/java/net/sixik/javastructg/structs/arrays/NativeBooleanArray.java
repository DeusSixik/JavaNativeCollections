package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypes;

public class NativeBooleanArray extends NativeArray {

    public NativeBooleanArray(int initialCapacity) {
        super(initialCapacity, NativeTypes.BOOLEAN);
    }

    public NativeBooleanArray(NativeBooleanArray otherArray) {
        super(otherArray);
    }

    public void add(boolean value) {
        if(length == capacity) {
            grow();
        }
        set(length, value);
        length++;
    }

    public void set(int index, boolean value) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.putByte(offset, value ? (byte) 1 : (byte) 0);
    }

    public boolean get(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        return unsafe.getByte(offset) == 1;
    }

    @Override
    public NativeArray copy() {
        return new NativeBooleanArray(this);
    }
}
