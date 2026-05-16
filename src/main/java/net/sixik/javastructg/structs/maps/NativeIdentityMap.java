package net.sixik.javastructg.structs.maps;

import java.util.Arrays;

abstract class NativeIdentityMap extends NativeMap {

    protected static final Object NULL_KEY = new Object();
    protected static final Object DELETED_KEY = new Object();

    protected Object[] keys;

    protected NativeIdentityMap(int initialCapacity) {
        super(initialCapacity);
        this.keys = new Object[capacity];
    }

    @Override
    public void clear() {
        Arrays.fill(keys, null);
        super.clear();
    }

    @Override
    public void freeMemory() {
        keys = null;
        super.freeMemory();
    }

    protected int indexForIdentity(Object key) {
        int hash = key == null ? 0 : System.identityHashCode(key);
        hash ^= hash >>> 16;
        return hash & mask;
    }

    protected int indexForStoredIdentity(Object storedKey) {
        return storedKey == NULL_KEY ? indexForIdentity(null) : indexForIdentity(storedKey);
    }

    protected Object maskNull(Object key) {
        return key == null ? NULL_KEY : key;
    }

    protected boolean isUsedSlot(Object slot) {
        return slot != null && slot != DELETED_KEY;
    }
}
