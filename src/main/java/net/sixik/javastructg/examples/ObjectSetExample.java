package net.sixik.javastructg.examples;

import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.sets.NativeObjectSet;
import net.sixik.javastructg.utils.NativeUtils;
import sun.misc.Unsafe;

import java.util.Objects;

public final class ObjectSetExample {

    private ObjectSetExample() {
    }

    public static int countUnique(Person[] people) {
        PersonMemory memory = new PersonMemory();
        NativeObjectSet<Person> set = new NativeObjectSet<>(people.length * 2, memory, Person::new);
        try {
            for (Person person : people) {
                set.add(person);
            }
            return set.size();
        } finally {
            set.freeMemory();
        }
    }

    public static Person[] uniqueInEncounterOrder(Person[] people) {
        PersonMemory memory = new PersonMemory();
        NativeObjectSet<Person> seen = new NativeObjectSet<>(people.length * 2, memory, Person::new);
        try {
            Person[] out = new Person[people.length];
            int outSize = 0;

            for (Person person : people) {
                if (seen.add(person)) {
                    out[outSize++] = new Person(person.name, person.x, person.y);
                }
            }

            Person[] result = new Person[outSize];
            System.arraycopy(out, 0, result, 0, outSize);
            return result;
        } finally {
            seen.freeMemory();
        }
    }

    public static boolean contains(Person[] people, Person needle) {
        PersonMemory memory = new PersonMemory();
        NativeObjectSet<Person> set = new NativeObjectSet<>(people.length * 2, memory, Person::new);
        try {
            for (Person person : people) {
                set.add(person);
            }
            return set.contains(needle);
        } finally {
            set.freeMemory();
        }
    }

    public static final class Person {
        public String name;
        public int x;
        public int y;

        public Person() {
        }

        public Person(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    public static final class PersonMemory implements NativeTypeMemory<Person> {
        public static final int MAX_NAME_LENGTH = 32;

        private static final NativeStructLayout LAYOUT;
        private static final long X_OFFSET;
        private static final long Y_OFFSET;
        private static final NativeStructLayout.StringField NAME_FIELD;

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            X_OFFSET = builder.intField();
            Y_OFFSET = builder.intField();
            NAME_FIELD = builder.intLengthPrefixedStringField(MAX_NAME_LENGTH);
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
            long seed = 0;
            seed = NativeUtils.hashCombine(seed, element.name != null ? element.name.hashCode() : 0);
            seed = NativeUtils.hashCombine(seed, element.x);
            seed = NativeUtils.hashCombine(seed, element.y);
            return NativeUtils.mix(seed);
        }

        @Override
        public boolean equals(Person left, Person right) {
            return left.x == right.x
                    && left.y == right.y
                    && Objects.equals(left.name, right.name);
        }

        @Override
        public boolean supportsEqualsMemory() {
            return true;
        }

        @Override
        public boolean supportsHashMemory() {
            return true;
        }

        @Override
        public long hashMemory(Unsafe unsafe, long offset) {
            long seed = 0;
            seed = NativeUtils.hashCombine(seed, hashNameInMemory(unsafe, offset));
            seed = NativeUtils.hashCombine(seed, unsafe.getInt(offset + X_OFFSET));
            seed = NativeUtils.hashCombine(seed, unsafe.getInt(offset + Y_OFFSET));
            return NativeUtils.mix(seed);
        }

        @Override
        public boolean equalsMemory(Unsafe unsafe, long offset, Person value) {
            if (unsafe.getInt(offset + X_OFFSET) != value.x) {
                return false;
            }
            if (unsafe.getInt(offset + Y_OFFSET) != value.y) {
                return false;
            }
            return Objects.equals(NAME_FIELD.read(unsafe, offset), value.name);
        }

        private static int hashNameInMemory(Unsafe unsafe, long offset) {
            long lengthAddress = offset + NAME_FIELD.lengthOffset();
            int length = unsafe.getInt(lengthAddress);
            long dataAddress = offset + NAME_FIELD.dataOffset();
            int hash = 0;

            for (int i = 0; i < length; i++) {
                hash = 31 * hash + unsafe.getChar(dataAddress + (i * 2L));
            }

            return hash;
        }
    }
}
