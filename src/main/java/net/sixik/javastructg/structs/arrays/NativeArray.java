package net.sixik.javastructg.structs.arrays;

import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.structs.NativeUtils;
import sun.misc.Unsafe;

public abstract class NativeArray implements NativeType {

    protected static final Unsafe unsafe = NativeUtils.getUnsafe();

    protected int capacity;
    protected int length = 0;
    protected long memoryAddress;
    protected long size_on_memory;

    public NativeArray(int initialCapacity, long size_on_memory) {
        this.capacity = initialCapacity;
        this.size_on_memory = size_on_memory;
        long totalBytes = (long) this.capacity * this.size_on_memory;
        this.memoryAddress = unsafe.allocateMemory(totalBytes);
        unsafe.setMemory(this.memoryAddress, totalBytes, (byte) 0);
    }

    protected NativeArray(NativeArray otherArray) {
        this.capacity = otherArray.capacity;
        this.size_on_memory = otherArray.size_on_memory;
        this.memoryAddress = otherArray.memoryAddress;
        this.length = otherArray.length;

        long totalBytes = (long) this.capacity * this.size_on_memory;
        if (totalBytes > 0) {
            this.memoryAddress = unsafe.allocateMemory(totalBytes);
            unsafe.copyMemory(otherArray.memoryAddress, this.memoryAddress, totalBytes);
        } else {
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
        if (memoryAddress != 0) {
            unsafe.freeMemory(memoryAddress);
            memoryAddress = 0;
            capacity = 0;
            length = 0;
        }
    }

    public long ptr() {
        return memoryAddress;
    }

    protected void grow() {
        int newCapacity = capacity + (capacity >> 1); // * 1.5
        if (newCapacity < 10) {
            newCapacity = 10;
        }

        long oldTotalBytes = (long) capacity * size_on_memory;
        long newTotalBytes = (long) newCapacity * size_on_memory;

        long newAddress = unsafe.allocateMemory(newTotalBytes);
        unsafe.setMemory(newAddress, newTotalBytes, (byte) 0);

        if (capacity > 0) {
            unsafe.copyMemory(this.memoryAddress, newAddress, oldTotalBytes);
            unsafe.freeMemory(this.memoryAddress);
        }

        this.memoryAddress = newAddress;
        this.capacity = newCapacity;
    }

    /**
     * Очищает слот памяти, заполняя его нулями. Индексы остальных элементов НЕ сдвигаются.
     */
    public void clearSlot(int index) {
        if (index < 0 || index >= capacity) return;

        long offset = memoryAddress + ((long) index * size_on_memory);
        unsafe.setMemory(offset, size_on_memory, (byte) 0);
    }

    public void remove(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + length);
        }

        /*
            Количество элементов которых нам нужно сдвинуть в лево
         */
        int numMoved = length - index - 1;

        if (numMoved > 0) {
            long srcOffset = memoryAddress + ((long) (index + 1) * size_on_memory);
            long dstOffset = memoryAddress + ((long) index * size_on_memory);

            /*
                Cдвигаем хвост массива на один слот влево
             */
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
