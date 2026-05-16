package net.sixik.javastructg;

import net.sixik.javastructg.examples.RealUseExample;
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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Threads(1)
public class RealUseTerrainJmhBenchmark {

    @Benchmark
    public long heapArray_chunkGeneration(TerrainState state) {
        int[] heightMap = new int[state.heightMapSize];
        short[] stateIds = new short[state.blockCount];
        long checksum = fillHeapArrays(state, heightMap, stateIds);
        return checksum + sampleHeapArrays(state, heightMap, stateIds);
    }

    @Benchmark
    public long arrayList_chunkGeneration(TerrainState state) {
        ArrayList<Integer> heightMap = new ArrayList<>(state.heightMapSize);
        ArrayList<Short> stateIds = new ArrayList<>(state.blockCount);
        long checksum = fillArrayLists(state, heightMap, stateIds);
        return checksum + sampleArrayLists(state, heightMap, stateIds);
    }

    @Benchmark
    public long nativeArray_chunkGeneration(TerrainState state) {
        NativeIntArray heightMap = new NativeIntArray(state.heightMapSize);
        NativeShortArray stateIds = new NativeShortArray(state.blockCount);
        try {
            long checksum = fillNativeArrays(state, heightMap, stateIds);
            return checksum + sampleNativeArrays(state, heightMap, stateIds);
        } finally {
            stateIds.freeMemory();
            heightMap.freeMemory();
        }
    }

    private static long fillHeapArrays(TerrainState state, int[] heightMap, short[] stateIds) {
        long checksum = 0L;
        for (int z = 0; z < state.depth; z++) {
            for (int x = 0; x < state.width; x++) {
                int surfaceY = surfaceHeight(state.heightNoise, x, z, state.height);
                heightMap[heightIndex(state.width, x, z)] = surfaceY;
                checksum = mix(checksum, surfaceY);

                for (int y = 0; y < state.height; y++) {
                    short stateId = stateFor(state.caveNoise, x, y, z, surfaceY);
                    stateIds[blockIndex(state.width, state.depth, x, y, z)] = stateId;
                    checksum = mix(checksum, stateId);
                }
            }
        }
        return checksum;
    }

    private static long fillArrayLists(TerrainState state, ArrayList<Integer> heightMap, ArrayList<Short> stateIds) {
        long checksum = 0L;
        for (int z = 0; z < state.depth; z++) {
            for (int x = 0; x < state.width; x++) {
                int surfaceY = surfaceHeight(state.heightNoise, x, z, state.height);
                heightMap.add(surfaceY);
                checksum = mix(checksum, surfaceY);

                for (int y = 0; y < state.height; y++) {
                    short stateId = stateFor(state.caveNoise, x, y, z, surfaceY);
                    stateIds.add(stateId);
                    checksum = mix(checksum, stateId);
                }
            }
        }
        return checksum;
    }

    private static long fillNativeArrays(TerrainState state, NativeIntArray heightMap, NativeShortArray stateIds) {
        long checksum = 0L;
        for (int z = 0; z < state.depth; z++) {
            for (int x = 0; x < state.width; x++) {
                int surfaceY = surfaceHeight(state.heightNoise, x, z, state.height);
                heightMap.set(heightIndex(state.width, x, z), surfaceY);
                checksum = mix(checksum, surfaceY);

                for (int y = 0; y < state.height; y++) {
                    short stateId = stateFor(state.caveNoise, x, y, z, surfaceY);
                    stateIds.set(blockIndex(state.width, state.depth, x, y, z), stateId);
                    checksum = mix(checksum, stateId);
                }
            }
        }
        return checksum;
    }

    private static long sampleHeapArrays(TerrainState state, int[] heightMap, short[] stateIds) {
        long checksum = 0L;
        for (int i = 0; i < state.sampleXs.length; i++) {
            int x = state.sampleXs[i];
            int z = state.sampleZs[i];
            int y = state.sampleYs[i];
            checksum = mix(checksum, heightMap[heightIndex(state.width, x, z)]);
            checksum = mix(checksum, stateIds[blockIndex(state.width, state.depth, x, y, z)]);
        }
        return checksum;
    }

    private static long sampleArrayLists(TerrainState state, ArrayList<Integer> heightMap, ArrayList<Short> stateIds) {
        long checksum = 0L;
        for (int i = 0; i < state.sampleXs.length; i++) {
            int x = state.sampleXs[i];
            int z = state.sampleZs[i];
            int y = state.sampleYs[i];
            checksum = mix(checksum, heightMap.get(heightIndex(state.width, x, z)));
            checksum = mix(checksum, stateIds.get(blockIndex(state.width, state.depth, x, y, z)));
        }
        return checksum;
    }

    private static long sampleNativeArrays(TerrainState state, NativeIntArray heightMap, NativeShortArray stateIds) {
        long checksum = 0L;
        for (int i = 0; i < state.sampleXs.length; i++) {
            int x = state.sampleXs[i];
            int z = state.sampleZs[i];
            int y = state.sampleYs[i];
            checksum = mix(checksum, heightMap.get(heightIndex(state.width, x, z)));
            checksum = mix(checksum, stateIds.get(blockIndex(state.width, state.depth, x, y, z)));
        }
        return checksum;
    }

    private static int surfaceHeight(PerlinNoise noise, int x, int z, int maxHeight) {
        double base = noise.noise(x * 0.09, z * 0.09, 0.0);
        double detail = noise.noise(x * 0.18 + 17.0, z * 0.18 - 11.0, 2.0) * 0.5;
        double combined = (base + detail) / 1.5;
        int height = 2 + (int) Math.round((combined + 1.0) * 2.0);
        return clamp(height, 1, maxHeight - 2);
    }

    private static short stateFor(PerlinNoise caveNoise, int x, int y, int z, int surfaceY) {
        if (y > surfaceY) {
            return y <= RealUseExample.SEA_LEVEL ? RealUseExample.WATER_STATE_ID : RealUseExample.AIR_STATE_ID;
        }

        if (y == 1 && surfaceY > RealUseExample.SEA_LEVEL + 1) {
            double lavaPocket = caveNoise.noise(x * 0.35 + 31.0, 7.0, z * 0.35 - 19.0);
            if (lavaPocket > 0.48) {
                return RealUseExample.LAVA_STATE_ID;
            }
        }

        if (y > 1 && y < surfaceY - 1) {
            double cave = caveNoise.noise(x * 0.16, y * 0.28, z * 0.16);
            if (cave > 0.36) {
                return y <= RealUseExample.SEA_LEVEL ? RealUseExample.WATER_STATE_ID : RealUseExample.AIR_STATE_ID;
            }
        }

        if (y == surfaceY) {
            return surfaceY >= RealUseExample.SEA_LEVEL ? RealUseExample.GRASS_STATE_ID : RealUseExample.WATERLOGGED_DIRT_STATE_ID;
        }
        if (y >= surfaceY - 2) {
            return RealUseExample.DIRT_STATE_ID;
        }
        return RealUseExample.STONE_STATE_ID;
    }

    private static int blockIndex(int width, int depth, int x, int y, int z) {
        return x + (z * width) + (y * width * depth);
    }

    private static int heightIndex(int width, int x, int z) {
        return x + (z * width);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    private static long mix(long checksum, int value) {
        return (checksum * 1_315_423_911L) + value;
    }

    @State(Scope.Thread)
    public static class TerrainState {
        private final int width = RealUseExample.CHUNK_WIDTH;
        private final int depth = RealUseExample.CHUNK_DEPTH;
        private final int height = RealUseExample.CHUNK_HEIGHT;
        private final int heightMapSize = RealUseExample.HEIGHTMAP_SIZE;
        private final int blockCount = RealUseExample.BLOCK_COUNT;

        private final int[] sampleXs = new int[32];
        private final int[] sampleYs = new int[32];
        private final int[] sampleZs = new int[32];

        private PerlinNoise heightNoise;
        private PerlinNoise caveNoise;

        @Setup(Level.Iteration)
        public void setupIteration() {
            long seed = 123_456_789L;
            heightNoise = new PerlinNoise(seed);
            caveNoise = new PerlinNoise(seed ^ 0x9E3779B97F4A7C15L);

            Random random = new Random(seed ^ 0x5DEECE66DL);
            for (int i = 0; i < sampleXs.length; i++) {
                sampleXs[i] = random.nextInt(width);
                sampleYs[i] = random.nextInt(height);
                sampleZs[i] = random.nextInt(depth);
            }
        }
    }

    private static final class PerlinNoise {
        private final int[] permutation = new int[512];

        private PerlinNoise(long seed) {
            int[] base = new int[256];
            for (int i = 0; i < base.length; i++) {
                base[i] = i;
            }

            Random random = new Random(seed);
            for (int i = base.length - 1; i > 0; i--) {
                int swapIndex = random.nextInt(i + 1);
                int tmp = base[i];
                base[i] = base[swapIndex];
                base[swapIndex] = tmp;
            }

            for (int i = 0; i < permutation.length; i++) {
                permutation[i] = base[i & 255];
            }
        }

        private double noise(double x, double y, double z) {
            int floorX = fastFloor(x);
            int floorY = fastFloor(y);
            int floorZ = fastFloor(z);

            int cellX = floorX & 255;
            int cellY = floorY & 255;
            int cellZ = floorZ & 255;

            double localX = x - floorX;
            double localY = y - floorY;
            double localZ = z - floorZ;

            double u = fade(localX);
            double v = fade(localY);
            double w = fade(localZ);

            int a = permutation[cellX] + cellY;
            int aa = permutation[a] + cellZ;
            int ab = permutation[a + 1] + cellZ;
            int b = permutation[cellX + 1] + cellY;
            int ba = permutation[b] + cellZ;
            int bb = permutation[b + 1] + cellZ;

            return lerp(w,
                    lerp(v,
                            lerp(u, grad(permutation[aa], localX, localY, localZ), grad(permutation[ba], localX - 1.0, localY, localZ)),
                            lerp(u, grad(permutation[ab], localX, localY - 1.0, localZ), grad(permutation[bb], localX - 1.0, localY - 1.0, localZ))
                    ),
                    lerp(v,
                            lerp(u, grad(permutation[aa + 1], localX, localY, localZ - 1.0), grad(permutation[ba + 1], localX - 1.0, localY, localZ - 1.0)),
                            lerp(u, grad(permutation[ab + 1], localX, localY - 1.0, localZ - 1.0), grad(permutation[bb + 1], localX - 1.0, localY - 1.0, localZ - 1.0))
                    )
            );
        }

        private static int fastFloor(double value) {
            int truncated = (int) value;
            return value < truncated ? truncated - 1 : truncated;
        }

        private static double fade(double value) {
            return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
        }

        private static double lerp(double factor, double left, double right) {
            return left + factor * (right - left);
        }

        private static double grad(int hash, double x, double y, double z) {
            int masked = hash & 15;
            double first = masked < 8 ? x : y;
            double second = masked < 4 ? y : (masked == 12 || masked == 14 ? x : z);
            return ((masked & 1) == 0 ? first : -first) + ((masked & 2) == 0 ? second : -second);
        }
    }
}
