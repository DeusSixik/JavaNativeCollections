import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.maps.NativeObject2ByteMap;
import net.sixik.javastructg.structs.maps.NativeObject2IntMap;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeObjectPrimitiveMapsTest {

    @Test
    public void testObject2IntPutGetUpdateRemoveAndResize() {
        NativeObject2IntMap<Key> map = new NativeObject2IntMap<>(4, new KeyMemory());
        try {
            assertTrue(map.put(new Key(1, 2, 3), 123));
            assertFalse(map.put(new Key(1, 2, 3), 124));
            assertTrue(map.put(new Key(2, 3, 4), 234));

            assertEquals(124, map.get(new Key(1, 2, 3), -1));
            assertEquals(234, map.get(new Key(2, 3, 4), -1));
            assertEquals(-1, map.get(new Key(9, 9, 9), -1));

            for (int i = 0; i < 300; i++) {
                assertTrue(map.put(new Key(i + 100, -i, i * 2), i * 7));
            }

            for (int i = 0; i < 300; i++) {
                assertEquals(i * 7, map.get(new Key(i + 100, -i, i * 2), -1));
            }

            assertTrue(map.remove(new Key(1, 2, 3)));
            assertFalse(map.containsKey(new Key(1, 2, 3)));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testObject2IntPrehashedOperationsDoNotCallHash() {
        KeyMemory hashMemory = new KeyMemory();
        NativeObject2IntMap<Key> map = new NativeObject2IntMap<>(8, new ThrowingHashKeyMemory());
        try {
            Key key = new Key(10, 20, 30);
            Key equalKey = new Key(10, 20, 30);
            long hash = hashMemory.hash(key);

            assertTrue(map.putPrehashed(key, hash, 100));
            assertFalse(map.putPrehashed(equalKey, hash, 101));
            assertEquals(101, map.getPrehashed(equalKey, hash, -1));
            assertTrue(map.containsKeyPrehashed(equalKey, hash));
            assertTrue(map.removePrehashed(equalKey, hash));
            assertFalse(map.containsKeyPrehashed(equalKey, hash));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testObject2BytePutGetUpdateRemoveAndResize() {
        NativeObject2ByteMap<Key> map = new NativeObject2ByteMap<>(4, new KeyMemory());
        try {
            assertTrue(map.put(new Key(1, 2, 3), (byte) 12));
            assertFalse(map.put(new Key(1, 2, 3), (byte) 13));
            assertTrue(map.put(new Key(2, 3, 4), (byte) 23));

            assertEquals((byte) 13, map.get(new Key(1, 2, 3), (byte) -1));
            assertEquals((byte) 23, map.get(new Key(2, 3, 4), (byte) -1));

            for (int i = 0; i < 300; i++) {
                assertTrue(map.put(new Key(i + 100, -i, i * 2), (byte) i));
            }

            for (int i = 0; i < 300; i++) {
                assertEquals((byte) i, map.get(new Key(i + 100, -i, i * 2), (byte) -1));
            }

            assertTrue(map.remove(new Key(1, 2, 3)));
            assertFalse(map.containsKey(new Key(1, 2, 3)));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testObject2BytePrehashedOperationsDoNotCallHash() {
        KeyMemory hashMemory = new KeyMemory();
        NativeObject2ByteMap<Key> map = new NativeObject2ByteMap<>(8, new ThrowingHashKeyMemory());
        try {
            Key key = new Key(3, 4, 5);
            Key equalKey = new Key(3, 4, 5);
            long hash = hashMemory.hash(key);

            assertTrue(map.putPrehashed(key, hash, (byte) 30));
            assertFalse(map.putPrehashed(equalKey, hash, (byte) 31));
            assertEquals((byte) 31, map.getPrehashed(equalKey, hash, (byte) -1));
            assertTrue(map.removePrehashed(equalKey, hash));
            assertFalse(map.containsKeyPrehashed(equalKey, hash));
        } finally {
            map.freeMemory();
        }
    }

    private static final class Key {
        private int x;
        private int y;
        private int z;

        private Key() {
        }

        private Key(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class KeyMemory implements NativeTypeMemory<Key> {
        private static final long X_OFFSET = 0L;
        private static final long Y_OFFSET = 4L;
        private static final long Z_OFFSET = 8L;
        private static final long SIZE = 12L;

        @Override
        public long sizeof() {
            return SIZE;
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Key outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
            outElement.z = unsafe.getInt(offset + Z_OFFSET);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Key element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            unsafe.putInt(offset + Z_OFFSET, element.z);
        }

        @Override
        public long hash(Key element) {
            int result = element.x;
            result = 31 * result + element.y;
            result = 31 * result + element.z;
            return result;
        }

        @Override
        public boolean supportsEqualsMemory() {
            return true;
        }

        @Override
        public boolean equalsMemory(Unsafe unsafe, long offset, Key value) {
            return unsafe.getInt(offset + X_OFFSET) == value.x
                    && unsafe.getInt(offset + Y_OFFSET) == value.y
                    && unsafe.getInt(offset + Z_OFFSET) == value.z;
        }
    }

    private static final class ThrowingHashKeyMemory extends KeyMemory {
        @Override
        public long hash(Key element) {
            throw new AssertionError("Prehashed map path must not call hash()");
        }
    }
}
