import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.maps.NativeInt2IntMap;
import net.sixik.javastructg.structs.maps.NativeInt2LongMap;
import net.sixik.javastructg.structs.maps.NativeInt2ObjectMap;
import net.sixik.javastructg.structs.maps.NativeLong2BooleanMap;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeIntMapsTest {

    @Test
    public void testInt2IntPutGetUpdateRemoveAndResize() {
        NativeInt2IntMap map = new NativeInt2IntMap(4);
        try {
            assertTrue(map.put(1, 10));
            assertFalse(map.put(1, 11));
            assertTrue(map.put(-2, -20));

            assertEquals(11, map.get(1, -1));
            assertEquals(-20, map.get(-2, -1));
            assertEquals(-1, map.get(3, -1));

            for (int i = 0; i < 300; i++) {
                assertTrue(map.put(i + 100, i * 2));
            }

            assertTrue(map.capacity() >= 512);
            for (int i = 0; i < 300; i++) {
                assertEquals(i * 2, map.get(i + 100, -1));
            }

            assertTrue(map.remove(1));
            assertFalse(map.containsKey(1));
            assertFalse(map.remove(1));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testInt2LongPutGetUpdateRemoveAndResize() {
        NativeInt2LongMap map = new NativeInt2LongMap(4);
        try {
            assertTrue(map.put(1, 100L));
            assertFalse(map.put(1, 101L));
            assertTrue(map.put(-2, -200L));

            assertEquals(101L, map.get(1, -1L));
            assertEquals(-200L, map.get(-2, -1L));

            for (int i = 0; i < 300; i++) {
                assertTrue(map.put(i + 100, i * 10L));
            }

            for (int i = 0; i < 300; i++) {
                assertEquals(i * 10L, map.get(i + 100, -1L));
            }

            assertTrue(map.remove(-2));
            assertFalse(map.containsKey(-2));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testInt2ObjectPutGetUpdateRemoveAndResize() {
        NativeInt2ObjectMap<Value> map = new NativeInt2ObjectMap<>(4, new ValueMemory());
        Value out = new Value();
        try {
            assertTrue(map.put(1, new Value(10, 100L)));
            assertFalse(map.put(1, new Value(11, 101L)));
            assertTrue(map.get(1, out));
            assertValue(out, 11, 101L);

            for (int i = 0; i < 300; i++) {
                assertTrue(map.put(i + 100, new Value(i, i * 5L)));
            }

            for (int i = 0; i < 300; i++) {
                assertTrue(map.get(i + 100, out));
                assertValue(out, i, i * 5L);
            }

            assertTrue(map.remove(1));
            assertFalse(map.get(1, out));
        } finally {
            map.freeMemory();
        }
    }

    @Test
    public void testLong2BooleanPutGetUpdateRemoveAndResize() {
        NativeLong2BooleanMap map = new NativeLong2BooleanMap(4);
        try {
            assertTrue(map.put(10L, true));
            assertFalse(map.put(10L, false));
            assertTrue(map.put(-7L, true));

            assertFalse(map.get(10L, true));
            assertTrue(map.get(-7L, false));
            assertTrue(map.get(99L, true));

            for (long i = 0; i < 300; i++) {
                assertTrue(map.put(i + 1000L, (i & 1L) == 0L));
            }

            for (long i = 0; i < 300; i++) {
                assertEquals((i & 1L) == 0L, map.get(i + 1000L, false));
            }

            assertTrue(map.remove(10L));
            assertFalse(map.containsKey(10L));
        } finally {
            map.freeMemory();
        }
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
