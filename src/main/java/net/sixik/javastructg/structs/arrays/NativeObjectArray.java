package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeTypeMemory;

public class NativeObjectArray<Element> extends NativeArray {

    private final NativeTypeMemory<Element> typeMemory;

    public NativeObjectArray(int initialCapacity, NativeTypeMemory<Element> typeMemory) {
        super(initialCapacity, typeMemory.sizeof());
        this.typeMemory = typeMemory;
    }

    public NativeObjectArray(NativeObjectArray<Element> otherArray) {
        super(otherArray);
        this.typeMemory = otherArray.typeMemory;
    }

    public void add(Element element) {
        if (length == capacity) {
            grow();
        }

        set(length, element);
        length++;
    }

    public void set(int index, Element element) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        typeMemory.writeToMemory(unsafe, offset, element);
    }

    public void get(int index, Element outBuffer) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        typeMemory.readFromMemory(unsafe, offset, outBuffer);
    }

    @Override
    public NativeArray copy() {
        return new NativeObjectArray<>(this);
    }
}
