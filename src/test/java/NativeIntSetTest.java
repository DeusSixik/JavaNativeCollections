import net.sixik.javastructg.structs.sets.NativeIntSet;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeIntSetTest {

    @Test
    public void testAddContainsAndRemove() {
        NativeIntSet set = new NativeIntSet(8);
        try {
            assertTrue(set.isEmpty());
            assertTrue(set.add(10));
            assertTrue(set.add(-7));
            assertTrue(set.contains(10));
            assertTrue(set.contains(-7));
            assertFalse(set.contains(99));
            assertEquals(2, set.size());

            assertTrue(set.remove(10));
            assertFalse(set.contains(10));
            assertFalse(set.remove(10));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testDuplicateAddDoesNotGrowSize() {
        NativeIntSet set = new NativeIntSet(4);
        try {
            assertTrue(set.add(42));
            assertFalse(set.add(42));
            assertFalse(set.add(42));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testDeletedSlotsCanBeReused() {
        NativeIntSet set = new NativeIntSet(8);
        try {
            assertTrue(set.add(1));
            assertTrue(set.add(9));
            assertTrue(set.remove(1));
            assertFalse(set.contains(1));
            assertTrue(set.add(17));
            assertTrue(set.contains(17));
            assertTrue(set.contains(9));
            assertEquals(2, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testClearResetsContent() {
        NativeIntSet set = new NativeIntSet(8);
        try {
            set.add(1);
            set.add(2);
            set.add(3);

            set.clear();

            assertTrue(set.isEmpty());
            assertFalse(set.contains(1));
            assertFalse(set.contains(2));
            assertFalse(set.contains(3));
            assertTrue(set.add(4));
            assertTrue(set.contains(4));
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testResizeKeepsAllValues() {
        NativeIntSet set = new NativeIntSet(4);
        Set<Integer> expected = new HashSet<>();
        try {
            for (int i = -200; i <= 200; i++) {
                assertTrue(set.add(i));
                expected.add(i);
            }

            assertTrue(set.capacity() >= 512);
            assertEquals(expected.size(), set.size());

            for (int value : expected) {
                assertTrue(set.contains(value));
            }
        } finally {
            set.freeMemory();
        }
    }
}
