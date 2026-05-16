package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.sets.NativeHashSet;
import net.sixik.javastructg.structs.sets.NativeObjectSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
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
// A tiny benchmark suite with only the scenarios that are easiest to explain to newcomers.
public class SetStoryJmhBenchmark {

    // Safe mode: the set must keep real object equality semantics.
    @Benchmark
    public void javaHashSet_safeContainsMiss(SafeContainsMissState state, Blackhole blackhole) {
        for (StoryPerson query : state.missQueries) {
            blackhole.consume(state.hashSet.contains(query));
        }
    }

    @Benchmark
    public void nativeObjectSet_safeContainsMiss(SafeContainsMissState state, Blackhole blackhole) {
        for (StoryPerson query : state.missQueries) {
            blackhole.consume(state.nativeObjectSet.contains(query));
        }
    }

    @Benchmark
    public void nativeObjectSet_safeContainsMissPrehashed(SafeContainsMissState state, Blackhole blackhole) {
        for (int i = 0; i < state.missQueries.length; i++) {
            blackhole.consume(state.nativeObjectSet.containsPrehashed(state.missQueries[i], state.missHashes[i]));
        }
    }

    @Benchmark
    public void javaHashSet_safeRemoveEqualCopies(SafeRemoveState state, Blackhole blackhole) {
        for (StoryPerson query : state.removeQueries) {
            blackhole.consume(state.hashSet.remove(query));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeObjectSet_safeRemoveEqualCopies(SafeRemoveState state, Blackhole blackhole) {
        for (StoryPerson query : state.removeQueries) {
            blackhole.consume(state.nativeObjectSet.remove(query));
        }
        blackhole.consume(state.nativeObjectSet.size());
    }

    @Benchmark
    public void nativeObjectSet_safeRemoveEqualCopiesPrehashed(SafeRemoveState state, Blackhole blackhole) {
        for (int i = 0; i < state.removeQueries.length; i++) {
            blackhole.consume(state.nativeObjectSet.removePrehashed(state.removeQueries[i], state.removeHashes[i]));
        }
        blackhole.consume(state.nativeObjectSet.size());
    }

    // Fast mode: compare raw hash-based throughput when collision safety is not required.
    @Benchmark
    public void javaHashSet_fastContainsHitBaseline(FastContainsHitState state, Blackhole blackhole) {
        for (StoryPerson query : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(query));
        }
    }

    @Benchmark
    public void nativeHashSet_fastContainsHitPrehashed(FastContainsHitState state, Blackhole blackhole) {
        for (long hash : state.hitHashes) {
            blackhole.consume(state.nativeHashSet.containsHash(hash));
        }
    }

    @Benchmark
    public void javaHashSet_fastAddUniqueBaseline(FastAddState state, Blackhole blackhole) {
        HashSet<StoryPerson> set = new HashSet<>(state.expectedCapacity);
        for (StoryPerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeHashSet_fastAddUniquePrehashed(FastAddState state, Blackhole blackhole) {
        NativeHashSet<StoryPerson> set = new NativeHashSet<>(state.expectedCapacity, state.memory);
        try {
            for (long hash : state.valueHashes) {
                blackhole.consume(set.addHash(hash));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @State(Scope.Thread)
    public abstract static class StoryBaseState {
        protected static final int SIZE = 65_536;
        protected static final int NAME_LENGTH = 28;

        // Each benchmark call processes the full dataset for more stable numbers.
        protected StoryPerson[] values;
        protected long[] valueHashes;
        protected int expectedCapacity;
        protected final StoryPersonMemory memory = new StoryPersonMemory();

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createValues(SIZE, NAME_LENGTH, 91_337);
            valueHashes = computeHashes(values, memory);
            expectedCapacity = SIZE << 1;
        }
    }

    @State(Scope.Thread)
    public static class SafeContainsMissState extends StoryBaseState {
        private StoryPerson[] missQueries;
        private long[] missHashes;
        private NativeObjectSet<StoryPerson> nativeObjectSet;
        private HashSet<StoryPerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            missQueries = createMisses(values);
            missHashes = computeHashes(missQueries, memory);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, StoryPerson::new);
            hashSet = new HashSet<>(expectedCapacity);
            for (StoryPerson value : values) {
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
    public static class SafeRemoveState extends StoryBaseState {
        private StoryPerson[] removeQueries;
        private long[] removeHashes;
        private NativeObjectSet<StoryPerson> nativeObjectSet;
        private HashSet<StoryPerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removeQueries = copyValues(shuffledCopy(values, 12_345L));
            removeHashes = computeHashes(removeQueries, memory);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, StoryPerson::new);
            hashSet = new HashSet<>(expectedCapacity);
            for (StoryPerson value : values) {
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
    public static class FastContainsHitState extends StoryBaseState {
        private StoryPerson[] hitQueries;
        private long[] hitHashes;
        private NativeHashSet<StoryPerson> nativeHashSet;
        private HashSet<StoryPerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 77_777L);
            hitHashes = computeHashes(hitQueries, memory);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeHashSet = new NativeHashSet<>(expectedCapacity, memory);
            hashSet = new HashSet<>(expectedCapacity);
            for (StoryPerson value : values) {
                nativeHashSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeHashSet != null) {
                nativeHashSet.freeMemory();
                nativeHashSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class FastAddState extends StoryBaseState {
    }

    private static StoryPerson[] createValues(int size, int nameLength, int seed) {
        StoryPerson[] values = new StoryPerson[size];
        for (int i = 0; i < size; i++) {
            int valueSeed = seed + i;
            values[i] = new StoryPerson(createName(valueSeed, nameLength), valueSeed, -valueSeed);
        }
        return values;
    }

    private static StoryPerson[] copyValues(StoryPerson[] source) {
        StoryPerson[] copy = new StoryPerson[source.length];
        for (int i = 0; i < source.length; i++) {
            StoryPerson value = source[i];
            copy[i] = new StoryPerson(value.name, value.x, value.y);
        }
        return copy;
    }

    private static StoryPerson[] createMisses(StoryPerson[] values) {
        StoryPerson[] misses = new StoryPerson[values.length];
        for (int i = 0; i < values.length; i++) {
            StoryPerson value = values[i];
            misses[i] = new StoryPerson(mutateName(value.name), value.x, value.y);
        }
        return misses;
    }

    private static long[] computeHashes(StoryPerson[] values, StoryPersonMemory memory) {
        long[] hashes = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            hashes[i] = memory.hash(values[i]);
        }
        return hashes;
    }

    private static StoryPerson[] shuffledCopy(StoryPerson[] source, long seed) {
        StoryPerson[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            StoryPerson tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static String createName(int seed, int length) {
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

    private static final class StoryPerson {
        private String name;
        private int x;
        private int y;

        private StoryPerson() {
        }

        private StoryPerson(String name, int x, int y) {
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
            if (!(other instanceof StoryPerson person)) {
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

    private static final class StoryPersonMemory implements NativeTypeMemory<StoryPerson> {
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
        public void readFromMemory(Unsafe unsafe, long offset, StoryPerson outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
            outElement.name = NAME_FIELD.read(unsafe, offset);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, StoryPerson element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            NAME_FIELD.write(unsafe, offset, element.name);
        }

        @Override
        public long hash(StoryPerson element) {
            int result = element.name != null ? element.name.hashCode() : 0;
            result = 31 * result + element.x;
            result = 31 * result + element.y;
            return result;
        }

        @Override
        public boolean equals(StoryPerson left, StoryPerson right) {
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
        public boolean equalsMemory(Unsafe unsafe, long offset, StoryPerson value) {
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
