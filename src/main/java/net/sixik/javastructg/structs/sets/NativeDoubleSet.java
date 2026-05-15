package net.sixik.javastructg.structs.sets;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeDoubleArray;
import net.sixik.javastructg.utils.NativeUtils;

public final class NativeDoubleSet extends NativeSet {

    private NativeByteArray states;
    private NativeDoubleArray keys;

    public NativeDoubleSet(int initialCapacity) {
        super(initialCapacity);
        this.states = new NativeByteArray(capacity);
        this.keys = new NativeDoubleArray(capacity);
    }

    public boolean add(double value) {
        ensureCapacityForInsert();

        int index = (int) (NativeUtils.mix(value) & mask);
        int firstDeleted = -1;

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                int target = firstDeleted >= 0 ? firstDeleted : index;
                if (firstDeleted < 0) {
                    usedSlots++;
                }
                states.set(target, USED);
                keys.set(target, value);
                size++;
                return true;
            }

            if (state == DELETED) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (sameBits(keys.get(index), value)) {
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean contains(double value) {
        int index = (int) (NativeUtils.mix(value) & mask);

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && sameBits(keys.get(index), value)) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(double value) {
        int index = (int) (NativeUtils.mix(value) & mask);

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && sameBits(keys.get(index), value)) {
                states.set(index, DELETED);
                size--;
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    @Override
    public void clear() {
        NativeRawPrimitives.fillBytes(states.ptr(), capacity, EMPTY);
        super.clear();
    }

    @Override
    public long sizeof() {
        return states.sizeof() + keys.sizeof();
    }

    @Override
    public void freeMemory() {
        if (states != null) {
            states.freeMemory();
            states = null;
        }
        if (keys != null) {
            keys.freeMemory();
            keys = null;
        }
        super.freeMemory();
    }

    @Override
    protected void rehash(int newCapacity) {
        NativeByteArray oldStates = states;
        NativeDoubleArray oldKeys = keys;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        keys = new NativeDoubleArray(capacity);
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldStates.get(i) == USED) {
                add(oldKeys.get(i));
            }
        }

        oldStates.freeMemory();
        oldKeys.freeMemory();
    }

    private static boolean sameBits(double left, double right) {
        return Double.doubleToLongBits(left) == Double.doubleToLongBits(right);
    }
}
