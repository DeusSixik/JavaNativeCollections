package net.sixik.javastructg.structs.sets;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeFloatArray;
import net.sixik.javastructg.utils.NativeUtils;

public final class NativeFloatSet extends NativeSet {

    private NativeByteArray states;
    private NativeFloatArray keys;

    public NativeFloatSet(int initialCapacity) {
        super(initialCapacity);
        this.states = new NativeByteArray(capacity);
        this.keys = new NativeFloatArray(capacity);
    }

    public boolean add(float value) {
        ensureCapacityForInsert();

        int index = NativeUtils.mix(value) & mask;
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

    public boolean contains(float value) {
        int index = NativeUtils.mix(value) & mask;

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

    public boolean remove(float value) {
        int index = NativeUtils.mix(value) & mask;

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
        NativeFloatArray oldKeys = keys;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        keys = new NativeFloatArray(capacity);
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

    private static boolean sameBits(float left, float right) {
        return Float.floatToIntBits(left) == Float.floatToIntBits(right);
    }
}
