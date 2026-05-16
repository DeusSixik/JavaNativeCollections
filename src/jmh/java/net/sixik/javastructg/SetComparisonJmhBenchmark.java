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
public class SetComparisonJmhBenchmark {

    @Benchmark
    public void hashSetAddUnique(AddState state, Blackhole blackhole) {
        HashSet<SuitePerson> set = new HashSet<>(state.expectedCapacity);
        for (SuitePerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeObjectSetAddUnique(AddState state, Blackhole blackhole) {
        NativeObjectSet<SuitePerson> set = new NativeObjectSet<>(state.expectedCapacity, state.memory, SuitePerson::new);
        try {
            for (SuitePerson value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void nativeHashSetAddUnique(AddState state, Blackhole blackhole) {
        NativeHashSet<SuitePerson> set = new NativeHashSet<>(state.expectedCapacity, state.memory);
        try {
            for (SuitePerson value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void nativeHashSetAddUniquePrehashed(AddState state, Blackhole blackhole) {
        NativeHashSet<SuitePerson> set = new NativeHashSet<>(state.expectedCapacity, state.memory);
        try {
            for (long hash : state.valueHashes) {
                blackhole.consume(set.addHash(hash));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetAddDuplicate(DuplicateAddState state, Blackhole blackhole) {
        for (SuitePerson value : state.duplicateOrder) {
            blackhole.consume(state.hashSet.add(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeObjectSetAddDuplicate(DuplicateAddState state, Blackhole blackhole) {
        for (SuitePerson value : state.duplicateOrder) {
            blackhole.consume(state.nativeObjectSet.add(value));
        }
        blackhole.consume(state.nativeObjectSet.size());
    }

    @Benchmark
    public void nativeHashSetAddDuplicate(DuplicateAddState state, Blackhole blackhole) {
        for (SuitePerson value : state.duplicateOrder) {
            blackhole.consume(state.nativeHashSet.add(value));
        }
        blackhole.consume(state.nativeHashSet.size());
    }

    @Benchmark
    public void nativeHashSetAddDuplicatePrehashed(DuplicateAddState state, Blackhole blackhole) {
        for (long hash : state.duplicateHashes) {
            blackhole.consume(state.nativeHashSet.addHash(hash));
        }
        blackhole.consume(state.nativeHashSet.size());
    }

    @Benchmark
    public void hashSetContainsHit(QueryState state, Blackhole blackhole) {
        for (SuitePerson value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsHit(QueryState state, Blackhole blackhole) {
        for (SuitePerson value : state.hitQueries) {
            blackhole.consume(state.nativeObjectSet.contains(value));
        }
    }

    @Benchmark
    public void nativeHashSetContainsHit(QueryState state, Blackhole blackhole) {
        for (SuitePerson value : state.hitQueries) {
            blackhole.consume(state.nativeHashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeHashSetContainsHitPrehashed(QueryState state, Blackhole blackhole) {
        for (long hash : state.hitHashes) {
            blackhole.consume(state.nativeHashSet.containsHash(hash));
        }
    }

    @Benchmark
    public void hashSetContainsMiss(QueryState state, Blackhole blackhole) {
        for (SuitePerson value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsMiss(QueryState state, Blackhole blackhole) {
        for (SuitePerson value : state.missQueries) {
            blackhole.consume(state.nativeObjectSet.contains(value));
        }
    }

    @Benchmark
    public void nativeHashSetContainsMiss(QueryState state, Blackhole blackhole) {
        for (SuitePerson value : state.missQueries) {
            blackhole.consume(state.nativeHashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeHashSetContainsMissPrehashed(QueryState state, Blackhole blackhole) {
        for (long hash : state.missHashes) {
            blackhole.consume(state.nativeHashSet.containsHash(hash));
        }
    }

    @Benchmark
    public void hashSetClearAndRefill(ClearRefillState state, Blackhole blackhole) {
        HashSet<SuitePerson> set = state.hashSet;
        set.clear();
        for (SuitePerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeObjectSetClearAndRefill(ClearRefillState state, Blackhole blackhole) {
        NativeObjectSet<SuitePerson> set = state.nativeObjectSet;
        set.clear();
        for (SuitePerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeHashSetClearAndRefill(ClearRefillState state, Blackhole blackhole) {
        NativeHashSet<SuitePerson> set = state.nativeHashSet;
        set.clear();
        for (SuitePerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeHashSetClearAndRefillPrehashed(ClearRefillState state, Blackhole blackhole) {
        NativeHashSet<SuitePerson> set = state.nativeHashSet;
        set.clear();
        for (long hash : state.valueHashes) {
            blackhole.consume(set.addHash(hash));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void hashSetRemove(RemoveState state, Blackhole blackhole) {
        for (SuitePerson value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeObjectSetRemove(RemoveState state, Blackhole blackhole) {
        for (SuitePerson value : state.removalOrder) {
            blackhole.consume(state.nativeObjectSet.remove(value));
        }
        blackhole.consume(state.nativeObjectSet.size());
    }

    @Benchmark
    public void nativeHashSetRemove(RemoveState state, Blackhole blackhole) {
        for (SuitePerson value : state.removalOrder) {
            blackhole.consume(state.nativeHashSet.remove(value));
        }
        blackhole.consume(state.nativeHashSet.size());
    }

    @Benchmark
    public void nativeHashSetRemovePrehashed(RemoveState state, Blackhole blackhole) {
        for (long hash : state.removalHashes) {
            blackhole.consume(state.nativeHashSet.removeHash(hash));
        }
        blackhole.consume(state.nativeHashSet.size());
    }

    @State(Scope.Thread)
    public abstract static class ObjectStateBase {
        @Param({"1024", "65536"})
        public int size;

        @Param({"12", "28"})
        public int nameLength;

        protected SuitePerson[] values;
        protected long[] valueHashes;
        protected int expectedCapacity;
        protected final SuitePersonMemory memory = new SuitePersonMemory();

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createValues(size, nameLength, 17_431);
            valueHashes = computeHashes(values, memory);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class AddState extends ObjectStateBase {
    }

    @State(Scope.Thread)
    public static class DuplicateAddState extends ObjectStateBase {
        private SuitePerson[] duplicateOrder;
        private long[] duplicateHashes;
        private NativeObjectSet<SuitePerson> nativeObjectSet;
        private NativeHashSet<SuitePerson> nativeHashSet;
        private HashSet<SuitePerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            duplicateOrder = shuffledCopy(values, 9101L);
            duplicateHashes = computeHashes(duplicateOrder, memory);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, SuitePerson::new);
            nativeHashSet = new NativeHashSet<>(expectedCapacity, memory);
            hashSet = new HashSet<>(expectedCapacity);
            for (SuitePerson value : values) {
                nativeObjectSet.add(value);
                nativeHashSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeObjectSet != null) {
                nativeObjectSet.freeMemory();
                nativeObjectSet = null;
            }
            if (nativeHashSet != null) {
                nativeHashSet.freeMemory();
                nativeHashSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class QueryState extends ObjectStateBase {
        private SuitePerson[] hitQueries;
        private SuitePerson[] missQueries;
        private long[] hitHashes;
        private long[] missHashes;
        private NativeObjectSet<SuitePerson> nativeObjectSet;
        private NativeHashSet<SuitePerson> nativeHashSet;
        private HashSet<SuitePerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 9202L);
            missQueries = createMisses(values);
            hitHashes = computeHashes(hitQueries, memory);
            missHashes = computeHashes(missQueries, memory);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, SuitePerson::new);
            nativeHashSet = new NativeHashSet<>(expectedCapacity, memory);
            hashSet = new HashSet<>(expectedCapacity);
            for (SuitePerson value : values) {
                nativeObjectSet.add(value);
                nativeHashSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeObjectSet != null) {
                nativeObjectSet.freeMemory();
                nativeObjectSet = null;
            }
            if (nativeHashSet != null) {
                nativeHashSet.freeMemory();
                nativeHashSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class ClearRefillState extends ObjectStateBase {
        private NativeObjectSet<SuitePerson> nativeObjectSet;
        private NativeHashSet<SuitePerson> nativeHashSet;
        private HashSet<SuitePerson> hashSet;

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, SuitePerson::new);
            nativeHashSet = new NativeHashSet<>(expectedCapacity, memory);
            hashSet = new HashSet<>(expectedCapacity);
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeObjectSet != null) {
                nativeObjectSet.freeMemory();
                nativeObjectSet = null;
            }
            if (nativeHashSet != null) {
                nativeHashSet.freeMemory();
                nativeHashSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class RemoveState extends ObjectStateBase {
        private SuitePerson[] removalOrder;
        private long[] removalHashes;
        private NativeObjectSet<SuitePerson> nativeObjectSet;
        private NativeHashSet<SuitePerson> nativeHashSet;
        private HashSet<SuitePerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 9303L);
            removalHashes = computeHashes(removalOrder, memory);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeObjectSet = new NativeObjectSet<>(expectedCapacity, memory, SuitePerson::new);
            nativeHashSet = new NativeHashSet<>(expectedCapacity, memory);
            hashSet = new HashSet<>(expectedCapacity);
            for (SuitePerson value : values) {
                nativeObjectSet.add(value);
                nativeHashSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeObjectSet != null) {
                nativeObjectSet.freeMemory();
                nativeObjectSet = null;
            }
            if (nativeHashSet != null) {
                nativeHashSet.freeMemory();
                nativeHashSet = null;
            }
            hashSet = null;
        }
    }

    private static SuitePerson[] createValues(int size, int nameLength, int seed) {
        SuitePerson[] values = new SuitePerson[size];
        for (int i = 0; i < size; i++) {
            int valueSeed = seed + i;
            values[i] = new SuitePerson(createName(valueSeed, nameLength), valueSeed, -valueSeed);
        }
        return values;
    }

    private static SuitePerson[] createMisses(SuitePerson[] values) {
        SuitePerson[] misses = new SuitePerson[values.length];
        for (int i = 0; i < values.length; i++) {
            SuitePerson value = values[i];
            misses[i] = new SuitePerson(mutateName(value.name), value.x, value.y);
        }
        return misses;
    }

    private static long[] computeHashes(SuitePerson[] values, SuitePersonMemory memory) {
        long[] hashes = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            hashes[i] = memory.hash(values[i]);
        }
        return hashes;
    }

    private static SuitePerson[] shuffledCopy(SuitePerson[] source, long seed) {
        SuitePerson[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            SuitePerson tmp = copy[i];
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

    private static final class SuitePerson {
        private String name;
        private int x;
        private int y;

        private SuitePerson() {
        }

        private SuitePerson(String name, int x, int y) {
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
            if (!(other instanceof SuitePerson person)) {
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

    private static final class SuitePersonMemory implements NativeTypeMemory<SuitePerson> {
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
        public void readFromMemory(Unsafe unsafe, long offset, SuitePerson outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
            outElement.name = NAME_FIELD.read(unsafe, offset);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, SuitePerson element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            NAME_FIELD.write(unsafe, offset, element.name);
        }

        @Override
        public long hash(SuitePerson element) {
            int result = element.name != null ? element.name.hashCode() : 0;
            result = 31 * result + element.x;
            result = 31 * result + element.y;
            return result;
        }

        @Override
        public boolean equals(SuitePerson left, SuitePerson right) {
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
        public boolean equalsMemory(Unsafe unsafe, long offset, SuitePerson value) {
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
