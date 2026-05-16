import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.maps.Object2NativeMap;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Object2NativeMapTest {

    @Test
    public void testPutGetUpdateRemoveAndResize() {
        Object2NativeMap<Key, Value> map = new Object2NativeMap<>(4, new ValueMemory());
        Value out = new Value();
        try {
            assertTrue(map.put(new Key("a", 1), new Value(10, 100L)));
            assertFalse(map.put(new Key("a", 1), new Value(11, 101L)));
            assertTrue(map.put(new Key("b", 2), new Value(20, 200L)));

            assertTrue(map.get(new Key("a", 1), out));
            assertValue(out, 11, 101L);
            assertTrue(map.get(new Key("b", 2), out));
            assertValue(out, 20, 200L);
            assertFalse(map.get(new Key("missing", 9), out));

            for (int i = 0; i < 300; i++) {
                assertTrue(map.put(new Key("k" + i, i), new Value(i, i * 7L)));
            }

            assertTrue(map.capacity() >= 512);
            for (int i = 0; i < 300; i++) {
                assertTrue(map.get(new Key("k" + i, i), out));
                assertValue(out, i, i * 7L);
            }

            assertTrue(map.remove(new Key("a", 1)));
            assertFalse(map.containsKey(new Key("a", 1)));
            assertFalse(map.remove(new Key("a", 1)));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testNullKeyAndClear() {
        Object2NativeMap<Key, Value> map = new Object2NativeMap<>(4, new ValueMemory());
        Value out = new Value();
        try {
            assertTrue(map.put(null, new Value(1, 10L)));
            assertFalse(map.put(null, new Value(2, 20L)));

            assertTrue(map.get(null, out));
            assertValue(out, 2, 20L);
            assertTrue(map.containsKey(null));

            map.clear();

            assertTrue(map.isEmpty());
            assertFalse(map.get(null, out));
            assertTrue(map.put(null, new Value(3, 30L)));
            assertTrue(map.get(null, out));
            assertValue(out, 3, 30L);
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testCollisionsAndDeletedSlotReuse() {
        Object2NativeMap<CollidingKey, Value> map = new Object2NativeMap<>(8, new ValueMemory());
        Value out = new Value();
        try {
            CollidingKey first = new CollidingKey(1);
            CollidingKey second = new CollidingKey(2);
            CollidingKey third = new CollidingKey(3);

            assertTrue(map.put(first, new Value(1, 10L)));
            assertTrue(map.put(second, new Value(2, 20L)));
            assertTrue(map.remove(first));
            assertTrue(map.put(third, new Value(3, 30L)));

            assertFalse(map.get(first, out));
            assertTrue(map.get(new CollidingKey(2), out));
            assertValue(out, 2, 20L);
            assertTrue(map.get(new CollidingKey(3), out));
            assertValue(out, 3, 30L);
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testPrehashedOperationsDoNotCallHashCode() {
        Object2NativeMap<ThrowingHashKey, Value> map = new Object2NativeMap<>(8, new ValueMemory());
        Value out = new Value();
        try {
            ThrowingHashKey key = new ThrowingHashKey(1);
            ThrowingHashKey equalKey = new ThrowingHashKey(1);
            long hash = 42L;

            assertTrue(map.putPrehashed(key, hash, new Value(10, 100L)));
            assertFalse(map.putPrehashed(equalKey, hash, new Value(11, 101L)));
            assertTrue(map.getPrehashed(equalKey, hash, out));
            assertValue(out, 11, 101L);
            assertTrue(map.containsKeyPrehashed(equalKey, hash));
            assertTrue(map.removePrehashed(equalKey, hash));
            assertFalse(map.getPrehashed(equalKey, hash, out));
        } finally {
            map.freeMemory();
        }
    }

    private static void assertValue(Value value, int x, long y) {
        assertEquals(x, value.x);
        assertEquals(y, value.y);
    }

    private static final class Key {
        private final String name;
        private final int id;

        private Key(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + id;
            return result;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            if (id != key.id) {
                return false;
            }
            if (name == null) {
                return key.name == null;
            }
            return name.equals(key.name);
        }
    }

    private static final class CollidingKey {
        private final int id;

        private CollidingKey(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof CollidingKey key && id == key.id;
        }
    }

    private static final class ThrowingHashKey {
        private final int id;

        private ThrowingHashKey(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            throw new AssertionError("Prehashed Object2NativeMap path must not call hashCode()");
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ThrowingHashKey key && id == key.id;
        }
    }

    private static final class Value {
        private int x;
        private long y;

        private Value() {
        }

        private Value(int x, long y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class ValueMemory implements NativeTypeMemory<Value> {
        private static final long X_OFFSET = 0L;
        private static final long Y_OFFSET = 8L;
        private static final long SIZE = 16L;

        @Override
        public long sizeof() {
            return SIZE;
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Value outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getLong(offset + Y_OFFSET);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Value element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putLong(offset + Y_OFFSET, element.y);
        }
    }
}
