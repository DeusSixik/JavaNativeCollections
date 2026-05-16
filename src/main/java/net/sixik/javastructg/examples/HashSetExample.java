package net.sixik.javastructg.examples;

import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.sets.NativeHashSet;
import sun.misc.Unsafe;

public final class HashSetExample {

    private HashSetExample() {
    }

    public static int countDistinctHashes(Person[] people) {
        PersonMemory memory = new PersonMemory();
        NativeHashSet<Person> set = new NativeHashSet<>(people.length * 2, memory);
        try {
            for (Person person : people) {
                set.add(person);
            }
            return set.size();
        } finally {
            set.freeMemory();
        }
    }

    public static boolean containsHash(Person[] people, Person needle) {
        PersonMemory memory = new PersonMemory();
        NativeHashSet<Person> set = new NativeHashSet<>(people.length * 2, memory);
        try {
            for (Person person : people) {
                set.add(person);
            }
            return set.contains(needle);
        } finally {
            set.freeMemory();
        }
    }

    public static PrehashedPersonSet prehashed(Person[] people) {
        PrehashedPersonSet set = new PrehashedPersonSet(people.length * 2);
        for (Person person : people) {
            set.add(person);
        }
        return set;
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

    public static final class PrehashedPersonSet implements AutoCloseable {
        private final PersonMemory memory = new PersonMemory();
        private final NativeHashSet<Person> set;

        public PrehashedPersonSet(int expectedCapacity) {
            this.set = new NativeHashSet<>(expectedCapacity, memory);
        }

        public long hash(Person person) {
            return memory.hash(person);
        }

        public boolean add(Person person) {
            return set.addHash(hash(person));
        }

        public boolean addHash(long hash) {
            return set.addHash(hash);
        }

        public boolean contains(Person person) {
            return set.containsHash(hash(person));
        }

        public boolean containsHash(long hash) {
            return set.containsHash(hash);
        }

        public boolean remove(Person person) {
            return set.removeHash(hash(person));
        }

        public boolean removeHash(long hash) {
            return set.removeHash(hash);
        }

        public int size() {
            return set.size();
        }

        @Override
        public void close() {
            set.freeMemory();
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
            int result = element.name != null ? element.name.hashCode() : 0;
            result = 31 * result + element.x;
            result = 31 * result + element.y;
            return result;
        }
    }
}
