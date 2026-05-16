import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.maps.NativeObject2LongMap;
import net.sixik.javastructg.structs.maps.NativeObject2ObjectMap;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeObjectMapsTest {

    @Test
    public void testObject2LongPutGetUpdateRemoveAndResize() {
        NativeObject2LongMap<Key> map = new NativeObject2LongMap<>(4, new KeyMemory(), Key::new);
        try {
            assertTrue(map.put(new Key(1, 2), 12L));
            assertFalse(map.put(new Key(1, 2), 13L));
            assertTrue(map.put(new Key(2, 3), 23L));

            assertEquals(13L, map.get(new Key(1, 2), -1L));
            assertEquals(23L, map.get(new Key(2, 3), -1L));
            assertEquals(-1L, map.get(new Key(9, 9), -1L));

            for (int i = 0; i < 200; i++) {
                assertTrue(map.put(new Key(i + 100, -i), i * 5L));
            }

            assertEquals(202, map.size());
            for (int i = 0; i < 200; i++) {
                assertEquals(i * 5L, map.get(new Key(i + 100, -i), -1L));
            }

            assertTrue(map.remove(new Key(1, 2)));
            assertFalse(map.containsKey(new Key(1, 2)));
            assertFalse(map.remove(new Key(1, 2)));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testObject2LongPrehashedOperationsDoNotCallHash() {
        KeyMemory hashMemory = new KeyMemory();
        NativeObject2LongMap<Key> map = new NativeObject2LongMap<>(8, new ThrowingHashKeyMemory(), Key::new);
        try {
            Key key = new Key(10, 20);
            Key equalKey = new Key(10, 20);
            long hash = hashMemory.hash(key);

            assertTrue(map.putPrehashed(key, hash, 100L));
            assertFalse(map.putPrehashed(equalKey, hash, 101L));
            assertEquals(101L, map.getPrehashed(equalKey, hash, -1L));
            assertTrue(map.containsKeyPrehashed(equalKey, hash));
            assertTrue(map.removePrehashed(equalKey, hash));
            assertFalse(map.containsKeyPrehashed(equalKey, hash));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testObject2ObjectPutGetUpdateRemoveAndResize() {
        NativeObject2ObjectMap<Key, Value> map = new NativeObject2ObjectMap<>(
                4,
                new KeyMemory(),
                Key::new,
                new ValueMemory()
        );
        Value out = new Value();
        try {
            assertTrue(map.put(new Key(1, 2), new Value(10L, 20)));
            assertFalse(map.put(new Key(1, 2), new Value(11L, 21)));
            assertTrue(map.get(new Key(1, 2), out));
            assertValue(out, 11L, 21);

            for (int i = 0; i < 200; i++) {
                assertTrue(map.put(new Key(i + 1000, i), new Value(i * 10L, -i)));
            }

            assertEquals(201, map.size());
            for (int i = 0; i < 200; i++) {
                assertTrue(map.get(new Key(i + 1000, i), out));
                assertValue(out, i * 10L, -i);
            }

            assertTrue(map.remove(new Key(1, 2)));
            assertFalse(map.get(new Key(1, 2), out));
            assertFalse(map.remove(new Key(1, 2)));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testObject2ObjectPrehashedOperationsDoNotCallHash() {
        KeyMemory hashMemory = new KeyMemory();
        NativeObject2ObjectMap<Key, Value> map = new NativeObject2ObjectMap<>(
                8,
                new ThrowingHashKeyMemory(),
                Key::new,
                new ValueMemory()
        );
        Value out = new Value();
        try {
            Key key = new Key(3, 4);
            Key equalKey = new Key(3, 4);
            long hash = hashMemory.hash(key);

            assertTrue(map.putPrehashed(key, hash, new Value(30L, 40)));
            assertFalse(map.putPrehashed(equalKey, hash, new Value(31L, 41)));
            assertTrue(map.getPrehashed(equalKey, hash, out));
            assertValue(out, 31L, 41);
            assertTrue(map.removePrehashed(equalKey, hash));
            assertFalse(map.getPrehashed(equalKey, hash, out));
        } finally {
            map.freeMemory();
        }
    }

    private static void assertValue(Value value, long a, int b) {
        assertEquals(a, value.a);
        assertEquals(b, value.b);
    }

    private static final class Key {
        private int x;
        private int y;

        private Key() {
        }

        private Key(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class Value {
        private long a;
        private int b;

        private Value() {
        }

        private Value(long a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    private static class KeyMemory implements NativeTypeMemory<Key> {
        private static final long X_OFFSET = 0L;
        private static final long Y_OFFSET = 4L;
        private static final long SIZE = 8L;

        @Override
        public long sizeof() {
            return SIZE;
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Key outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Key element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
        }

        @Override
        public long hash(Key element) {
            return 31L * element.x + element.y;
        }

        @Override
        public boolean supportsEqualsMemory() {
            return true;
        }

        @Override
        public boolean equalsMemory(Unsafe unsafe, long offset, Key value) {
            return unsafe.getInt(offset + X_OFFSET) == value.x
                    && unsafe.getInt(offset + Y_OFFSET) == value.y;
        }
    }

    private static final class ThrowingHashKeyMemory extends KeyMemory {
        @Override
        public long hash(Key element) {
            throw new AssertionError("Prehashed map path must not call hash()");
        }
    }

    private static final class ValueMemory implements NativeTypeMemory<Value> {
        private static final long A_OFFSET = 0L;
        private static final long B_OFFSET = 8L;
        private static final long SIZE = 16L;

        @Override
        public long sizeof() {
            return SIZE;
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Value outElement) {
            outElement.a = unsafe.getLong(offset + A_OFFSET);
            outElement.b = unsafe.getInt(offset + B_OFFSET);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Value element) {
            unsafe.putLong(offset + A_OFFSET, element.a);
            unsafe.putInt(offset + B_OFFSET, element.b);
        }
    }
}
