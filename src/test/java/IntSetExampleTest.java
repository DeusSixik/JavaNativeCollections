import net.sixik.javastructg.IntSetExample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntSetExampleTest {

    @Test
    public void testCountUnique() {
        assertEquals(4, IntSetExample.countUnique(new int[]{1, 2, 1, 3, 2, 4}));
    }

    @Test
    public void testUniqueInEncounterOrder() {
        assertArrayEquals(
                new int[]{7, 1, 9, 3},
                IntSetExample.uniqueInEncounterOrder(new int[]{7, 1, 7, 9, 1, 3, 9})
        );
    }

    @Test
    public void testContainsAny() {
        assertTrue(IntSetExample.containsAny(new int[]{10, 20, 30}, new int[]{5, 20, 40}));
        assertFalse(IntSetExample.containsAny(new int[]{10, 20, 30}, new int[]{5, 6, 7}));
    }
}
