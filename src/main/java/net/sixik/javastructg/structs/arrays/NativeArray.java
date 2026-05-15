package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.structs.NativeUtils;
import sun.misc.Unsafe;

public abstract class NativeArray implements NativeType {

    protected static final Unsafe unsafe = NativeUtils.getUnsafe();
    public static final long MEMORY_ALIGNMENT = 64L;

    protected int capacity;
    protected int length = 0;
    protected long rawMemoryAddress;
    protected long memoryAddress;
    protected long size_on_memory;

    public NativeArray(int initialCapacity, long size_on_memory) {
        this.capacity = initialCapacity;
        this.size_on_memory = size_on_memory;
        long totalBytes = (long) this.capacity * this.size_on_memory;
        this.rawMemoryAddress = allocateAlignedRaw(totalBytes);
        this.memoryAddress = alignedView(this.rawMemoryAddress);
        unsafe.setMemory(this.memoryAddress, totalBytes, (byte) 0);
    }

    protected NativeArray(NativeArray otherArray) {
        this.capacity = otherArray.capacity;
        this.size_on_memory = otherArray.size_on_memory;
        this.length = otherArray.length;

        long totalBytes = (long) this.capacity * this.size_on_memory;
        if (totalBytes > 0) {
            this.rawMemoryAddress = allocateAlignedRaw(totalBytes);
            this.memoryAddress = alignedView(this.rawMemoryAddress);
            unsafe.copyMemory(otherArray.memoryAddress, this.memoryAddress, totalBytes);
        } else {
            this.rawMemoryAddress = 0;
            this.memoryAddress = 0;
        }
    }

    public abstract NativeArray copy();

    @Override
    public long sizeof() {
        return (long) capacity * size_on_memory;
    }

    public long elementSizeof() {
        return size_on_memory;
    }

    public int size() {
        return length;
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public void freeMemory() {
        if (rawMemoryAddress != 0) {
            unsafe.freeMemory(rawMemoryAddress);
            rawMemoryAddress = 0;
            memoryAddress = 0;
            capacity = 0;
            length = 0;
        }
    }

    public long ptr() {
        return memoryAddress;
    }

    protected final void markWritten(int elementCount) {
        if (length < elementCount) {
            length = elementCount;
        }
    }

    protected final void copyNativeMemory(long sourceAddress, long destinationAddress, long bytes) {
        if (bytes <= 0 || sourceAddress == destinationAddress) {
            return;
        }
        unsafe.copyMemory(sourceAddress, destinationAddress, bytes);
    }

    protected void grow() {
        int newCapacity = capacity + (capacity >> 1); // * 1.5
        if (newCapacity < 10) {
            newCapacity = 10;
        }

        long oldTotalBytes = (long) capacity * size_on_memory;
        long newTotalBytes = (long) newCapacity * size_on_memory;

        long newRawAddress = allocateAlignedRaw(newTotalBytes);
        long newAddress = alignedView(newRawAddress);
        unsafe.setMemory(newAddress, newTotalBytes, (byte) 0);

        if (capacity > 0) {
            unsafe.copyMemory(this.memoryAddress, newAddress, oldTotalBytes);
            unsafe.freeMemory(this.rawMemoryAddress);
        }

        this.rawMemoryAddress = newRawAddress;
        this.memoryAddress = newAddress;
        this.capacity = newCapacity;
    }

    private static long allocateAlignedRaw(long totalBytes) {
        if (totalBytes <= 0) {
            return 0;
        }
        return unsafe.allocateMemory(totalBytes + MEMORY_ALIGNMENT - 1);
    }

    private static long alignedView(long rawAddress) {
        if (rawAddress == 0) {
            return 0;
        }
        long mask = MEMORY_ALIGNMENT - 1;
        return (rawAddress + mask) & ~mask;
    }

    public void clearSlot(int index) {
        if (index < 0 || index >= capacity) return;

        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.setMemory(offset, size_on_memory, (byte) 0);
    }

    public void remove(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + length);
        }

        int numMoved = length - index - 1;

        if (numMoved > 0) {
            long srcOffset = memoryAddress + ((long) (index + 1) * size_on_memory);
            long dstOffset = memoryAddress + ((long) index * size_on_memory);
            unsafe.copyMemory(srcOffset, dstOffset, numMoved * size_on_memory);
        }

        length--;
    }

    public boolean isNull(int index) {
        long offset = memoryAddress + ((long) index * size_on_memory);
        short id = unsafe.getShort(offset);
        return id == 0;
    }
}
