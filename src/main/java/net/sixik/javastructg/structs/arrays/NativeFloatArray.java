package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeTypes;

public class NativeFloatArray extends NativeArray {

    private static final int ELEMENT_SHIFT = 2;

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

    public long addressAt(int index) {
        return memoryAddress + (((long) index) << ELEMENT_SHIFT);
    }

    public void set(int index, float value) {
        unsafe.putFloat(addressAt(index), value);
    }

    public float get(int index) {
        return unsafe.getFloat(addressAt(index));
    }

    public void copyFrom(float[] source) {
        copyFrom(source, source.length);
    }

    public void copyFrom(float[] source, int elementCount) {
        copyFrom(source, 0, 0, elementCount);
    }

    public void copyTo(float[] destination) {
        copyTo(destination, length);
    }

    public void copyTo(float[] destination, int elementCount) {
        copyTo(0, destination, 0, elementCount);
    }

    public void copyFrom(float[] source, int sourceIndex, int destinationIndex, int elementCount) {
        NativeRawPrimitives.copyFloatsFromArray(addressAt(destinationIndex), source, sourceIndex, elementCount);
        markWritten(destinationIndex + elementCount);
    }

    public void copyTo(int sourceIndex, float[] destination, int destinationIndex, int elementCount) {
        NativeRawPrimitives.copyFloatsToArray(addressAt(sourceIndex), destination, destinationIndex, elementCount);
    }

    public void copyTo(NativeFloatArray destination) {
        copyTo(0, destination, 0, length);
    }

    public void copyTo(int sourceIndex, NativeFloatArray destination, int destinationIndex, int elementCount) {
        copyNativeMemory(addressAt(sourceIndex), destination.addressAt(destinationIndex), ((long) elementCount) << ELEMENT_SHIFT);
        destination.markWritten(destinationIndex + elementCount);
    }

    public void fill(float value) {
        fill(0, capacity, value);
    }

    public void fill(int startIndex, int elementCount, float value) {
        NativeRawPrimitives.fillFloats(addressAt(startIndex), elementCount, value);
        markWritten(startIndex + elementCount);
    }

    public NativeFloatCursor cursor() {
        return cursor(0, length);
    }

    public NativeFloatCursor cursorFrom(int startIndex) {
        return cursor(startIndex, length - startIndex);
    }

    public NativeFloatSlice slice() {
        return new NativeFloatSlice(this, 0, length);
    }

    public NativeFloatSlice slice(int startIndex, int elementCount) {
        return new NativeFloatSlice(this, startIndex, elementCount);
    }

    public NativeFloatSlice tailSlice(int startIndex) {
        return new NativeFloatSlice(this, startIndex, length - startIndex);
    }

    public NativeFloatSlice writeSlice(int startIndex, int elementCount) {
        markWritten(startIndex + elementCount);
        return new NativeFloatSlice(this, startIndex, elementCount);
    }

    public NativeFloatCursor writeCursor(int elementCount) {
        return writeCursor(0, elementCount);
    }

    public NativeFloatCursor writeCursor(int startIndex, int elementCount) {
        markWritten(startIndex + elementCount);
        return new NativeFloatCursor(this, startIndex, elementCount);
    }

    private NativeFloatCursor cursor(int startIndex, int elementCount) {
        return new NativeFloatCursor(this, startIndex, elementCount);
    }

    @Override
    public NativeArray copy() {
        return new NativeFloatArray(this);
    }
}
