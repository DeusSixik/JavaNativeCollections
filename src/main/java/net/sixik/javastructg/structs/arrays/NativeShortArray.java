package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeTypes;

public class NativeShortArray extends NativeArray {

    private static final int ELEMENT_SHIFT = 1;

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

    public long addressAt(int index) {
        return memoryAddress + (((long) index) << ELEMENT_SHIFT);
    }

    public void set(int index, short value) {
        unsafe.putShort(addressAt(index), value);
    }

    public short get(int index) {
        return unsafe.getShort(addressAt(index));
    }

    public void copyFrom(short[] source) {
        copyFrom(source, source.length);
    }

    public void copyFrom(short[] source, int elementCount) {
        copyFrom(source, 0, 0, elementCount);
    }

    public void copyTo(short[] destination) {
        copyTo(destination, length);
    }

    public void copyTo(short[] destination, int elementCount) {
        copyTo(0, destination, 0, elementCount);
    }

    public void copyFrom(short[] source, int sourceIndex, int destinationIndex, int elementCount) {
        NativeRawPrimitives.copyShortsFromArray(addressAt(destinationIndex), source, sourceIndex, elementCount);
        markWritten(destinationIndex + elementCount);
    }

    public void copyTo(int sourceIndex, short[] destination, int destinationIndex, int elementCount) {
        NativeRawPrimitives.copyShortsToArray(addressAt(sourceIndex), destination, destinationIndex, elementCount);
    }

    public void copyTo(NativeShortArray destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, NativeShortArray destination, int destinationIndex, int elementCount) {
        copyNativeMemory(addressAt(sourceIndex), destination.addressAt(destinationIndex), ((long) elementCount) << ELEMENT_SHIFT);
        destination.markWritten(destinationIndex + elementCount);
    }

    public void fill(short value) {
        fill(0, capacity, value);
    }

    public void fill(int startIndex, int elementCount, short value) {
        NativeRawPrimitives.fillShorts(addressAt(startIndex), elementCount, value);
        markWritten(startIndex + elementCount);
    }

    public NativeShortCursor cursor() {
        return cursor(0, length);
    }

    public NativeShortCursor cursorFrom(int startIndex) {
        return cursor(startIndex, length - startIndex);
    }

    public NativeShortSlice slice() {
        return new NativeShortSlice(this, 0, length);
    }

    public NativeShortSlice slice(int startIndex, int elementCount) {
        return new NativeShortSlice(this, startIndex, elementCount);
    }

    public NativeShortSlice tailSlice(int startIndex) {
        return new NativeShortSlice(this, startIndex, length - startIndex);
    }

    public NativeShortSlice writeSlice(int startIndex, int elementCount) {
        markWritten(startIndex + elementCount);
        return new NativeShortSlice(this, startIndex, elementCount);
    }

    public NativeShortCursor writeCursor(int elementCount) {
        return writeCursor(0, elementCount);
    }

    public NativeShortCursor writeCursor(int startIndex, int elementCount) {
        markWritten(startIndex + elementCount);
        return new NativeShortCursor(this, startIndex, elementCount);
    }

    private NativeShortCursor cursor(int startIndex, int elementCount) {
        return new NativeShortCursor(this, startIndex, elementCount);
    }

    @Override
    public NativeArray copy() {
        return new NativeShortArray(this);
    }
}
