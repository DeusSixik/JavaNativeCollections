package net.sixik.javastructg.structs.deques;

import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.utils.NativeUtils;

import java.util.NoSuchElementException;

public final class NativeIntDeque implements NativeType {

    private NativeIntArray elements;
    private int capacity;
    private int mask;
    private int head;
    private int tail;
    private int size;

    public NativeIntDeque(int initialCapacity) {
        this.capacity = NativeUtils.nextPowerOfTwo(Math.max(8, initialCapacity));
        this.mask = capacity - 1;
        this.elements = new NativeIntArray(capacity);
    }

    public void addFirst(int value) {
        ensureCapacityForAdd();
        head = (head - 1) & mask;
        elements.set(head, value);
        size++;
    }

    public void addLast(int value) {
        ensureCapacityForAdd();
        elements.set(tail, value);
        tail = (tail + 1) & mask;
        size++;
    }

    public int removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException("NativeIntDeque is empty");
        }

        int value = elements.get(head);
        head = (head + 1) & mask;
        size--;
        return value;
    }

    public int removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("NativeIntDeque is empty");
        }

        tail = (tail - 1) & mask;
        int value = elements.get(tail);
        size--;
        return value;
    }

    public int pollFirst(int defaultValue) {
        return size == 0 ? defaultValue : removeFirst();
    }

    public int pollLast(int defaultValue) {
        return size == 0 ? defaultValue : removeLast();
    }

    public int getFirst() {
        if (size == 0) {
            throw new NoSuchElementException("NativeIntDeque is empty");
        }
        return elements.get(head);
    }

    public int getLast() {
        if (size == 0) {
            throw new NoSuchElementException("NativeIntDeque is empty");
        }
        return elements.get((tail - 1) & mask);
    }

    public int peekFirst(int defaultValue) {
        return size == 0 ? defaultValue : elements.get(head);
    }

    public int peekLast(int defaultValue) {
        return size == 0 ? defaultValue : elements.get((tail - 1) & mask);
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
        NativeIntArray oldElements = elements;
        int oldCapacity = capacity;
        int oldMask = mask;
        int newCapacity = oldCapacity << 1;
        NativeIntArray newElements = new NativeIntArray(newCapacity);

        for (int i = 0; i < size; i++) {
            newElements.set(i, oldElements.get((head + i) & oldMask));
        }

        oldElements.freeMemory();
        elements = newElements;
        capacity = newCapacity;
        mask = newCapacity - 1;
        head = 0;
        tail = size;
    }
}
