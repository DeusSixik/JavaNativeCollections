import net.sixik.javastructg.StructExample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StructExampleTest {

    @Test
    public void testRoundTripSingle() {
        StructExample.Person person = StructExample.roundTripSingle("Alice", 10, 20, 30);

        assertEquals("Alice", person.name);
        assertEquals(10, person.x);
        assertEquals(20, person.y);
        assertEquals(30, person.z);
    }

    @Test
    public void testSumXAcrossArray() {
        StructExample.Person[] people = new StructExample.Person[]{
                new StructExample.Person("A", 3, 1, 1),
                new StructExample.Person("B", 5, 2, 2),
                new StructExample.Person("C", 7, 3, 3)
        };

        assertEquals(15, StructExample.sumX(people));
    }
}
