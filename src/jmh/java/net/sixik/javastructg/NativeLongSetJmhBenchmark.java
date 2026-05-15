package net.sixik.javastructg;

import net.sixik.javastructg.structs.sets.NativeLongSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
public class NativeLongSetJmhBenchmark {

    @Benchmark
    public void nativeLongSetAdd(AddState state, Blackhole blackhole) {
        NativeLongSet set = new NativeLongSet(state.expectedCapacity);
        try {
            for (long value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetAdd(AddState state, Blackhole blackhole) {
        HashSet<Long> set = new HashSet<>(state.expectedCapacity);
        for (long value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeLongSetContainsHit(QueryState state, Blackhole blackhole) {
        for (long value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetContainsHit(QueryState state, Blackhole blackhole) {
        for (long value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeLongSetContainsMiss(QueryState state, Blackhole blackhole) {
        for (long value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetContainsMiss(QueryState state, Blackhole blackhole) {
        for (long value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeLongSetRemove(RemoveState state, Blackhole blackhole) {
        for (long value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void hashSetRemove(RemoveState state, Blackhole blackhole) {
        for (long value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @State(Scope.Thread)
    public static class AddState {

        @Param({"1024", "65536"})
        public int size;

        private long[] values;
        private int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createUniqueValues(size, 42L);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class QueryState {

        @Param({"1024", "65536"})
        public int size;

        private long[] values;
        private long[] hitQueries;
        private long[] missQueries;
        private NativeLongSet nativeSet;
        private HashSet<Long> hashSet;
        private int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createUniqueValues(size, 84L);
            hitQueries = createShuffledCopy(values, 126L);
            missQueries = createMissValues(values, 168L);
            expectedCapacity = size << 1;
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeLongSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);

            for (long value : values) {
                nativeSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeSet != null) {
                nativeSet.freeMemory();
                nativeSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class RemoveState {

        @Param({"1024", "65536"})
        public int size;

        private long[] values;
        private long[] removalOrder;
        private NativeLongSet nativeSet;
        private HashSet<Long> hashSet;
        private int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createUniqueValues(size, 222L);
            removalOrder = createShuffledCopy(values, 333L);
            expectedCapacity = size << 1;
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeSet = new NativeLongSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);

            for (long value : values) {
                nativeSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeSet != null) {
                nativeSet.freeMemory();
                nativeSet = null;
            }
            hashSet = null;
        }
    }

    private static long[] createUniqueValues(int size, long seed) {
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            values[i] = seed + (0x9E3779B97F4A7C15L * i);
        }
        return values;
    }

    private static long[] createShuffledCopy(long[] source, long seed) {
        long[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            long tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static long[] createMissValues(long[] existing, long seed) {
        long[] misses = new long[existing.length];
        for (int i = 0; i < misses.length; i++) {
            misses[i] = ~(existing[i] + seed);
        }
        return misses;
    }
}
