package net.sixik.javastructg.structs.sets;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.utils.NativeUtils;

public final class NativeIntSet extends NativeSet {

    /*
        Храним ключи и состояния в двух отдельных off-heap массивах.
        Это проще для первой реализации и хорошо читается при отладке.
     */
    private NativeByteArray states;
    private NativeIntArray keys;


    public NativeIntSet(int initialCapacity) {
        super(initialCapacity);
        this.states = new NativeByteArray(capacity);
        this.keys = new NativeIntArray(capacity);
    }

    public boolean add(int value) {
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
            } else if (keys.get(index) == value) {
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean contains(int value) {
        int index = NativeUtils.mix(value) & mask;

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                return false;
            }

            if (state == USED && keys.get(index) == value) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(int value) {
        int index = NativeUtils.mix(value) & mask;

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                return false;
            }

            if (state == USED && keys.get(index) == value) {
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
        NativeIntArray oldKeys = keys;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        keys = new NativeIntArray(capacity);
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
}
