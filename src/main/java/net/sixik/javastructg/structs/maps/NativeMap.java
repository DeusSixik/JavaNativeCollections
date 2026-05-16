package net.sixik.javastructg.structs.maps;

import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.utils.NativeUtils;

public abstract class NativeMap implements NativeType {

    protected static final byte EMPTY = 0;
    protected static final byte USED = 1;
    protected static final byte DELETED = 2;

    protected int capacity;
    protected int size;
    protected int mask;
    protected int usedSlots;

    protected NativeMap(int initialCapacity) {
        this.capacity = NativeUtils.nextPowerOfTwo(Math.max(8, initialCapacity));
        this.mask = capacity - 1;
        this.size = 0;
        this.usedSlots = 0;
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

    protected void ensureCapacityForInsert() {
        if (usedSlots * 4 >= capacity * 3) {
            rehash(capacity * 2);
        }
    }

    protected abstract void rehash(int newCapacity);

    public void clear() {
        size = 0;
        usedSlots = 0;
    }

    @Override
    public void freeMemory() {
        capacity = 0;
        mask = 0;
        size = 0;
        usedSlots = 0;
    }
}
