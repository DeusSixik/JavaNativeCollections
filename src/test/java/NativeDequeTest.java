import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.deques.NativeArrayDeque;
import net.sixik.javastructg.structs.deques.NativeIntDeque;
import net.sixik.javastructg.structs.deques.NativeLongDeque;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeDequeTest {

    @Test
    public void testIntDequeAddPeekPollBothEnds() {
        NativeIntDeque deque = new NativeIntDeque(4);
        try {
            assertTrue(deque.isEmpty());
            deque.addLast(10);
            deque.addLast(20);
            deque.addFirst(5);

            assertEquals(3, deque.size());
            assertEquals(5, deque.getFirst());
            assertEquals(20, deque.getLast());
            assertEquals(5, deque.removeFirst());
            assertEquals(20, deque.removeLast());
            assertEquals(10, deque.removeFirst());
            assertEquals(-1, deque.pollFirst(-1));
            assertThrows(NoSuchElementException.class, deque::removeFirst);
        } finally {
            deque.freeMemory();
        }
    }

    @Test
    public void testIntDequeWrapAndGrowKeepsOrder() {
        NativeIntDeque deque = new NativeIntDeque(8);
        try {
            for (int i = 0; i < 8; i++) {
                deque.addLast(i);
            }
            for (int i = 0; i < 4; i++) {
                assertEquals(i, deque.removeFirst());
            }
            for (int i = 8; i < 20; i++) {
                deque.addLast(i);
            }

            assertTrue(deque.capacity() >= 16);
            for (int i = 4; i < 20; i++) {
                assertEquals(i, deque.removeFirst());
            }
            assertTrue(deque.isEmpty());
        } finally {
            deque.freeMemory();
        }
    }

    @Test
    public void testLongDequeAddPeekPollBothEnds() {
        NativeLongDeque deque = new NativeLongDeque(4);
        try {
            assertTrue(deque.isEmpty());
            deque.addLast(10L);
            deque.addLast(20L);
            deque.addFirst(5L);

            assertEquals(3, deque.size());
            assertEquals(5L, deque.getFirst());
            assertEquals(20L, deque.getLast());
            assertEquals(5L, deque.removeFirst());
            assertEquals(20L, deque.removeLast());
            assertEquals(10L, deque.removeFirst());
            assertEquals(-1L, deque.pollFirst(-1L));
            assertThrows(NoSuchElementException.class, deque::removeFirst);
        } finally {
            deque.freeMemory();
        }
    }

    @Test
    public void testLongDequeWrapAndGrowKeepsOrder() {
        NativeLongDeque deque = new NativeLongDeque(8);
        try {
            for (long i = 0; i < 8; i++) {
                deque.addLast(i);
            }
            for (long i = 0; i < 4; i++) {
                assertEquals(i, deque.removeFirst());
            }
            for (long i = 8; i < 20; i++) {
                deque.addLast(i);
            }

            assertTrue(deque.capacity() >= 16);
            for (long i = 4; i < 20; i++) {
                assertEquals(i, deque.removeFirst());
            }
            assertTrue(deque.isEmpty());
        } finally {
            deque.freeMemory();
        }
    }

    @Test
    public void testObjectDequeAddPeekPollAndDiscard() {
        NativeArrayDeque<Value> deque = new NativeArrayDeque<>(4, new ValueMemory());
        Value out = new Value();
        try {
            assertFalse(deque.pollFirst(out));
            deque.addLast(new Value(1, 10L));
            deque.addLast(new Value(2, 20L));
            deque.addFirst(new Value(0, 0L));

            assertTrue(deque.peekFirst(out));
            assertValue(out, 0, 0L);
            assertTrue(deque.peekLast(out));
            assertValue(out, 2, 20L);

            deque.removeFirst(out);
            assertValue(out, 0, 0L);
            assertTrue(deque.discardLast());
            deque.removeLast(out);
            assertValue(out, 1, 10L);
            assertFalse(deque.discardFirst());
            assertThrows(NoSuchElementException.class, () -> deque.removeLast(out));
        } finally {
            deque.freeMemory();
        }
    }

    @Test
    public void testObjectDequeWrapAndGrowKeepsOrder() {
        NativeArrayDeque<Value> deque = new NativeArrayDeque<>(8, new ValueMemory());
        Value out = new Value();
        try {
            for (int i = 0; i < 8; i++) {
                deque.addLast(new Value(i, i * 10L));
            }
            for (int i = 0; i < 4; i++) {
                deque.removeFirst(out);
                assertValue(out, i, i * 10L);
            }
            for (int i = 8; i < 20; i++) {
                deque.addLast(new Value(i, i * 10L));
            }

            assertTrue(deque.capacity() >= 16);
            for (int i = 4; i < 20; i++) {
                deque.removeFirst(out);
                assertValue(out, i, i * 10L);
            }
            assertTrue(deque.isEmpty());
        } finally {
            deque.freeMemory();
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
