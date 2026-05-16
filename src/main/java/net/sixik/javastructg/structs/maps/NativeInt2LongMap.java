package net.sixik.javastructg.structs.maps;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeLongArray;
import net.sixik.javastructg.utils.NativeUtils;
import sun.misc.Unsafe;

public final class NativeInt2LongMap extends NativeMap {

    private static final Unsafe UNSAFE = NativeUtils.getUnsafe();

    private NativeByteArray states;
    private NativeIntArray keys;
    private NativeLongArray values;
    private long statesAddress;
    private long keysAddress;
    private long valuesAddress;

    public NativeInt2LongMap(int initialCapacity) {
        super(initialCapacity);
        this.states = new NativeByteArray(capacity);
        this.keys = new NativeIntArray(capacity);
        this.values = new NativeLongArray(capacity);
        refreshAddresses();
    }

    public boolean put(int key, long value) {
        ensureCapacityForInsert();

        int index = indexForKey(key);
        int firstDeleted = -1;

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                int target = firstDeleted >= 0 ? firstDeleted : index;
                if (firstDeleted < 0) {
                    usedSlots++;
                }
                UNSAFE.putByte(statesAddress + target, USED);
                UNSAFE.putInt(intAddress(keysAddress, target), key);
                UNSAFE.putLong(longAddress(valuesAddress, target), value);
                size++;
                return true;
            }

            if (state == DELETED) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (UNSAFE.getInt(intAddress(keysAddress, index)) == key) {
                UNSAFE.putLong(longAddress(valuesAddress, index), value);
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public long get(int key, long defaultValue) {
        int index = indexForKey(key);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return defaultValue;
            }
            if (state == USED && UNSAFE.getInt(intAddress(keysAddress, index)) == key) {
                return UNSAFE.getLong(longAddress(valuesAddress, index));
            }

            index = (index + 1) & mask;
        }
    }

    public long getOrDefault(int key, long defaultValue) {
        return get(key, defaultValue);
    }

    public boolean containsKey(int key) {
        int index = indexForKey(key);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && UNSAFE.getInt(intAddress(keysAddress, index)) == key) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(int key) {
        int index = indexForKey(key);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && UNSAFE.getInt(intAddress(keysAddress, index)) == key) {
                UNSAFE.putByte(statesAddress + index, DELETED);
                size--;
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    @Override
    public void clear() {
        NativeRawPrimitives.fillBytes(statesAddress, capacity, EMPTY);
        super.clear();
    }

    @Override
    public long sizeof() {
        return states.sizeof() + keys.sizeof() + values.sizeof();
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
        if (values != null) {
            values.freeMemory();
            values = null;
        }
        super.freeMemory();
    }

    @Override
    protected void rehash(int newCapacity) {
        NativeByteArray oldStates = states;
        NativeIntArray oldKeys = keys;
        NativeLongArray oldValues = values;
        int oldCapacity = capacity;
        long oldStatesAddress = oldStates.ptr();
        long oldKeysAddress = oldKeys.ptr();
        long oldValuesAddress = oldValues.ptr();

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        keys = new NativeIntArray(capacity);
        values = new NativeLongArray(capacity);
        refreshAddresses();
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (UNSAFE.getByte(oldStatesAddress + i) == USED) {
                reinsert(UNSAFE.getInt(intAddress(oldKeysAddress, i)), UNSAFE.getLong(longAddress(oldValuesAddress, i)));
            }
        }

        oldStates.freeMemory();
        oldKeys.freeMemory();
        oldValues.freeMemory();
    }

    private void reinsert(int key, long value) {
        int index = indexForKey(key);

        while (true) {
            if (UNSAFE.getByte(statesAddress + index) == EMPTY) {
                UNSAFE.putByte(statesAddress + index, USED);
                UNSAFE.putInt(intAddress(keysAddress, index), key);
                UNSAFE.putLong(longAddress(valuesAddress, index), value);
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private int indexForKey(int key) {
        return spreadInt(key) & mask;
    }

    private static long intAddress(long baseAddress, int index) {
        return baseAddress + (((long) index) << 2);
    }

    private static int spreadInt(int value) {
        return value ^ (value >>> 16);
    }

    private static long longAddress(long baseAddress, int index) {
        return baseAddress + (((long) index) << 3);
    }

    private void refreshAddresses() {
        statesAddress = states.ptr();
        keysAddress = keys.ptr();
        valuesAddress = values.ptr();
    }
}
