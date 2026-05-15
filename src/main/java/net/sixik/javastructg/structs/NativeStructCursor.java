package net.sixik.javastructg.structs;

import sun.misc.Unsafe;

public final class NativeStructCursor {

    private final Unsafe unsafe;
    private final long baseAddress;
    private final int structSizeBytes;

    private long currentOffset;

    public NativeStructCursor(Unsafe unsafe, long baseAddress, int structSizeBytes) {
        this.unsafe = unsafe;
        this.baseAddress = baseAddress;
        this.structSizeBytes = structSizeBytes;
    }

    /**
     * Прыгает на начало указанного элемента в массиве.
     * Вызывать перед началом чтения или записи нового блока.
     */
    public void seekToIndex(int index) {
        this.currentOffset = baseAddress + ((long) index * structSizeBytes);
    }

    // =========================================
    // ЗАПИСЬ (WRITE)
    // =========================================

    public void put(byte value) {
        unsafe.putByte(currentOffset, value);
        currentOffset += Byte.BYTES; // 1
    }

    public void put(boolean value) {
        unsafe.putByte(currentOffset, (byte) (value ? 1 : 0));
        currentOffset += Byte.BYTES;
    }

    public void put(short value) {
        unsafe.putShort(currentOffset, value);
        currentOffset += Short.BYTES; // 2
    }

    public void put(char value) {
        unsafe.putChar(currentOffset, value);
        currentOffset += Character.BYTES; // 2
    }

    public void put(int value) {
        unsafe.putInt(currentOffset, value);
        currentOffset += Integer.BYTES; // 4
    }

    public void put(float value) {
        unsafe.putFloat(currentOffset, value);
        currentOffset += Float.BYTES; // 4
    }

    public void put(long value) {
        unsafe.putLong(currentOffset, value);
        currentOffset += Long.BYTES; // 8
    }

    public void put(double value) {
        unsafe.putDouble(currentOffset, value);
        currentOffset += Double.BYTES; // 8
    }

    public byte getByte() {
        byte value = unsafe.getByte(currentOffset);
        currentOffset += Byte.BYTES;
        return value;
    }

    public boolean getBoolean() {
        boolean value = unsafe.getByte(currentOffset) != 0;
        currentOffset += Byte.BYTES;
        return value;
    }

    public short getShort() {
        short value = unsafe.getShort(currentOffset);
        currentOffset += Short.BYTES;
        return value;
    }

    public char getChar() {
        char value = unsafe.getChar(currentOffset);
        currentOffset += Character.BYTES;
        return value;
    }

    public int getInt() {
        int value = unsafe.getInt(currentOffset);
        currentOffset += Integer.BYTES;
        return value;
    }

    public float getFloat() {
        float value = unsafe.getFloat(currentOffset);
        currentOffset += Float.BYTES;
        return value;
    }

    public long getLong() {
        long value = unsafe.getLong(currentOffset);
        currentOffset += Long.BYTES;
        return value;
    }

    public double getDouble() {
        double value = unsafe.getDouble(currentOffset);
        currentOffset += Double.BYTES;
        return value;
    }
}
