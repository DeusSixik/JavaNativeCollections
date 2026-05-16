import net.sixik.javastructg.examples.HashSetExample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HashSetExampleTest {

    @Test
    public void testCountDistinctHashes() {
        HashSetExample.Person[] people = new HashSetExample.Person[]{
                new HashSetExample.Person("A", 1, 10),
                new HashSetExample.Person("B", 2, 20),
                new HashSetExample.Person("A", 1, 10),
                new HashSetExample.Person("C", 3, 30),
                new HashSetExample.Person("B", 2, 20)
        };

        assertEquals(3, HashSetExample.countDistinctHashes(people));
    }

    @Test
    public void testContainsHash() {
        HashSetExample.Person[] people = new HashSetExample.Person[]{
                new HashSetExample.Person("Left", 10, 20),
                new HashSetExample.Person("Right", 30, 40)
        };

        assertTrue(HashSetExample.containsHash(people, new HashSetExample.Person("Left", 10, 20)));
        assertFalse(HashSetExample.containsHash(people, new HashSetExample.Person("Missing", 10, 20)));
    }

    @Test
    public void testPrehashedSetUsage() {
        HashSetExample.Person[] people = new HashSetExample.Person[]{
                new HashSetExample.Person("Left", 10, 20),
                new HashSetExample.Person("Right", 30, 40),
                new HashSetExample.Person("Left", 10, 20)
        };

        try (HashSetExample.PrehashedPersonSet set = HashSetExample.prehashed(people)) {
            long leftHash = set.hash(new HashSetExample.Person("Left", 10, 20));
            long missingHash = set.hash(new HashSetExample.Person("Missing", 10, 20));

            assertEquals(2, set.size());
            assertTrue(set.containsHash(leftHash));
            assertFalse(set.containsHash(missingHash));
            assertTrue(set.removeHash(leftHash));
            assertFalse(set.containsHash(leftHash));
            assertEquals(1, set.size());
        }
    }
}
