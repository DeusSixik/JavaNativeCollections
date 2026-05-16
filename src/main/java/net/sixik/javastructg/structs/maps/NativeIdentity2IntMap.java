package net.sixik.javastructg.structs.maps;

public final class NativeIdentity2IntMap<K> extends NativeIdentityMap {

    private int[] values;

    public NativeIdentity2IntMap(int initialCapacity) {
        super(initialCapacity);
        this.values = new int[capacity];
    }

    public boolean put(K key, int value) {
        ensureCapacityForInsert();

        Object storedKey = maskNull(key);
        int index = indexForIdentity(key);
        int firstDeleted = -1;

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                int target = firstDeleted >= 0 ? firstDeleted : index;
                if (firstDeleted < 0) {
                    usedSlots++;
                }
                keys[target] = storedKey;
                values[target] = value;
                size++;
                return true;
            }

            if (slot == DELETED_KEY) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (slot == storedKey) {
                values[index] = value;
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public int get(K key, int defaultValue) {
        Object storedKey = maskNull(key);
        int index = indexForIdentity(key);

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                return defaultValue;
            }
            if (slot == storedKey) {
                return values[index];
            }

            index = (index + 1) & mask;
        }
    }

    public int getOrDefault(K key, int defaultValue) {
        return get(key, defaultValue);
    }

    public boolean containsKey(K key) {
        Object storedKey = maskNull(key);
        int index = indexForIdentity(key);

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                return false;
            }
            if (slot == storedKey) {
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean remove(K key) {
        Object storedKey = maskNull(key);
        int index = indexForIdentity(key);

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                return false;
            }
            if (slot == storedKey) {
                keys[index] = DELETED_KEY;
                size--;
                return true;
            }

            index = (index + 1) & mask;
        }
    }

    @Override
    public long sizeof() {
        return (long) capacity * Integer.BYTES;
    }

    @Override
    public void freeMemory() {
        values = null;
        super.freeMemory();
    }

    @Override
    protected void rehash(int newCapacity) {
        Object[] oldKeys = keys;
        int[] oldValues = values;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        keys = new Object[capacity];
        values = new int[capacity];
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Object slot = oldKeys[i];
            if (isUsedSlot(slot)) {
                reinsertStored(slot, oldValues[i]);
            }
        }
    }

    private void reinsertStored(Object storedKey, int value) {
        int index = indexForStoredIdentity(storedKey);

        while (true) {
            if (keys[index] == null) {
                keys[index] = storedKey;
                values[index] = value;
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }
}
