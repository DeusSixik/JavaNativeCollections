import net.sixik.javastructg.structs.maps.NativeIdentity2BooleanMap;
import net.sixik.javastructg.structs.maps.NativeIdentity2ByteMap;
import net.sixik.javastructg.structs.maps.NativeIdentity2IntMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeIdentityMapsTest {

    @Test
    public void testIdentity2IntUsesReferenceIdentityNotEquals() {
        NativeIdentity2IntMap<Key> map = new NativeIdentity2IntMap<>(4);
        try {
            Key left = new Key(1);
            Key equalCopy = new Key(1);

            assertTrue(map.put(left, 10));
            assertTrue(map.put(equalCopy, 20));
            assertFalse(map.put(left, 11));

            assertEquals(11, map.get(left, -1));
            assertEquals(20, map.get(equalCopy, -1));
            assertEquals(-1, map.get(new Key(1), -1));
            assertEquals(2, map.size());

            assertTrue(map.remove(left));
            assertFalse(map.containsKey(left));
            assertEquals(20, map.get(equalCopy, -1));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testIdentity2IntSupportsNullAndResize() {
        NativeIdentity2IntMap<Key> map = new NativeIdentity2IntMap<>(4);
        try {
            assertTrue(map.put(null, 100));
            assertFalse(map.put(null, 101));
            assertEquals(101, map.get(null, -1));

            Key[] keys = new Key[300];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = new Key(i);
                assertTrue(map.put(keys[i], i * 3));
            }

            assertTrue(map.capacity() >= 512);
            for (int i = 0; i < keys.length; i++) {
                assertEquals(i * 3, map.get(keys[i], -1));
            }
            assertEquals(101, map.get(null, -1));

            map.clear();
            assertTrue(map.isEmpty());
            assertEquals(-1, map.get(null, -1));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testIdentity2BytePutGetRemoveAndResize() {
        NativeIdentity2ByteMap<Key> map = new NativeIdentity2ByteMap<>(4);
        try {
            Key left = new Key(1);
            Key equalCopy = new Key(1);

            assertTrue(map.put(left, (byte) 10));
            assertTrue(map.put(equalCopy, (byte) 20));
            assertFalse(map.put(left, (byte) 11));

            assertEquals((byte) 11, map.get(left, (byte) -1));
            assertEquals((byte) 20, map.get(equalCopy, (byte) -1));

            for (int i = 0; i < 300; i++) {
                assertTrue(map.put(new Key(i + 100), (byte) i));
            }

            assertTrue(map.remove(left));
            assertFalse(map.containsKey(left));
            assertEquals((byte) 20, map.get(equalCopy, (byte) -1));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testIdentity2BooleanPutGetRemoveAndResize() {
        NativeIdentity2BooleanMap<Key> map = new NativeIdentity2BooleanMap<>(4);
        try {
            Key left = new Key(1);
            Key equalCopy = new Key(1);

            assertTrue(map.put(left, true));
            assertTrue(map.put(equalCopy, false));
            assertFalse(map.put(left, false));

            assertFalse(map.get(left, true));
            assertFalse(map.get(equalCopy, true));
            assertTrue(map.get(new Key(1), true));

            for (int i = 0; i < 300; i++) {
                assertTrue(map.put(new Key(i + 100), (i & 1) == 0));
            }

            assertTrue(map.remove(left));
            assertFalse(map.containsKey(left));
            assertFalse(map.get(equalCopy, true));
        } finally {
            map.freeMemory();
        }
    }

    private static final class Key {
        private final int value;

        private Key(int value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && value == key.value;
        }
    }
}
