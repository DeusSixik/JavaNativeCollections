package net.sixik.javastructg.structs.maps;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import net.sixik.javastructg.utils.NativeUtils;
import sun.misc.Unsafe;

import java.util.Objects;
import java.util.function.Supplier;

public final class NativeObject2ByteMap<K> extends NativeMap {

    private static final Unsafe UNSAFE = NativeUtils.getUnsafe();

    private final NativeTypeMemory<K> keyMemory;
    private final boolean supportsEqualsMemory;
    private final long keySize;
    private final K probeBuffer;

    private NativeByteArray states;
    private NativeIntArray hashes;
    private NativeObjectArray<K> keys;
    private NativeByteArray values;
    private long statesAddress;
    private long hashesAddress;
    private long keysAddress;
    private long valuesAddress;

    public NativeObject2ByteMap(int initialCapacity, NativeTypeMemory<K> keyMemory) {
        this(initialCapacity, keyMemory, null);
    }

    public NativeObject2ByteMap(int initialCapacity, NativeTypeMemory<K> keyMemory, Supplier<K> keyBufferFactory) {
        super(initialCapacity);
        this.keyMemory = keyMemory;
        this.supportsEqualsMemory = keyMemory.supportsEqualsMemory();
        this.keySize = keyMemory.sizeof();
        this.probeBuffer = supportsEqualsMemory ? null : Objects.requireNonNull(keyBufferFactory, "keyBufferFactory").get();
        this.states = new NativeByteArray(capacity);
        this.hashes = new NativeIntArray(capacity);
        this.keys = new NativeObjectArray<>(capacity, keyMemory);
        this.values = new NativeByteArray(capacity);
        refreshAddresses();
    }

    public boolean put(K key, byte value) {
        return putPrehashed(key, keyMemory.hash(key), value);
    }

    public boolean putPrehashed(K key, long hash, byte value) {
        ensureCapacityForInsert();

        int mixedHash = spreadHash(hash);
        int index = indexForHash(mixedHash);
        int firstDeleted = -1;

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                int target = firstDeleted >= 0 ? firstDeleted : index;
                if (firstDeleted < 0) {
                    usedSlots++;
                }
                UNSAFE.putByte(statesAddress + target, USED);
                UNSAFE.putInt(hashAddress(target), mixedHash);
                keyMemory.writeToMemory(UNSAFE, keyAddress(target), key);
                UNSAFE.putByte(valueAddress(target), value);
                size++;
                return true;
            }

            if (state == DELETED) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (UNSAFE.getInt(hashAddress(index)) == mixedHash && slotEqualsKey(keyAddress(index), key)) {
                UNSAFE.putByte(valueAddress(index), value);
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public byte get(K key, byte defaultValue) {
        return getPrehashed(key, keyMemory.hash(key), defaultValue);
    }

    public byte getOrDefault(K key, byte defaultValue) {
        return get(key, defaultValue);
    }

    public byte getPrehashed(K key, long hash, byte defaultValue) {
        int mixedHash = spreadHash(hash);
        int index = indexForHash(mixedHash);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return defaultValue;
            }
            if (state == USED
                    && UNSAFE.getInt(hashAddress(index)) == mixedHash
                    && slotEqualsKey(keyAddress(index), key)) {
                return UNSAFE.getByte(valueAddress(index));
            }

            index = (index + 1) & mask;
        }
    }

    public boolean containsKey(K key) {
        return containsKeyPrehashed(key, keyMemory.hash(key));
    }

    public boolean containsKeyPrehashed(K key, long hash) {
        int mixedHash = spreadHash(hash);
        int index = indexForHash(mixedHash);

        while (true) {
            byte state = UNSAFE.getByte(statesAddress + index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED
                    && UNSAFE.getInt(hashAddress(index)) == mixedHash
                    && slotEqualsKey(keyAddress(index), key)) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(K key) {
        return removePrehashed(key, keyMemory.hash(key));
    }

    public boolean removePrehashed(K key, long hash) {
        int mixedHash = spreadHash(hash);
        int index = indexForHash(mixedHash);

        while (true) {
            long stateAddress = statesAddress + index;
            byte state = UNSAFE.getByte(stateAddress);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED
                    && UNSAFE.getInt(hashAddress(index)) == mixedHash
                    && slotEqualsKey(keyAddress(index), key)) {
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
        return states.sizeof() + hashes.sizeof() + keys.sizeof() + values.sizeof();
    }

    @Override
    public void freeMemory() {
        if (states != null) {
            states.freeMemory();
            states = null;
        }
        if (hashes != null) {
            hashes.freeMemory();
            hashes = null;
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
        NativeIntArray oldHashes = hashes;
        NativeObjectArray<K> oldKeys = keys;
        NativeByteArray oldValues = values;
        int oldCapacity = capacity;
        long oldStatesAddress = oldStates.ptr();
        long oldHashesAddress = oldHashes.ptr();
        long oldKeysAddress = oldKeys.ptr();

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        hashes = new NativeIntArray(capacity);
        keys = new NativeObjectArray<>(capacity, keyMemory);
        values = new NativeByteArray(capacity);
        refreshAddresses();
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (UNSAFE.getByte(oldStatesAddress + i) == USED) {
                reinsert(
                        UNSAFE.getInt(oldHashesAddress + (((long) i) << 2)),
                        oldKeysAddress + (((long) i) * keySize),
                        UNSAFE.getByte(oldValues.ptr() + i)
                );
            }
        }

        oldStates.freeMemory();
        oldHashes.freeMemory();
        oldKeys.freeMemory();
        oldValues.freeMemory();
    }

    private void reinsert(int mixedHash, long oldKeyAddress, byte value) {
        int index = indexForHash(mixedHash);

        while (true) {
            if (UNSAFE.getByte(statesAddress + index) == EMPTY) {
                UNSAFE.putByte(statesAddress + index, USED);
                UNSAFE.putInt(hashAddress(index), mixedHash);
                UNSAFE.copyMemory(oldKeyAddress, keyAddress(index), keySize);
                UNSAFE.putByte(valueAddress(index), value);
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private boolean slotEqualsKey(long address, K key) {
        if (supportsEqualsMemory) {
            return keyMemory.equalsMemory(UNSAFE, address, key);
        }

        keyMemory.readFromMemory(UNSAFE, address, probeBuffer);
        return keyMemory.equals(key, probeBuffer);
    }

    private int spreadHash(long hash) {
        int value = (int) (hash ^ (hash >>> 32));
        return value ^ (value >>> 16);
    }

    private int indexForHash(int mixedHash) {
        return mixedHash & mask;
    }

    private long hashAddress(int index) {
        return hashesAddress + (((long) index) << 2);
    }

    private long keyAddress(int index) {
        return keysAddress + (((long) index) * keySize);
    }

    private long valueAddress(int index) {
        return valuesAddress + index;
    }

    private void refreshAddresses() {
        statesAddress = states.ptr();
        hashesAddress = hashes.ptr();
        keysAddress = keys.ptr();
        valuesAddress = values.ptr();
    }
}
