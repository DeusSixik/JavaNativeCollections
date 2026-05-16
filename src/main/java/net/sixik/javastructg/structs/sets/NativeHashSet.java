package net.sixik.javastructg.structs.sets;

import net.sixik.javastructg.structs.NativeRawPrimitives;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeLongArray;
import net.sixik.javastructg.utils.NativeUtils;

public final class NativeHashSet<T> extends NativeSet {

    private static final long EMPTY_SLOT = 0L;
    private static final long DELETED_SLOT = 1L;
    private static final long USED_MARKER = 2L;

    private final NativeTypeMemory<T> typeMemory;

    private NativeLongArray slots;

    public NativeHashSet(int initialCapacity, NativeTypeMemory<T> typeMemory) {
        super(initialCapacity);
        this.typeMemory = typeMemory;
        this.slots = new NativeLongArray(capacity);
    }

    public boolean add(T value) {
        return addHash(typeMemory.hash(value));
    }

    public boolean addHash(long hash) {
        ensureCapacityForInsert();

        long encoded = encodeHash(hash);
        int index = indexForEncoded(encoded);
        int firstDeleted = -1;

        while (true) {
            long slot = slots.get(index);

            if (slot == EMPTY_SLOT) {
                int target = firstDeleted >= 0 ? firstDeleted : index;
                if (firstDeleted < 0) {
                    usedSlots++;
                }
                slots.set(target, encoded);
                size++;
                return true;
            }

            if (slot == DELETED_SLOT) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (slot == encoded) {
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean contains(T value) {
        return containsHash(typeMemory.hash(value));
    }

    public boolean containsHash(long hash) {
        long encoded = encodeHash(hash);
        int index = indexForEncoded(encoded);

        while (true) {
            long slot = slots.get(index);

            if (slot == EMPTY_SLOT) {
                return false;
            }
            if (slot == encoded) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(T value) {
        return removeHash(typeMemory.hash(value));
    }

    public boolean removeHash(long hash) {
        long encoded = encodeHash(hash);
        int index = indexForEncoded(encoded);

        while (true) {
            long slot = slots.get(index);

            if (slot == EMPTY_SLOT) {
                return false;
            }
            if (slot == encoded) {
                slots.set(index, DELETED_SLOT);
                size--;
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    @Override
    public void clear() {
        NativeRawPrimitives.fillLongs(slots.ptr(), capacity, EMPTY_SLOT);
        super.clear();
    }

    @Override
    public long sizeof() {
        return slots.sizeof();
    }

    @Override
    public void freeMemory() {
        if (slots != null) {
            slots.freeMemory();
            slots = null;
        }
        super.freeMemory();
    }

    @Override
    protected void rehash(int newCapacity) {
        NativeLongArray oldSlots = slots;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        slots = new NativeLongArray(capacity);
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            long slot = oldSlots.get(i);
            if (isUsedSlot(slot)) {
                reinsertEncoded(slot);
            }
        }

        oldSlots.freeMemory();
    }

    private void reinsertEncoded(long encoded) {
        int index = indexForEncoded(encoded);

        while (true) {
            if (slots.get(index) == EMPTY_SLOT) {
                slots.set(index, encoded);
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private static boolean isUsedSlot(long slot) {
        return slot != EMPTY_SLOT && slot != DELETED_SLOT;
    }

    // NativeHashSet is intentionally lossy: set identity is the mixed 32-bit fingerprint.
    private long encodeHash(long hash) {
        return (((long) NativeUtils.mix(hash)) << 32) | USED_MARKER;
    }

    private int indexForEncoded(long encoded) {
        return ((int) (encoded >> 32)) & mask;
    }
}
