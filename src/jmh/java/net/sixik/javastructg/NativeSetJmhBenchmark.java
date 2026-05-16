package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.sets.NativeBooleanSet;
import net.sixik.javastructg.structs.sets.NativeByteSet;
import net.sixik.javastructg.structs.sets.NativeCharSet;
import net.sixik.javastructg.structs.sets.NativeDoubleSet;
import net.sixik.javastructg.structs.sets.NativeFloatSet;
import net.sixik.javastructg.structs.sets.NativeHashSet;
import net.sixik.javastructg.structs.sets.NativeIntSet;
import net.sixik.javastructg.structs.sets.NativeLongSet;
import net.sixik.javastructg.structs.sets.NativeObjectSet;
import net.sixik.javastructg.structs.sets.NativeShortSet;
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
import sun.misc.Unsafe;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
public class NativeSetJmhBenchmark {

    @Benchmark
    public void nativeIntSetAdd(IntAddState state, Blackhole blackhole) {
        NativeIntSet set = new NativeIntSet(state.expectedCapacity);
        try {
            for (int value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetIntAdd(IntAddState state, Blackhole blackhole) {
        HashSet<Integer> set = new HashSet<>(state.expectedCapacity);
        for (int value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeIntSetContainsHit(IntQueryState state, Blackhole blackhole) {
        for (int value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetIntContainsHit(IntQueryState state, Blackhole blackhole) {
        for (int value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeIntSetContainsMiss(IntQueryState state, Blackhole blackhole) {
        for (int value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetIntContainsMiss(IntQueryState state, Blackhole blackhole) {
        for (int value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeIntSetRemove(IntRemoveState state, Blackhole blackhole) {
        for (int value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void hashSetIntRemove(IntRemoveState state, Blackhole blackhole) {
        for (int value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeLongSetAdd(LongAddState state, Blackhole blackhole) {
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
    public void hashSetLongAdd(LongAddState state, Blackhole blackhole) {
        HashSet<Long> set = new HashSet<>(state.expectedCapacity);
        for (long value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeLongSetContainsHit(LongQueryState state, Blackhole blackhole) {
        for (long value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetLongContainsHit(LongQueryState state, Blackhole blackhole) {
        for (long value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeLongSetContainsMiss(LongQueryState state, Blackhole blackhole) {
        for (long value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetLongContainsMiss(LongQueryState state, Blackhole blackhole) {
        for (long value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeLongSetRemove(LongRemoveState state, Blackhole blackhole) {
        for (long value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void hashSetLongRemove(LongRemoveState state, Blackhole blackhole) {
        for (long value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeByteSetAdd(ByteAddState state, Blackhole blackhole) {
        NativeByteSet set = new NativeByteSet(state.expectedCapacity);
        try {
            for (byte value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetByteAdd(ByteAddState state, Blackhole blackhole) {
        HashSet<Byte> set = new HashSet<>(state.expectedCapacity);
        for (byte value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeByteSetContainsHit(ByteQueryState state, Blackhole blackhole) {
        for (byte value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetByteContainsHit(ByteQueryState state, Blackhole blackhole) {
        for (byte value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeByteSetContainsMiss(ByteQueryState state, Blackhole blackhole) {
        for (byte value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetByteContainsMiss(ByteQueryState state, Blackhole blackhole) {
        for (byte value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeByteSetRemove(ByteRemoveState state, Blackhole blackhole) {
        for (byte value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void hashSetByteRemove(ByteRemoveState state, Blackhole blackhole) {
        for (byte value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeShortSetAdd(ShortAddState state, Blackhole blackhole) {
        NativeShortSet set = new NativeShortSet(state.expectedCapacity);
        try {
            for (short value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetShortAdd(ShortAddState state, Blackhole blackhole) {
        HashSet<Short> set = new HashSet<>(state.expectedCapacity);
        for (short value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeShortSetContainsHit(ShortQueryState state, Blackhole blackhole) {
        for (short value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetShortContainsHit(ShortQueryState state, Blackhole blackhole) {
        for (short value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeShortSetContainsMiss(ShortQueryState state, Blackhole blackhole) {
        for (short value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetShortContainsMiss(ShortQueryState state, Blackhole blackhole) {
        for (short value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeShortSetRemove(ShortRemoveState state, Blackhole blackhole) {
        for (short value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void hashSetShortRemove(ShortRemoveState state, Blackhole blackhole) {
        for (short value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeCharSetAdd(CharAddState state, Blackhole blackhole) {
        NativeCharSet set = new NativeCharSet(state.expectedCapacity);
        try {
            for (char value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetCharAdd(CharAddState state, Blackhole blackhole) {
        HashSet<Character> set = new HashSet<>(state.expectedCapacity);
        for (char value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeCharSetContainsHit(CharQueryState state, Blackhole blackhole) {
        for (char value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetCharContainsHit(CharQueryState state, Blackhole blackhole) {
        for (char value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeCharSetContainsMiss(CharQueryState state, Blackhole blackhole) {
        for (char value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetCharContainsMiss(CharQueryState state, Blackhole blackhole) {
        for (char value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeCharSetRemove(CharRemoveState state, Blackhole blackhole) {
        for (char value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void hashSetCharRemove(CharRemoveState state, Blackhole blackhole) {
        for (char value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeFloatSetAdd(FloatAddState state, Blackhole blackhole) {
        NativeFloatSet set = new NativeFloatSet(state.expectedCapacity);
        try {
            for (float value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetFloatAdd(FloatAddState state, Blackhole blackhole) {
        HashSet<Float> set = new HashSet<>(state.expectedCapacity);
        for (float value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeFloatSetContainsHit(FloatQueryState state, Blackhole blackhole) {
        for (float value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetFloatContainsHit(FloatQueryState state, Blackhole blackhole) {
        for (float value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeFloatSetContainsMiss(FloatQueryState state, Blackhole blackhole) {
        for (float value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetFloatContainsMiss(FloatQueryState state, Blackhole blackhole) {
        for (float value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeFloatSetRemove(FloatRemoveState state, Blackhole blackhole) {
        for (float value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void hashSetFloatRemove(FloatRemoveState state, Blackhole blackhole) {
        for (float value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeDoubleSetAdd(DoubleAddState state, Blackhole blackhole) {
        NativeDoubleSet set = new NativeDoubleSet(state.expectedCapacity);
        try {
            for (double value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void hashSetDoubleAdd(DoubleAddState state, Blackhole blackhole) {
        HashSet<Double> set = new HashSet<>(state.expectedCapacity);
        for (double value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeDoubleSetContainsHit(DoubleQueryState state, Blackhole blackhole) {
        for (double value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetDoubleContainsHit(DoubleQueryState state, Blackhole blackhole) {
        for (double value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeDoubleSetContainsMiss(DoubleQueryState state, Blackhole blackhole) {
        for (double value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetDoubleContainsMiss(DoubleQueryState state, Blackhole blackhole) {
        for (double value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeDoubleSetRemove(DoubleRemoveState state, Blackhole blackhole) {
        for (double value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void hashSetDoubleRemove(DoubleRemoveState state, Blackhole blackhole) {
        for (double value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @Benchmark
    public void nativeBooleanSetAdd(BooleanAddState state, Blackhole blackhole) {
        NativeBooleanSet set = state.nativeSet;
        for (int i = 0; i < state.cycles; i++) {
            set.clear();
            blackhole.consume(set.add(true));
            blackhole.consume(set.add(false));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void hashSetBooleanAdd(BooleanAddState state, Blackhole blackhole) {
        HashSet<Boolean> set = state.hashSet;
        for (int i = 0; i < state.cycles; i++) {
            set.clear();
            blackhole.consume(set.add(Boolean.TRUE));
            blackhole.consume(set.add(Boolean.FALSE));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeBooleanSetContains(BooleanQueryState state, Blackhole blackhole) {
        for (boolean value : state.queries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void hashSetBooleanContains(BooleanQueryState state, Blackhole blackhole) {
        for (boolean value : state.queries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeBooleanSetRemove(BooleanRemoveState state, Blackhole blackhole) {
        NativeBooleanSet set = state.nativeSet;
        for (int i = 0; i < state.cycles; i++) {
            set.clear();
            set.add(true);
            set.add(false);
            blackhole.consume(set.remove(true));
            blackhole.consume(set.remove(false));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void hashSetBooleanRemove(BooleanRemoveState state, Blackhole blackhole) {
        HashSet<Boolean> set = state.hashSet;
        for (int i = 0; i < state.cycles; i++) {
            set.clear();
            set.add(Boolean.TRUE);
            set.add(Boolean.FALSE);
            blackhole.consume(set.remove(Boolean.TRUE));
            blackhole.consume(set.remove(Boolean.FALSE));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeObjectSetAdd(ObjectAddState state, Blackhole blackhole) {
        NativeObjectSet<BenchmarkPerson> set = new NativeObjectSet<>(state.expectedCapacity, state.memory, BenchmarkPerson::new);
        try {
            for (BenchmarkPerson value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void nativeHashSetObjectAdd(ObjectAddState state, Blackhole blackhole) {
        NativeHashSet<BenchmarkPerson> set = new NativeHashSet<>(state.expectedCapacity, state.memory);
        try {
            for (BenchmarkPerson value : state.values) {
                blackhole.consume(set.add(value));
            }
            blackhole.consume(set.size());
        } finally {
            set.freeMemory();
        }
    }

    @Benchmark
    public void nativeHashSetObjectAddPrehashed(ObjectAddState state, Blackhole blackhole) {
        NativeHashSet<BenchmarkPerson> set = new NativeHashSet<>(state.expectedCapacity, state.memory);
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
    public void hashSetObjectAdd(ObjectAddState state, Blackhole blackhole) {
        HashSet<BenchmarkPerson> set = new HashSet<>(state.expectedCapacity);
        for (BenchmarkPerson value : state.values) {
            blackhole.consume(set.add(value));
        }
        blackhole.consume(set.size());
    }

    @Benchmark
    public void nativeObjectSetContainsHit(ObjectQueryState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.hitQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void nativeHashSetObjectContainsHit(ObjectQueryState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.hitQueries) {
            blackhole.consume(state.nativeHashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeHashSetObjectContainsHitPrehashed(ObjectQueryState state, Blackhole blackhole) {
        for (long hash : state.hitQueryHashes) {
            blackhole.consume(state.nativeHashSet.containsHash(hash));
        }
    }

    @Benchmark
    public void hashSetObjectContainsHit(ObjectQueryState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.hitQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetContainsMiss(ObjectQueryState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.missQueries) {
            blackhole.consume(state.nativeSet.contains(value));
        }
    }

    @Benchmark
    public void nativeHashSetObjectContainsMiss(ObjectQueryState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.missQueries) {
            blackhole.consume(state.nativeHashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeHashSetObjectContainsMissPrehashed(ObjectQueryState state, Blackhole blackhole) {
        for (long hash : state.missQueryHashes) {
            blackhole.consume(state.nativeHashSet.containsHash(hash));
        }
    }

    @Benchmark
    public void hashSetObjectContainsMiss(ObjectQueryState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.missQueries) {
            blackhole.consume(state.hashSet.contains(value));
        }
    }

    @Benchmark
    public void nativeObjectSetRemove(ObjectRemoveState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.removalOrder) {
            blackhole.consume(state.nativeSet.remove(value));
        }
        blackhole.consume(state.nativeSet.size());
    }

    @Benchmark
    public void nativeHashSetObjectRemove(ObjectRemoveState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.removalOrder) {
            blackhole.consume(state.nativeHashSet.remove(value));
        }
        blackhole.consume(state.nativeHashSet.size());
    }

    @Benchmark
    public void nativeHashSetObjectRemovePrehashed(ObjectRemoveState state, Blackhole blackhole) {
        for (long hash : state.removalHashes) {
            blackhole.consume(state.nativeHashSet.removeHash(hash));
        }
        blackhole.consume(state.nativeHashSet.size());
    }

    @Benchmark
    public void hashSetObjectRemove(ObjectRemoveState state, Blackhole blackhole) {
        for (BenchmarkPerson value : state.removalOrder) {
            blackhole.consume(state.hashSet.remove(value));
        }
        blackhole.consume(state.hashSet.size());
    }

    @State(Scope.Thread)
    public abstract static class IntStateBase {
        @Param({"1024", "65536"})
        public int size;
        protected int[] values;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createIntValues(size, 17);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class IntAddState extends IntStateBase {
    }

    @State(Scope.Thread)
    public static class IntQueryState extends IntStateBase {
        private int[] hitQueries;
        private int[] missQueries;
        private NativeIntSet nativeSet;
        private HashSet<Integer> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 101);
            missQueries = createIntMisses(values);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeIntSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (int value : values) {
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
    public static class IntRemoveState extends IntStateBase {
        private int[] removalOrder;
        private NativeIntSet nativeSet;
        private HashSet<Integer> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 102);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeSet = new NativeIntSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (int value : values) {
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

    @State(Scope.Thread)
    public abstract static class LongStateBase {
        @Param({"1024", "65536"})
        public int size;
        protected long[] values;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createLongValues(size, 23L);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class LongAddState extends LongStateBase {
    }

    @State(Scope.Thread)
    public static class LongQueryState extends LongStateBase {
        private long[] hitQueries;
        private long[] missQueries;
        private NativeLongSet nativeSet;
        private HashSet<Long> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 201L);
            missQueries = createLongMisses(values);
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
    public static class LongRemoveState extends LongStateBase {
        private long[] removalOrder;
        private NativeLongSet nativeSet;
        private HashSet<Long> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 202L);
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

    @State(Scope.Thread)
    public abstract static class ByteStateBase {
        @Param({"32", "64"})
        public int size;
        protected byte[] values;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createByteValues(size);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class ByteAddState extends ByteStateBase {
    }

    @State(Scope.Thread)
    public static class ByteQueryState extends ByteStateBase {
        private byte[] hitQueries;
        private byte[] missQueries;
        private NativeByteSet nativeSet;
        private HashSet<Byte> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 301L);
            missQueries = createByteMisses(values);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeByteSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (byte value : values) {
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
    public static class ByteRemoveState extends ByteStateBase {
        private byte[] removalOrder;
        private NativeByteSet nativeSet;
        private HashSet<Byte> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 302L);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeSet = new NativeByteSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (byte value : values) {
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

    @State(Scope.Thread)
    public abstract static class ShortStateBase {
        @Param({"1024", "16384"})
        public int size;
        protected short[] values;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createShortValues(size);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class ShortAddState extends ShortStateBase {
    }

    @State(Scope.Thread)
    public static class ShortQueryState extends ShortStateBase {
        private short[] hitQueries;
        private short[] missQueries;
        private NativeShortSet nativeSet;
        private HashSet<Short> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 401L);
            missQueries = createShortMisses(values);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeShortSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (short value : values) {
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
    public static class ShortRemoveState extends ShortStateBase {
        private short[] removalOrder;
        private NativeShortSet nativeSet;
        private HashSet<Short> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 402L);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeSet = new NativeShortSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (short value : values) {
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

    @State(Scope.Thread)
    public abstract static class CharStateBase {
        @Param({"1024", "16384"})
        public int size;
        protected char[] values;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createCharValues(size);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class CharAddState extends CharStateBase {
    }

    @State(Scope.Thread)
    public static class CharQueryState extends CharStateBase {
        private char[] hitQueries;
        private char[] missQueries;
        private NativeCharSet nativeSet;
        private HashSet<Character> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 501L);
            missQueries = createCharMisses(values);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeCharSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (char value : values) {
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
    public static class CharRemoveState extends CharStateBase {
        private char[] removalOrder;
        private NativeCharSet nativeSet;
        private HashSet<Character> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 502L);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeSet = new NativeCharSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (char value : values) {
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

    @State(Scope.Thread)
    public abstract static class FloatStateBase {
        @Param({"1024", "65536"})
        public int size;
        protected float[] values;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createFloatValues(size, 0.25f);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class FloatAddState extends FloatStateBase {
    }

    @State(Scope.Thread)
    public static class FloatQueryState extends FloatStateBase {
        private float[] hitQueries;
        private float[] missQueries;
        private NativeFloatSet nativeSet;
        private HashSet<Float> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 601L);
            missQueries = createFloatMisses(values);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeFloatSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (float value : values) {
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
    public static class FloatRemoveState extends FloatStateBase {
        private float[] removalOrder;
        private NativeFloatSet nativeSet;
        private HashSet<Float> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 602L);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeSet = new NativeFloatSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (float value : values) {
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

    @State(Scope.Thread)
    public abstract static class DoubleStateBase {
        @Param({"1024", "65536"})
        public int size;
        protected double[] values;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createDoubleValues(size, 0.5d);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class DoubleAddState extends DoubleStateBase {
    }

    @State(Scope.Thread)
    public static class DoubleQueryState extends DoubleStateBase {
        private double[] hitQueries;
        private double[] missQueries;
        private NativeDoubleSet nativeSet;
        private HashSet<Double> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 701L);
            missQueries = createDoubleMisses(values);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeDoubleSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (double value : values) {
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
    public static class DoubleRemoveState extends DoubleStateBase {
        private double[] removalOrder;
        private NativeDoubleSet nativeSet;
        private HashSet<Double> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 702L);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeSet = new NativeDoubleSet(expectedCapacity);
            hashSet = new HashSet<>(expectedCapacity);
            for (double value : values) {
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

    @State(Scope.Thread)
    public static class BooleanAddState {
        @Param({"1024", "65536"})
        public int cycles;
        private NativeBooleanSet nativeSet;
        private HashSet<Boolean> hashSet;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeBooleanSet(8);
            hashSet = new HashSet<>(8);
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
    public static class BooleanQueryState {
        @Param({"1024", "65536"})
        public int size;
        private boolean[] queries;
        private NativeBooleanSet nativeSet;
        private HashSet<Boolean> hashSet;

        @Setup(Level.Trial)
        public void setupTrial() {
            queries = createBooleanQueries(size);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeBooleanSet(8);
            nativeSet.add(true);
            nativeSet.add(false);
            hashSet = new HashSet<>(8);
            hashSet.add(Boolean.TRUE);
            hashSet.add(Boolean.FALSE);
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
    public static class BooleanRemoveState {
        @Param({"1024", "65536"})
        public int cycles;
        private NativeBooleanSet nativeSet;
        private HashSet<Boolean> hashSet;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeBooleanSet(8);
            hashSet = new HashSet<>(8);
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
    public abstract static class ObjectStateBase {
        @Param({"1024", "65536"})
        public int size;
        protected BenchmarkPerson[] values;
        protected long[] valueHashes;
        protected int expectedCapacity;
        protected final BenchmarkPersonMemory memory = new BenchmarkPersonMemory();

        @Setup(Level.Trial)
        public void setupTrial() {
            values = createObjectValues(size, 800);
            valueHashes = computeObjectHashes(values, memory);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class ObjectAddState extends ObjectStateBase {
    }

    @State(Scope.Thread)
    public static class ObjectQueryState extends ObjectStateBase {
        private BenchmarkPerson[] hitQueries;
        private BenchmarkPerson[] missQueries;
        private long[] hitQueryHashes;
        private long[] missQueryHashes;
        private NativeObjectSet<BenchmarkPerson> nativeSet;
        private NativeHashSet<BenchmarkPerson> nativeHashSet;
        private HashSet<BenchmarkPerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            hitQueries = shuffledCopy(values, 801L);
            missQueries = createObjectMisses(values);
            hitQueryHashes = computeObjectHashes(hitQueries, memory);
            missQueryHashes = computeObjectHashes(missQueries, memory);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeSet = new NativeObjectSet<>(expectedCapacity, memory, BenchmarkPerson::new);
            nativeHashSet = new NativeHashSet<>(expectedCapacity, memory);
            hashSet = new HashSet<>(expectedCapacity);
            for (BenchmarkPerson value : values) {
                nativeSet.add(value);
                nativeHashSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeSet != null) {
                nativeSet.freeMemory();
                nativeSet = null;
            }
            if (nativeHashSet != null) {
                nativeHashSet.freeMemory();
                nativeHashSet = null;
            }
            hashSet = null;
        }
    }

    @State(Scope.Thread)
    public static class ObjectRemoveState extends ObjectStateBase {
        private BenchmarkPerson[] removalOrder;
        private long[] removalHashes;
        private NativeObjectSet<BenchmarkPerson> nativeSet;
        private NativeHashSet<BenchmarkPerson> nativeHashSet;
        private HashSet<BenchmarkPerson> hashSet;

        @Setup(Level.Trial)
        @Override
        public void setupTrial() {
            super.setupTrial();
            removalOrder = shuffledCopy(values, 802L);
            removalHashes = computeObjectHashes(removalOrder, memory);
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeSet = new NativeObjectSet<>(expectedCapacity, memory, BenchmarkPerson::new);
            nativeHashSet = new NativeHashSet<>(expectedCapacity, memory);
            hashSet = new HashSet<>(expectedCapacity);
            for (BenchmarkPerson value : values) {
                nativeSet.add(value);
                nativeHashSet.add(value);
                hashSet.add(value);
            }
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeSet != null) {
                nativeSet.freeMemory();
                nativeSet = null;
            }
            if (nativeHashSet != null) {
                nativeHashSet.freeMemory();
                nativeHashSet = null;
            }
            hashSet = null;
        }
    }

    private static int[] createIntValues(int size, int seed) {
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = seed + (i * 0x9e3779b9);
        }
        return values;
    }

    private static int[] createIntMisses(int[] values) {
        int[] misses = new int[values.length];
        for (int i = 0; i < misses.length; i++) {
            misses[i] = values[i] + 1;
        }
        return misses;
    }

    private static long[] createLongValues(int size, long seed) {
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            values[i] = seed + (0x9E3779B97F4A7C15L * i);
        }
        return values;
    }

    private static long[] createLongMisses(long[] values) {
        long[] misses = new long[values.length];
        for (int i = 0; i < misses.length; i++) {
            misses[i] = values[i] + 1L;
        }
        return misses;
    }

    private static byte[] createByteValues(int size) {
        byte[] values = new byte[size];
        for (int i = 0; i < size; i++) {
            values[i] = (byte) i;
        }
        return values;
    }

    private static byte[] createByteMisses(byte[] values) {
        byte[] misses = new byte[values.length];
        int shift = values.length + 1;
        for (int i = 0; i < misses.length; i++) {
            misses[i] = (byte) (i + shift);
        }
        return misses;
    }

    private static short[] createShortValues(int size) {
        short[] values = new short[size];
        for (int i = 0; i < size; i++) {
            values[i] = (short) i;
        }
        return values;
    }

    private static short[] createShortMisses(short[] values) {
        short[] misses = new short[values.length];
        int shift = values.length + 1;
        for (int i = 0; i < misses.length; i++) {
            misses[i] = (short) (i + shift);
        }
        return misses;
    }

    private static char[] createCharValues(int size) {
        char[] values = new char[size];
        for (int i = 0; i < size; i++) {
            values[i] = (char) i;
        }
        return values;
    }

    private static char[] createCharMisses(char[] values) {
        char[] misses = new char[values.length];
        int shift = values.length + 1;
        for (int i = 0; i < misses.length; i++) {
            misses[i] = (char) (i + shift);
        }
        return misses;
    }

    private static float[] createFloatValues(int size, float offset) {
        float[] values = new float[size];
        for (int i = 0; i < size; i++) {
            values[i] = i + offset;
        }
        return values;
    }

    private static float[] createFloatMisses(float[] values) {
        float[] misses = new float[values.length];
        for (int i = 0; i < misses.length; i++) {
            misses[i] = values[i] + 0.5f;
        }
        return misses;
    }

    private static double[] createDoubleValues(int size, double offset) {
        double[] values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = i + offset;
        }
        return values;
    }

    private static double[] createDoubleMisses(double[] values) {
        double[] misses = new double[values.length];
        for (int i = 0; i < misses.length; i++) {
            misses[i] = values[i] + 0.5d;
        }
        return misses;
    }

    private static boolean[] createBooleanQueries(int size) {
        boolean[] values = new boolean[size];
        for (int i = 0; i < size; i++) {
            values[i] = (i & 1) == 0;
        }
        return values;
    }

    private static BenchmarkPerson[] createObjectValues(int size, int seed) {
        BenchmarkPerson[] values = new BenchmarkPerson[size];
        for (int i = 0; i < size; i++) {
            values[i] = new BenchmarkPerson("p" + (seed + i), seed + i, -(seed + i));
        }
        return values;
    }

    private static BenchmarkPerson[] createObjectMisses(BenchmarkPerson[] values) {
        BenchmarkPerson[] misses = new BenchmarkPerson[values.length];
        for (int i = 0; i < misses.length; i++) {
            BenchmarkPerson value = values[i];
            misses[i] = new BenchmarkPerson(value.name + "!", value.x, value.y);
        }
        return misses;
    }

    private static long[] computeObjectHashes(BenchmarkPerson[] values, BenchmarkPersonMemory memory) {
        long[] hashes = new long[values.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = memory.hash(values[i]);
        }
        return hashes;
    }

    private static int[] shuffledCopy(int[] source, long seed) {
        int[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static long[] shuffledCopy(long[] source, long seed) {
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

    private static byte[] shuffledCopy(byte[] source, long seed) {
        byte[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            byte tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static short[] shuffledCopy(short[] source, long seed) {
        short[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            short tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static char[] shuffledCopy(char[] source, long seed) {
        char[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static float[] shuffledCopy(float[] source, long seed) {
        float[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            float tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static double[] shuffledCopy(double[] source, long seed) {
        double[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static BenchmarkPerson[] shuffledCopy(BenchmarkPerson[] source, long seed) {
        BenchmarkPerson[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            BenchmarkPerson tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static final class BenchmarkPerson {
        private String name;
        private int x;
        private int y;

        private BenchmarkPerson() {
        }

        private BenchmarkPerson(String name, int x, int y) {
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
            if (!(other instanceof BenchmarkPerson person)) {
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

    private static final class BenchmarkPersonMemory implements NativeTypeMemory<BenchmarkPerson> {
        private static final int MAX_NAME_LENGTH = 32;
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
        public void readFromMemory(Unsafe unsafe, long offset, BenchmarkPerson outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
            outElement.name = NAME_FIELD.read(unsafe, offset);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, BenchmarkPerson element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            NAME_FIELD.write(unsafe, offset, element.name);
        }

        @Override
        public long hash(BenchmarkPerson element) {
            int result = element.name != null ? element.name.hashCode() : 0;
            result = 31 * result + element.x;
            result = 31 * result + element.y;
            return result;
        }

        @Override
        public boolean equals(BenchmarkPerson left, BenchmarkPerson right) {
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
        public boolean equalsMemory(Unsafe unsafe, long offset, BenchmarkPerson value) {
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
