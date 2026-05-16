package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.sets.NativeObjectSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import sun.misc.Unsafe;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class ObjectSetFocusedJmhBenchmark {

    @Benchmark
    public void hashSetAddUnique(AddState state, Blackhole blackhole) {
        HashSet<FocusedPerson> set = new HashSet<>(state.expectedCapacity);
        for (FocusedPerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeObjectSetAddUnique(AddState state, Blackhole blackhole) {
        NativeObjectSet<FocusedPerson> set = new NativeObjectSet<>(state.expectedCapacity, state.memory, FocusedPerson::new);
        try {
            for (FocusedPerson value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void nativeObjectSetAddUniquePrehashed(AddState state, Blackhole blackhole) {
        NativeObjectSet<FocusedPerson> set = new NativeObjectSet<>(state.expectedCapacity, state.memory, FocusedPerson::new);
        try {
            for (int i = 0; i < state.values.length; i++) {
                blackhole.consume(set.addPrehashed(state.values[i], state.valueHashes[i]));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetContainsHitSameInstance(QueryState state, Blackhole blackhole) {
        for (FocusedPerson value : state.hitSameInstanceQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsHitSameInstance(QueryState state, Blackhole blackhole) {
        for (FocusedPerson value : state.hitSameInstanceQueries) {
            blackhole.consume(state.nativeObjectSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsHitSameInstancePrehashed(QueryState state, Blackhole blackhole) {
        for (int i = 0; i < state.hitSameInstanceQueries.length; i++) {
            blackhole.consume(state.nativeObjectSet.containsPrehashed(state.hitSameInstanceQueries[i], state.hitSameInstanceHashes[i]));
        }
    }

    @Benchmark
    public void hashSetContainsHitEqualCopy(QueryState state, Blackhole blackhole) {
        for (FocusedPerson value : state.hitEqualCopyQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsHitEqualCopy(QueryState state, Blackhole blackhole) {
        for (FocusedPerson value : state.hitEqualCopyQueries) {
            blackhole.consume(state.nativeObjectSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsHitEqualCopyPrehashed(QueryState state, Blackhole blackhole) {
        for (int i = 0; i < state.hitEqualCopyQueries.length; i++) {
            blackhole.consume(state.nativeObjectSet.containsPrehashed(state.hitEqualCopyQueries[i], state.hitEqualCopyHashes[i]));
        }
    }

    @Benchmark
    public void hashSetContainsMissNearMiss(QueryState state, Blackhole blackhole) {
        for (FocusedPerson value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsMissNearMiss(QueryState state, Blackhole blackhole) {
        for (FocusedPerson value : state.missQueries) {
            blackhole.consume(state.nativeObjectSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsMissNearMissPrehashed(QueryState state, Blackhole blackhole) {
        for (int i = 0; i < state.missQueries.length; i++) {
            blackhole.consume(state.nativeObjectSet.containsPrehashed(state.missQueries[i], state.missHashes[i]));
        }
    }

    @Benchmark
    public void hashSetAddDuplicate(DuplicateAddState state, Blackhole blackhole) {
        for (FocusedPerson value : state.duplicateQueries) {
            blackhole.consume(state.hashSet.add(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeObjectSetAddDuplicate(DuplicateAddState state, Blackhole blackhole) {
        for (FocusedPerson value : state.duplicateQueries) {
            blackhole.consume(state.nativeObjectSet.add(value));
        }
        blackhole.consume(state.nativeObjectSet.size());
    }

    @Benchmark
    public void nativeObjectSetAddDuplicatePrehashed(DuplicateAddState state, Blackhole blackhole) {
        for (int i = 0; i < state.duplicateQueries.length; i++) {
            blackhole.consume(state.nativeObjectSet.addPrehashed(state.duplicateQueries[i], state.duplicateHashes[i]));
        }
        blackhole.consume(state.nativeObjectSet.size());
    }

    @Benchmark
    public void hashSetRemoveEqualCopy(RemoveState state, Blackhole blackhole) {
        for (FocusedPerson value : state.removeQueries) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeObjectSetRemoveEqualCopy(RemoveState state, Blackhole blackhole) {
        for (FocusedPerson value : state.removeQueries) {
            blackhole.consume(state.nativeObjectSet.remove(value));
        }
        blackhole.consume(state.nativeObjectSet.size());
    }

    @Benchmark
    public void nativeObjectSetRemoveEqualCopyPrehashed(RemoveState state, Blackhole blackhole) {
        for (int i = 0; i < state.removeQueries.length; i++) {
            blackhole.consume(state.nativeObjectSet.removePrehashed(state.removeQueries[i], state.removeHashes[i]));
        }
        blackhole.consume(state.nativeObjectSet.size());
    }

    @Benchmark
    public void hashSetClearAndRefill(ClearRefillState state, Blackhole blackhole) {
        HashSet<FocusedPerson> set = state.hashSet;
        set.clear();
        for (FocusedPerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeObjectSetClearAndRefill(ClearRefillState state, Blackhole blackhole) {
        NativeObjectSet<FocusedPerson> set = state.nativeObjectSet;
        set.clear();
        for (FocusedPerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeObjectSetClearAndRefillPrehashed(ClearRefillState state, Blackhole blackhole) {
        NativeObjectSet<FocusedPerson> set = state.nativeObjectSet;
        set.clear();
        for (int i = 0; i < state.values.length; i++) {
            blackhole.consume(set.addPrehashed(state.values[i], state.valueHashes[i]));
        }
        blackhole.consume(set.size());
    }

    @State(Scope.Thread)
    public abstract static class ObjectStateBase {
        @Param({"1024", "65536"})
        public int size;

        @Param({"0", "12", "28"})
        public int nameLength;

        protected FocusedPerson[] values;
        protected long[] valueHashes;
        protected int expectedCapacity;
        protected final FocusedPersonMemory memory = new FocusedPersonMemory();

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createValues(size, nameLength, 44_117);
            valueHashes = computeHashes(values, memory);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class AddState extends ObjectStateBase {
    }

    @State(Scope.Thread)
    public static class QueryState extends ObjectStateBase {
        private FocusedPerson[] hitSameInstanceQueries;
        private FocusedPerson[] hitEqualCopyQueries;
        private FocusedPerson[] missQueries;
        private long[] hitSameInstanceHashes;
        private long[] hitEqualCopyHashes;
        private long[] missHashes;
        private NativeObjectSet<FocusedPerson> nativeObjectSet;
        private HashSet<FocusedPerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitSameInstanceQueries = shuffledCopy(values, 11_101L);
            hitEqualCopyQueries = copyValues(hitSameInstanceQueries);
            missQueries = createMisses(values);
            hitSameInstanceHashes = computeHashes(hitSameInstanceQueries, memory);
            hitEqualCopyHashes = computeHashes(hitEqualCopyQueries, memory);
            missHashes = computeHashes(missQueries, memory);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, FocusedPerson::new);
            hashSet = new HashSet<>(expectedCapacity);
            for (FocusedPerson value : values) {
                nativeObjectSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeObjectSet != null) {
                nativeObjectSet.freeMemory();
                nativeObjectSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class DuplicateAddState extends ObjectStateBase {
        private FocusedPerson[] duplicateQueries;
        private long[] duplicateHashes;
        private NativeObjectSet<FocusedPerson> nativeObjectSet;
        private HashSet<FocusedPerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            duplicateQueries = copyValues(shuffledCopy(values, 22_202L));
            duplicateHashes = computeHashes(duplicateQueries, memory);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, FocusedPerson::new);
            hashSet = new HashSet<>(expectedCapacity);
            for (FocusedPerson value : values) {
                nativeObjectSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeObjectSet != null) {
                nativeObjectSet.freeMemory();
                nativeObjectSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class RemoveState extends ObjectStateBase {
        private FocusedPerson[] removeQueries;
        private long[] removeHashes;
        private NativeObjectSet<FocusedPerson> nativeObjectSet;
        private HashSet<FocusedPerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removeQueries = copyValues(shuffledCopy(values, 33_303L));
            removeHashes = computeHashes(removeQueries, memory);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, FocusedPerson::new);
            hashSet = new HashSet<>(expectedCapacity);
            for (FocusedPerson value : values) {
                nativeObjectSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeObjectSet != null) {
                nativeObjectSet.freeMemory();
                nativeObjectSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class ClearRefillState extends ObjectStateBase {
        private NativeObjectSet<FocusedPerson> nativeObjectSet;
        private HashSet<FocusedPerson> hashSet;

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, FocusedPerson::new);
            hashSet = new HashSet<>(expectedCapacity);
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeObjectSet != null) {
                nativeObjectSet.freeMemory();
                nativeObjectSet = null;
            }
            hashSet = null;
        }
    }

    private static FocusedPerson[] createValues(int size, int nameLength, int seed) {
        FocusedPerson[] values = new FocusedPerson[size];
        for (int i = 0; i < size; i++) {
            int valueSeed = seed + i;
            values[i] = new FocusedPerson(createName(valueSeed, nameLength), valueSeed, -valueSeed);
        }
        return values;
    }

    private static FocusedPerson[] copyValues(FocusedPerson[] source) {
        FocusedPerson[] copy = new FocusedPerson[source.length];
        for (int i = 0; i < source.length; i++) {
            FocusedPerson value = source[i];
            copy[i] = new FocusedPerson(value.name, value.x, value.y);
        }
        return copy;
    }

    private static FocusedPerson[] createMisses(FocusedPerson[] values) {
        FocusedPerson[] misses = new FocusedPerson[values.length];
        for (int i = 0; i < values.length; i++) {
            FocusedPerson value = values[i];
            if (value.name.isEmpty()) {
                misses[i] = new FocusedPerson(value.name, value.x + 1, value.y);
            } else {
                misses[i] = new FocusedPerson(mutateName(value.name), value.x, value.y);
            }
        }
        return misses;
    }

    private static long[] computeHashes(FocusedPerson[] values, FocusedPersonMemory memory) {
        long[] hashes = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            hashes[i] = memory.hash(values[i]);
        }
        return hashes;
    }

    private static FocusedPerson[] shuffledCopy(FocusedPerson[] source, long seed) {
        FocusedPerson[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            FocusedPerson tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static String createName(int seed, int length) {
        if (length <= 0) {
            return "";
        }

        String base = "p" + Integer.toHexString(seed);
        if (base.length() >= length) {
            return base.substring(0, length);
        }

        StringBuilder builder = new StringBuilder(length);
        builder.append(base);
        while (builder.length() < length) {
            builder.append((char) ('a' + ((seed + builder.length()) % 26)));
        }
        return builder.toString();
    }

    private static String mutateName(String name) {
        char replacement = name.charAt(name.length() - 1) == 'z' ? 'y' : 'z';
        return name.substring(0, name.length() - 1) + replacement;
    }

    private static final class FocusedPerson {
        private String name;
        private int x;
        private int y;

        private FocusedPerson() {
        }

        private FocusedPerson(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + x;
            result = 31 * result + y;
            return result;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof FocusedPerson person)) {
                return false;
            }
            if (x != person.x || y != person.y) {
                return false;
            }
            if (name == null) {
                return person.name == null;
            }
            return name.equals(person.name);
        }
    }

    private static final class FocusedPersonMemory implements NativeTypeMemory<FocusedPerson> {
        private static final int MAX_NAME_LENGTH = 48;
        private static final NativeStructLayout LAYOUT;
        private static final long X_OFFSET;
        private static final long Y_OFFSET;
        private static final NativeStructLayout.StringField NAME_FIELD;

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            X_OFFSET = builder.intField();
            Y_OFFSET = builder.intField();
            NAME_FIELD = builder.intLengthPrefixedStringField(MAX_NAME_LENGTH);
            LAYOUT = builder.build();
        }

        @Override
        public long sizeof() {
            return LAYOUT.sizeof();
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, FocusedPerson outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
            outElement.name = NAME_FIELD.read(unsafe, offset);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, FocusedPerson element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            NAME_FIELD.write(unsafe, offset, element.name);
        }

        @Override
        public long hash(FocusedPerson element) {
            int result = element.name != null ? element.name.hashCode() : 0;
            result = 31 * result + element.x;
            result = 31 * result + element.y;
            return result;
        }

        @Override
        public boolean equals(FocusedPerson left, FocusedPerson right) {
            if (left.x != right.x || left.y != right.y) {
                return false;
            }
            if (left.name == null) {
                return right.name == null;
            }
            return left.name.equals(right.name);
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
        public boolean supportsEqualsMemory() {
            return true;
        }

        @Override
        public boolean equalsMemory(Unsafe unsafe, long offset, FocusedPerson value) {
            if (unsafe.getInt(offset + X_OFFSET) != value.x) {
                return false;
            }
            if (unsafe.getInt(offset + Y_OFFSET) != value.y) {
                return false;
            }
            return NAME_FIELD.equals(unsafe, offset, value.name);
        }
    }
}
