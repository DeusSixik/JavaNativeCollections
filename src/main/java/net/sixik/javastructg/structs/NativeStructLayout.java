package net.sixik.javastructg.structs;

import net.sixik.javastructg.utils.NativeUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteOrder;

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

        public StructField structField(NativeStructLayout layout) {
            long fieldOffset = field(layout.sizeof(), layout.alignment());
            return new StructField(fieldOffset, layout);
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

        private long field(long sizeBytes, int alignmentBytes) {
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

    public static final class StructField {

        private final long offset;
        private final NativeStructLayout layout;

        private StructField(long offset, NativeStructLayout layout) {
            this.offset = offset;
            this.layout = layout;
        }

        public long offset() {
            return offset;
        }

        public NativeStructLayout layout() {
            return layout;
        }

        public long sizeof() {
            return layout.sizeof();
        }

        public int alignment() {
            return layout.alignment();
        }

        public long address(long structAddress) {
            return structAddress + offset;
        }
    }

    public static final class StringField {

        private static final Unsafe STRING_UNSAFE = NativeUtils.getUnsafe();
        private static final long STRING_VALUE_OFFSET;
        private static final long STRING_CODER_OFFSET;
        private static final boolean STRING_VALUE_IS_BYTES;
        private static final long BYTE_ARRAY_BASE_OFFSET = STRING_UNSAFE.arrayBaseOffset(byte[].class);
        private static final long CHAR_ARRAY_BASE_OFFSET = STRING_UNSAFE.arrayBaseOffset(char[].class);
        private static final boolean DIRECT_UTF16_LAYOUT = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

        static {
            try {
                Field valueField = String.class.getDeclaredField("value");
                STRING_VALUE_OFFSET = STRING_UNSAFE.objectFieldOffset(valueField);
                STRING_VALUE_IS_BYTES = valueField.getType() == byte[].class;

                if (STRING_VALUE_IS_BYTES) {
                    Field coderField = String.class.getDeclaredField("coder");
                    STRING_CODER_OFFSET = STRING_UNSAFE.objectFieldOffset(coderField);
                } else {
                    STRING_CODER_OFFSET = -1L;
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to access String internals", e);
            }
        }

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
            if (length == 0) {
                return;
            }

            long dataAddress = structAddress + dataOffset;
            if (STRING_VALUE_IS_BYTES) {
                byte[] bytes = (byte[]) STRING_UNSAFE.getObject(value, STRING_VALUE_OFFSET);
                if (isLatin1(value)) {
                    writeLatin1(unsafe, dataAddress, bytes, length);
                    return;
                }
                if (DIRECT_UTF16_LAYOUT) {
                    unsafe.copyMemory(bytes, BYTE_ARRAY_BASE_OFFSET, null, dataAddress, length * 2L);
                    return;
                }
                writeUtf16Bytes(unsafe, dataAddress, bytes, length);
                return;
            }

            char[] chars = (char[]) STRING_UNSAFE.getObject(value, STRING_VALUE_OFFSET);
            unsafe.copyMemory(chars, CHAR_ARRAY_BASE_OFFSET, null, dataAddress, length * 2L);
        }

        private static void writeLatin1(Unsafe unsafe, long dataAddress, byte[] bytes, int length) {
            int i = 0;
            while (i + 4 <= length) {
                unsafe.putLong(dataAddress + (((long) i) << 1), packLatin1Quad(bytes, i));
                i += 4;
            }
            if (i + 2 <= length) {
                unsafe.putInt(dataAddress + (((long) i) << 1), packLatin1Pair(bytes, i));
                i += 2;
            }
            if (i < length) {
                unsafe.putChar(dataAddress + (((long) i) << 1), (char) Byte.toUnsignedInt(bytes[i]));
            }
        }

        private static void writeUtf16Bytes(Unsafe unsafe, long dataAddress, byte[] bytes, int length) {
            for (int i = 0; i < length; i++) {
                unsafe.putChar(dataAddress + (((long) i) << 1), utf16CharAt(bytes, i));
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
            if (length == 0) {
                return true;
            }

            if (STRING_VALUE_IS_BYTES) {
                byte[] bytes = (byte[]) STRING_UNSAFE.getObject(value, STRING_VALUE_OFFSET);
                if (isLatin1(value)) {
                    return equalsLatin1(unsafe, dataAddress, bytes, length);
                }
                if (DIRECT_UTF16_LAYOUT) {
                    return equalsUtf16Direct(unsafe, dataAddress, bytes, length);
                }
                return equalsUtf16Bytes(unsafe, dataAddress, bytes, length);
            }

            char[] chars = (char[]) STRING_UNSAFE.getObject(value, STRING_VALUE_OFFSET);
            for (int i = 0; i < length; i++) {
                if (unsafe.getChar(dataAddress + (((long) i) << 1)) != chars[i]) {
                    return false;
                }
            }

            return true;
        }

        public int hashCode(Unsafe unsafe, long structAddress) {
            int length = getLength(unsafe, structAddress + lengthOffset);
            long dataAddress = structAddress + dataOffset;
            int hash = 0;

            int i = 0;
            while (i + 4 <= length) {
                long packed = unsafe.getLong(dataAddress + (((long) i) << 1));
                hash = 31 * hash + (char) (packed & 0xFFFFL);
                hash = 31 * hash + (char) ((packed >>> 16) & 0xFFFFL);
                hash = 31 * hash + (char) ((packed >>> 32) & 0xFFFFL);
                hash = 31 * hash + (char) ((packed >>> 48) & 0xFFFFL);
                i += 4;
            }
            while (i < length) {
                hash = 31 * hash + unsafe.getChar(dataAddress + (((long) i) << 1));
                i++;
            }

            return hash;
        }

        private static boolean equalsLatin1(Unsafe unsafe, long dataAddress, byte[] bytes, int length) {
            int i = 0;
            while (i + 4 <= length) {
                if (unsafe.getLong(dataAddress + (((long) i) << 1)) != packLatin1Quad(bytes, i)) {
                    return false;
                }
                i += 4;
            }
            if (i + 2 <= length) {
                if (unsafe.getInt(dataAddress + (((long) i) << 1)) != packLatin1Pair(bytes, i)) {
                    return false;
                }
                i += 2;
            }
            if (i < length) {
                return unsafe.getChar(dataAddress + (((long) i) << 1)) == (char) Byte.toUnsignedInt(bytes[i]);
            }
            return true;
        }

        private static boolean equalsUtf16Direct(Unsafe unsafe, long dataAddress, byte[] bytes, int length) {
            long byteOffset = BYTE_ARRAY_BASE_OFFSET;
            long endOffset = byteOffset + (length * 2L);

            while (byteOffset + Long.BYTES <= endOffset) {
                if (unsafe.getLong(dataAddress) != STRING_UNSAFE.getLong(bytes, byteOffset)) {
                    return false;
                }
                dataAddress += Long.BYTES;
                byteOffset += Long.BYTES;
            }
            if (byteOffset + Integer.BYTES <= endOffset) {
                if (unsafe.getInt(dataAddress) != STRING_UNSAFE.getInt(bytes, byteOffset)) {
                    return false;
                }
                dataAddress += Integer.BYTES;
                byteOffset += Integer.BYTES;
            }
            if (byteOffset < endOffset) {
                return unsafe.getShort(dataAddress) == STRING_UNSAFE.getShort(bytes, byteOffset);
            }
            return true;
        }

        private static boolean equalsUtf16Bytes(Unsafe unsafe, long dataAddress, byte[] bytes, int length) {
            for (int i = 0; i < length; i++) {
                if (unsafe.getChar(dataAddress + (((long) i) << 1)) != utf16CharAt(bytes, i)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isLatin1(String value) {
            return STRING_UNSAFE.getByte(value, STRING_CODER_OFFSET) == 0;
        }

        private static long packLatin1Quad(byte[] bytes, int index) {
            return Byte.toUnsignedLong(bytes[index])
                    | (Byte.toUnsignedLong(bytes[index + 1]) << 16)
                    | (Byte.toUnsignedLong(bytes[index + 2]) << 32)
                    | (Byte.toUnsignedLong(bytes[index + 3]) << 48);
        }

        private static int packLatin1Pair(byte[] bytes, int index) {
            return Byte.toUnsignedInt(bytes[index])
                    | (Byte.toUnsignedInt(bytes[index + 1]) << 16);
        }

        private static char utf16CharAt(byte[] bytes, int index) {
            int byteIndex = index << 1;
            return (char) (Byte.toUnsignedInt(bytes[byteIndex])
                    | (Byte.toUnsignedInt(bytes[byteIndex + 1]) << 8));
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
