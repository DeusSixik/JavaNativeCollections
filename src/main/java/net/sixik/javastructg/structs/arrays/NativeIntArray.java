package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeTypes;

public class NativeIntArray extends NativeArray {

    private static final int ELEMENT_SHIFT = 2;

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

    public long addressAt(int index) {
        return memoryAddress + (((long) index) << ELEMENT_SHIFT);
    }

    public void set(int index, int value) {
        unsafe.putInt(addressAt(index), value);
    }

    public int get(int index) {
        return unsafe.getInt(addressAt(index));
    }

    public void copyFrom(int[] source) {
        copyFrom(source, source.length);
    }

    public void copyFrom(int[] source, int elementCount) {
        copyFrom(source, 0, 0, elementCount);
    }

    public void copyFrom(int[] source, int sourceIndex, int destinationIndex, int elementCount) {
        NativeRawPrimitives.copyIntsFromArray(addressAt(destinationIndex), source, sourceIndex, elementCount);
        markWritten(destinationIndex + elementCount);
    }

    public void copyTo(int[] destination) {
        copyTo(destination, length);
    }

    public void copyTo(int[] destination, int elementCount) {
        copyTo(0, destination, 0, elementCount);
    }

    public void copyTo(int sourceIndex, int[] destination, int destinationIndex, int elementCount) {
        NativeRawPrimitives.copyIntsToArray(addressAt(sourceIndex), destination, destinationIndex, elementCount);
    }

    public void copyTo(NativeIntArray destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, NativeIntArray destination, int destinationIndex, int elementCount) {
        copyNativeMemory(addressAt(sourceIndex), destination.addressAt(destinationIndex), ((long) elementCount) << ELEMENT_SHIFT);
        destination.markWritten(destinationIndex + elementCount);
    }

    public void fill(int value) {
        fill(0, capacity, value);
    }

    public void fill(int startIndex, int elementCount, int value) {
        NativeRawPrimitives.fillInts(addressAt(startIndex), elementCount, value);
        markWritten(startIndex + elementCount);
    }

    public NativeIntCursor cursor() {
        return cursor(0, length);
    }

    public NativeIntCursor cursorFrom(int startIndex) {
        return cursor(startIndex, length - startIndex);
    }

    public NativeIntSlice slice() {
        return new NativeIntSlice(this, 0, length);
    }

    public NativeIntSlice slice(int startIndex, int elementCount) {
        return new NativeIntSlice(this, startIndex, elementCount);
    }

    public NativeIntSlice tailSlice(int startIndex) {
        return new NativeIntSlice(this, startIndex, length - startIndex);
    }

    public NativeIntSlice writeSlice(int startIndex, int elementCount) {
        markWritten(startIndex + elementCount);
        return new NativeIntSlice(this, startIndex, elementCount);
    }

    public NativeIntCursor writeCursor(int elementCount) {
        return writeCursor(0, elementCount);
    }

    public NativeIntCursor writeCursor(int startIndex, int elementCount) {
        markWritten(startIndex + elementCount);
        return new NativeIntCursor(this, startIndex, elementCount);
    }

    private NativeIntCursor cursor(int startIndex, int elementCount) {
        return new NativeIntCursor(this, startIndex, elementCount);
    }

    @Override
    public NativeArray copy() {
        return new NativeIntArray(this);
    }
}
