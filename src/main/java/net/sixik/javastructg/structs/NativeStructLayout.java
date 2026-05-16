package net.sixik.javastructg.structs;

import sun.misc.Unsafe;

public final class NativeStructLayout {

    private final long size;
    private final int alignment;

    private NativeStructLayout(long size, int alignment) {
        this.size = size;
        this.alignment = alignment;
    }

    public long sizeof() {
        return size;
    }

    public int alignment() {
        return alignment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private long offset;
        private int maxAlignment = 1;

        public long byteField() {
            return field(NativeTypes.BYTE, NativeTypes.BYTE);
        }

        public long booleanField() {
            return field(NativeTypes.BOOLEAN, NativeTypes.BOOLEAN);
        }

        public long shortField() {
            return field(NativeTypes.SHORT, NativeTypes.SHORT);
        }

        public long charField() {
            return field(NativeTypes.CHAR, NativeTypes.CHAR);
        }

        public long intField() {
            return field(NativeTypes.INT, NativeTypes.INT);
        }

        public long floatField() {
            return field(NativeTypes.FLOAT, NativeTypes.FLOAT);
        }

        public long longField() {
            return field(NativeTypes.LONG, NativeTypes.LONG);
        }

        public long doubleField() {
            return field(NativeTypes.DOUBLE, NativeTypes.DOUBLE);
        }

        public long bytesField(int sizeBytes) {
            return field(sizeBytes, 1);
        }

        public long bytesField(int sizeBytes, int alignmentBytes) {
            return field(sizeBytes, alignmentBytes);
        }

        public StringField intLengthPrefixedStringField(int maxChars) {
            return stringField(maxChars, NativeTypes.INT);
        }

        public StringField shortLengthPrefixedStringField(int maxChars) {
            return stringField(maxChars, NativeTypes.SHORT);
        }

        public Builder padTo(int alignmentBytes) {
            offset = alignUp(offset, alignmentBytes);
            maxAlignment = Math.max(maxAlignment, alignmentBytes);
            return this;
        }

        public NativeStructLayout build() {
            long finalSize = alignUp(offset, maxAlignment);
            return new NativeStructLayout(finalSize, maxAlignment);
        }

        private long field(int sizeBytes, int alignmentBytes) {
            offset = alignUp(offset, alignmentBytes);
            long fieldOffset = offset;
            offset += sizeBytes;
            maxAlignment = Math.max(maxAlignment, alignmentBytes);
            return fieldOffset;
        }

        private StringField stringField(int maxChars, int lengthBytes) {
            long lengthOffset = field(lengthBytes, lengthBytes);
            long dataOffset = field(maxChars * NativeTypes.CHAR, NativeTypes.CHAR);
            return new StringField(lengthOffset, dataOffset, maxChars, lengthBytes);
        }

        private static long alignUp(long value, int alignmentBytes) {
            long mask = alignmentBytes - 1L;
            return (value + mask) & ~mask;
        }
    }

    public static final class StringField {

        private final long lengthOffset;
        private final long dataOffset;
        private final int maxChars;
        private final int lengthBytes;

        private StringField(long lengthOffset, long dataOffset, int maxChars, int lengthBytes) {
            this.lengthOffset = lengthOffset;
            this.dataOffset = dataOffset;
            this.maxChars = maxChars;
            this.lengthBytes = lengthBytes;
        }

        public long lengthOffset() {
            return lengthOffset;
        }

        public long dataOffset() {
            return dataOffset;
        }

        public int maxChars() {
            return maxChars;
        }

        public int lengthBytes() {
            return lengthBytes;
        }

        public void write(Unsafe unsafe, long structAddress, String value) {
            int length = value == null ? 0 : Math.min(value.length(), maxChars);
            putLength(unsafe, structAddress + lengthOffset, length);
            long dataAddress = structAddress + dataOffset;
            for (int i = 0; i < length; i++) {
                unsafe.putChar(dataAddress + (i * 2L), value.charAt(i));
            }
        }

        public String read(Unsafe unsafe, long structAddress) {
            int length = getLength(unsafe, structAddress + lengthOffset);
            if (length <= 0) {
                return "";
            }

            char[] chars = new char[length];
            unsafe.copyMemory(null, structAddress + dataOffset, chars, Unsafe.ARRAY_CHAR_BASE_OFFSET, length * 2L);
            return new String(chars);
        }

        public boolean equals(Unsafe unsafe, long structAddress, String value) {
            int length = getLength(unsafe, structAddress + lengthOffset);
            if (value == null) {
                return false;
            }
            if (length != value.length()) {
                return false;
            }

            long dataAddress = structAddress + dataOffset;
            for (int i = 0; i < length; i++) {
                if (unsafe.getChar(dataAddress + (i * 2L)) != value.charAt(i)) {
                    return false;
                }
            }

            return true;
        }

        public int hashCode(Unsafe unsafe, long structAddress) {
            int length = getLength(unsafe, structAddress + lengthOffset);
            long dataAddress = structAddress + dataOffset;
            int hash = 0;

            for (int i = 0; i < length; i++) {
                hash = 31 * hash + unsafe.getChar(dataAddress + (i * 2L));
            }

            return hash;
        }

        private void putLength(Unsafe unsafe, long address, int value) {
            switch (lengthBytes) {
                case 1 -> unsafe.putByte(address, (byte) value);
                case 2 -> unsafe.putShort(address, (short) value);
                case 4 -> unsafe.putInt(address, value);
                default -> throw new IllegalStateException("Unsupported string length field size: " + lengthBytes);
            }
        }

        private int getLength(Unsafe unsafe, long address) {
            return switch (lengthBytes) {
                case 1 -> Byte.toUnsignedInt(unsafe.getByte(address));
                case 2 -> Short.toUnsignedInt(unsafe.getShort(address));
                case 4 -> unsafe.getInt(address);
                default -> throw new IllegalStateException("Unsupported string length field size: " + lengthBytes);
            };
        }
    }
}
