package net.sixik.javastructg.structs.maps;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeLongArray;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import net.sixik.javastructg.utils.NativeUtils;
import sun.misc.Unsafe;

public final class NativeLong2ObjectMap<V> extends NativeMap {

    private static final Unsafe UNSAFE = NativeUtils.getUnsafe();

    private final NativeTypeMemory<V> valueMemory;
    private final long valueSize;

    private NativeByteArray states;
    private NativeLongArray keys;
    private NativeObjectArray<V> values;
    private long statesAddress;
    private long valuesAddress;

    public NativeLong2ObjectMap(int initialCapacity, NativeTypeMemory<V> valueMemory) {
        super(initialCapacity);
        this.valueMemory = valueMemory;
        this.valueSize = valueMemory.sizeof();
        this.states = new NativeByteArray(capacity);
        this.keys = new NativeLongArray(capacity);
        this.values = new NativeObjectArray<>(capacity, valueMemory);
        refreshAddresses();
    }

    public boolean put(long key, V value) {
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
                keys.set(target, key);
                writeValue(target, value);
                size++;
                return true;
            }

            if (state == DELETED) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (keys.get(index) == key) {
                writeValue(index, value);
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean get(long key, V outValue) {
        int index = indexForKey(key);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && keys.get(index) == key) {
                valueMemory.readFromMemory(UNSAFE, valueAddress(index), outValue);
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean containsKey(long key) {
        int index = indexForKey(key);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && keys.get(index) == key) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(long key) {
        int index = indexForKey(key);

        while (true) {
            long stateAddress = statesAddress + index;
            byte state = UNSAFE.getByte(stateAddress);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && keys.get(index) == key) {
                UNSAFE.putByte(stateAddress, DELETED);
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
        NativeObjectArray<V> oldValues = values;
        int oldCapacity = capacity;
        long oldStatesAddress = oldStates.ptr();
        long oldValuesAddress = oldValues.ptr();

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        keys = new NativeLongArray(capacity);
        values = new NativeObjectArray<>(capacity, valueMemory);
        refreshAddresses();
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (UNSAFE.getByte(oldStatesAddress + i) == USED) {
                reinsert(oldKeys.get(i), oldValuesAddress + (((long) i) * valueSize));
            }
        }

        oldStates.freeMemory();
        oldKeys.freeMemory();
        oldValues.freeMemory();
    }

    private void reinsert(long key, long oldValueAddress) {
        int index = indexForKey(key);

        while (true) {
            if (UNSAFE.getByte(statesAddress + index) == EMPTY) {
                UNSAFE.putByte(statesAddress + index, USED);
                keys.set(index, key);
                UNSAFE.copyMemory(oldValueAddress, valueAddress(index), valueSize);
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private void writeValue(int index, V value) {
        valueMemory.writeToMemory(UNSAFE, valueAddress(index), value);
    }

    private long valueAddress(int index) {
        return valuesAddress + (((long) index) * valueSize);
    }

    private int indexForKey(long key) {
        return (int) (NativeUtils.mix(key) & mask);
    }

    private void refreshAddresses() {
        statesAddress = states.ptr();
        valuesAddress = values.ptr();
    }
}
