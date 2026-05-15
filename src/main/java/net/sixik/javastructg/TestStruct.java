package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.NativeTypes;
import sun.misc.Unsafe;

public class TestStruct implements NativeTypeMemory<TestStruct> {

    public static final int MAX_STRING_LENGTH = NativeTypes.UUID;
    private static final NativeStructLayout LAYOUT;
    private static final long X_OFFSET;
    private static final long Y_OFFSET;
    private static final long Z_OFFSET;
    private static final NativeStructLayout.StringField NAME_FIELD;
    public static final int SIZEOF;

    static {
        NativeStructLayout.Builder builder = NativeStructLayout.builder();
        X_OFFSET = builder.intField();
        Y_OFFSET = builder.intField();
        Z_OFFSET = builder.intField();
        NAME_FIELD = builder.intLengthPrefixedStringField(MAX_STRING_LENGTH);
        LAYOUT = builder.build();
        SIZEOF = (int) LAYOUT.sizeof();
    }

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
        outElement.x = unsafe.getInt(offset + X_OFFSET);
        outElement.y = unsafe.getInt(offset + Y_OFFSET);
        outElement.z = unsafe.getInt(offset + Z_OFFSET);
        outElement.myName = NAME_FIELD.read(unsafe, offset);
    }

    @Override
    public void writeToMemory(Unsafe unsafe, long offset, TestStruct element) {
        unsafe.putInt(offset + X_OFFSET, element.x);
        unsafe.putInt(offset + Y_OFFSET, element.y);
        unsafe.putInt(offset + Z_OFFSET, element.z);
        NAME_FIELD.write(unsafe, offset, element.myName);
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
