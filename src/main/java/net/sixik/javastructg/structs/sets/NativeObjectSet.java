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

    private NativeByteArray states;
    private NativeIntArray hashes;
    private NativeObjectArray<T> keys;
    private final T probeBuffer;

    public NativeObjectSet(int initialCapacity, NativeTypeMemory<T> typeMemory, Supplier<T> bufferFactory) {
        super(initialCapacity);
        this.typeMemory = typeMemory;
        this.supportsEqualsMemory = typeMemory.supportsEqualsMemory();
        this.states = new NativeByteArray(capacity);
        this.hashes = new NativeIntArray(capacity);
        this.keys = new NativeObjectArray<>(capacity, typeMemory);
        this.probeBuffer = supportsEqualsMemory ? null : bufferFactory.get();
    }

    public boolean add(T value) {
        ensureCapacityForInsert();

        long hash = typeMemory.hash(value);
        int mixedHash = spreadHash(hash);
        int index = indexForHash(mixedHash);
        int firstDeleted = -1;

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                int target = firstDeleted >= 0 ? firstDeleted : index;
                if (firstDeleted < 0) {
                    usedSlots++;
                }
                states.set(target, USED);
                hashes.set(target, mixedHash);
                keys.set(target, value);
                size++;
                return true;
            }

            if (state == DELETED) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (hashes.get(index) == mixedHash && slotEqualsValue(index, value)) {
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean contains(T value) {
        int mixedHash = spreadHash(typeMemory.hash(value));
        int index = indexForHash(mixedHash);

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && hashes.get(index) == mixedHash && slotEqualsValue(index, value)) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(T value) {
        int mixedHash = spreadHash(typeMemory.hash(value));
        int index = indexForHash(mixedHash);

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && hashes.get(index) == mixedHash && slotEqualsValue(index, value)) {
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
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldStates.get(i) == USED) {
                reinsertRehashedSlot(oldHashes.get(i), oldKeys.addressAt(i));
            }
        }

        oldStates.freeMemory();
        oldHashes.freeMemory();
        oldKeys.freeMemory();
    }

    private boolean slotEqualsValue(int index, T value) {
        long address = keys.addressAt(index);
        if (supportsEqualsMemory) {
            return typeMemory.equalsMemory(UNSAFE, address, value);
        }

        keys.get(index, probeBuffer);
        return typeMemory.equals(value, probeBuffer);
    }

    private void reinsertRehashedSlot(int mixedHash, long sourceAddress) {
        int index = indexForHash(mixedHash);

        while (true) {
            if (states.get(index) == EMPTY) {
                states.set(index, USED);
                hashes.set(index, mixedHash);
                UNSAFE.copyMemory(sourceAddress, keys.addressAt(index), typeMemory.sizeof());
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
}
