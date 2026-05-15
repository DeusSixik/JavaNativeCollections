import net.sixik.javastructg.examples.ObjectSetExample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectSetExampleTest {

    @Test
    public void testCountUnique() {
        ObjectSetExample.Person[] people = new ObjectSetExample.Person[]{
                new ObjectSetExample.Person("A", 1, 10),
                new ObjectSetExample.Person("B", 2, 20),
                new ObjectSetExample.Person("A", 1, 10),
                new ObjectSetExample.Person("C", 3, 30),
                new ObjectSetExample.Person("B", 2, 20)
        };

        assertEquals(3, ObjectSetExample.countUnique(people));
    }

    @Test
    public void testUniqueInEncounterOrder() {
        ObjectSetExample.Person[] people = new ObjectSetExample.Person[]{
                new ObjectSetExample.Person("A", 1, 10),
                new ObjectSetExample.Person("B", 2, 20),
                new ObjectSetExample.Person("A", 1, 10),
                new ObjectSetExample.Person("C", 3, 30)
        };

        ObjectSetExample.Person[] unique = ObjectSetExample.uniqueInEncounterOrder(people);

        assertEquals(3, unique.length);
        assertPerson(unique[0], "A", 1, 10);
        assertPerson(unique[1], "B", 2, 20);
        assertPerson(unique[2], "C", 3, 30);
    }

    @Test
    public void testContains() {
        ObjectSetExample.Person[] people = new ObjectSetExample.Person[]{
                new ObjectSetExample.Person("Left", 10, 20),
                new ObjectSetExample.Person("Right", 30, 40)
        };

        assertTrue(ObjectSetExample.contains(people, new ObjectSetExample.Person("Left", 10, 20)));
        assertFalse(ObjectSetExample.contains(people, new ObjectSetExample.Person("Missing", 10, 20)));
    }

    private static void assertPerson(ObjectSetExample.Person actual, String name, int x, int y) {
        assertEquals(name, actual.name);
        assertEquals(x, actual.x);
        assertEquals(y, actual.y);
    }
}
