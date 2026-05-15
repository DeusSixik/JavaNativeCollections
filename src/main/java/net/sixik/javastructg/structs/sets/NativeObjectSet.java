package net.sixik.javastructg.structs.sets;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeByteArray;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import net.sixik.javastructg.utils.NativeUtils;

import java.util.function.Supplier;

public final class NativeObjectSet<T> extends NativeSet {

    private static final sun.misc.Unsafe UNSAFE = NativeUtils.getUnsafe();

    private final NativeTypeMemory<T> typeMemory;
    private final Supplier<T> bufferFactory;

    private NativeByteArray states;
    private NativeObjectArray<T> keys;
    private final T probeBuffer;
    private final T rehashBuffer;

    public NativeObjectSet(int initialCapacity, NativeTypeMemory<T> typeMemory, Supplier<T> bufferFactory) {
        super(initialCapacity);
        this.typeMemory = typeMemory;
        this.bufferFactory = bufferFactory;
        this.states = new NativeByteArray(capacity);
        this.keys = new NativeObjectArray<>(capacity, typeMemory);
        this.probeBuffer = bufferFactory.get();
        this.rehashBuffer = bufferFactory.get();
    }

    public boolean add(T value) {
        ensureCapacityForInsert();

        int index = indexForHash(typeMemory.hash(value));
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
            } else if (slotEqualsValue(index, value)) {
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean contains(T value) {
        int index = indexForHash(typeMemory.hash(value));

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && slotEqualsValue(index, value)) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(T value) {
        int index = indexForHash(typeMemory.hash(value));

        while (true) {
            byte state = states.get(index);

            if (state == EMPTY) {
                return false;
            }
            if (state == USED && slotEqualsValue(index, value)) {
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
        NativeObjectArray<T> oldKeys = keys;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        states = new NativeByteArray(capacity);
        keys = new NativeObjectArray<>(capacity, typeMemory);
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldStates.get(i) == USED) {
                reinsertRehashedSlot(oldKeys.addressAt(i));
            }
        }

        oldStates.freeMemory();
        oldKeys.freeMemory();
    }

    private boolean slotEqualsValue(int index, T value) {
        long address = keys.addressAt(index);
        if (typeMemory.supportsEqualsMemory()) {
            return typeMemory.equalsMemory(UNSAFE, address, value);
        }

        keys.get(index, probeBuffer);
        return typeMemory.equals(value, probeBuffer);
    }

    private void reinsertRehashedSlot(long sourceAddress) {
        int index = indexForHash(hashAt(sourceAddress));

        while (true) {
            if (states.get(index) == EMPTY) {
                states.set(index, USED);
                UNSAFE.copyMemory(sourceAddress, keys.addressAt(index), typeMemory.sizeof());
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private long hashAt(long address) {
        if (typeMemory.supportsHashMemory()) {
            return typeMemory.hashMemory(UNSAFE, address);
        }

        typeMemory.readFromMemory(UNSAFE, address, rehashBuffer);
        return typeMemory.hash(rehashBuffer);
    }

    private int indexForHash(long hash) {
        return (int) (NativeUtils.mix(hash) & mask);
    }
}
