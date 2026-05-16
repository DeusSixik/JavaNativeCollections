package net.sixik.javastructg.structs.maps;

import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import sun.misc.Unsafe;

import java.util.Arrays;

import static net.sixik.javastructg.utils.NativeUtils.getUnsafe;

public final class Object2NativeMap<K, V> extends NativeMap {

    private static final Unsafe UNSAFE = getUnsafe();
    private static final Object NULL_KEY = new Object();
    private static final Object DELETED_KEY = new Object();

    private final NativeTypeMemory<V> valueMemory;
    private final long valueSize;

    private Object[] keys;
    private int[] hashes;
    private NativeObjectArray<V> values;

    public Object2NativeMap(int initialCapacity, NativeTypeMemory<V> valueMemory) {
        super(initialCapacity);
        this.valueMemory = valueMemory;
        this.valueSize = valueMemory.sizeof();
        this.keys = new Object[capacity];
        this.hashes = new int[capacity];
        this.values = new NativeObjectArray<>(capacity, valueMemory);
    }

    public boolean put(K key, V value) {
        return putPrehashed(key, hash(key), value);
    }

    public boolean putPrehashed(K key, long hash, V value) {
        ensureCapacityForInsert();

        Object storedKey = maskNull(key);
        int mixedHash = spreadHash(hash);
        int index = mixedHash & mask;
        int firstDeleted = -1;

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                int target = firstDeleted >= 0 ? firstDeleted : index;
                if (firstDeleted < 0) {
                    usedSlots++;
                }
                keys[target] = storedKey;
                hashes[target] = mixedHash;
                writeValue(target, value);
                size++;
                return true;
            }

            if (slot == DELETED_KEY) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (hashes[index] == mixedHash && keyEquals(slot, key)) {
                writeValue(index, value);
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean get(K key, V outValue) {
        return getPrehashed(key, hash(key), outValue);
    }

    public boolean getPrehashed(K key, long hash, V outValue) {
        int mixedHash = spreadHash(hash);
        int index = mixedHash & mask;

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                return false;
            }
            if (slot != DELETED_KEY && hashes[index] == mixedHash && keyEquals(slot, key)) {
                valueMemory.readFromMemory(UNSAFE, values.addressAt(index), outValue);
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean containsKey(K key) {
        return containsKeyPrehashed(key, hash(key));
    }

    public boolean containsKeyPrehashed(K key, long hash) {
        int mixedHash = spreadHash(hash);
        int index = mixedHash & mask;

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                return false;
            }
            if (slot != DELETED_KEY && hashes[index] == mixedHash && keyEquals(slot, key)) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(K key) {
        return removePrehashed(key, hash(key));
    }

    public boolean removePrehashed(K key, long hash) {
        int mixedHash = spreadHash(hash);
        int index = mixedHash & mask;

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                return false;
            }
            if (slot != DELETED_KEY && hashes[index] == mixedHash && keyEquals(slot, key)) {
                keys[index] = DELETED_KEY;
                size--;
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    @Override
    public void clear() {
        Arrays.fill(keys, null);
        super.clear();
    }

    @Override
    public long sizeof() {
        return values.sizeof();
    }

    @Override
    public void freeMemory() {
        keys = null;
        hashes = null;
        if (values != null) {
            values.freeMemory();
            values = null;
        }
        super.freeMemory();
    }

    @Override
    protected void rehash(int newCapacity) {
        Object[] oldKeys = keys;
        int[] oldHashes = hashes;
        NativeObjectArray<V> oldValues = values;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        keys = new Object[capacity];
        hashes = new int[capacity];
        values = new NativeObjectArray<>(capacity, valueMemory);
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Object slot = oldKeys[i];
            if (slot != null && slot != DELETED_KEY) {
                reinsertStored(slot, oldHashes[i], oldValues.addressAt(i));
            }
        }

        oldValues.freeMemory();
    }

    private void reinsertStored(Object storedKey, int mixedHash, long oldValueAddress) {
        int index = mixedHash & mask;

        while (true) {
            if (keys[index] == null) {
                keys[index] = storedKey;
                hashes[index] = mixedHash;
                UNSAFE.copyMemory(oldValueAddress, values.addressAt(index), valueSize);
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private void writeValue(int index, V value) {
        valueMemory.writeToMemory(UNSAFE, values.addressAt(index), value);
    }

    private Object maskNull(K key) {
        return key == null ? NULL_KEY : key;
    }

    private boolean keyEquals(Object storedKey, K key) {
        return storedKey == NULL_KEY ? key == null : storedKey.equals(key);
    }

    private long hash(K key) {
        return key == null ? 0L : key.hashCode();
    }

    private int spreadHash(long hash) {
        int value = (int) (hash ^ (hash >>> 32));
        return value ^ (value >>> 16);
    }
}
