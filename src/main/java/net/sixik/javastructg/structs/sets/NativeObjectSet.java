package net.sixik.javastructg.structs.sets;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import net.sixik.javastructg.utils.NativeUtils;

import java.util.function.Supplier;

public final class NativeObjectSet<T> extends NativeSet {

    private static final sun.misc.Unsafe UNSAFE = NativeUtils.getUnsafe();

    private final NativeTypeMemory<T> typeMemory;
    private final boolean supportsEqualsMemory;
    private final long keySize;

    private NativeByteArray states;
    private NativeIntArray hashes;
    private NativeObjectArray<T> keys;
    private final T probeBuffer;
    private long statesAddress;
    private long hashesAddress;
    private long keysAddress;

    public NativeObjectSet(int initialCapacity, NativeTypeMemory<T> typeMemory, Supplier<T> bufferFactory) {
        super(initialCapacity);
        this.typeMemory = typeMemory;
        this.supportsEqualsMemory = typeMemory.supportsEqualsMemory();
        this.keySize = typeMemory.sizeof();
        this.states = new NativeByteArray(capacity);
        this.hashes = new NativeIntArray(capacity);
        this.keys = new NativeObjectArray<>(capacity, typeMemory);
        this.probeBuffer = supportsEqualsMemory ? null : bufferFactory.get();
        refreshAddresses();
    }

    public boolean add(T value) {
        return addPrehashed(value, typeMemory.hash(value));
    }

    public boolean addPrehashed(T value, long hash) {
        ensureCapacityForInsert();

        int mixedHash = spreadHash(hash);
        int index = indexForHash(mixedHash);
        int firstDeleted = -1;

        while (true) {
            long stateAddress = statesAddress + index;
            byte state = UNSAFE.getByte(stateAddress);

            if (state == EMPTY) {
                int target = firstDeleted >= 0 ? firstDeleted : index;
                if (firstDeleted < 0) {
                    usedSlots++;
                }
                long targetStateAddress = statesAddress + target;
                long targetHashAddress = hashesAddress + (((long) target) << 2);
                long targetKeyAddress = keysAddress + (((long) target) * keySize);
                UNSAFE.putByte(targetStateAddress, USED);
                UNSAFE.putInt(targetHashAddress, mixedHash);
                typeMemory.writeToMemory(UNSAFE, targetKeyAddress, value);
                size++;
                return true;
            }

            if (state == DELETED) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (UNSAFE.getInt(hashesAddress + (((long) index) << 2)) == mixedHash
                    && slotEqualsValue(keysAddress + (((long) index) * keySize), value)) {
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean contains(T value) {
        return containsPrehashed(value, typeMemory.hash(value));
    }

    public boolean containsPrehashed(T value, long hash) {
        int mixedHash = spreadHash(hash);
        int index = indexForHash(mixedHash);

        while (true) {
            long stateAddress = statesAddress + index;
            byte state = UNSAFE.getByte(stateAddress);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED
                    && UNSAFE.getInt(hashesAddress + (((long) index) << 2)) == mixedHash
                    && slotEqualsValue(keysAddress + (((long) index) * keySize), value)) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(T value) {
        return removePrehashed(value, typeMemory.hash(value));
    }

    public boolean removePrehashed(T value, long hash) {
        int mixedHash = spreadHash(hash);
        int index = indexForHash(mixedHash);

        while (true) {
            long stateAddress = statesAddress + index;
            byte state = UNSAFE.getByte(stateAddress);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED
                    && UNSAFE.getInt(hashesAddress + (((long) index) << 2)) == mixedHash
                    && slotEqualsValue(keysAddress + (((long) index) * keySize), value)) {
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
        return states.sizeof() + hashes.sizeof() + keys.sizeof();
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
        super.freeMemory();
    }

    @Override
    protected void rehash(int newCapacity) {
        NativeByteArray oldStates = states;
        NativeIntArray oldHashes = hashes;
        NativeObjectArray<T> oldKeys = keys;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        hashes = new NativeIntArray(capacity);
        keys = new NativeObjectArray<>(capacity, typeMemory);
        refreshAddresses();
        size = 0;
        usedSlots = 0;

        long oldStatesAddress = oldStates.ptr();
        long oldHashesAddress = oldHashes.ptr();
        long oldKeysAddress = oldKeys.ptr();
        for (int i = 0; i < oldCapacity; i++) {
            if (UNSAFE.getByte(oldStatesAddress + i) == USED) {
                reinsertRehashedSlot(
                        UNSAFE.getInt(oldHashesAddress + (((long) i) << 2)),
                        oldKeysAddress + (((long) i) * keySize)
                );
            }
        }

        oldStates.freeMemory();
        oldHashes.freeMemory();
        oldKeys.freeMemory();
    }

    private boolean slotEqualsValue(long address, T value) {
        if (supportsEqualsMemory) {
            return typeMemory.equalsMemory(UNSAFE, address, value);
        }

        typeMemory.readFromMemory(UNSAFE, address, probeBuffer);
        return typeMemory.equals(value, probeBuffer);
    }

    private void reinsertRehashedSlot(int mixedHash, long sourceAddress) {
        int index = indexForHash(mixedHash);

        while (true) {
            long stateAddress = statesAddress + index;
            if (UNSAFE.getByte(stateAddress) == EMPTY) {
                long hashAddress = hashesAddress + (((long) index) << 2);
                long keyAddress = keysAddress + (((long) index) * keySize);
                UNSAFE.putByte(stateAddress, USED);
                UNSAFE.putInt(hashAddress, mixedHash);
                UNSAFE.copyMemory(sourceAddress, keyAddress, keySize);
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private int spreadHash(long hash) {
        return (int) NativeUtils.mix(hash);
    }

    private int indexForHash(int mixedHash) {
        return mixedHash & mask;
    }

    private void refreshAddresses() {
        statesAddress = states.ptr();
        hashesAddress = hashes.ptr();
        keysAddress = keys.ptr();
    }
}
