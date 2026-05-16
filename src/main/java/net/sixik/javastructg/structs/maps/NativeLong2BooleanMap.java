package net.sixik.javastructg.structs.maps;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeLongArray;
import net.sixik.javastructg.utils.NativeUtils;
import sun.misc.Unsafe;

public final class NativeLong2BooleanMap extends NativeMap {

    private static final Unsafe UNSAFE = NativeUtils.getUnsafe();

    private NativeByteArray states;
    private NativeLongArray keys;
    private NativeByteArray values;
    private long statesAddress;
    private long keysAddress;
    private long valuesAddress;

    public NativeLong2BooleanMap(int initialCapacity) {
        super(initialCapacity);
        this.states = new NativeByteArray(capacity);
        this.keys = new NativeLongArray(capacity);
        this.values = new NativeByteArray(capacity);
        refreshAddresses();
    }

    public boolean put(long key, boolean value) {
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
                UNSAFE.putLong(longAddress(keysAddress, target), key);
                UNSAFE.putByte(valuesAddress + target, encode(value));
                size++;
                return true;
            }

            if (state == DELETED) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (UNSAFE.getLong(longAddress(keysAddress, index)) == key) {
                UNSAFE.putByte(valuesAddress + index, encode(value));
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean get(long key, boolean defaultValue) {
        int index = indexForKey(key);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return defaultValue;
            }
            if (state == USED && UNSAFE.getLong(longAddress(keysAddress, index)) == key) {
                return decode(UNSAFE.getByte(valuesAddress + index));
            }

            index = (index + 1) & mask;
        }
    }

    public boolean getOrDefault(long key, boolean defaultValue) {
        return get(key, defaultValue);
    }

    public boolean containsKey(long key) {
        int index = indexForKey(key);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && UNSAFE.getLong(longAddress(keysAddress, index)) == key) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(long key) {
        int index = indexForKey(key);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && UNSAFE.getLong(longAddress(keysAddress, index)) == key) {
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
        NativeLongArray oldKeys = keys;
        NativeByteArray oldValues = values;
        int oldCapacity = capacity;
        long oldStatesAddress = oldStates.ptr();
        long oldKeysAddress = oldKeys.ptr();
        long oldValuesAddress = oldValues.ptr();

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        keys = new NativeLongArray(capacity);
        values = new NativeByteArray(capacity);
        refreshAddresses();
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (UNSAFE.getByte(oldStatesAddress + i) == USED) {
                reinsert(UNSAFE.getLong(longAddress(oldKeysAddress, i)), UNSAFE.getByte(oldValuesAddress + i));
            }
        }

        oldStates.freeMemory();
        oldKeys.freeMemory();
        oldValues.freeMemory();
    }

    private void reinsert(long key, byte encodedValue) {
        int index = indexForKey(key);

        while (true) {
            if (UNSAFE.getByte(statesAddress + index) == EMPTY) {
                UNSAFE.putByte(statesAddress + index, USED);
                UNSAFE.putLong(longAddress(keysAddress, index), key);
                UNSAFE.putByte(valuesAddress + index, encodedValue);
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private int indexForKey(long key) {
        return spreadLong(key) & mask;
    }

    private static byte encode(boolean value) {
        return value ? (byte) 1 : (byte) 0;
    }

    private static boolean decode(byte value) {
        return value != 0;
    }

    private static long longAddress(long baseAddress, int index) {
        return baseAddress + (((long) index) << 3);
    }

    private void refreshAddresses() {
        statesAddress = states.ptr();
        keysAddress = keys.ptr();
        valuesAddress = values.ptr();
    }

    private static int spreadLong(long value) {
        int hash = (int) (value ^ (value >>> 32));
        return hash ^ (hash >>> 16);
    }
}
