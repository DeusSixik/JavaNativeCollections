import net.sixik.javastructg.structs.sets.NativeLongSet;
import net.sixik.javastructg.utils.NativeUtils;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeLongSetTest {

    @Test
    public void testAddContainsAndRemove() {
        NativeLongSet set = new NativeLongSet(8);
        try {
            assertTrue(set.isEmpty());
            assertTrue(set.add(10L));
            assertTrue(set.add(-7L));
            assertTrue(set.contains(10L));
            assertTrue(set.contains(-7L));
            assertFalse(set.contains(99L));
            assertEquals(2, set.size());

            assertTrue(set.remove(10L));
            assertFalse(set.contains(10L));
            assertFalse(set.remove(10L));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testDuplicateAddDoesNotGrowSize() {
        NativeLongSet set = new NativeLongSet(4);
        try {
            assertTrue(set.add(42L));
            assertFalse(set.add(42L));
            assertFalse(set.add(42L));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testCollisionProbeChainWorks() {
        NativeLongSet set = new NativeLongSet(8);
        try {
            long first = 1L;
            long second = findCollision(first, set.capacity());
            long third = findAnotherCollision(first, second, set.capacity());

            assertTrue(set.add(first));
            assertTrue(set.add(second));
            assertTrue(set.add(third));

            assertTrue(set.contains(first));
            assertTrue(set.contains(second));
            assertTrue(set.contains(third));
            assertEquals(3, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testDeletedSlotsCanBeReused() {
        NativeLongSet set = new NativeLongSet(8);
        try {
            long first = 1L;
            long second = findCollision(first, set.capacity());
            long third = findAnotherCollision(first, second, set.capacity());

            assertTrue(set.add(first));
            assertTrue(set.add(second));
            assertTrue(set.remove(first));
            assertFalse(set.contains(first));

            assertTrue(set.add(third));
            assertTrue(set.contains(third));
            assertTrue(set.contains(second));
            assertEquals(2, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testClearResetsContent() {
        NativeLongSet set = new NativeLongSet(8);
        try {
            set.add(1L);
            set.add(2L);
            set.add(3L);

            set.clear();

            assertTrue(set.isEmpty());
            assertFalse(set.contains(1L));
            assertFalse(set.contains(2L));
            assertFalse(set.contains(3L));
            assertTrue(set.add(4L));
            assertTrue(set.contains(4L));
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testResizeKeepsAllValues() {
        NativeLongSet set = new NativeLongSet(4);
        Set<Long> expected = new HashSet<>();
        try {
            for (long i = -200; i <= 200; i++) {
                assertTrue(set.add(i));
                expected.add(i);
            }

            assertTrue(set.capacity() >= 512);
            assertEquals(expected.size(), set.size());

            for (long value : expected) {
                assertTrue(set.contains(value));
            }
        } finally {
            set.freeMemory();
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
}
