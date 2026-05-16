package net.sixik.javastructg.structs.deques;

import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import net.sixik.javastructg.utils.NativeUtils;
import sun.misc.Unsafe;

import java.util.NoSuchElementException;

public final class NativeArrayDeque<T> implements NativeType {

    private static final Unsafe UNSAFE = NativeUtils.getUnsafe();

    private final NativeTypeMemory<T> typeMemory;
    private final long elementSize;

    private NativeObjectArray<T> elements;
    private int capacity;
    private int mask;
    private int head;
    private int tail;
    private int size;

    public NativeArrayDeque(int initialCapacity, NativeTypeMemory<T> typeMemory) {
        this.typeMemory = typeMemory;
        this.elementSize = typeMemory.sizeof();
        this.capacity = NativeUtils.nextPowerOfTwo(Math.max(8, initialCapacity));
        this.mask = capacity - 1;
        this.elements = new NativeObjectArray<>(capacity, typeMemory);
    }

    public void addFirst(T value) {
        ensureCapacityForAdd();
        head = (head - 1) & mask;
        writeSlot(head, value);
        size++;
    }

    public void addLast(T value) {
        ensureCapacityForAdd();
        writeSlot(tail, value);
        tail = (tail + 1) & mask;
        size++;
    }

    public void removeFirst(T outBuffer) {
        if (!pollFirst(outBuffer)) {
            throw new NoSuchElementException("NativeArrayDeque is empty");
        }
    }

    public void removeLast(T outBuffer) {
        if (!pollLast(outBuffer)) {
            throw new NoSuchElementException("NativeArrayDeque is empty");
        }
    }

    public boolean pollFirst(T outBuffer) {
        if (size == 0) {
            return false;
        }

        readSlot(head, outBuffer);
        head = (head + 1) & mask;
        size--;
        return true;
    }

    public boolean pollLast(T outBuffer) {
        if (size == 0) {
            return false;
        }

        tail = (tail - 1) & mask;
        readSlot(tail, outBuffer);
        size--;
        return true;
    }

    public void getFirst(T outBuffer) {
        if (!peekFirst(outBuffer)) {
            throw new NoSuchElementException("NativeArrayDeque is empty");
        }
    }

    public void getLast(T outBuffer) {
        if (!peekLast(outBuffer)) {
            throw new NoSuchElementException("NativeArrayDeque is empty");
        }
    }

    public boolean peekFirst(T outBuffer) {
        if (size == 0) {
            return false;
        }

        readSlot(head, outBuffer);
        return true;
    }

    public boolean peekLast(T outBuffer) {
        if (size == 0) {
            return false;
        }

        readSlot((tail - 1) & mask, outBuffer);
        return true;
    }

    public boolean discardFirst() {
        if (size == 0) {
            return false;
        }

        head = (head + 1) & mask;
        size--;
        return true;
    }

    public boolean discardLast() {
        if (size == 0) {
            return false;
        }

        tail = (tail - 1) & mask;
        size--;
        return true;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int capacity() {
        return capacity;
    }

    public void clear() {
        head = 0;
        tail = 0;
        size = 0;
    }

    @Override
    public long sizeof() {
        return elements.sizeof();
    }

    @Override
    public void freeMemory() {
        if (elements != null) {
            elements.freeMemory();
            elements = null;
        }
        capacity = 0;
        mask = 0;
        head = 0;
        tail = 0;
        size = 0;
    }

    private void ensureCapacityForAdd() {
        if (size == capacity) {
            grow();
        }
    }

    private void grow() {
        NativeObjectArray<T> oldElements = elements;
        int oldCapacity = capacity;
        int oldMask = mask;
        int newCapacity = oldCapacity << 1;
        NativeObjectArray<T> newElements = new NativeObjectArray<>(newCapacity, typeMemory);

        for (int i = 0; i < size; i++) {
            UNSAFE.copyMemory(
                    oldElements.addressAt((head + i) & oldMask),
                    newElements.addressAt(i),
                    elementSize
            );
        }

        oldElements.freeMemory();
        elements = newElements;
        capacity = newCapacity;
        mask = newCapacity - 1;
        head = 0;
        tail = size;
    }

    private void writeSlot(int index, T value) {
        typeMemory.writeToMemory(UNSAFE, elements.addressAt(index), value);
    }

    private void readSlot(int index, T outBuffer) {
        if (outBuffer != null) {
            typeMemory.readFromMemory(UNSAFE, elements.addressAt(index), outBuffer);
        }
    }
}
