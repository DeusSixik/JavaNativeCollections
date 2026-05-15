package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypes;

public class NativeCharArray extends NativeArray {

    public NativeCharArray(int initialCapacity) {
        super(initialCapacity, NativeTypes.CHAR);
    }

    public NativeCharArray(NativeCharArray otherArray) {
        super(otherArray);
    }

    public void add(char value) {
        if(length == capacity) {
            grow();
        }
        set(length, value);
        length++;
    }

    public void set(int index, char value) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.putChar(offset, value);
    }

    public char get(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        return unsafe.getChar(offset);
    }

    @Override
    public NativeArray copy() {
        return new NativeCharArray(this);
    }
}
