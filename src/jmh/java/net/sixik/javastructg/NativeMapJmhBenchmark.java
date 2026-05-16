package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.maps.NativeLong2ByteMap;
import net.sixik.javastructg.structs.maps.NativeLong2LongMap;
import net.sixik.javastructg.structs.maps.NativeLong2ObjectMap;
import net.sixik.javastructg.structs.maps.NativeObject2LongMap;
import net.sixik.javastructg.structs.maps.NativeObject2ObjectMap;
import net.sixik.javastructg.structs.maps.Object2NativeMap;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import sun.misc.Unsafe;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class NativeMapJmhBenchmark {

    @Benchmark
    public void nativeLong2LongPut(LongMapAddState state, Blackhole blackhole) {
        NativeLong2LongMap map = new NativeLong2LongMap(state.expectedCapacity);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.longValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapLong2LongPut(LongMapAddState state, Blackhole blackhole) {
        HashMap<Long, Long> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedKeys.length; i++) {
            blackhole.consume(map.put(state.boxedKeys[i], state.boxedLongValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeLong2LongGetHit(LongMapQueryState state, Blackhole blackhole) {
        for (long key : state.hitKeys) {
            blackhole.consume(state.nativeLong2LongMap.get(key, Long.MIN_VALUE));
        }
    }

    @Benchmark
    public void hashMapLong2LongGetHit(LongMapQueryState state, Blackhole blackhole) {
        for (Long key : state.boxedHitKeys) {
            blackhole.consume(state.hashMapLong2Long.get(key));
        }
    }

    @Benchmark
    public void nativeLong2LongGetMiss(LongMapQueryState state, Blackhole blackhole) {
        for (long key : state.missKeys) {
            blackhole.consume(state.nativeLong2LongMap.get(key, Long.MIN_VALUE));
        }
    }

    @Benchmark
    public void hashMapLong2LongGetMiss(LongMapQueryState state, Blackhole blackhole) {
        for (Long key : state.boxedMissKeys) {
            blackhole.consume(state.hashMapLong2Long.get(key));
        }
    }

    @Benchmark
    public void nativeLong2LongRemove(LongMapRemoveState state, Blackhole blackhole) {
        for (long key : state.hitKeys) {
            blackhole.consume(state.nativeLong2LongMap.remove(key));
        }
        blackhole.consume(state.nativeLong2LongMap.size());
    }

    @Benchmark
    public void hashMapLong2LongRemove(LongMapRemoveState state, Blackhole blackhole) {
        for (Long key : state.boxedHitKeys) {
            blackhole.consume(state.hashMapLong2Long.remove(key));
        }
        blackhole.consume(state.hashMapLong2Long.size());
    }

    @Benchmark
    public void nativeLong2BytePut(LongMapAddState state, Blackhole blackhole) {
        NativeLong2ByteMap map = new NativeLong2ByteMap(state.expectedCapacity);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.byteValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapLong2BytePut(LongMapAddState state, Blackhole blackhole) {
        HashMap<Long, Byte> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedKeys.length; i++) {
            blackhole.consume(map.put(state.boxedKeys[i], state.boxedByteValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeLong2ByteGetHit(LongMapQueryState state, Blackhole blackhole) {
        for (long key : state.hitKeys) {
            blackhole.consume(state.nativeLong2ByteMap.get(key, (byte) -1));
        }
    }

    @Benchmark
    public void hashMapLong2ByteGetHit(LongMapQueryState state, Blackhole blackhole) {
        for (Long key : state.boxedHitKeys) {
            blackhole.consume(state.hashMapLong2Byte.get(key));
        }
    }

    @Benchmark
    public void nativeLong2ObjectPut(LongObjectMapAddState state, Blackhole blackhole) {
        NativeLong2ObjectMap<Payload> map = new NativeLong2ObjectMap<>(state.expectedCapacity, state.payloadMemory);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.payloads[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapLong2ObjectPut(LongObjectMapAddState state, Blackhole blackhole) {
        HashMap<Long, Payload> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedKeys.length; i++) {
            blackhole.consume(map.put(state.boxedKeys[i], state.payloads[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeLong2ObjectGetHit(LongObjectMapQueryState state, Blackhole blackhole) {
        Payload out = state.outPayload;
        for (long key : state.hitKeys) {
            boolean found = state.nativeLong2ObjectMap.get(key, out);
            blackhole.consume(found);
            blackhole.consume(out.id);
            blackhole.consume(out.weight);
        }
    }

    @Benchmark
    public void hashMapLong2ObjectGetHit(LongObjectMapQueryState state, Blackhole blackhole) {
        for (Long key : state.boxedHitKeys) {
            Payload value = state.hashMapLong2Object.get(key);
            blackhole.consume(value.id);
            blackhole.consume(value.weight);
        }
    }

    @Benchmark
    public void nativeObject2LongPut(ObjectMapAddState state, Blackhole blackhole) {
        NativeObject2LongMap<MapKey> map = new NativeObject2LongMap<>(state.expectedCapacity, state.keyMemory);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.longValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void nativeObject2LongPutPrehashed(ObjectMapAddState state, Blackhole blackhole) {
        NativeObject2LongMap<MapKey> map = new NativeObject2LongMap<>(state.expectedCapacity, state.keyMemory);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.putPrehashed(state.keys[i], state.keyHashes[i], state.longValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapObject2LongPut(ObjectMapAddState state, Blackhole blackhole) {
        HashMap<MapKey, Long> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.keys.length; i++) {
            blackhole.consume(map.put(state.keys[i], state.boxedLongValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeObject2LongGetHit(ObjectMapQueryState state, Blackhole blackhole) {
        for (MapKey key : state.hitKeys) {
            blackhole.consume(state.nativeObject2LongMap.get(key, Long.MIN_VALUE));
        }
    }

    @Benchmark
    public void nativeObject2LongGetHitPrehashed(ObjectMapQueryState state, Blackhole blackhole) {
        for (int i = 0; i < state.hitKeys.length; i++) {
            blackhole.consume(state.nativeObject2LongMap.getPrehashed(state.hitKeys[i], state.hitKeyHashes[i], Long.MIN_VALUE));
        }
    }

    @Benchmark
    public void hashMapObject2LongGetHit(ObjectMapQueryState state, Blackhole blackhole) {
        for (MapKey key : state.hitKeys) {
            blackhole.consume(state.hashMapObject2Long.get(key));
        }
    }

    @Benchmark
    public void nativeObject2LongGetMissPrehashed(ObjectMapQueryState state, Blackhole blackhole) {
        for (int i = 0; i < state.missKeys.length; i++) {
            blackhole.consume(state.nativeObject2LongMap.getPrehashed(state.missKeys[i], state.missKeyHashes[i], Long.MIN_VALUE));
        }
    }

    @Benchmark
    public void hashMapObject2LongGetMiss(ObjectMapQueryState state, Blackhole blackhole) {
        for (MapKey key : state.missKeys) {
            blackhole.consume(state.hashMapObject2Long.get(key));
        }
    }

    @Benchmark
    public void nativeObject2ObjectPut(ObjectMapAddState state, Blackhole blackhole) {
        NativeObject2ObjectMap<MapKey, Payload> map = new NativeObject2ObjectMap<>(
                state.expectedCapacity,
                state.keyMemory,
                state.payloadMemory
        );
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.payloads[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void nativeObject2ObjectPutPrehashed(ObjectMapAddState state, Blackhole blackhole) {
        NativeObject2ObjectMap<MapKey, Payload> map = new NativeObject2ObjectMap<>(
                state.expectedCapacity,
                state.keyMemory,
                state.payloadMemory
        );
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.putPrehashed(state.keys[i], state.keyHashes[i], state.payloads[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapObject2ObjectPut(ObjectMapAddState state, Blackhole blackhole) {
        HashMap<MapKey, Payload> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.keys.length; i++) {
            blackhole.consume(map.put(state.keys[i], state.payloads[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeObject2ObjectGetHit(ObjectMapQueryState state, Blackhole blackhole) {
        Payload out = state.outPayload;
        for (MapKey key : state.hitKeys) {
            boolean found = state.nativeObject2ObjectMap.get(key, out);
            blackhole.consume(found);
            blackhole.consume(out.id);
            blackhole.consume(out.weight);
        }
    }

    @Benchmark
    public void nativeObject2ObjectGetHitPrehashed(ObjectMapQueryState state, Blackhole blackhole) {
        Payload out = state.outPayload;
        for (int i = 0; i < state.hitKeys.length; i++) {
            boolean found = state.nativeObject2ObjectMap.getPrehashed(state.hitKeys[i], state.hitKeyHashes[i], out);
            blackhole.consume(found);
            blackhole.consume(out.id);
            blackhole.consume(out.weight);
        }
    }

    @Benchmark
    public void hashMapObject2ObjectGetHit(ObjectMapQueryState state, Blackhole blackhole) {
        for (MapKey key : state.hitKeys) {
            Payload value = state.hashMapObject2Object.get(key);
            blackhole.consume(value.id);
            blackhole.consume(value.weight);
        }
    }

    @Benchmark
    public void object2NativePut(ObjectMapAddState state, Blackhole blackhole) {
        Object2NativeMap<MapKey, Payload> map = new Object2NativeMap<>(state.expectedCapacity, state.payloadMemory);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.payloads[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void object2NativePutPrehashed(ObjectMapAddState state, Blackhole blackhole) {
        Object2NativeMap<MapKey, Payload> map = new Object2NativeMap<>(state.expectedCapacity, state.payloadMemory);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.putPrehashed(state.keys[i], state.keyHashes[i], state.payloads[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void object2NativeGetHit(ObjectMapQueryState state, Blackhole blackhole) {
        Payload out = state.outPayload;
        for (MapKey key : state.hitKeys) {
            boolean found = state.object2NativeMap.get(key, out);
            blackhole.consume(found);
            blackhole.consume(out.id);
            blackhole.consume(out.weight);
        }
    }

    @Benchmark
    public void object2NativeGetHitPrehashed(ObjectMapQueryState state, Blackhole blackhole) {
        Payload out = state.outPayload;
        for (int i = 0; i < state.hitKeys.length; i++) {
            boolean found = state.object2NativeMap.getPrehashed(state.hitKeys[i], state.hitKeyHashes[i], out);
            blackhole.consume(found);
            blackhole.consume(out.id);
            blackhole.consume(out.weight);
        }
    }

    @State(Scope.Thread)
    public static class LongMapAddState extends LongMapStateBase {
    }

    @State(Scope.Thread)
    public static class LongMapQueryState extends LongMapStateBase {
        private NativeLong2LongMap nativeLong2LongMap;
        private NativeLong2ByteMap nativeLong2ByteMap;
        private HashMap<Long, Long> hashMapLong2Long;
        private HashMap<Long, Byte> hashMapLong2Byte;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeLong2LongMap = new NativeLong2LongMap(expectedCapacity);
            nativeLong2ByteMap = new NativeLong2ByteMap(expectedCapacity);
            hashMapLong2Long = new HashMap<>(expectedCapacity);
            hashMapLong2Byte = new HashMap<>(expectedCapacity);

            for (int i = 0; i < keys.length; i++) {
                nativeLong2LongMap.put(keys[i], longValues[i]);
                nativeLong2ByteMap.put(keys[i], byteValues[i]);
                hashMapLong2Long.put(boxedKeys[i], boxedLongValues[i]);
                hashMapLong2Byte.put(boxedKeys[i], boxedByteValues[i]);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeLong2LongMap != null) {
                nativeLong2LongMap.freeMemory();
                nativeLong2LongMap = null;
            }
            if (nativeLong2ByteMap != null) {
                nativeLong2ByteMap.freeMemory();
                nativeLong2ByteMap = null;
            }
            hashMapLong2Long = null;
            hashMapLong2Byte = null;
        }
    }

    @State(Scope.Thread)
    public static class LongMapRemoveState extends LongMapStateBase {
        private NativeLong2LongMap nativeLong2LongMap;
        private HashMap<Long, Long> hashMapLong2Long;

        @Setup(Level.Invocation)
        public void setupInvocation() {
            nativeLong2LongMap = new NativeLong2LongMap(expectedCapacity);
            hashMapLong2Long = new HashMap<>(expectedCapacity);

            for (int i = 0; i < keys.length; i++) {
                nativeLong2LongMap.put(keys[i], longValues[i]);
                hashMapLong2Long.put(boxedKeys[i], boxedLongValues[i]);
            }
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            if (nativeLong2LongMap != null) {
                nativeLong2LongMap.freeMemory();
                nativeLong2LongMap = null;
            }
            hashMapLong2Long = null;
        }
    }

    @State(Scope.Thread)
    public static class LongObjectMapAddState extends LongMapStateBase {
        private final PayloadMemory payloadMemory = new PayloadMemory();
    }

    @State(Scope.Thread)
    public static class LongObjectMapQueryState extends LongMapStateBase {
        private final PayloadMemory payloadMemory = new PayloadMemory();
        private final Payload outPayload = new Payload();
        private NativeLong2ObjectMap<Payload> nativeLong2ObjectMap;
        private HashMap<Long, Payload> hashMapLong2Object;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeLong2ObjectMap = new NativeLong2ObjectMap<>(expectedCapacity, payloadMemory);
            hashMapLong2Object = new HashMap<>(expectedCapacity);

            for (int i = 0; i < keys.length; i++) {
                nativeLong2ObjectMap.put(keys[i], payloads[i]);
                hashMapLong2Object.put(boxedKeys[i], payloads[i]);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeLong2ObjectMap != null) {
                nativeLong2ObjectMap.freeMemory();
                nativeLong2ObjectMap = null;
            }
            hashMapLong2Object = null;
        }
    }

    @State(Scope.Thread)
    public abstract static class LongMapStateBase {
        @Param({"1024", "65536"})
        public int size;

        protected long[] keys;
        protected long[] hitKeys;
        protected long[] missKeys;
        protected long[] longValues;
        protected byte[] byteValues;
        protected Payload[] payloads;
        protected Long[] boxedKeys;
        protected Long[] boxedHitKeys;
        protected Long[] boxedMissKeys;
        protected Long[] boxedLongValues;
        protected Byte[] boxedByteValues;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            keys = createUniqueLongKeys(size, 11_003L);
            hitKeys = shuffledCopy(keys, 17_171L);
            missKeys = createMissLongKeys(keys, 23_323L);
            longValues = createLongValues(size, 31_337L);
            byteValues = createByteValues(size);
            payloads = createPayloads(size);
            boxedKeys = box(keys);
            boxedHitKeys = box(hitKeys);
            boxedMissKeys = box(missKeys);
            boxedLongValues = box(longValues);
            boxedByteValues = box(byteValues);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class ObjectMapAddState extends ObjectMapStateBase {
    }

    @State(Scope.Thread)
    public static class ObjectMapQueryState extends ObjectMapStateBase {
        private final Payload outPayload = new Payload();
        private NativeObject2LongMap<MapKey> nativeObject2LongMap;
        private NativeObject2ObjectMap<MapKey, Payload> nativeObject2ObjectMap;
        private Object2NativeMap<MapKey, Payload> object2NativeMap;
        private HashMap<MapKey, Long> hashMapObject2Long;
        private HashMap<MapKey, Payload> hashMapObject2Object;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeObject2LongMap = new NativeObject2LongMap<>(expectedCapacity, keyMemory);
            nativeObject2ObjectMap = new NativeObject2ObjectMap<>(expectedCapacity, keyMemory, payloadMemory);
            object2NativeMap = new Object2NativeMap<>(expectedCapacity, payloadMemory);
            hashMapObject2Long = new HashMap<>(expectedCapacity);
            hashMapObject2Object = new HashMap<>(expectedCapacity);

            for (int i = 0; i < keys.length; i++) {
                nativeObject2LongMap.putPrehashed(keys[i], keyHashes[i], longValues[i]);
                nativeObject2ObjectMap.putPrehashed(keys[i], keyHashes[i], payloads[i]);
                object2NativeMap.putPrehashed(keys[i], keyHashes[i], payloads[i]);
                hashMapObject2Long.put(keys[i], boxedLongValues[i]);
                hashMapObject2Object.put(keys[i], payloads[i]);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeObject2LongMap != null) {
                nativeObject2LongMap.freeMemory();
                nativeObject2LongMap = null;
            }
            if (nativeObject2ObjectMap != null) {
                nativeObject2ObjectMap.freeMemory();
                nativeObject2ObjectMap = null;
            }
            if (object2NativeMap != null) {
                object2NativeMap.freeMemory();
                object2NativeMap = null;
            }
            hashMapObject2Long = null;
            hashMapObject2Object = null;
        }
    }

    @State(Scope.Thread)
    public abstract static class ObjectMapStateBase {
        @Param({"1024", "65536"})
        public int size;

        protected final MapKeyMemory keyMemory = new MapKeyMemory();
        protected final PayloadMemory payloadMemory = new PayloadMemory();
        protected MapKey[] keys;
        protected MapKey[] hitKeys;
        protected MapKey[] missKeys;
        protected long[] keyHashes;
        protected long[] hitKeyHashes;
        protected long[] missKeyHashes;
        protected long[] longValues;
        protected Long[] boxedLongValues;
        protected Payload[] payloads;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            keys = createObjectKeys(size, 41_441);
            hitKeys = copyAndShuffleKeys(keys, 43_443L);
            missKeys = createMissKeys(keys);
            keyHashes = hashKeys(keys, keyMemory);
            hitKeyHashes = hashKeys(hitKeys, keyMemory);
            missKeyHashes = hashKeys(missKeys, keyMemory);
            longValues = createLongValues(size, 47_447L);
            boxedLongValues = box(longValues);
            payloads = createPayloads(size);
            expectedCapacity = size << 1;
        }
    }

    private static long[] createUniqueLongKeys(int size, long seed) {
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            values[i] = seed + (0x9E3779B97F4A7C15L * i);
        }
        return values;
    }

    private static long[] createLongValues(int size, long seed) {
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            values[i] = NativeUtils.mix(seed + i);
        }
        return values;
    }

    private static byte[] createByteValues(int size) {
        byte[] values = new byte[size];
        for (int i = 0; i < size; i++) {
            values[i] = (byte) (i * 31);
        }
        return values;
    }

    private static Payload[] createPayloads(int size) {
        Payload[] values = new Payload[size];
        for (int i = 0; i < size; i++) {
            values[i] = new Payload(i, NativeUtils.mix(59_059L + i));
        }
        return values;
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

    private static long[] createMissLongKeys(long[] source, long seed) {
        long[] misses = new long[source.length];
        for (int i = 0; i < source.length; i++) {
            misses[i] = ~(source[i] + seed);
        }
        return misses;
    }

    private static Long[] box(long[] source) {
        Long[] boxed = new Long[source.length];
        for (int i = 0; i < source.length; i++) {
            boxed[i] = source[i];
        }
        return boxed;
    }

    private static Byte[] box(byte[] source) {
        Byte[] boxed = new Byte[source.length];
        for (int i = 0; i < source.length; i++) {
            boxed[i] = source[i];
        }
        return boxed;
    }

    private static MapKey[] createObjectKeys(int size, int seed) {
        MapKey[] values = new MapKey[size];
        for (int i = 0; i < size; i++) {
            int v = seed + i;
            values[i] = new MapKey(v, v * 31, ~v, i & 3);
        }
        return values;
    }

    private static MapKey[] copyAndShuffleKeys(MapKey[] source, long seed) {
        MapKey[] copy = new MapKey[source.length];
        for (int i = 0; i < source.length; i++) {
            MapKey key = source[i];
            copy[i] = new MapKey(key.x, key.y, key.z, key.dimension);
        }

        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            MapKey tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static MapKey[] createMissKeys(MapKey[] source) {
        MapKey[] misses = new MapKey[source.length];
        for (int i = 0; i < source.length; i++) {
            MapKey key = source[i];
            misses[i] = new MapKey(key.x, key.y, key.z + 1, key.dimension);
        }
        return misses;
    }

    private static long[] hashKeys(MapKey[] keys, MapKeyMemory memory) {
        long[] hashes = new long[keys.length];
        for (int i = 0; i < keys.length; i++) {
            hashes[i] = memory.hash(keys[i]);
        }
        return hashes;
    }

    private static final class MapKey {
        private int x;
        private int y;
        private int z;
        private int dimension;

        private MapKey() {
        }

        private MapKey(int x, int y, int z, int dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            result = 31 * result + dimension;
            return result;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MapKey key)) {
                return false;
            }
            return x == key.x && y == key.y && z == key.z && dimension == key.dimension;
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

        @Override
        public int hashCode() {
            return Objects.hash(id, weight);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Payload payload)) {
                return false;
            }
            return id == payload.id && weight == payload.weight;
        }
    }

    private static final class MapKeyMemory implements NativeTypeMemory<MapKey> {
        private static final long X_OFFSET = 0L;
        private static final long Y_OFFSET = 4L;
        private static final long Z_OFFSET = 8L;
        private static final long DIMENSION_OFFSET = 12L;
        private static final long SIZE = 16L;

        @Override
        public long sizeof() {
            return SIZE;
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, MapKey outElement) {
            outElement.x = unsafe.getInt(offset + X_OFFSET);
            outElement.y = unsafe.getInt(offset + Y_OFFSET);
            outElement.z = unsafe.getInt(offset + Z_OFFSET);
            outElement.dimension = unsafe.getInt(offset + DIMENSION_OFFSET);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, MapKey element) {
            unsafe.putInt(offset + X_OFFSET, element.x);
            unsafe.putInt(offset + Y_OFFSET, element.y);
            unsafe.putInt(offset + Z_OFFSET, element.z);
            unsafe.putInt(offset + DIMENSION_OFFSET, element.dimension);
        }

        @Override
        public long hash(MapKey element) {
            int result = element.x;
            result = 31 * result + element.y;
            result = 31 * result + element.z;
            result = 31 * result + element.dimension;
            return result;
        }

        @Override
        public boolean supportsEqualsMemory() {
            return true;
        }

        @Override
        public boolean equalsMemory(Unsafe unsafe, long offset, MapKey value) {
            return unsafe.getInt(offset + X_OFFSET) == value.x
                    && unsafe.getInt(offset + Y_OFFSET) == value.y
                    && unsafe.getInt(offset + Z_OFFSET) == value.z
                    && unsafe.getInt(offset + DIMENSION_OFFSET) == value.dimension;
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
