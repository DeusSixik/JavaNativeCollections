package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
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

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
public class NativeArrayJmhBenchmark {

    @Benchmark
    public void nativeArrayWrite(BenchmarkState state) {
        state.nativeArrayWrite();
    }

    @Benchmark
    public void heapArrayWrite(BenchmarkState state) {
        state.heapArrayWrite();
    }

    @Benchmark
    public void arrayListWrite(BenchmarkState state) {
        state.arrayListWrite();
    }

    @Benchmark
    public void nativeArraySequentialRead(BenchmarkState state, Blackhole blackhole) {
        state.nativeArraySequentialRead(blackhole);
    }

    @Benchmark
    public void heapArraySequentialRead(BenchmarkState state, Blackhole blackhole) {
        state.heapArraySequentialRead(blackhole);
    }

    @Benchmark
    public void arrayListSequentialRead(BenchmarkState state, Blackhole blackhole) {
        state.arrayListSequentialRead(blackhole);
    }

    @Benchmark
    public void nativeArrayRandomRead(BenchmarkState state, Blackhole blackhole) {
        state.nativeArrayRandomRead(blackhole);
    }

    @Benchmark
    public void heapArrayRandomRead(BenchmarkState state, Blackhole blackhole) {
        state.heapArrayRandomRead(blackhole);
    }

    @Benchmark
    public void arrayListRandomRead(BenchmarkState state, Blackhole blackhole) {
        state.arrayListRandomRead(blackhole);
    }

    @State(Scope.Thread)
    public static class BenchmarkState {

        private static final int ARRAY_SIZE = 200_000;
        private static final int RANDOM_READ_COUNT = 400_000;

        private final BenchmarkStructMemory structMemory = new BenchmarkStructMemory();

        private BenchmarkStruct[] values;
        private int[] randomIndexes;

        private NativeObjectArray<BenchmarkStruct> nativeArray;
        private BenchmarkStruct[] heapArray;
        private ArrayList<BenchmarkStruct> arrayList;

        @Setup(Level.Trial)
        public void setupTrial() {
            values = new BenchmarkStruct[ARRAY_SIZE];
            Random random = new Random(42L);
            for (int i = 0; i < ARRAY_SIZE; i++) {
                values[i] = new BenchmarkStruct(
                        (short) (i % Short.MAX_VALUE),
                        random.nextFloat() * 1000.0f,
                        random.nextFloat() * 1000.0f
                );
            }

            randomIndexes = new int[RANDOM_READ_COUNT];
            for (int i = 0; i < RANDOM_READ_COUNT; i++) {
                randomIndexes[i] = random.nextInt(ARRAY_SIZE);
            }
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeArray = new NativeObjectArray<>(ARRAY_SIZE, structMemory);
            heapArray = new BenchmarkStruct[ARRAY_SIZE];
            arrayList = new ArrayList<>(ARRAY_SIZE);

            for (int i = 0; i < ARRAY_SIZE; i++) {
                BenchmarkStruct value = values[i];
                nativeArray.set(i, value);
                heapArray[i] = value;
                arrayList.add(value);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeArray != null) {
                nativeArray.freeMemory();
                nativeArray = null;
            }
            heapArray = null;
            arrayList = null;
        }

        private void nativeArrayWrite() {
            for (int i = 0; i < values.length; i++) {
                nativeArray.set(i, values[i]);
            }
        }

        private void heapArrayWrite() {
            for (int i = 0; i < values.length; i++) {
                heapArray[i] = values[i];
            }
        }

        private void arrayListWrite() {
            for (int i = 0; i < values.length; i++) {
                arrayList.set(i, values[i]);
            }
        }

        private void nativeArraySequentialRead(Blackhole blackhole) {
            BenchmarkStruct cursor = new BenchmarkStruct();
            for (int i = 0; i < values.length; i++) {
                nativeArray.get(i, cursor);
                blackhole.consume(cursor.id);
                blackhole.consume(cursor.temp);
                blackhole.consume(cursor.humidity);
            }
        }

        private void heapArraySequentialRead(Blackhole blackhole) {
            for (BenchmarkStruct value : heapArray) {
                blackhole.consume(value.id);
                blackhole.consume(value.temp);
                blackhole.consume(value.humidity);
            }
        }

        private void arrayListSequentialRead(Blackhole blackhole) {
            for (int i = 0; i < arrayList.size(); i++) {
                BenchmarkStruct value = arrayList.get(i);
                blackhole.consume(value.id);
                blackhole.consume(value.temp);
                blackhole.consume(value.humidity);
            }
        }

        private void nativeArrayRandomRead(Blackhole blackhole) {
            BenchmarkStruct cursor = new BenchmarkStruct();
            for (int index : randomIndexes) {
                nativeArray.get(index, cursor);
                blackhole.consume(cursor.id);
                blackhole.consume(cursor.temp);
                blackhole.consume(cursor.humidity);
            }
        }

        private void heapArrayRandomRead(Blackhole blackhole) {
            for (int index : randomIndexes) {
                BenchmarkStruct value = heapArray[index];
                blackhole.consume(value.id);
                blackhole.consume(value.temp);
                blackhole.consume(value.humidity);
            }
        }

        private void arrayListRandomRead(Blackhole blackhole) {
            for (int index : randomIndexes) {
                BenchmarkStruct value = arrayList.get(index);
                blackhole.consume(value.id);
                blackhole.consume(value.temp);
                blackhole.consume(value.humidity);
            }
        }
    }

    private static final class BenchmarkStruct {
        private short id;
        private float temp;
        private float humidity;

        private BenchmarkStruct() {
        }

        private BenchmarkStruct(short id, float temp, float humidity) {
            this.id = id;
            this.temp = temp;
            this.humidity = humidity;
        }
    }

    private static final class BenchmarkStructMemory implements NativeTypeMemory<BenchmarkStruct> {
        private static final NativeStructLayout LAYOUT;
        private static final long ID_OFFSET;
        private static final long TEMP_OFFSET;
        private static final long HUMIDITY_OFFSET;

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            ID_OFFSET = builder.shortField();
            TEMP_OFFSET = builder.floatField();
            HUMIDITY_OFFSET = builder.floatField();
            LAYOUT = builder.build();
        }

        @Override
        public long sizeof() {
            return LAYOUT.sizeof();
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, BenchmarkStruct element) {
            unsafe.putShort(offset + ID_OFFSET, element.id);
            unsafe.putFloat(offset + TEMP_OFFSET, element.temp);
            unsafe.putFloat(offset + HUMIDITY_OFFSET, element.humidity);
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, BenchmarkStruct outElement) {
            outElement.id = unsafe.getShort(offset + ID_OFFSET);
            outElement.temp = unsafe.getFloat(offset + TEMP_OFFSET);
            outElement.humidity = unsafe.getFloat(offset + HUMIDITY_OFFSET);
        }
    }
}
