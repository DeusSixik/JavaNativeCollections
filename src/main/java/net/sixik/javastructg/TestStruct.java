package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.NativeTypes;
import sun.misc.Unsafe;

public class TestStruct implements NativeTypeMemory<TestStruct> {

    public static final int MAX_STRING_LENGTH = NativeTypes.UUID;
    public static final int SIZEOF = 16 + (MAX_STRING_LENGTH * NativeTypes.CHAR);

    private String myName;
    private int x;
    private int y;
    private int z;

    public TestStruct() {

    }

    public TestStruct(String myName, int x, int y, int z) {
        update(myName, x, y, z);
    }

    public void update(String myName, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.myName = myName;
    }

    public void clean() {
        update("null", 0, 0, 0);
    }

    @Override
    public void readFromMemory(Unsafe unsafe, long offset, TestStruct outElement) {
        outElement.x = unsafe.getInt(offset);
        outElement.y = unsafe.getInt(offset + 4);
        outElement.z = unsafe.getInt(offset + 8);

        int len = unsafe.getInt(offset + 12);
        if (len > 0) {
            char[] chars = new char[len];

            // Быстрое копирование из нативной памяти обратно в массив на хипе
            unsafe.copyMemory(
                    null,
                    offset + 16,
                    chars,
                    Unsafe.ARRAY_CHAR_BASE_OFFSET,
                    len * 2L
            );
            outElement.myName = new String(chars);
        } else {
            outElement.myName = "";
        }
    }

    @Override
    public void writeToMemory(Unsafe unsafe, long offset, TestStruct element) {
        unsafe.putInt(offset, element.x);
        unsafe.putInt(offset + 4, element.y);
        unsafe.putInt(offset + 8, element.z);

        if (element.myName != null) {
            char[] chars = element.myName.toCharArray(); // В Java 9+ строки внутри байтовые, но toCharArray() универсальнее
            int len = Math.min(chars.length, MAX_STRING_LENGTH);

            // Пишем реальную длину строки
            unsafe.putInt(offset + 12, len);

            // Быстрое копирование массива из Heap (хипа) в Off-Heap (нативную память)
            unsafe.copyMemory(
                    chars,
                    Unsafe.ARRAY_CHAR_BASE_OFFSET, // Смещение начала данных в массиве на хипе
                    null,
                    offset + 16,                   // Наш целевой адрес в нативной памяти
                    len * 2L                       // Количество байт (len * 2, так как char = 2 байта)
            );
        } else {
            unsafe.putInt(offset + 12, 0);
        }
    }

    @Override
    public long sizeof() {
        return SIZEOF;
    }

    @Override
    public String toString() {
        return "Struct: " + myName + ", " + x + ", " + y + ", " + z;
    }
}
