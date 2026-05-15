package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypes;

public class NativeFloatArray extends NativeArray {

    public NativeFloatArray(int initialCapacity) {
        super(initialCapacity, NativeTypes.FLOAT);
    }

    public NativeFloatArray(NativeFloatArray otherArray) {
        super(otherArray);
    }

    public void add(float value) {
        if(length == capacity) {
            grow();
        }
        set(length, value);
        length++;
    }

    public void set(int index, float value) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.putFloat(offset, value);
    }

    public float get(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        return unsafe.getFloat(offset);
    }

    @Override
    public NativeArray copy() {
        return new NativeFloatArray(this);
    }
}
