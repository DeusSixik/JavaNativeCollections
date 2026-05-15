import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeArrayTest {

    private NativeObjectArray<TestStruct> nativeArray;
    private TestStructMemory structMemory;

    @BeforeEach
    public void setup() {
        structMemory = new TestStructMemory();
        nativeArray = new NativeObjectArray<>(10, structMemory);
    }

    @AfterEach
    public void tearDown() {
        if (nativeArray != null) {
            nativeArray.freeMemory();
        }
    }

    @Test
    public void testSizeAndMemoryAllocation() {
        assertEquals(0, nativeArray.size());
        assertEquals(10, nativeArray.capacity());
        assertEquals(10L * structMemory.sizeof(), nativeArray.sizeof());
    }

    @Test
    public void testReadWriteSingleElement() {
        TestStruct dataToWrite = new TestStruct((short) 42, 36.6f, 0.99f);
        TestStruct bufferToRead = new TestStruct();

        nativeArray.add(dataToWrite);
        nativeArray.get(0, bufferToRead);

        assertEquals(1, nativeArray.size());
        assertEquals((short) 42, bufferToRead.id);
        assertEquals(36.6f, bufferToRead.temp);
        assertEquals(0.99f, bufferToRead.humidity);
    }

    @Test
    public void testMemoryIsolation() {
        nativeArray.add(new TestStruct((short) 10, 1.0f, 1.0f));
        nativeArray.add(new TestStruct((short) 20, 2.0f, 2.0f));
        nativeArray.add(new TestStruct((short) 30, 3.0f, 3.0f));

        TestStruct buffer = new TestStruct();

        nativeArray.get(1, buffer);
        assertEquals((short) 20, buffer.id);
        assertEquals(2.0f, buffer.temp);

        nativeArray.get(0, buffer);
        assertEquals((short) 10, buffer.id);

        nativeArray.get(2, buffer);
        assertEquals((short) 30, buffer.id);
    }

    @Test
    public void testOverwriteElement() {
        nativeArray.add(new TestStruct((short) 1, 1f, 1f));
        nativeArray.add(new TestStruct((short) 2, 2f, 2f));
        nativeArray.add(new TestStruct((short) 3, 3f, 3f));
        nativeArray.add(new TestStruct((short) 5, 0f, 0f));

        TestStruct buffer = new TestStruct();
        nativeArray.set(3, new TestStruct((short) 99, 100f, 50f));
        nativeArray.get(3, buffer);

        assertEquals((short) 99, buffer.id);
        assertEquals(100f, buffer.temp);
        assertEquals(50f, buffer.humidity);
    }

    @Test
    public void testGrowPreservesValues() {
        NativeObjectArray<TestStruct> smallArray = new NativeObjectArray<>(2, structMemory);
        TestStruct buffer = new TestStruct();

        try {
            smallArray.add(new TestStruct((short) 7, 7f, 7f));
            smallArray.add(new TestStruct((short) 8, 8f, 8f));
            smallArray.add(new TestStruct((short) 9, 9f, 9f));

            assertEquals(3, smallArray.size());
            assertTrue(smallArray.capacity() >= 3);

            smallArray.get(0, buffer);
            assertEquals((short) 7, buffer.id);
            smallArray.get(2, buffer);
            assertEquals((short) 9, buffer.id);
        } finally {
            smallArray.freeMemory();
        }
    }

    @Test
    public void testCopyConstructorCreatesIndependentBuffer() {
        nativeArray.add(new TestStruct((short) 11, 1.1f, 1.2f));
        nativeArray.add(new TestStruct((short) 22, 2.1f, 2.2f));

        NativeObjectArray<TestStruct> copy = new NativeObjectArray<>(nativeArray);
        TestStruct buffer = new TestStruct();

        try {
            nativeArray.set(0, new TestStruct((short) 33, 3.1f, 3.2f));

            copy.get(0, buffer);
            assertEquals((short) 11, buffer.id);
            assertEquals(1.1f, buffer.temp);
            assertEquals(2, copy.size());
            assertNotEquals(nativeArray.ptr(), copy.ptr());
        } finally {
            copy.freeMemory();
        }
    }

    static class TestStruct {
        short id;
        float temp;
        float humidity;

        TestStruct() {
        }

        TestStruct(short id, float temp, float humidity) {
            this.id = id;
            this.temp = temp;
            this.humidity = humidity;
        }
    }

    static class TestStructMemory implements NativeTypeMemory<TestStruct> {
        @Override
        public long sizeof() {
            return 10;
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, TestStruct element) {
            unsafe.putShort(offset, element.id);
            unsafe.putFloat(offset + 2, element.temp);
            unsafe.putFloat(offset + 6, element.humidity);
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, TestStruct outElement) {
            outElement.id = unsafe.getShort(offset);
            outElement.temp = unsafe.getFloat(offset + 2);
            outElement.humidity = unsafe.getFloat(offset + 6);
        }
    }
}
