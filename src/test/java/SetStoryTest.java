import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.sets.NativeHashSet;
import net.sixik.javastructg.structs.sets.NativeObjectSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Set story tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class SetStoryTest {

    @Test
    public void safe_native_object_set_distinguishes_different_objects_even_when_hashes_collide() {
        CollidingPersonMemory memory = new CollidingPersonMemory();
        NativeObjectSet<Person> set = new NativeObjectSet<>(8, memory, Person::new);
        try {
            Person left = new Person("left", 1, 11);
            Person right = new Person("right", 2, 22);

            assertTrue(set.add(left));
            assertTrue(set.add(right));
            assertTrue(set.contains(new Person("left", 1, 11)));
            assertTrue(set.contains(new Person("right", 2, 22)));
            assertEquals(2, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void safe_native_object_set_prehashed_reuses_hash_but_keeps_real_object_equality() {
        PersonMemory hashSource = new PersonMemory();
        ThrowingHashPersonMemory setMemory = new ThrowingHashPersonMemory();
        NativeObjectSet<Person> set = new NativeObjectSet<>(8, setMemory, Person::new);
        try {
            Person left = new Person("left", 1, 11);
            Person duplicateLeft = new Person("left", 1, 11);
            Person missing = new Person("missing", 1, 11);

            long leftHash = hashSource.hash(left);
            long missingHash = hashSource.hash(missing);

            assertTrue(set.addPrehashed(left, leftHash));
            assertFalse(set.addPrehashed(duplicateLeft, leftHash));
            assertTrue(set.containsPrehashed(duplicateLeft, leftHash));
            assertFalse(set.containsPrehashed(missing, missingHash));
            assertTrue(set.removePrehashed(duplicateLeft, leftHash));
            assertFalse(set.containsPrehashed(duplicateLeft, leftHash));
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void fast_native_hash_set_uses_hash_identity_and_can_merge_collisions() {
        CollidingPersonMemory memory = new CollidingPersonMemory();
        NativeHashSet<Person> set = new NativeHashSet<>(8, memory);
        try {
            Person left = new Person("left", 1, 11);
            Person right = new Person("right", 2, 22);

            assertTrue(set.add(left));
            assertFalse(set.add(right));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    private static final class Person {
        private String name;
        private int x;
        private int y;

        private Person() {
        }

        private Person(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    private static class PersonMemory implements NativeTypeMemory<Person> {
        private static final NativeStructLayout LAYOUT;
        private static final long X_OFFSET;
        private static final long Y_OFFSET;
        private static final NativeStructLayout.StringField NAME_FIELD;

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            X_OFFSET = builder.intField();
            Y_OFFSET = builder.intField();
            NAME_FIELD = builder.intLengthPrefixedStringField(32);
            LAYOUT = builder.build();
        }

        @Override
        public long sizeof() {
            return LAYOUT.sizeof();
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Person outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
            outElement.name = NAME_FIELD.read(unsafe, offset);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Person element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            NAME_FIELD.write(unsafe, offset, element.name);
        }

        @Override
        public long hash(Person element) {
            int result = element.name != null ? element.name.hashCode() : 0;
            result = 31 * result + element.x;
            result = 31 * result + element.y;
            return result;
        }

        @Override
        public boolean equals(Person left, Person right) {
            return left.x == right.x
                    && left.y == right.y
                    && java.util.Objects.equals(left.name, right.name);
        }

        @Override
        public boolean supportsHashMemory() {
            return true;
        }

        @Override
        public long hashMemory(Unsafe unsafe, long offset) {
            int result = NAME_FIELD.hashCode(unsafe, offset);
            result = 31 * result + unsafe.getInt(offset + X_OFFSET);
            result = 31 * result + unsafe.getInt(offset + Y_OFFSET);
            return result;
        }

        @Override
        public boolean supportsEqualsMemory() {
            return true;
        }

        @Override
        public boolean equalsMemory(Unsafe unsafe, long offset, Person value) {
            if (unsafe.getInt(offset + X_OFFSET) != value.x) {
                return false;
            }
            if (unsafe.getInt(offset + Y_OFFSET) != value.y) {
                return false;
            }
            return NAME_FIELD.equals(unsafe, offset, value.name);
        }
    }

    private static final class ThrowingHashPersonMemory extends PersonMemory {
        @Override
        public long hash(Person element) {
            throw new AssertionError("Prehashed story test must not call hash() inside NativeObjectSet");
        }
    }

    private static final class CollidingPersonMemory extends PersonMemory {
        @Override
        public long hash(Person element) {
            return 1L;
        }

        @Override
        public long hashMemory(Unsafe unsafe, long offset) {
            return 1L;
        }
    }
}
