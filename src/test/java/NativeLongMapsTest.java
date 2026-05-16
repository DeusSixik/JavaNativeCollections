import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.maps.NativeLong2ByteMap;
import net.sixik.javastructg.structs.maps.NativeLong2LongMap;
import net.sixik.javastructg.structs.maps.NativeLong2ObjectMap;
import net.sixik.javastructg.utils.NativeUtils;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeLongMapsTest {

    @Test
    public void testLong2LongPutGetUpdateRemoveAndClear() {
        NativeLong2LongMap map = new NativeLong2LongMap(4);
        try {
            assertTrue(map.isEmpty());
            assertTrue(map.put(10L, 100L));
            assertFalse(map.put(10L, 101L));
            assertTrue(map.put(-7L, -70L));

            assertEquals(2, map.size());
            assertEquals(101L, map.get(10L, -1L));
            assertEquals(-70L, map.get(-7L, -1L));
            assertEquals(-1L, map.get(99L, -1L));

            assertTrue(map.containsKey(10L));
            assertTrue(map.remove(10L));
            assertFalse(map.containsKey(10L));
            assertFalse(map.remove(10L));
            assertEquals(1, map.size());

            map.clear();
            assertTrue(map.isEmpty());
            assertEquals(-1L, map.get(-7L, -1L));
            assertTrue(map.put(5L, 50L));
            assertEquals(50L, map.get(5L, -1L));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testLong2LongResizeKeepsValues() {
        NativeLong2LongMap map = new NativeLong2LongMap(4);
        try {
            for (long i = -200; i <= 200; i++) {
                assertTrue(map.put(i, i * 10L));
            }

            assertTrue(map.capacity() >= 512);
            assertEquals(401, map.size());

            for (long i = -200; i <= 200; i++) {
                assertEquals(i * 10L, map.get(i, Long.MIN_VALUE));
            }
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testLong2BytePutGetUpdateRemoveAndResize() {
        NativeLong2ByteMap map = new NativeLong2ByteMap(4);
        try {
            assertTrue(map.put(1L, (byte) 10));
            assertFalse(map.put(1L, (byte) 11));
            assertTrue(map.put(2L, (byte) -2));

            assertEquals((byte) 11, map.get(1L, (byte) -1));
            assertEquals((byte) -2, map.get(2L, (byte) -1));
            assertEquals((byte) -1, map.get(3L, (byte) -1));
            assertTrue(map.remove(1L));
            assertFalse(map.containsKey(1L));

            for (long i = 10; i < 300; i++) {
                assertTrue(map.put(i, (byte) i));
            }

            for (long i = 10; i < 300; i++) {
                assertEquals((byte) i, map.get(i, (byte) -1));
            }
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testLong2ByteReusesDeletedSlotInProbeChain() {
        NativeLong2ByteMap map = new NativeLong2ByteMap(8);
        try {
            long first = 1L;
            long second = findCollision(first, map.capacity());
            long third = findAnotherCollision(first, second, map.capacity());

            assertTrue(map.put(first, (byte) 1));
            assertTrue(map.put(second, (byte) 2));
            assertTrue(map.remove(first));
            assertTrue(map.put(third, (byte) 3));

            assertEquals((byte) 2, map.get(second, (byte) -1));
            assertEquals((byte) 3, map.get(third, (byte) -1));
            assertFalse(map.containsKey(first));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testLong2ObjectPutGetUpdateRemoveAndResize() {
        NativeLong2ObjectMap<Value> map = new NativeLong2ObjectMap<>(4, new ValueMemory());
        Value out = new Value();
        try {
            assertTrue(map.put(10L, new Value(1, 100L)));
            assertFalse(map.put(10L, new Value(2, 200L)));
            assertTrue(map.get(10L, out));
            assertValue(out, 2, 200L);

            for (long i = 0; i < 200; i++) {
                assertTrue(map.put(i + 1000L, new Value((int) i, i * 3L)));
            }

            assertEquals(201, map.size());
            for (long i = 0; i < 200; i++) {
                assertTrue(map.get(i + 1000L, out));
                assertValue(out, (int) i, i * 3L);
            }

            assertTrue(map.remove(10L));
            assertFalse(map.get(10L, out));
            assertFalse(map.remove(10L));
        } finally {
            map.freeMemory();
        }
    }

    private static long findCollision(long value, int capacity) {
        long targetBucket = bucket(value, capacity);

        for (long candidate = value + 1; candidate < Long.MAX_VALUE; candidate++) {
            if (bucket(candidate, capacity) == targetBucket) {
                return candidate;
            }
        }

        throw new AssertionError("Failed to find collision for value " + value);
    }

    private static long findAnotherCollision(long first, long second, int capacity) {
        long targetBucket = bucket(first, capacity);

        for (long candidate = second + 1; candidate < Long.MAX_VALUE; candidate++) {
            if (bucket(candidate, capacity) == targetBucket) {
                return candidate;
            }
        }

        throw new AssertionError("Failed to find another collision for value " + first);
    }

    private static long bucket(long value, int capacity) {
        return NativeUtils.mix(value) & (capacity - 1L);
    }

    private static void assertValue(Value value, int x, long y) {
        assertEquals(x, value.x);
        assertEquals(y, value.y);
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
