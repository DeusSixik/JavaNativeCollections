import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("benchmark")
public class NativeArrayBenchmarkTest {

    private static final int ARRAY_SIZE = 200_000;
    private static final int RANDOM_READ_COUNT = 400_000;
    private static final int WARMUP_ROUNDS = 3;
    private static final int MEASURE_ROUNDS = 5;

    private final BenchmarkStructMemory structMemory = new BenchmarkStructMemory();

    @Test
    public void compareWriteThroughput() {
        BenchmarkData data = BenchmarkData.create(ARRAY_SIZE, RANDOM_READ_COUNT);

        BenchmarkStats nativeStats = runBenchmark(data, this::measureNativeWrite);
        BenchmarkStats heapStats = runBenchmark(data, this::measureHeapWrite);
        BenchmarkStats arrayListStats = runBenchmark(data, this::measureArrayListWrite);

        assertEquals(heapStats.checksum(), nativeStats.checksum());
        assertEquals(heapStats.checksum(), arrayListStats.checksum());
        printStats("write", nativeStats, heapStats, arrayListStats, ARRAY_SIZE);
    }

    @Test
    public void compareSequentialReadThroughput() {
        BenchmarkData data = BenchmarkData.create(ARRAY_SIZE, RANDOM_READ_COUNT);

        BenchmarkStats nativeStats = runBenchmark(data, this::measureNativeSequentialRead);
        BenchmarkStats heapStats = runBenchmark(data, this::measureHeapSequentialRead);
        BenchmarkStats arrayListStats = runBenchmark(data, this::measureArrayListSequentialRead);

        assertEquals(heapStats.checksum(), nativeStats.checksum());
        assertEquals(heapStats.checksum(), arrayListStats.checksum());
        printStats("sequential-read", nativeStats, heapStats, arrayListStats, ARRAY_SIZE);
    }

    @Test
    public void compareRandomReadThroughput() {
        BenchmarkData data = BenchmarkData.create(ARRAY_SIZE, RANDOM_READ_COUNT);

        BenchmarkStats nativeStats = runBenchmark(data, this::measureNativeRandomRead);
        BenchmarkStats heapStats = runBenchmark(data, this::measureHeapRandomRead);
        BenchmarkStats arrayListStats = runBenchmark(data, this::measureArrayListRandomRead);

        assertEquals(heapStats.checksum(), nativeStats.checksum());
        assertEquals(heapStats.checksum(), arrayListStats.checksum());
        printStats("random-read", nativeStats, heapStats, arrayListStats, RANDOM_READ_COUNT);
    }

    private BenchmarkStats runBenchmark(BenchmarkData data, BenchmarkScenario scenario) {
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            scenario.run(data);
        }

        long[] timings = new long[MEASURE_ROUNDS];
        long checksum = 0L;
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            BenchmarkRun run = scenario.run(data);
            timings[i] = run.nanos();
            checksum = run.checksum();
        }

        return new BenchmarkStats(timings, checksum);
    }

    private BenchmarkRun measureNativeWrite(BenchmarkData data) {
        NativeObjectArray<BenchmarkStruct> nativeArray = new NativeObjectArray<>(data.values.length, structMemory);
        try {
            long start = System.nanoTime();
            for (int i = 0; i < data.values.length; i++) {
                nativeArray.set(i, data.values[i]);
            }
            long elapsed = System.nanoTime() - start;

            BenchmarkStruct cursor = new BenchmarkStruct();
            long checksum = 0L;
            checksum += sampleChecksum(nativeArray, cursor, 0);
            checksum += sampleChecksum(nativeArray, cursor, data.values.length / 2);
            checksum += sampleChecksum(nativeArray, cursor, data.values.length - 1);

            return new BenchmarkRun(elapsed, checksum);
        } finally {
            nativeArray.freeMemory();
        }
    }

    private BenchmarkRun measureHeapWrite(BenchmarkData data) {
        BenchmarkStruct[] heapArray = new BenchmarkStruct[data.values.length];

        long start = System.nanoTime();
        for (int i = 0; i < data.values.length; i++) {
            BenchmarkStruct value = data.values[i];
            heapArray[i] = new BenchmarkStruct(value.id, value.temp, value.humidity);
        }
        long elapsed = System.nanoTime() - start;

        long checksum = 0L;
        checksum += checksum(heapArray[0]);
        checksum += checksum(heapArray[data.values.length / 2]);
        checksum += checksum(heapArray[data.values.length - 1]);
        return new BenchmarkRun(elapsed, checksum);
    }

    private BenchmarkRun measureArrayListWrite(BenchmarkData data) {
        ArrayList<BenchmarkStruct> arrayList = new ArrayList<>(data.values.length);

        long start = System.nanoTime();
        for (BenchmarkStruct value : data.values) {
            arrayList.add(new BenchmarkStruct(value.id, value.temp, value.humidity));
        }
        long elapsed = System.nanoTime() - start;

        long checksum = 0L;
        checksum += checksum(arrayList.get(0));
        checksum += checksum(arrayList.get(data.values.length / 2));
        checksum += checksum(arrayList.get(data.values.length - 1));
        return new BenchmarkRun(elapsed, checksum);
    }

    private BenchmarkRun measureNativeSequentialRead(BenchmarkData data) {
        NativeObjectArray<BenchmarkStruct> nativeArray = new NativeObjectArray<>(data.values.length, structMemory);
        try {
            for (int i = 0; i < data.values.length; i++) {
                nativeArray.set(i, data.values[i]);
            }

            BenchmarkStruct cursor = new BenchmarkStruct();
            long checksum = 0L;
            long start = System.nanoTime();
            for (int i = 0; i < data.values.length; i++) {
                nativeArray.get(i, cursor);
                checksum += checksum(cursor);
            }
            long elapsed = System.nanoTime() - start;
            return new BenchmarkRun(elapsed, checksum);
        } finally {
            nativeArray.freeMemory();
        }
    }

    private BenchmarkRun measureHeapSequentialRead(BenchmarkData data) {
        BenchmarkStruct[] heapArray = copyToHeap(data.values);

        long checksum = 0L;
        long start = System.nanoTime();
        for (BenchmarkStruct value : heapArray) {
            checksum += checksum(value);
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkRun(elapsed, checksum);
    }

    private BenchmarkRun measureArrayListSequentialRead(BenchmarkData data) {
        ArrayList<BenchmarkStruct> arrayList = copyToArrayList(data.values);

        long checksum = 0L;
        long start = System.nanoTime();
        for (BenchmarkStruct value : arrayList) {
            checksum += checksum(value);
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkRun(elapsed, checksum);
    }

    private BenchmarkRun measureNativeRandomRead(BenchmarkData data) {
        NativeObjectArray<BenchmarkStruct> nativeArray = new NativeObjectArray<>(data.values.length, structMemory);
        try {
            for (int i = 0; i < data.values.length; i++) {
                nativeArray.set(i, data.values[i]);
            }

            BenchmarkStruct cursor = new BenchmarkStruct();
            long checksum = 0L;
            long start = System.nanoTime();
            for (int index : data.randomIndexes) {
                nativeArray.get(index, cursor);
                checksum += checksum(cursor);
            }
            long elapsed = System.nanoTime() - start;
            return new BenchmarkRun(elapsed, checksum);
        } finally {
            nativeArray.freeMemory();
        }
    }

    private BenchmarkRun measureHeapRandomRead(BenchmarkData data) {
        BenchmarkStruct[] heapArray = copyToHeap(data.values);

        long checksum = 0L;
        long start = System.nanoTime();
        for (int index : data.randomIndexes) {
            checksum += checksum(heapArray[index]);
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkRun(elapsed, checksum);
    }

    private BenchmarkRun measureArrayListRandomRead(BenchmarkData data) {
        ArrayList<BenchmarkStruct> arrayList = copyToArrayList(data.values);

        long checksum = 0L;
        long start = System.nanoTime();
        for (int index : data.randomIndexes) {
            checksum += checksum(arrayList.get(index));
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkRun(elapsed, checksum);
    }

    private BenchmarkStruct[] copyToHeap(BenchmarkStruct[] source) {
        BenchmarkStruct[] heapArray = new BenchmarkStruct[source.length];
        for (int i = 0; i < source.length; i++) {
            BenchmarkStruct value = source[i];
            heapArray[i] = new BenchmarkStruct(value.id, value.temp, value.humidity);
        }
        return heapArray;
    }

    private ArrayList<BenchmarkStruct> copyToArrayList(BenchmarkStruct[] source) {
        ArrayList<BenchmarkStruct> arrayList = new ArrayList<>(source.length);
        for (BenchmarkStruct value : source) {
            arrayList.add(new BenchmarkStruct(value.id, value.temp, value.humidity));
        }
        return arrayList;
    }

    private long sampleChecksum(NativeObjectArray<BenchmarkStruct> nativeArray, BenchmarkStruct cursor, int index) {
        nativeArray.get(index, cursor);
        return checksum(cursor);
    }

    private long checksum(BenchmarkStruct value) {
        long result = value.id;
        result = (31L * result) + Float.floatToIntBits(value.temp);
        result = (31L * result) + Float.floatToIntBits(value.humidity);
        return result;
    }

    private void printStats(
            String scenario,
            BenchmarkStats nativeStats,
            BenchmarkStats heapStats,
            BenchmarkStats arrayListStats,
            int operations
    ) {
        long nativeMedian = nativeStats.medianNanos();
        long heapMedian = heapStats.medianNanos();
        long arrayListMedian = arrayListStats.medianNanos();

        double nativeOpsPerSecond = operations / (nativeMedian / 1_000_000_000.0);
        double heapOpsPerSecond = operations / (heapMedian / 1_000_000_000.0);
        double arrayListOpsPerSecond = operations / (arrayListMedian / 1_000_000_000.0);
        double ratioToHeap = nativeMedian / (double) heapMedian;
        double ratioToArrayList = nativeMedian / (double) arrayListMedian;

        System.out.printf(
                "%nScenario: %s%nNativeArray median: %.3f ms (%.0f ops/s)%nHeap array median: %.3f ms (%.0f ops/s)%nArrayList median: %.3f ms (%.0f ops/s)%nNative/Heap ratio: %.2fx%nNative/ArrayList ratio: %.2fx%n",
                scenario,
                nativeMedian / 1_000_000.0,
                nativeOpsPerSecond,
                heapMedian / 1_000_000.0,
                heapOpsPerSecond,
                arrayListMedian / 1_000_000.0,
                arrayListOpsPerSecond,
                ratioToHeap,
                ratioToArrayList
        );
    }

    @FunctionalInterface
    private interface BenchmarkScenario {
        BenchmarkRun run(BenchmarkData data);
    }

    private record BenchmarkRun(long nanos, long checksum) {
    }

    private record BenchmarkStats(long[] timings, long checksum) {
        long medianNanos() {
            long[] copy = Arrays.copyOf(timings, timings.length);
            Arrays.sort(copy);
            return copy[copy.length / 2];
        }
    }

    private static final class BenchmarkData {
        private final BenchmarkStruct[] values;
        private final int[] randomIndexes;

        private BenchmarkData(BenchmarkStruct[] values, int[] randomIndexes) {
            this.values = values;
            this.randomIndexes = randomIndexes;
        }

        private static BenchmarkData create(int size, int randomReadCount) {
            BenchmarkStruct[] values = new BenchmarkStruct[size];
            Random random = new Random(42L);
            for (int i = 0; i < size; i++) {
                values[i] = new BenchmarkStruct(
                        (short) (i % Short.MAX_VALUE),
                        random.nextFloat() * 1000.0f,
                        random.nextFloat() * 1000.0f
                );
            }

            int[] randomIndexes = new int[randomReadCount];
            for (int i = 0; i < randomReadCount; i++) {
                randomIndexes[i] = random.nextInt(size);
            }
            return new BenchmarkData(values, randomIndexes);
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
        @Override
        public long sizeof() {
            return 10L;
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, BenchmarkStruct element) {
            unsafe.putShort(offset, element.id);
            unsafe.putFloat(offset + 2, element.temp);
            unsafe.putFloat(offset + 6, element.humidity);
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, BenchmarkStruct outElement) {
            outElement.id = unsafe.getShort(offset);
            outElement.temp = unsafe.getFloat(offset + 2);
            outElement.humidity = unsafe.getFloat(offset + 6);
        }
    }
}
