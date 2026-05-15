package net.sixik.javastructg;

import net.sixik.javastructg.structs.arrays.NativeFloatArray;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeShortArray;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
public class NativePrimitiveArrayJmhBenchmark {

    @Benchmark
    public void nativeIntArrayWrite(IntState state) {
        for (int i = 0; i < state.values.length; i++) {
            state.nativeArray.set(i, state.values[i]);
        }
    }

    @Benchmark
    public void heapIntArrayWrite(IntState state) {
        for (int i = 0; i < state.values.length; i++) {
            state.heapArray[i] = state.values[i];
        }
    }

    @Benchmark
    public void nativeIntArraySequentialRead(IntState state, Blackhole blackhole) {
        for (int i = 0; i < state.values.length; i++) {
            blackhole.consume(state.nativeArray.get(i));
        }
    }

    @Benchmark
    public void heapIntArraySequentialRead(IntState state, Blackhole blackhole) {
        for (int value : state.heapArray) {
            blackhole.consume(value);
        }
    }

    @Benchmark
    public void nativeIntArrayRandomRead(IntState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(state.nativeArray.get(index));
        }
    }

    @Benchmark
    public void heapIntArrayRandomRead(IntState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(state.heapArray[index]);
        }
    }

    @Benchmark
    public void nativeFloatArrayWrite(FloatState state) {
        for (int i = 0; i < state.values.length; i++) {
            state.nativeArray.set(i, state.values[i]);
        }
    }

    @Benchmark
    public void heapFloatArrayWrite(FloatState state) {
        for (int i = 0; i < state.values.length; i++) {
            state.heapArray[i] = state.values[i];
        }
    }

    @Benchmark
    public void nativeFloatArraySequentialRead(FloatState state, Blackhole blackhole) {
        for (int i = 0; i < state.values.length; i++) {
            blackhole.consume(state.nativeArray.get(i));
        }
    }

    @Benchmark
    public void heapFloatArraySequentialRead(FloatState state, Blackhole blackhole) {
        for (float value : state.heapArray) {
            blackhole.consume(value);
        }
    }

    @Benchmark
    public void nativeFloatArrayRandomRead(FloatState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(state.nativeArray.get(index));
        }
    }

    @Benchmark
    public void heapFloatArrayRandomRead(FloatState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(state.heapArray[index]);
        }
    }

    @Benchmark
    public void nativeShortArrayWrite(ShortState state) {
        for (int i = 0; i < state.values.length; i++) {
            state.nativeArray.set(i, state.values[i]);
        }
    }

    @Benchmark
    public void heapShortArrayWrite(ShortState state) {
        for (int i = 0; i < state.values.length; i++) {
            state.heapArray[i] = state.values[i];
        }
    }

    @Benchmark
    public void nativeShortArraySequentialRead(ShortState state, Blackhole blackhole) {
        for (int i = 0; i < state.values.length; i++) {
            blackhole.consume(state.nativeArray.get(i));
        }
    }

    @Benchmark
    public void heapShortArraySequentialRead(ShortState state, Blackhole blackhole) {
        for (short value : state.heapArray) {
            blackhole.consume(value);
        }
    }

    @Benchmark
    public void nativeShortArrayRandomRead(ShortState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(state.nativeArray.get(index));
        }
    }

    @Benchmark
    public void heapShortArrayRandomRead(ShortState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(state.heapArray[index]);
        }
    }

    @State(Scope.Thread)
    public static class IntState {

        private static final int ARRAY_SIZE = 200_000;
        private static final int RANDOM_READ_COUNT = 400_000;

        private int[] values;
        private int[] randomIndexes;
        private NativeIntArray nativeArray;
        private int[] heapArray;

        @Setup(Level.Trial)
        public void setupTrial() {
            Random random = new Random(42L);
            values = new int[ARRAY_SIZE];
            for (int i = 0; i < ARRAY_SIZE; i++) {
                values[i] = random.nextInt();
            }

            randomIndexes = new int[RANDOM_READ_COUNT];
            for (int i = 0; i < RANDOM_READ_COUNT; i++) {
                randomIndexes[i] = random.nextInt(ARRAY_SIZE);
            }
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeArray = new NativeIntArray(ARRAY_SIZE);
            heapArray = new int[ARRAY_SIZE];

            for (int i = 0; i < ARRAY_SIZE; i++) {
                nativeArray.set(i, values[i]);
                heapArray[i] = values[i];
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeArray != null) {
                nativeArray.freeMemory();
                nativeArray = null;
            }
            heapArray = null;
        }
    }

    @State(Scope.Thread)
    public static class FloatState {

        private static final int ARRAY_SIZE = 200_000;
        private static final int RANDOM_READ_COUNT = 400_000;

        private float[] values;
        private int[] randomIndexes;
        private NativeFloatArray nativeArray;
        private float[] heapArray;

        @Setup(Level.Trial)
        public void setupTrial() {
            Random random = new Random(84L);
            values = new float[ARRAY_SIZE];
            for (int i = 0; i < ARRAY_SIZE; i++) {
                values[i] = random.nextFloat() * 1000.0f;
            }

            randomIndexes = new int[RANDOM_READ_COUNT];
            for (int i = 0; i < RANDOM_READ_COUNT; i++) {
                randomIndexes[i] = random.nextInt(ARRAY_SIZE);
            }
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeArray = new NativeFloatArray(ARRAY_SIZE);
            heapArray = new float[ARRAY_SIZE];

            for (int i = 0; i < ARRAY_SIZE; i++) {
                nativeArray.set(i, values[i]);
                heapArray[i] = values[i];
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeArray != null) {
                nativeArray.freeMemory();
                nativeArray = null;
            }
            heapArray = null;
        }
    }

    @State(Scope.Thread)
    public static class ShortState {

        private static final int ARRAY_SIZE = 200_000;
        private static final int RANDOM_READ_COUNT = 400_000;

        private short[] values;
        private int[] randomIndexes;
        private NativeShortArray nativeArray;
        private short[] heapArray;

        @Setup(Level.Trial)
        public void setupTrial() {
            Random random = new Random(126L);
            values = new short[ARRAY_SIZE];
            for (int i = 0; i < ARRAY_SIZE; i++) {
                values[i] = (short) random.nextInt(Short.MAX_VALUE + 1);
            }

            randomIndexes = new int[RANDOM_READ_COUNT];
            for (int i = 0; i < RANDOM_READ_COUNT; i++) {
                randomIndexes[i] = random.nextInt(ARRAY_SIZE);
            }
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeArray = new NativeShortArray(ARRAY_SIZE);
            heapArray = new short[ARRAY_SIZE];

            for (int i = 0; i < ARRAY_SIZE; i++) {
                nativeArray.set(i, values[i]);
                heapArray[i] = values[i];
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeArray != null) {
                nativeArray.freeMemory();
                nativeArray = null;
            }
            heapArray = null;
        }
    }
}
