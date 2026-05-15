package net.sixik.javastructg;

import net.sixik.javastructg.structs.arrays.NativeFloatArray;
import net.sixik.javastructg.structs.arrays.NativeFloatCursor;
import net.sixik.javastructg.structs.arrays.NativeFloatSlice;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeIntCursor;
import net.sixik.javastructg.structs.arrays.NativeIntSlice;
import net.sixik.javastructg.structs.arrays.NativeShortArray;
import net.sixik.javastructg.structs.arrays.NativeShortCursor;
import net.sixik.javastructg.structs.arrays.NativeShortSlice;
import net.sixik.javastructg.utils.NativeUtils;
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
import sun.misc.Unsafe;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
public class NativePrimitiveArrayJmhBenchmark {

    private static final Unsafe UNSAFE = NativeUtils.getUnsafe();

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
    public void nativeIntArrayRawWrite(IntState state) {
        long address = state.nativeArray.ptr();
        int[] values = state.values;
        for (int i = 0; i < values.length; i++, address += Integer.BYTES) {
            UNSAFE.putInt(address, values[i]);
        }
    }

    @Benchmark
    public void nativeIntArrayBulkWrite(IntState state) {
        state.nativeArray.copyFrom(state.values);
    }

    @Benchmark
    public void nativeIntArrayCursorWrite(IntState state) {
        NativeIntCursor cursor = state.writeCursor;
        cursor.rewind();
        int[] values = state.values;
        for (int i = 0; i < values.length; i++) {
            cursor.put(values[i]);
        }
    }

    @Benchmark
    public void nativeIntArraySliceWrite(IntState state) {
        NativeIntSlice slice = state.writeSlice;
        int[] values = state.values;
        for (int i = 0; i < values.length; i++) {
            slice.set(i, values[i]);
        }
    }

    @Benchmark
    public void nativeIntArrayFill(IntState state) {
        state.nativeArray.fill(state.fillValue);
    }

    @Benchmark
    public void heapIntArrayFill(IntState state) {
        Arrays.fill(state.heapArray, state.fillValue);
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
    public void nativeIntArrayRawSequentialRead(IntState state, Blackhole blackhole) {
        long end = state.nativeArray.addressAt(state.values.length);
        for (long address = state.nativeArray.ptr(); address < end; address += Integer.BYTES) {
            blackhole.consume(UNSAFE.getInt(address));
        }
    }

    @Benchmark
    public void nativeIntArrayCursorSequentialRead(IntState state, Blackhole blackhole) {
        NativeIntCursor cursor = state.readCursor;
        cursor.rewind();
        while (cursor.hasRemaining()) {
            blackhole.consume(cursor.get());
        }
    }

    @Benchmark
    public void nativeIntArraySliceSequentialRead(IntState state, Blackhole blackhole) {
        NativeIntSlice slice = state.readSlice;
        for (int i = 0; i < slice.length(); i++) {
            blackhole.consume(slice.get(i));
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
    public void nativeIntArrayRawRandomRead(IntState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(UNSAFE.getInt(state.nativeArray.addressAt(index)));
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
    public void nativeFloatArrayRawWrite(FloatState state) {
        long address = state.nativeArray.ptr();
        float[] values = state.values;
        for (int i = 0; i < values.length; i++, address += Float.BYTES) {
            UNSAFE.putFloat(address, values[i]);
        }
    }

    @Benchmark
    public void nativeFloatArrayBulkWrite(FloatState state) {
        state.nativeArray.copyFrom(state.values);
    }

    @Benchmark
    public void nativeFloatArrayCursorWrite(FloatState state) {
        NativeFloatCursor cursor = state.writeCursor;
        cursor.rewind();
        float[] values = state.values;
        for (int i = 0; i < values.length; i++) {
            cursor.put(values[i]);
        }
    }

    @Benchmark
    public void nativeFloatArraySliceWrite(FloatState state) {
        NativeFloatSlice slice = state.writeSlice;
        float[] values = state.values;
        for (int i = 0; i < values.length; i++) {
            slice.set(i, values[i]);
        }
    }

    @Benchmark
    public void nativeFloatArrayFill(FloatState state) {
        state.nativeArray.fill(state.fillValue);
    }

    @Benchmark
    public void heapFloatArrayFill(FloatState state) {
        Arrays.fill(state.heapArray, state.fillValue);
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
    public void nativeFloatArrayRawSequentialRead(FloatState state, Blackhole blackhole) {
        long end = state.nativeArray.addressAt(state.values.length);
        for (long address = state.nativeArray.ptr(); address < end; address += Float.BYTES) {
            blackhole.consume(UNSAFE.getFloat(address));
        }
    }

    @Benchmark
    public void nativeFloatArrayCursorSequentialRead(FloatState state, Blackhole blackhole) {
        NativeFloatCursor cursor = state.readCursor;
        cursor.rewind();
        while (cursor.hasRemaining()) {
            blackhole.consume(cursor.get());
        }
    }

    @Benchmark
    public void nativeFloatArraySliceSequentialRead(FloatState state, Blackhole blackhole) {
        NativeFloatSlice slice = state.readSlice;
        for (int i = 0; i < slice.length(); i++) {
            blackhole.consume(slice.get(i));
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
    public void nativeFloatArrayRawRandomRead(FloatState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(UNSAFE.getFloat(state.nativeArray.addressAt(index)));
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
    public void nativeShortArrayRawWrite(ShortState state) {
        long address = state.nativeArray.ptr();
        short[] values = state.values;
        for (int i = 0; i < values.length; i++, address += Short.BYTES) {
            UNSAFE.putShort(address, values[i]);
        }
    }

    @Benchmark
    public void nativeShortArrayBulkWrite(ShortState state) {
        state.nativeArray.copyFrom(state.values);
    }

    @Benchmark
    public void nativeShortArrayCursorWrite(ShortState state) {
        NativeShortCursor cursor = state.writeCursor;
        cursor.rewind();
        short[] values = state.values;
        for (int i = 0; i < values.length; i++) {
            cursor.put(values[i]);
        }
    }

    @Benchmark
    public void nativeShortArraySliceWrite(ShortState state) {
        NativeShortSlice slice = state.writeSlice;
        short[] values = state.values;
        for (int i = 0; i < values.length; i++) {
            slice.set(i, values[i]);
        }
    }

    @Benchmark
    public void nativeShortArrayFill(ShortState state) {
        state.nativeArray.fill(state.fillValue);
    }

    @Benchmark
    public void heapShortArrayFill(ShortState state) {
        Arrays.fill(state.heapArray, state.fillValue);
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
    public void nativeShortArrayRawSequentialRead(ShortState state, Blackhole blackhole) {
        long end = state.nativeArray.addressAt(state.values.length);
        for (long address = state.nativeArray.ptr(); address < end; address += Short.BYTES) {
            blackhole.consume(UNSAFE.getShort(address));
        }
    }

    @Benchmark
    public void nativeShortArrayCursorSequentialRead(ShortState state, Blackhole blackhole) {
        NativeShortCursor cursor = state.readCursor;
        cursor.rewind();
        while (cursor.hasRemaining()) {
            blackhole.consume(cursor.get());
        }
    }

    @Benchmark
    public void nativeShortArraySliceSequentialRead(ShortState state, Blackhole blackhole) {
        NativeShortSlice slice = state.readSlice;
        for (int i = 0; i < slice.length(); i++) {
            blackhole.consume(slice.get(i));
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

    @Benchmark
    public void nativeShortArrayRawRandomRead(ShortState state, Blackhole blackhole) {
        for (int index : state.randomIndexes) {
            blackhole.consume(UNSAFE.getShort(state.nativeArray.addressAt(index)));
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
        private NativeIntCursor readCursor;
        private NativeIntCursor writeCursor;
        private NativeIntSlice readSlice;
        private NativeIntSlice writeSlice;
        private int fillValue;

        @Setup(Level.Trial)
        public void setupTrial() {
            Random random = new Random(42L);
            values = new int[ARRAY_SIZE];
            for (int i = 0; i < ARRAY_SIZE; i++) {
                values[i] = random.nextInt();
            }
            fillValue = random.nextInt();

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

            writeCursor = nativeArray.writeCursor(ARRAY_SIZE);
            readCursor = nativeArray.cursor();
            writeSlice = nativeArray.writeSlice(0, ARRAY_SIZE);
            readSlice = nativeArray.slice();
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeArray != null) {
                nativeArray.freeMemory();
                nativeArray = null;
            }
            heapArray = null;
            readCursor = null;
            writeCursor = null;
            readSlice = null;
            writeSlice = null;
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
        private NativeFloatCursor readCursor;
        private NativeFloatCursor writeCursor;
        private NativeFloatSlice readSlice;
        private NativeFloatSlice writeSlice;
        private float fillValue;

        @Setup(Level.Trial)
        public void setupTrial() {
            Random random = new Random(84L);
            values = new float[ARRAY_SIZE];
            for (int i = 0; i < ARRAY_SIZE; i++) {
                values[i] = random.nextFloat() * 1000.0f;
            }
            fillValue = random.nextFloat() * 1000.0f;

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

            writeCursor = nativeArray.writeCursor(ARRAY_SIZE);
            readCursor = nativeArray.cursor();
            writeSlice = nativeArray.writeSlice(0, ARRAY_SIZE);
            readSlice = nativeArray.slice();
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeArray != null) {
                nativeArray.freeMemory();
                nativeArray = null;
            }
            heapArray = null;
            readCursor = null;
            writeCursor = null;
            readSlice = null;
            writeSlice = null;
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
        private NativeShortCursor readCursor;
        private NativeShortCursor writeCursor;
        private NativeShortSlice readSlice;
        private NativeShortSlice writeSlice;
        private short fillValue;

        @Setup(Level.Trial)
        public void setupTrial() {
            Random random = new Random(126L);
            values = new short[ARRAY_SIZE];
            for (int i = 0; i < ARRAY_SIZE; i++) {
                values[i] = (short) random.nextInt(Short.MAX_VALUE + 1);
            }
            fillValue = (short) random.nextInt(Short.MAX_VALUE + 1);

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

            writeCursor = nativeArray.writeCursor(ARRAY_SIZE);
            readCursor = nativeArray.cursor();
            writeSlice = nativeArray.writeSlice(0, ARRAY_SIZE);
            readSlice = nativeArray.slice();
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeArray != null) {
                nativeArray.freeMemory();
                nativeArray = null;
            }
            heapArray = null;
            readCursor = null;
            writeCursor = null;
            readSlice = null;
            writeSlice = null;
        }
    }
}
