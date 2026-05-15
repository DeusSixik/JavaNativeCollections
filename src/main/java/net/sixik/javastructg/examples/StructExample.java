package net.sixik.javastructg.examples;

import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import sun.misc.Unsafe;

public final class StructExample {

    private StructExample() {
    }

    public static Person roundTripSingle(String name, int x, int y, int z) {
        PersonMemory memory = new PersonMemory();
        NativeObjectArray<Person> nativeArray = new NativeObjectArray<>(1, memory);
        try {
            nativeArray.set(0, new Person(name, x, y, z));

            Person out = new Person();
            nativeArray.get(0, out);
            return out;
        } finally {
            nativeArray.freeMemory();
        }
    }

    public static int sumX(Person[] people) {
        PersonMemory memory = new PersonMemory();
        NativeObjectArray<Person> nativeArray = new NativeObjectArray<>(people.length, memory);
        try {
            for (int i = 0; i < people.length; i++) {
                nativeArray.set(i, people[i]);
            }

            Person cursor = new Person();
            int sum = 0;
            for (int i = 0; i < people.length; i++) {
                nativeArray.get(i, cursor);
                sum += cursor.x;
            }
            return sum;
        } finally {
            nativeArray.freeMemory();
        }
    }

    public static final class Person {
        public String name;
        public int x;
        public int y;
        public int z;

        public Person() {
        }

        public Person(String name, int x, int y, int z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final class PersonMemory implements NativeTypeMemory<Person> {
        public static final int MAX_NAME_LENGTH = 32;

        private static final NativeStructLayout LAYOUT;
        private static final long X_OFFSET;
        private static final long Y_OFFSET;
        private static final long Z_OFFSET;
        private static final NativeStructLayout.StringField NAME_FIELD;

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            X_OFFSET = builder.intField();
            Y_OFFSET = builder.intField();
            Z_OFFSET = builder.intField();
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
            outElement.z = unsafe.getInt(offset + Z_OFFSET);
            outElement.name = NAME_FIELD.read(unsafe, offset);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Person element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            unsafe.putInt(offset + Z_OFFSET, element.z);
            NAME_FIELD.write(unsafe, offset, element.name);
        }
    }
}
