import net.sixik.javastructg.structs.NativeStructCursor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class NativeStructTest {

    // Допустим, мы выделили 36 символов под строку
    public static final int MAX_STRING_LENGTH = 36;

    // Считаем размер: 
    // 2 байта (под реальную длину строки) + (36 символов * 2 байта) + 12 байт (три int'а)
    public static final int SIZEOF = Short.BYTES + (MAX_STRING_LENGTH * Character.BYTES) + (3 * Integer.BYTES);

    private Unsafe unsafe;
    private long memoryAddress;
    private NativeStructCursor cursor;

    @BeforeEach
    public void setup() throws Exception {
        // Достаем Unsafe
        java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (Unsafe) f.get(null);

        // Выделяем память под 10 таких структур
        memoryAddress = unsafe.allocateMemory((long) 10 * SIZEOF);
        unsafe.setMemory(memoryAddress, (long) 10 * SIZEOF, (byte) 0);

        // Инициализируем наш курсор
        cursor = new NativeStructCursor(unsafe, memoryAddress, SIZEOF);
    }

    @AfterEach
    public void tearDown() {
        // Обязательно чистим память после каждого теста!
        if (memoryAddress != 0) {
            unsafe.freeMemory(memoryAddress);
        }
    }

    @Test
    public void testStringAndIntsSerialization() {
        // 1. Подготавливаем тестовые данные
        String originalText = UUID.randomUUID().toString(); // Длина ровно 36
        int x = 150;
        int y = 64;
        int z = -1000;

        // ==========================================
        // ЗАПИСЬ (WRITE) в индекс 0
        // ==========================================
        cursor.seekToIndex(0);
        writeFixedString(cursor, originalText);
        cursor.put(x);
        cursor.put(y);
        cursor.put(z);

        // Запишем еще что-нибудь в индекс 1 для проверки изоляции
        cursor.seekToIndex(1);
        writeFixedString(cursor, "Short Text");
        cursor.put(1);
        cursor.put(2);
        cursor.put(3);

        // ==========================================
        // ЧТЕНИЕ (READ) из индекса 0
        // ==========================================
        cursor.seekToIndex(0);
        String readText = readFixedString(cursor);
        int readX = cursor.getInt();
        int readY = cursor.getInt();
        int readZ = cursor.getInt();

        // 3. Проверяем (Asserts)
        assertEquals(originalText, readText, "Строки должны совпадать!");
        assertEquals(x, readX, "X должен совпадать!");
        assertEquals(y, readY, "Y должен совпадать!");
        assertEquals(z, readZ, "Z должен совпадать!");

        // Проверяем второй индекс, что ничего не затерлось
        cursor.seekToIndex(1);
        assertEquals("Short Text", readFixedString(cursor));
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (Добавь их в свой Struct / TypeMemory)
    // ==========================================

    private void writeFixedString(NativeStructCursor cursor, String text) {
        // Защита от переполнения: обрезаем строку, если она больше лимита
        int length = Math.min(text.length(), MAX_STRING_LENGTH);

        // 1. Пишем реальную длину строки (чтобы знать, сколько читать обратно)
        cursor.put((short) length);

        // 2. Пишем сами символы
        for (int i = 0; i < length; i++) {
            cursor.put(text.charAt(i));
        }

        // 3. Добиваем остаток пустыми символами (Padding), 
        // чтобы курсор всегда сдвигался ровно на MAX_STRING_LENGTH
        for (int i = length; i < MAX_STRING_LENGTH; i++) {
            cursor.put('\0');
        }
    }

    private String readFixedString(NativeStructCursor cursor) {
        // 1. Читаем, сколько символов реально записано
        short length = cursor.getShort();

        // 2. Читаем сами символы в массив
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = cursor.getChar();
        }

        // 3. Важно: прокручиваем курсор вперед на остаток "пустых" байт, 
        // чтобы он встал ровно перед следующими переменными (int x, y, z)
        for (int i = length; i < MAX_STRING_LENGTH; i++) {
            cursor.getChar();
        }

        // 4. Возвращаем готовую строку
        return new String(chars);
    }
}