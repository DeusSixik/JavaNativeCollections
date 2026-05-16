import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NativeStructLayoutTest {

    @Test
    public void testShortFloatFloatLayoutProducesAlignedOffsets() {
        NativeStructLayout.Builder builder = NativeStructLayout.builder();
        long idOffset = builder.shortField();
        long tempOffset = builder.floatField();
        long humidityOffset = builder.floatField();
        NativeStructLayout layout = builder.build();

        assertEquals(0L, idOffset);
        assertEquals(4L, tempOffset);
        assertEquals(8L, humidityOffset);
        assertEquals(12L, layout.sizeof());
        assertEquals(NativeTypes.FLOAT, layout.alignment());
    }

    @Test
    public void testManualPaddingAndWideFields() {
        NativeStructLayout.Builder builder = NativeStructLayout.builder();
        long flagOffset = builder.byteField();
        builder.padTo(NativeTypes.LONG);
        long valueOffset = builder.longField();
        long tailOffset = builder.charField();
        NativeStructLayout layout = builder.build();

        assertEquals(0L, flagOffset);
        assertEquals(8L, valueOffset);
        assertEquals(16L, tailOffset);
        assertEquals(24L, layout.sizeof());
        assertEquals(NativeTypes.LONG, layout.alignment());
    }

    @Test
    public void testIntLengthPrefixedStringFieldLayout() {
        NativeStructLayout.Builder builder = NativeStructLayout.builder();
        long xOffset = builder.intField();
        NativeStructLayout.StringField nameField = builder.intLengthPrefixedStringField(36);
        NativeStructLayout layout = builder.build();

        assertEquals(0L, xOffset);
        assertEquals(4L, nameField.lengthOffset());
        assertEquals(8L, nameField.dataOffset());
        assertEquals(36, nameField.maxChars());
        assertEquals(NativeTypes.INT, nameField.lengthBytes());
        assertEquals(80L, layout.sizeof());
    }

    @Test
    public void testNestedStructFieldKeepsNestedAlignmentAndSize() {
        NativeStructLayout.Builder nestedBuilder = NativeStructLayout.builder();
        long nestedFlagOffset = nestedBuilder.byteField();
        long nestedValueOffset = nestedBuilder.floatField();
        NativeStructLayout nestedLayout = nestedBuilder.build();

        NativeStructLayout.Builder parentBuilder = NativeStructLayout.builder();
        long idOffset = parentBuilder.intField();
        NativeStructLayout.StructField nestedField = parentBuilder.structField(nestedLayout);
        long tailOffset = parentBuilder.shortField();
        NativeStructLayout parentLayout = parentBuilder.build();

        assertEquals(0L, nestedFlagOffset);
        assertEquals(4L, nestedValueOffset);
        assertEquals(8L, nestedLayout.sizeof());

        assertEquals(0L, idOffset);
        assertEquals(4L, nestedField.offset());
        assertEquals(nestedLayout.sizeof(), nestedField.sizeof());
        assertEquals(nestedLayout.alignment(), nestedField.alignment());
        assertEquals(12L, tailOffset);
        assertEquals(16L, parentLayout.sizeof());
        assertEquals(NativeTypes.FLOAT, parentLayout.alignment());
    }
}
