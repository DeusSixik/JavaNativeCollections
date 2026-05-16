package net.sixik.javastructg.structs.maps;

public final class NativeIdentity2BooleanMap<K> extends NativeIdentityMap {

    private byte[] values;

    public NativeIdentity2BooleanMap(int initialCapacity) {
        super(initialCapacity);
        this.values = new byte[capacity];
    }

    public boolean put(K key, boolean value) {
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
                values[target] = encode(value);
                size++;
                return true;
            }

            if (slot == DELETED_KEY) {
                if (firstDeleted < 0) {
                    firstDeleted = index;
                }
            } else if (slot == storedKey) {
                values[index] = encode(value);
                return false;
            }

            index = (index + 1) & mask;
        }
    }

    public boolean get(K key, boolean defaultValue) {
        Object storedKey = maskNull(key);
        int index = indexForIdentity(key);

        while (true) {
            Object slot = keys[index];

            if (slot == null) {
                return defaultValue;
            }
            if (slot == storedKey) {
                return decode(values[index]);
            }

            index = (index + 1) & mask;
        }
    }

    public boolean getOrDefault(K key, boolean defaultValue) {
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
        return capacity;
    }

    @Override
    public void freeMemory() {
        values = null;
        super.freeMemory();
    }

    @Override
    protected void rehash(int newCapacity) {
        Object[] oldKeys = keys;
        byte[] oldValues = values;
        int oldCapacity = capacity;

        capacity = newCapacity;
        mask = capacity - 1;
        keys = new Object[capacity];
        values = new byte[capacity];
        size = 0;
        usedSlots = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Object slot = oldKeys[i];
            if (isUsedSlot(slot)) {
                reinsertStored(slot, oldValues[i]);
            }
        }
    }

    private void reinsertStored(Object storedKey, byte encodedValue) {
        int index = indexForStoredIdentity(storedKey);

        while (true) {
            if (keys[index] == null) {
                keys[index] = storedKey;
                values[index] = encodedValue;
                size++;
                usedSlots++;
                return;
            }

            index = (index + 1) & mask;
        }
    }

    private static byte encode(boolean value) {
        return value ? (byte) 1 : (byte) 0;
    }

    private static boolean decode(byte value) {
        return value != 0;
    }
}
