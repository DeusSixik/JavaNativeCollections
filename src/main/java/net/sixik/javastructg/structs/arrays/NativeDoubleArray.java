package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypes;

public class NativeDoubleArray extends NativeArray {

    public NativeDoubleArray(int initialCapacity) {
        super(initialCapacity, NativeTypes.DOUBLE);
    }

    public NativeDoubleArray(NativeDoubleArray otherArray) {
        super(otherArray);
    }

    public void add(double value) {
        if(length == capacity) {
            grow();
        }
        set(length, value);
        length++;
    }

    public void set(int index, double value) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.putDouble(offset, value);
    }

    public double get(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        return unsafe.getDouble(offset);
    }

    @Override
    public NativeArray copy() {
        return new NativeDoubleArray(this);
    }
}
