import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.sets.NativeBooleanSet;
import net.sixik.javastructg.structs.sets.NativeByteSet;
import net.sixik.javastructg.structs.sets.NativeCharSet;
import net.sixik.javastructg.structs.sets.NativeDoubleSet;
import net.sixik.javastructg.structs.sets.NativeFloatSet;
import net.sixik.javastructg.structs.sets.NativeHashSet;
import net.sixik.javastructg.structs.sets.NativeObjectSet;
import net.sixik.javastructg.structs.sets.NativeShortSet;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NativeAdditionalSetsTest {

    @Test
    public void testByteSetAddContainsRemove() {
        NativeByteSet set = new NativeByteSet(8);
        try {
            assertTrue(set.add((byte) 10));
            assertTrue(set.add((byte) -7));
            assertFalse(set.add((byte) 10));
            assertTrue(set.contains((byte) 10));
            assertTrue(set.contains((byte) -7));
            assertTrue(set.remove((byte) 10));
            assertFalse(set.contains((byte) 10));
            assertFalse(set.remove((byte) 10));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testShortSetReusesDeletedSlot() {
        NativeShortSet set = new NativeShortSet(8);
        try {
            assertTrue(set.add((short) 1));
            assertTrue(set.add((short) 9));
            assertTrue(set.remove((short) 1));
            assertTrue(set.add((short) 17));
            assertTrue(set.contains((short) 17));
            assertTrue(set.contains((short) 9));
            assertEquals(2, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testCharSetClearResetsContent() {
        NativeCharSet set = new NativeCharSet(8);
        try {
            assertTrue(set.add('a'));
            assertTrue(set.add('b'));
            assertTrue(set.add('c'));

            set.clear();

            assertTrue(set.isEmpty());
            assertFalse(set.contains('a'));
            assertFalse(set.contains('b'));
            assertTrue(set.add('z'));
            assertTrue(set.contains('z'));
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testBooleanSetStoresBothValues() {
        NativeBooleanSet set = new NativeBooleanSet(4);
        try {
            assertTrue(set.add(true));
            assertTrue(set.add(false));
            assertFalse(set.add(true));
            assertFalse(set.add(false));
            assertTrue(set.contains(true));
            assertTrue(set.contains(false));
            assertTrue(set.remove(true));
            assertFalse(set.contains(true));
            assertTrue(set.contains(false));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testFloatSetUsesBitwiseEquality() {
        NativeFloatSet set = new NativeFloatSet(8);
        try {
            float canonicalNan = Float.intBitsToFloat(0x7fc00000);

            assertTrue(set.add(canonicalNan));
            assertFalse(set.add(Float.NaN));
            assertTrue(set.add(0.0f));
            assertTrue(set.add(-0.0f));

            assertTrue(set.contains(Float.NaN));
            assertTrue(set.contains(0.0f));
            assertTrue(set.contains(-0.0f));
            assertEquals(3, set.size());

            assertTrue(set.remove(0.0f));
            assertFalse(set.contains(0.0f));
            assertTrue(set.contains(-0.0f));
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testDoubleSetResizeKeepsValues() {
        NativeDoubleSet set = new NativeDoubleSet(4);
        Set<Long> expectedBits = new HashSet<>();
        try {
            for (int i = -200; i <= 200; i++) {
                double value = i * 0.5d;
                assertTrue(set.add(value));
                expectedBits.add(Double.doubleToLongBits(value));
            }

            assertTrue(set.add(Double.NaN));
            assertTrue(set.add(-0.0d));
            expectedBits.add(Double.doubleToLongBits(Double.NaN));
            expectedBits.add(Double.doubleToLongBits(-0.0d));

            assertTrue(set.capacity() >= 512);
            assertEquals(expectedBits.size(), set.size());

            for (long bits : expectedBits) {
                assertTrue(set.contains(Double.longBitsToDouble(bits)));
            }
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testObjectSetAddContainsRemoveAndResize() {
        PointMemory memory = new PointMemory();
        NativeObjectSet<Point> set = new NativeObjectSet<>(4, memory, Point::new);
        try {
            List<Point> expected = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                Point value = new Point("p" + i, i, -i);
                assertTrue(set.add(value));
                expected.add(value);
            }

            Point duplicate = new Point("p10", 10, -10);
            assertFalse(set.add(duplicate));
            assertTrue(set.contains(duplicate));
            assertTrue(set.remove(duplicate));
            assertFalse(set.contains(duplicate));
            assertFalse(set.remove(duplicate));
            assertEquals(expected.size() - 1, set.size());

            for (Point point : expected) {
                if (!(point.x == duplicate.x
                        && point.y == duplicate.y
                        && point.name.equals(duplicate.name))) {
                    assertTrue(set.contains(point));
                }
            }
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testObjectSetRehashCanAvoidReadBackWhenHashMemoryIsAvailable() {
        CountingPointMemory memory = new CountingPointMemory();
        NativeObjectSet<Point> set = new NativeObjectSet<>(4, memory, Point::new);
        try {
            for (int i = 0; i < 200; i++) {
                assertTrue(set.add(new Point("rehash-" + i, i, i + 1)));
            }

            assertEquals(200, set.size());
            assertEquals(0, memory.readCount);
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testObjectSetSupportsPrehashedOperationsWithoutCallingHash() {
        PointMemory hashMemory = new PointMemory();
        ThrowingHashPointMemory setMemory = new ThrowingHashPointMemory();
        NativeObjectSet<Point> set = new NativeObjectSet<>(8, setMemory, Point::new);
        try {
            Point left = new Point("left", 1, 11);
            Point right = new Point("right", 2, 22);
            Point missing = new Point("missing", 3, 33);

            long leftHash = hashMemory.hash(left);
            long rightHash = hashMemory.hash(right);
            long missingHash = hashMemory.hash(missing);

            assertTrue(set.addPrehashed(left, leftHash));
            assertTrue(set.addPrehashed(right, rightHash));
            assertFalse(set.addPrehashed(new Point("left", 1, 11), leftHash));

            assertTrue(set.containsPrehashed(new Point("left", 1, 11), leftHash));
            assertFalse(set.containsPrehashed(missing, missingHash));

            assertTrue(set.removePrehashed(new Point("left", 1, 11), leftHash));
            assertFalse(set.containsPrehashed(new Point("left", 1, 11), leftHash));
            assertFalse(set.removePrehashed(new Point("left", 1, 11), leftHash));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testObjectSetSupportsUtf16StringValues() {
        PointMemory memory = new PointMemory();
        NativeObjectSet<Point> set = new NativeObjectSet<>(8, memory, Point::new);
        try {
            Point left = new Point("\u0100-left", 1, 11);
            Point right = new Point("\u0416-right", 2, 22);

            assertTrue(set.add(left));
            assertTrue(set.add(right));
            assertTrue(set.contains(new Point("\u0100-left", 1, 11)));
            assertTrue(set.contains(new Point("\u0416-right", 2, 22)));
            assertTrue(set.remove(new Point("\u0100-left", 1, 11)));
            assertFalse(set.contains(new Point("\u0100-left", 1, 11)));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testObjectSetUnsafeHashEqualityWorksForCollisionFreeHashes() {
        PointMemory memory = new PointMemory();
        NativeHashSet<Point> set = new NativeHashSet<>(8, memory);
        try {
            Point left = new Point("left", 1, 11);
            Point right = new Point("right", 2, 22);

            assertTrue(set.add(left));
            assertTrue(set.add(right));
            assertTrue(set.contains(new Point("left", 1, 11)));
            assertTrue(set.contains(new Point("right", 2, 22)));
            assertTrue(set.remove(new Point("left", 1, 11)));
            assertFalse(set.contains(new Point("left", 1, 11)));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testHashSetSupportsPrehashedOperations() {
        PointMemory memory = new PointMemory();
        NativeHashSet<Point> set = new NativeHashSet<>(8, memory);
        try {
            Point left = new Point("left", 1, 11);
            Point right = new Point("right", 2, 22);

            long leftHash = memory.hash(left);
            long rightHash = memory.hash(right);

            assertTrue(set.addHash(leftHash));
            assertTrue(set.addHash(rightHash));
            assertFalse(set.addHash(leftHash));
            assertTrue(set.containsHash(leftHash));
            assertTrue(set.removeHash(leftHash));
            assertFalse(set.containsHash(leftHash));
            assertEquals(1, set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Test
    public void testObjectSetUnsafeHashEqualityTreatsHashCollisionAsDuplicate() {
        CollidingPointMemory memory = new CollidingPointMemory();
        NativeObjectSet<Point> safeSet = new NativeObjectSet<>(8, memory, Point::new);
        NativeHashSet<Point> unsafeSet = new NativeHashSet<>(8, memory);
        try {
            Point left = new Point("left", 1, 11);
            Point right = new Point("right", 2, 22);

            assertTrue(safeSet.add(left));
            assertTrue(safeSet.add(right));
            assertEquals(2, safeSet.size());

            assertTrue(unsafeSet.add(left));
            assertFalse(unsafeSet.add(right));
            assertEquals(1, unsafeSet.size());
        } finally {
            safeSet.freeMemory();
            unsafeSet.freeMemory();
        }
    }

    private static final class Point {
        private String name;
        private int x;
        private int y;

        private Point() {
        }

        private Point(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            throw new AssertionError("NativeObjectSet must not call Point.hashCode()");
        }

        @Override
        public boolean equals(Object other) {
            throw new AssertionError("NativeObjectSet must not call Point.equals()");
        }
    }

    private static class PointMemory implements NativeTypeMemory<Point> {
        private static final NativeStructLayout LAYOUT;
        private static final long X_OFFSET;
        private static final long Y_OFFSET;
        private static final NativeStructLayout.StringField NAME_FIELD;

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            X_OFFSET = builder.intField();
            Y_OFFSET = builder.intField();
            NAME_FIELD = builder.intLengthPrefixedStringField(32);
            LAYOUT = builder.build();
        }

        @Override
        public long sizeof() {
            return LAYOUT.sizeof();
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Point outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
            outElement.name = NAME_FIELD.read(unsafe, offset);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Point element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            NAME_FIELD.write(unsafe, offset, element.name);
        }

        @Override
        public long hash(Point element) {
            int result = element.name != null ? element.name.hashCode() : 0;
            result = 31 * result + element.x;
            result = 31 * result + element.y;
            return result;
        }

        @Override
        public boolean equals(Point left, Point right) {
            return left.x == right.x
                    && left.y == right.y
                    && java.util.Objects.equals(left.name, right.name);
        }

        @Override
        public boolean supportsEqualsMemory() {
            return true;
        }

        @Override
        public boolean supportsHashMemory() {
            return true;
        }

        @Override
        public long hashMemory(Unsafe unsafe, long offset) {
            int result = NAME_FIELD.hashCode(unsafe, offset);
            result = 31 * result + unsafe.getInt(offset + X_OFFSET);
            result = 31 * result + unsafe.getInt(offset + Y_OFFSET);
            return result;
        }

        @Override
        public boolean equalsMemory(Unsafe unsafe, long offset, Point value) {
            if (unsafe.getInt(offset + X_OFFSET) != value.x) {
                return false;
            }
            if (unsafe.getInt(offset + Y_OFFSET) != value.y) {
                return false;
            }
            return NAME_FIELD.equals(unsafe, offset, value.name);
        }
    }

    private static final class CountingPointMemory extends PointMemory {
        private int readCount;

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Point outElement) {
            readCount++;
            super.readFromMemory(unsafe, offset, outElement);
        }
    }

    private static final class ThrowingHashPointMemory extends PointMemory {
        @Override
        public long hash(Point element) {
            throw new AssertionError("Prehashed NativeObjectSet path must not call hash()");
        }
    }

    private static final class CollidingPointMemory extends PointMemory {
        @Override
        public long hash(Point element) {
            return 1L;
        }

        @Override
        public long hashMemory(Unsafe unsafe, long offset) {
            return 1L;
        }
    }
}
