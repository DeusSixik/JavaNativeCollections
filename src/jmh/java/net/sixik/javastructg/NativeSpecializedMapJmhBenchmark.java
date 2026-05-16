package net.sixik.javastructg;

import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.maps.NativeIdentity2BooleanMap;
import net.sixik.javastructg.structs.maps.NativeIdentity2ByteMap;
import net.sixik.javastructg.structs.maps.NativeIdentity2IntMap;
import net.sixik.javastructg.structs.maps.NativeInt2IntMap;
import net.sixik.javastructg.structs.maps.NativeInt2LongMap;
import net.sixik.javastructg.structs.maps.NativeInt2ObjectMap;
import net.sixik.javastructg.structs.maps.NativeLong2BooleanMap;
import net.sixik.javastructg.structs.maps.NativeObject2ByteMap;
import net.sixik.javastructg.structs.maps.NativeObject2IntMap;
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
import java.util.IdentityHashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class NativeSpecializedMapJmhBenchmark {

    @Benchmark
    public void nativeInt2IntPut(IntMapAddState state, Blackhole blackhole) {
        NativeInt2IntMap map = new NativeInt2IntMap(state.expectedCapacity);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.intValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapInt2IntPut(IntMapAddState state, Blackhole blackhole) {
        HashMap<Integer, Integer> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedKeys.length; i++) {
            blackhole.consume(map.put(state.boxedKeys[i], state.boxedIntValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeInt2IntGetHit(IntMapQueryState state, Blackhole blackhole) {
        for (int key : state.hitKeys) {
            blackhole.consume(state.nativeInt2IntMap.get(key, Integer.MIN_VALUE));
        }
    }

    @Benchmark
    public void hashMapInt2IntGetHit(IntMapQueryState state, Blackhole blackhole) {
        for (Integer key : state.boxedHitKeys) {
            blackhole.consume(state.hashMapInt2Int.get(key).intValue());
        }
    }

    @Benchmark
    public void nativeInt2LongPut(IntMapAddState state, Blackhole blackhole) {
        NativeInt2LongMap map = new NativeInt2LongMap(state.expectedCapacity);
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
    public void hashMapInt2LongPut(IntMapAddState state, Blackhole blackhole) {
        HashMap<Integer, Long> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedKeys.length; i++) {
            blackhole.consume(map.put(state.boxedKeys[i], state.boxedLongValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeInt2LongGetHit(IntMapQueryState state, Blackhole blackhole) {
        for (int key : state.hitKeys) {
            blackhole.consume(state.nativeInt2LongMap.get(key, Long.MIN_VALUE));
        }
    }

    @Benchmark
    public void hashMapInt2LongGetHit(IntMapQueryState state, Blackhole blackhole) {
        for (Integer key : state.boxedHitKeys) {
            blackhole.consume(state.hashMapInt2Long.get(key).longValue());
        }
    }

    @Benchmark
    public void nativeInt2ObjectPut(IntMapAddState state, Blackhole blackhole) {
        NativeInt2ObjectMap<Payload> map = new NativeInt2ObjectMap<>(state.expectedCapacity, state.payloadMemory);
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
    public void hashMapInt2ObjectPut(IntMapAddState state, Blackhole blackhole) {
        HashMap<Integer, Payload> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedKeys.length; i++) {
            blackhole.consume(map.put(state.boxedKeys[i], state.payloads[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeInt2ObjectGetHit(IntMapQueryState state, Blackhole blackhole) {
        Payload out = state.outPayload;
        for (int key : state.hitKeys) {
            boolean found = state.nativeInt2ObjectMap.get(key, out);
            blackhole.consume(found);
            blackhole.consume(out.id);
            blackhole.consume(out.weight);
        }
    }

    @Benchmark
    public void hashMapInt2ObjectGetHit(IntMapQueryState state, Blackhole blackhole) {
        for (Integer key : state.boxedHitKeys) {
            Payload value = state.hashMapInt2Object.get(key);
            blackhole.consume(value.id);
            blackhole.consume(value.weight);
        }
    }

    @Benchmark
    public void nativeLong2BooleanPut(LongBooleanMapAddState state, Blackhole blackhole) {
        NativeLong2BooleanMap map = new NativeLong2BooleanMap(state.expectedCapacity);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.booleanValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapLong2BooleanPut(LongBooleanMapAddState state, Blackhole blackhole) {
        HashMap<Long, Boolean> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.boxedKeys.length; i++) {
            blackhole.consume(map.put(state.boxedKeys[i], state.boxedBooleanValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeLong2BooleanGetHit(LongBooleanMapQueryState state, Blackhole blackhole) {
        for (long key : state.hitKeys) {
            blackhole.consume(state.nativeLong2BooleanMap.get(key, false));
        }
    }

    @Benchmark
    public void hashMapLong2BooleanGetHit(LongBooleanMapQueryState state, Blackhole blackhole) {
        for (Long key : state.boxedHitKeys) {
            blackhole.consume(state.hashMapLong2Boolean.get(key).booleanValue());
        }
    }

    @Benchmark
    public void nativeObject2IntPutPrehashed(ObjectPrimitiveMapAddState state, Blackhole blackhole) {
        NativeObject2IntMap<MapKey> map = new NativeObject2IntMap<>(state.expectedCapacity, state.keyMemory);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.putPrehashed(state.keys[i], state.keyHashes[i], state.intValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapObject2IntPut(ObjectPrimitiveMapAddState state, Blackhole blackhole) {
        HashMap<MapKey, Integer> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.keys.length; i++) {
            blackhole.consume(map.put(state.keys[i], state.boxedIntValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeObject2IntGetHitPrehashed(ObjectPrimitiveMapQueryState state, Blackhole blackhole) {
        for (int i = 0; i < state.hitKeys.length; i++) {
            blackhole.consume(state.nativeObject2IntMap.getPrehashed(state.hitKeys[i], state.hitKeyHashes[i], Integer.MIN_VALUE));
        }
    }

    @Benchmark
    public void hashMapObject2IntGetHit(ObjectPrimitiveMapQueryState state, Blackhole blackhole) {
        for (MapKey key : state.hitKeys) {
            blackhole.consume(state.hashMapObject2Int.get(key).intValue());
        }
    }

    @Benchmark
    public void nativeObject2BytePutPrehashed(ObjectPrimitiveMapAddState state, Blackhole blackhole) {
        NativeObject2ByteMap<MapKey> map = new NativeObject2ByteMap<>(state.expectedCapacity, state.keyMemory);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.putPrehashed(state.keys[i], state.keyHashes[i], state.byteValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void hashMapObject2BytePut(ObjectPrimitiveMapAddState state, Blackhole blackhole) {
        HashMap<MapKey, Byte> map = new HashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.keys.length; i++) {
            blackhole.consume(map.put(state.keys[i], state.boxedByteValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeObject2ByteGetHitPrehashed(ObjectPrimitiveMapQueryState state, Blackhole blackhole) {
        for (int i = 0; i < state.hitKeys.length; i++) {
            blackhole.consume(state.nativeObject2ByteMap.getPrehashed(state.hitKeys[i], state.hitKeyHashes[i], (byte) -1));
        }
    }

    @Benchmark
    public void hashMapObject2ByteGetHit(ObjectPrimitiveMapQueryState state, Blackhole blackhole) {
        for (MapKey key : state.hitKeys) {
            blackhole.consume(state.hashMapObject2Byte.get(key).byteValue());
        }
    }

    @Benchmark
    public void nativeIdentity2IntPut(IdentityMapAddState state, Blackhole blackhole) {
        NativeIdentity2IntMap<IdentityKey> map = new NativeIdentity2IntMap<>(state.expectedCapacity);
        try {
            for (int i = 0; i < state.keys.length; i++) {
                blackhole.consume(map.put(state.keys[i], state.intValues[i]));
            }
            blackhole.consume(map.size());
        } finally {
            map.freeMemory();
        }
    }

    @Benchmark
    public void identityHashMapIntPut(IdentityMapAddState state, Blackhole blackhole) {
        IdentityHashMap<IdentityKey, Integer> map = new IdentityHashMap<>(state.expectedCapacity);
        for (int i = 0; i < state.keys.length; i++) {
            blackhole.consume(map.put(state.keys[i], state.boxedIntValues[i]));
        }
        blackhole.consume(map.size());
    }

    @Benchmark
    public void nativeIdentity2IntGetHit(IdentityMapQueryState state, Blackhole blackhole) {
        for (IdentityKey key : state.hitKeys) {
            blackhole.consume(state.nativeIdentity2IntMap.get(key, Integer.MIN_VALUE));
        }
    }

    @Benchmark
    public void identityHashMapIntGetHit(IdentityMapQueryState state, Blackhole blackhole) {
        for (IdentityKey key : state.hitKeys) {
            blackhole.consume(state.identityHashMapInt.get(key).intValue());
        }
    }

    @Benchmark
    public void nativeIdentity2ByteGetHit(IdentityMapQueryState state, Blackhole blackhole) {
        for (IdentityKey key : state.hitKeys) {
            blackhole.consume(state.nativeIdentity2ByteMap.get(key, (byte) -1));
        }
    }

    @Benchmark
    public void identityHashMapByteGetHit(IdentityMapQueryState state, Blackhole blackhole) {
        for (IdentityKey key : state.hitKeys) {
            blackhole.consume(state.identityHashMapByte.get(key).byteValue());
        }
    }

    @Benchmark
    public void nativeIdentity2BooleanGetHit(IdentityMapQueryState state, Blackhole blackhole) {
        for (IdentityKey key : state.hitKeys) {
            blackhole.consume(state.nativeIdentity2BooleanMap.get(key, false));
        }
    }

    @Benchmark
    public void identityHashMapBooleanGetHit(IdentityMapQueryState state, Blackhole blackhole) {
        for (IdentityKey key : state.hitKeys) {
            blackhole.consume(state.identityHashMapBoolean.get(key).booleanValue());
        }
    }

    @State(Scope.Thread)
    public static class IntMapAddState extends IntMapStateBase {
    }

    @State(Scope.Thread)
    public static class IntMapQueryState extends IntMapStateBase {
        private final Payload outPayload = new Payload();
        private NativeInt2IntMap nativeInt2IntMap;
        private NativeInt2LongMap nativeInt2LongMap;
        private NativeInt2ObjectMap<Payload> nativeInt2ObjectMap;
        private HashMap<Integer, Integer> hashMapInt2Int;
        private HashMap<Integer, Long> hashMapInt2Long;
        private HashMap<Integer, Payload> hashMapInt2Object;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeInt2IntMap = new NativeInt2IntMap(expectedCapacity);
            nativeInt2LongMap = new NativeInt2LongMap(expectedCapacity);
            nativeInt2ObjectMap = new NativeInt2ObjectMap<>(expectedCapacity, payloadMemory);
            hashMapInt2Int = new HashMap<>(expectedCapacity);
            hashMapInt2Long = new HashMap<>(expectedCapacity);
            hashMapInt2Object = new HashMap<>(expectedCapacity);

            for (int i = 0; i < keys.length; i++) {
                nativeInt2IntMap.put(keys[i], intValues[i]);
                nativeInt2LongMap.put(keys[i], longValues[i]);
                nativeInt2ObjectMap.put(keys[i], payloads[i]);
                hashMapInt2Int.put(boxedKeys[i], boxedIntValues[i]);
                hashMapInt2Long.put(boxedKeys[i], boxedLongValues[i]);
                hashMapInt2Object.put(boxedKeys[i], payloads[i]);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeInt2IntMap != null) {
                nativeInt2IntMap.freeMemory();
                nativeInt2IntMap = null;
            }
            if (nativeInt2LongMap != null) {
                nativeInt2LongMap.freeMemory();
                nativeInt2LongMap = null;
            }
            if (nativeInt2ObjectMap != null) {
                nativeInt2ObjectMap.freeMemory();
                nativeInt2ObjectMap = null;
            }
            hashMapInt2Int = null;
            hashMapInt2Long = null;
            hashMapInt2Object = null;
        }
    }

    @State(Scope.Thread)
    public abstract static class IntMapStateBase {
        @Param({"1024", "65536"})
        public int size;

        protected final PayloadMemory payloadMemory = new PayloadMemory();
        protected int[] keys;
        protected int[] hitKeys;
        protected int[] intValues;
        protected long[] longValues;
        protected Payload[] payloads;
        protected Integer[] boxedKeys;
        protected Integer[] boxedHitKeys;
        protected Integer[] boxedIntValues;
        protected Long[] boxedLongValues;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            keys = createIntKeys(size, 71_071);
            hitKeys = shuffledCopy(keys, 73_073L);
            intValues = createIntValues(size);
            longValues = createLongValues(size, 79_079L);
            payloads = createPayloads(size);
            boxedKeys = box(keys);
            boxedHitKeys = box(hitKeys);
            boxedIntValues = box(intValues);
            boxedLongValues = box(longValues);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class LongBooleanMapAddState extends LongBooleanMapStateBase {
    }

    @State(Scope.Thread)
    public static class LongBooleanMapQueryState extends LongBooleanMapStateBase {
        private NativeLong2BooleanMap nativeLong2BooleanMap;
        private HashMap<Long, Boolean> hashMapLong2Boolean;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeLong2BooleanMap = new NativeLong2BooleanMap(expectedCapacity);
            hashMapLong2Boolean = new HashMap<>(expectedCapacity);
            for (int i = 0; i < keys.length; i++) {
                nativeLong2BooleanMap.put(keys[i], booleanValues[i]);
                hashMapLong2Boolean.put(boxedKeys[i], boxedBooleanValues[i]);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeLong2BooleanMap != null) {
                nativeLong2BooleanMap.freeMemory();
                nativeLong2BooleanMap = null;
            }
            hashMapLong2Boolean = null;
        }
    }

    @State(Scope.Thread)
    public abstract static class LongBooleanMapStateBase {
        @Param({"1024", "65536"})
        public int size;

        protected long[] keys;
        protected long[] hitKeys;
        protected boolean[] booleanValues;
        protected Long[] boxedKeys;
        protected Long[] boxedHitKeys;
        protected Boolean[] boxedBooleanValues;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            keys = createLongKeys(size, 83_083L);
            hitKeys = shuffledCopy(keys, 89_089L);
            booleanValues = createBooleanValues(size);
            boxedKeys = box(keys);
            boxedHitKeys = box(hitKeys);
            boxedBooleanValues = box(booleanValues);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class ObjectPrimitiveMapAddState extends ObjectPrimitiveMapStateBase {
    }

    @State(Scope.Thread)
    public static class ObjectPrimitiveMapQueryState extends ObjectPrimitiveMapStateBase {
        private NativeObject2IntMap<MapKey> nativeObject2IntMap;
        private NativeObject2ByteMap<MapKey> nativeObject2ByteMap;
        private HashMap<MapKey, Integer> hashMapObject2Int;
        private HashMap<MapKey, Byte> hashMapObject2Byte;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeObject2IntMap = new NativeObject2IntMap<>(expectedCapacity, keyMemory);
            nativeObject2ByteMap = new NativeObject2ByteMap<>(expectedCapacity, keyMemory);
            hashMapObject2Int = new HashMap<>(expectedCapacity);
            hashMapObject2Byte = new HashMap<>(expectedCapacity);

            for (int i = 0; i < keys.length; i++) {
                nativeObject2IntMap.putPrehashed(keys[i], keyHashes[i], intValues[i]);
                nativeObject2ByteMap.putPrehashed(keys[i], keyHashes[i], byteValues[i]);
                hashMapObject2Int.put(keys[i], boxedIntValues[i]);
                hashMapObject2Byte.put(keys[i], boxedByteValues[i]);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeObject2IntMap != null) {
                nativeObject2IntMap.freeMemory();
                nativeObject2IntMap = null;
            }
            if (nativeObject2ByteMap != null) {
                nativeObject2ByteMap.freeMemory();
                nativeObject2ByteMap = null;
            }
            hashMapObject2Int = null;
            hashMapObject2Byte = null;
        }
    }

    @State(Scope.Thread)
    public abstract static class ObjectPrimitiveMapStateBase {
        @Param({"1024", "65536"})
        public int size;

        protected final MapKeyMemory keyMemory = new MapKeyMemory();
        protected MapKey[] keys;
        protected MapKey[] hitKeys;
        protected long[] keyHashes;
        protected long[] hitKeyHashes;
        protected int[] intValues;
        protected byte[] byteValues;
        protected Integer[] boxedIntValues;
        protected Byte[] boxedByteValues;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            keys = createMapKeys(size, 97_097);
            hitKeys = copyAndShuffleKeys(keys, 101_101L);
            keyHashes = hashKeys(keys, keyMemory);
            hitKeyHashes = hashKeys(hitKeys, keyMemory);
            intValues = createIntValues(size);
            byteValues = createByteValues(size);
            boxedIntValues = box(intValues);
            boxedByteValues = box(byteValues);
            expectedCapacity = size << 1;
        }
    }

    @State(Scope.Thread)
    public static class IdentityMapAddState extends IdentityMapStateBase {
    }

    @State(Scope.Thread)
    public static class IdentityMapQueryState extends IdentityMapStateBase {
        private NativeIdentity2IntMap<IdentityKey> nativeIdentity2IntMap;
        private NativeIdentity2ByteMap<IdentityKey> nativeIdentity2ByteMap;
        private NativeIdentity2BooleanMap<IdentityKey> nativeIdentity2BooleanMap;
        private IdentityHashMap<IdentityKey, Integer> identityHashMapInt;
        private IdentityHashMap<IdentityKey, Byte> identityHashMapByte;
        private IdentityHashMap<IdentityKey, Boolean> identityHashMapBoolean;

        @Setup(Level.Iteration)
        public void setupIteration() {
            nativeIdentity2IntMap = new NativeIdentity2IntMap<>(expectedCapacity);
            nativeIdentity2ByteMap = new NativeIdentity2ByteMap<>(expectedCapacity);
            nativeIdentity2BooleanMap = new NativeIdentity2BooleanMap<>(expectedCapacity);
            identityHashMapInt = new IdentityHashMap<>(expectedCapacity);
            identityHashMapByte = new IdentityHashMap<>(expectedCapacity);
            identityHashMapBoolean = new IdentityHashMap<>(expectedCapacity);

            for (int i = 0; i < keys.length; i++) {
                nativeIdentity2IntMap.put(keys[i], intValues[i]);
                nativeIdentity2ByteMap.put(keys[i], byteValues[i]);
                nativeIdentity2BooleanMap.put(keys[i], booleanValues[i]);
                identityHashMapInt.put(keys[i], boxedIntValues[i]);
                identityHashMapByte.put(keys[i], boxedByteValues[i]);
                identityHashMapBoolean.put(keys[i], boxedBooleanValues[i]);
            }
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            if (nativeIdentity2IntMap != null) {
                nativeIdentity2IntMap.freeMemory();
                nativeIdentity2IntMap = null;
            }
            if (nativeIdentity2ByteMap != null) {
                nativeIdentity2ByteMap.freeMemory();
                nativeIdentity2ByteMap = null;
            }
            if (nativeIdentity2BooleanMap != null) {
                nativeIdentity2BooleanMap.freeMemory();
                nativeIdentity2BooleanMap = null;
            }
            identityHashMapInt = null;
            identityHashMapByte = null;
            identityHashMapBoolean = null;
        }
    }

    @State(Scope.Thread)
    public abstract static class IdentityMapStateBase {
        @Param({"1024", "65536"})
        public int size;

        protected IdentityKey[] keys;
        protected IdentityKey[] hitKeys;
        protected int[] intValues;
        protected byte[] byteValues;
        protected boolean[] booleanValues;
        protected Integer[] boxedIntValues;
        protected Byte[] boxedByteValues;
        protected Boolean[] boxedBooleanValues;
        protected int expectedCapacity;

        @Setup(Level.Trial)
        public void setupTrial() {
            keys = createIdentityKeys(size);
            hitKeys = shuffledCopy(keys, 103_103L);
            intValues = createIntValues(size);
            byteValues = createByteValues(size);
            booleanValues = createBooleanValues(size);
            boxedIntValues = box(intValues);
            boxedByteValues = box(byteValues);
            boxedBooleanValues = box(booleanValues);
            expectedCapacity = size << 1;
        }
    }

    private static int[] createIntKeys(int size, int seed) {
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = seed + (i * 0x9E3779B9);
        }
        return values;
    }

    private static long[] createLongKeys(int size, long seed) {
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            values[i] = seed + (0x9E3779B97F4A7C15L * i);
        }
        return values;
    }

    private static int[] createIntValues(int size) {
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = NativeUtils.mix(i + 1);
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

    private static boolean[] createBooleanValues(int size) {
        boolean[] values = new boolean[size];
        for (int i = 0; i < size; i++) {
            values[i] = (i & 1) == 0;
        }
        return values;
    }

    private static Payload[] createPayloads(int size) {
        Payload[] values = new Payload[size];
        for (int i = 0; i < size; i++) {
            values[i] = new Payload(i, NativeUtils.mix(107_107L + i));
        }
        return values;
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

    private static IdentityKey[] shuffledCopy(IdentityKey[] source, long seed) {
        IdentityKey[] copy = source.clone();
        Random random = new Random(seed);
        for (int i = copy.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            IdentityKey tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        return copy;
    }

    private static Integer[] box(int[] source) {
        Integer[] boxed = new Integer[source.length];
        for (int i = 0; i < source.length; i++) {
            boxed[i] = source[i];
        }
        return boxed;
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

    private static Boolean[] box(boolean[] source) {
        Boolean[] boxed = new Boolean[source.length];
        for (int i = 0; i < source.length; i++) {
            boxed[i] = source[i];
        }
        return boxed;
    }

    private static MapKey[] createMapKeys(int size, int seed) {
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

    private static long[] hashKeys(MapKey[] keys, MapKeyMemory memory) {
        long[] hashes = new long[keys.length];
        for (int i = 0; i < keys.length; i++) {
            hashes[i] = memory.hash(keys[i]);
        }
        return hashes;
    }

    private static IdentityKey[] createIdentityKeys(int size) {
        IdentityKey[] values = new IdentityKey[size];
        for (int i = 0; i < size; i++) {
            values[i] = new IdentityKey(i);
        }
        return values;
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

    private static final class IdentityKey {
        private final int value;

        private IdentityKey(int value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof IdentityKey key && value == key.value;
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
