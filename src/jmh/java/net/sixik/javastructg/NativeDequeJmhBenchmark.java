package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.deques.NativeArrayDeque;
import net.sixik.javastructg.structs.deques.NativeIntDeque;
import net.sixik.javastructg.structs.deques.NativeLongDeque;
import net.sixik.javastructg.utils.NativeUtils;
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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import sun.misc.Unsafe;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class NativeDequeJmhBenchmark {

    @Benchmark
    public void nativeIntDequeAddLastPollFirst(IntDequeState state, Blackhole blackhole) {
        NativeIntDeque deque = new NativeIntDeque(state.expectedCapacity);
        try {
            for (int value : state.intValues) {
                deque.addLast(value);
            }
            while (!deque.isEmpty()) {
                blackhole.consume(deque.removeFirst());
            }
        } finally {
            deque.freeMemory();
        }
    }

    @Benchmark
    public void arrayDequeIntAddLastPollFirst(IntDequeState state, Blackhole blackhole) {
        ArrayDeque<Integer> deque = new ArrayDeque<>(state.expectedCapacity);
        for (Integer value : state.boxedIntValues) {
            deque.addLast(value);
        }
        while (!deque.isEmpty()) {
            blackhole.consume(deque.removeFirst());
        }
    }

    @Benchmark
    public void nativeIntDequeMixedEnds(IntDequeState state, Blackhole blackhole) {
        NativeIntDeque deque = new NativeIntDeque(state.expectedCapacity);
        try {
            for (int i = 0; i < state.intValues.length; i++) {
                int value = state.intValues[i];
                if ((i & 1) == 0) {
                    deque.addFirst(value);
                } else {
                    deque.addLast(value);
                }
            }

            while (!deque.isEmpty()) {
                if ((deque.size() & 1) == 0) {
                    blackhole.consume(deque.removeFirst());
                } else {
                    blackhole.consume(deque.removeLast());
                }
            }
        } finally {
            deque.freeMemory();
        }
    }

    @Benchmark
    public void arrayDequeIntMixedEnds(IntDequeState state, Blackhole blackhole) {
        ArrayDeque<Integer> deque = new ArrayDeque<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedIntValues.length; i++) {
            Integer value = state.boxedIntValues[i];
            if ((i & 1) == 0) {
                deque.addFirst(value);
            } else {
                deque.addLast(value);
            }
        }

        while (!deque.isEmpty()) {
            if ((deque.size() & 1) == 0) {
                blackhole.consume(deque.removeFirst());
            } else {
                blackhole.consume(deque.removeLast());
            }
        }
    }

    @Benchmark
    public void nativeLongDequeAddLastPollFirst(LongDequeState state, Blackhole blackhole) {
        NativeLongDeque deque = new NativeLongDeque(state.expectedCapacity);
        try {
            for (long value : state.longValues) {
                deque.addLast(value);
            }
            while (!deque.isEmpty()) {
                blackhole.consume(deque.removeFirst());
            }
        } finally {
            deque.freeMemory();
        }
    }

    @Benchmark
    public void arrayDequeLongAddLastPollFirst(LongDequeState state, Blackhole blackhole) {
        ArrayDeque<Long> deque = new ArrayDeque<>(state.expectedCapacity);
        for (Long value : state.boxedLongValues) {
            deque.addLast(value);
        }
        while (!deque.isEmpty()) {
            blackhole.consume(deque.removeFirst());
        }
    }

    @Benchmark
    public void nativeLongDequeMixedEnds(LongDequeState state, Blackhole blackhole) {
        NativeLongDeque deque = new NativeLongDeque(state.expectedCapacity);
        try {
            for (int i = 0; i < state.longValues.length; i++) {
                long value = state.longValues[i];
                if ((i & 1) == 0) {
                    deque.addFirst(value);
                } else {
                    deque.addLast(value);
                }
            }

            while (!deque.isEmpty()) {
                if ((deque.size() & 1) == 0) {
                    blackhole.consume(deque.removeFirst());
                } else {
                    blackhole.consume(deque.removeLast());
                }
            }
        } finally {
            deque.freeMemory();
        }
    }

    @Benchmark
    public void arrayDequeLongMixedEnds(LongDequeState state, Blackhole blackhole) {
        ArrayDeque<Long> deque = new ArrayDeque<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedLongValues.length; i++) {
            Long value = state.boxedLongValues[i];
            if ((i & 1) == 0) {
                deque.addFirst(value);
            } else {
                deque.addLast(value);
            }
        }

        while (!deque.isEmpty()) {
            if ((deque.size() & 1) == 0) {
                blackhole.consume(deque.removeFirst());
            } else {
                blackhole.consume(deque.removeLast());
            }
        }
    }

    @Benchmark
    public void nativeObjectDequeAddLastPollFirst(ObjectDequeState state, Blackhole blackhole) {
        NativeArrayDeque<Payload> deque = new NativeArrayDeque<>(state.expectedCapacity, state.payloadMemory);
        Payload out = state.outPayload;
        try {
            for (Payload value : state.payloads) {
                deque.addLast(value);
            }
            while (deque.pollFirst(out)) {
                blackhole.consume(out.id);
                blackhole.consume(out.weight);
            }
        } finally {
            deque.freeMemory();
        }
    }

    @Benchmark
    public void arrayDequeObjectAddLastPollFirst(ObjectDequeState state, Blackhole blackhole) {
        ArrayDeque<Payload> deque = new ArrayDeque<>(state.expectedCapacity);
        for (Payload value : state.payloads) {
            deque.addLast(value);
        }
        while (!deque.isEmpty()) {
            Payload value = deque.removeFirst();
            blackhole.consume(value.id);
            blackhole.consume(value.weight);
        }
    }

    @Benchmark
    public void nativeObjectDequeAddFirstPollLast(ObjectDequeState state, Blackhole blackhole) {
        NativeArrayDeque<Payload> deque = new NativeArrayDeque<>(state.expectedCapacity, state.payloadMemory);
        Payload out = state.outPayload;
        try {
            for (Payload value : state.payloads) {
                deque.addFirst(value);
            }
            while (deque.pollLast(out)) {
                blackhole.consume(out.id);
                blackhole.consume(out.weight);
            }
        } finally {
            deque.freeMemory();
        }
    }

    @Benchmark
    public void arrayDequeObjectAddFirstPollLast(ObjectDequeState state, Blackhole blackhole) {
        ArrayDeque<Payload> deque = new ArrayDeque<>(state.expectedCapacity);
        for (Payload value : state.payloads) {
            deque.addFirst(value);
        }
        while (!deque.isEmpty()) {
            Payload value = deque.removeLast();
            blackhole.consume(value.id);
            blackhole.consume(value.weight);
        }
    }

    @State(Scope.Thread)
    public static class IntDequeState {
        @Param({"1024", "65536"})
        public int size;

        private int[] intValues;
        private Integer[] boxedIntValues;
        private int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            intValues = new int[size];
            boxedIntValues = new Integer[size];
            for (int i = 0; i < size; i++) {
                int value = NativeUtils.mix(60_061 + i);
                intValues[i] = value;
                boxedIntValues[i] = value;
            }
            expectedCapacity = size;
        }
    }

    @State(Scope.Thread)
    public static class LongDequeState {
        @Param({"1024", "65536"})
        public int size;

        private long[] longValues;
        private Long[] boxedLongValues;
        private int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            longValues = new long[size];
            boxedLongValues = new Long[size];
            for (int i = 0; i < size; i++) {
                long value = NativeUtils.mix(61_061L + i);
                longValues[i] = value;
                boxedLongValues[i] = value;
            }
            expectedCapacity = size;
        }
    }

    @State(Scope.Thread)
    public static class ObjectDequeState {
        @Param({"1024", "65536"})
        public int size;

        private final PayloadMemory payloadMemory = new PayloadMemory();
        private final Payload outPayload = new Payload();
        private Payload[] payloads;
        private int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            payloads = new Payload[size];
            for (int i = 0; i < size; i++) {
                payloads[i] = new Payload(i, NativeUtils.mix(67_067L + i));
            }
            expectedCapacity = size;
        }
    }

    private static final class Payload {
        private int id;
        private long weight;

        private Payload() {
        }

        private Payload(int id, long weight) {
            this.id = id;
            this.weight = weight;
        }
    }

    private static final class PayloadMemory implements NativeTypeMemory<Payload> {
        private static final long ID_OFFSET = 0L;
        private static final long WEIGHT_OFFSET = 8L;
        private static final long SIZE = 16L;

        @Override
        public long sizeof() {
            return SIZE;
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Payload outElement) {
            outElement.id = unsafe.getInt(offset + ID_OFFSET);
            outElement.weight = unsafe.getLong(offset + WEIGHT_OFFSET);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Payload element) {
            unsafe.putInt(offset + ID_OFFSET, element.id);
            unsafe.putLong(offset + WEIGHT_OFFSET, element.weight);
        }
    }
}
