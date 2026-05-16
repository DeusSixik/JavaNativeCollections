package net.sixik.javastructg.examples;

import net.sixik.javastructg.structs.NativeStructLayout;
import net.sixik.javastructg.structs.NativeTypeMemory;
import net.sixik.javastructg.structs.arrays.NativeIntArray;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import net.sixik.javastructg.structs.arrays.NativeShortArray;
import sun.misc.Unsafe;

import java.util.Random;

public final class RealUseExample {

    public static final int CHUNK_WIDTH = 32;
    public static final int CHUNK_DEPTH = 32;
    public static final int CHUNK_HEIGHT = 8;
    public static final int BLOCK_COUNT = CHUNK_WIDTH * CHUNK_DEPTH * CHUNK_HEIGHT;
    public static final int HEIGHTMAP_SIZE = CHUNK_WIDTH * CHUNK_DEPTH;
    public static final int SEA_LEVEL = 3;

    public static final int AIR_BLOCK_ID = 0;
    public static final int STONE_BLOCK_ID = 1;
    public static final int DIRT_BLOCK_ID = 2;
    public static final int GRASS_BLOCK_ID = 3;
    public static final int WATER_BLOCK_ID = 4;
    public static final int LAVA_BLOCK_ID = 5;

    public static final short AIR_STATE_ID = 0;
    public static final short STONE_STATE_ID = 1;
    public static final short DIRT_STATE_ID = 2;
    public static final short WATERLOGGED_DIRT_STATE_ID = 3;
    public static final short GRASS_STATE_ID = 4;
    public static final short WATER_STATE_ID = 5;
    public static final short LAVA_STATE_ID = 6;

    private RealUseExample() {
    }

    public static float resolveFriction(NativeObjectArray<Block> blockRegistry, BlockState state) {
        Block block = new Block();
        blockRegistry.get(state.blockId, block);
        return block.friction;
    }

    public static TerrainChunk generateTerrain(long seed) {
        TerrainChunk terrain = new TerrainChunk(
                defaultBlockRegistry(),
                defaultStatePalette(),
                new NativeIntArray(HEIGHTMAP_SIZE),
                new NativeShortArray(BLOCK_COUNT)
        );
        PerlinNoise heightNoise = new PerlinNoise(seed);
        PerlinNoise caveNoise = new PerlinNoise(seed ^ 0x9E3779B97F4A7C15L);

        try {
            for (int z = 0; z < CHUNK_DEPTH; z++) {
                for (int x = 0; x < CHUNK_WIDTH; x++) {
                    int surfaceY = surfaceHeight(heightNoise, x, z);
                    terrain.heightMap.set(heightIndex(x, z), surfaceY);

                    for (int y = 0; y < CHUNK_HEIGHT; y++) {
                        short stateId = stateFor(caveNoise, x, y, z, surfaceY);
                        terrain.stateIds.set(blockIndex(x, y, z), stateId);
                    }
                }
            }
            return terrain;
        } catch (RuntimeException e) {
            terrain.close();
            throw e;
        }
    }

    private static NativeObjectArray<Block> defaultBlockRegistry() {
        NativeObjectArray<Block> registry = new NativeObjectArray<>(6, new BlockMemory());
        registry.set(AIR_BLOCK_ID, new Block(AIR_BLOCK_ID, false, false, 0.0f, 0.6f, 1.0f, 1.0f, false));
        registry.set(STONE_BLOCK_ID, new Block(STONE_BLOCK_ID, true, false, 6.0f, 0.6f, 1.0f, 1.0f, false));
        registry.set(DIRT_BLOCK_ID, new Block(DIRT_BLOCK_ID, true, false, 0.5f, 0.6f, 1.0f, 1.0f, false));
        registry.set(GRASS_BLOCK_ID, new Block(GRASS_BLOCK_ID, true, true, 0.6f, 0.6f, 1.0f, 1.0f, false));
        registry.set(WATER_BLOCK_ID, new Block(WATER_BLOCK_ID, false, false, 100.0f, 0.8f, 0.8f, 1.0f, true));
        registry.set(LAVA_BLOCK_ID, new Block(LAVA_BLOCK_ID, false, false, 100.0f, 0.5f, 0.5f, 1.0f, true));
        return registry;
    }

    private static NativeObjectArray<BlockState> defaultStatePalette() {
        NativeObjectArray<BlockState> palette = new NativeObjectArray<>(7, new BlockStateMemory());
        palette.set(AIR_STATE_ID, new BlockState(AIR_BLOCK_ID, true, false, new StateData(0, false, true)));
        palette.set(STONE_STATE_ID, new BlockState(STONE_BLOCK_ID, false, true, new StateData(0, false, false)));
        palette.set(DIRT_STATE_ID, new BlockState(DIRT_BLOCK_ID, false, true, new StateData(0, false, false)));
        palette.set(WATERLOGGED_DIRT_STATE_ID, new BlockState(DIRT_BLOCK_ID, false, true, new StateData(0, true, false)));
        palette.set(GRASS_STATE_ID, new BlockState(GRASS_BLOCK_ID, false, true, new StateData(0, false, false)));
        palette.set(WATER_STATE_ID, new BlockState(WATER_BLOCK_ID, false, false, new StateData(0, false, true)));
        palette.set(LAVA_STATE_ID, new BlockState(LAVA_BLOCK_ID, false, false, new StateData(15, false, false)));
        return palette;
    }

    private static int surfaceHeight(PerlinNoise noise, int x, int z) {
        double base = noise.noise(x * 0.09, z * 0.09, 0.0);
        double detail = noise.noise(x * 0.18 + 17.0, z * 0.18 - 11.0, 2.0) * 0.5;
        double combined = (base + detail) / 1.5;
        int height = 2 + (int) Math.round((combined + 1.0) * 2.0);
        return clamp(height, 1, CHUNK_HEIGHT - 2);
    }

    private static short stateFor(PerlinNoise caveNoise, int x, int y, int z, int surfaceY) {
        if (y > surfaceY) {
            return y <= SEA_LEVEL ? WATER_STATE_ID : AIR_STATE_ID;
        }

        if (y == 1 && surfaceY > SEA_LEVEL + 1) {
            double lavaPocket = caveNoise.noise(x * 0.35 + 31.0, 7.0, z * 0.35 - 19.0);
            if (lavaPocket > 0.48) {
                return LAVA_STATE_ID;
            }
        }

        if (y > 1 && y < surfaceY - 1) {
            double cave = caveNoise.noise(x * 0.16, y * 0.28, z * 0.16);
            if (cave > 0.36) {
                return y <= SEA_LEVEL ? WATER_STATE_ID : AIR_STATE_ID;
            }
        }

        if (y == surfaceY) {
            return surfaceY >= SEA_LEVEL ? GRASS_STATE_ID : WATERLOGGED_DIRT_STATE_ID;
        }
        if (y >= surfaceY - 2) {
            return DIRT_STATE_ID;
        }
        return STONE_STATE_ID;
    }

    private static int blockIndex(int x, int y, int z) {
        return x + (z * CHUNK_WIDTH) + (y * CHUNK_WIDTH * CHUNK_DEPTH);
    }

    private static int heightIndex(int x, int z) {
        return x + (z * CHUNK_WIDTH);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static final class StateData {
        public int lightEmission;
        public boolean waterlogged;
        public boolean replaceable;

        public StateData() {
        }

        public StateData(int lightEmission, boolean waterlogged, boolean replaceable) {
            this.lightEmission = lightEmission;
            this.waterlogged = waterlogged;
            this.replaceable = replaceable;
        }
    }

    public static final class Block {
        public int blockId;
        public boolean hasCollision;
        public boolean randomlyTicking;
        public float explosionResistance;
        public float friction;
        public float speedFactor;
        public float jumpFactor;
        public boolean dynamicShape;

        public Block() {
        }

        public Block(
                int blockId,
                boolean hasCollision,
                boolean randomlyTicking,
                float explosionResistance,
                float friction,
                float speedFactor,
                float jumpFactor,
                boolean dynamicShape
        ) {
            this.blockId = blockId;
            this.hasCollision = hasCollision;
            this.randomlyTicking = randomlyTicking;
            this.explosionResistance = explosionResistance;
            this.friction = friction;
            this.speedFactor = speedFactor;
            this.jumpFactor = jumpFactor;
            this.dynamicShape = dynamicShape;
        }
    }

    public static final class BlockState {
        public int blockId;
        public boolean air;
        public boolean solidRender;
        public final StateData stateData = new StateData();

        public BlockState() {
        }

        public BlockState(int blockId, boolean air, boolean solidRender, StateData stateData) {
            this.blockId = blockId;
            this.air = air;
            this.solidRender = solidRender;
            this.stateData.lightEmission = stateData.lightEmission;
            this.stateData.waterlogged = stateData.waterlogged;
            this.stateData.replaceable = stateData.replaceable;
        }
    }

    public static final class TerrainChunk implements AutoCloseable {
        private final NativeObjectArray<Block> blockRegistry;
        private final NativeObjectArray<BlockState> statePalette;
        private final NativeIntArray heightMap;
        private final NativeShortArray stateIds;

        public TerrainChunk(
                NativeObjectArray<Block> blockRegistry,
                NativeObjectArray<BlockState> statePalette,
                NativeIntArray heightMap,
                NativeShortArray stateIds
        ) {
            this.blockRegistry = blockRegistry;
            this.statePalette = statePalette;
            this.heightMap = heightMap;
            this.stateIds = stateIds;
        }

        public int width() {
            return CHUNK_WIDTH;
        }

        public int depth() {
            return CHUNK_DEPTH;
        }

        public int height() {
            return CHUNK_HEIGHT;
        }

        public int surfaceHeight(int x, int z) {
            return heightMap.get(heightIndex(x, z));
        }

        public int paletteSize() {
            return 7;
        }

        public short getStateId(int x, int y, int z) {
            return stateIds.get(blockIndex(x, y, z));
        }

        public void getPaletteState(short stateId, BlockState out) {
            statePalette.get(stateId, out);
        }

        public void getState(int x, int y, int z, BlockState out) {
            short stateId = getStateId(x, y, z);
            getPaletteState(stateId, out);
        }

        public void getBlockById(int blockId, Block out) {
            blockRegistry.get(blockId, out);
        }

        public float resolveFriction(BlockState state) {
            return RealUseExample.resolveFriction(blockRegistry, state);
        }

        @Override
        public void close() {
            stateIds.freeMemory();
            heightMap.freeMemory();
            statePalette.freeMemory();
            blockRegistry.freeMemory();
        }
    }

    public static final class StateDataMemory implements NativeTypeMemory<StateData> {
        public static final NativeStructLayout LAYOUT;
        public static final long LIGHT_EMISSION_OFFSET;
        public static final long WATERLOGGED_OFFSET;
        public static final long REPLACEABLE_OFFSET;

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            LIGHT_EMISSION_OFFSET = builder.intField();
            WATERLOGGED_OFFSET = builder.booleanField();
            REPLACEABLE_OFFSET = builder.booleanField();
            LAYOUT = builder.build();
        }

        @Override
        public long sizeof() {
            return LAYOUT.sizeof();
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, StateData outElement) {
            outElement.lightEmission = unsafe.getInt(offset + LIGHT_EMISSION_OFFSET);
            outElement.waterlogged = unsafe.getByte(offset + WATERLOGGED_OFFSET) != 0;
            outElement.replaceable = unsafe.getByte(offset + REPLACEABLE_OFFSET) != 0;
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, StateData element) {
            unsafe.putInt(offset + LIGHT_EMISSION_OFFSET, element.lightEmission);
            unsafe.putByte(offset + WATERLOGGED_OFFSET, (byte) (element.waterlogged ? 1 : 0));
            unsafe.putByte(offset + REPLACEABLE_OFFSET, (byte) (element.replaceable ? 1 : 0));
        }
    }

    public static final class BlockMemory implements NativeTypeMemory<Block> {
        public static final NativeStructLayout LAYOUT;
        public static final long BLOCK_ID_OFFSET;
        public static final long HAS_COLLISION_OFFSET;
        public static final long RANDOMLY_TICKING_OFFSET;
        public static final long EXPLOSION_RESISTANCE_OFFSET;
        public static final long FRICTION_OFFSET;
        public static final long SPEED_FACTOR_OFFSET;
        public static final long JUMP_FACTOR_OFFSET;
        public static final long DYNAMIC_SHAPE_OFFSET;

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            BLOCK_ID_OFFSET = builder.intField();
            HAS_COLLISION_OFFSET = builder.booleanField();
            RANDOMLY_TICKING_OFFSET = builder.booleanField();
            EXPLOSION_RESISTANCE_OFFSET = builder.floatField();
            FRICTION_OFFSET = builder.floatField();
            SPEED_FACTOR_OFFSET = builder.floatField();
            JUMP_FACTOR_OFFSET = builder.floatField();
            DYNAMIC_SHAPE_OFFSET = builder.booleanField();
            LAYOUT = builder.build();
        }

        @Override
        public long sizeof() {
            return LAYOUT.sizeof();
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, Block outElement) {
            outElement.blockId = unsafe.getInt(offset + BLOCK_ID_OFFSET);
            outElement.hasCollision = unsafe.getByte(offset + HAS_COLLISION_OFFSET) != 0;
            outElement.randomlyTicking = unsafe.getByte(offset + RANDOMLY_TICKING_OFFSET) != 0;
            outElement.explosionResistance = unsafe.getFloat(offset + EXPLOSION_RESISTANCE_OFFSET);
            outElement.friction = unsafe.getFloat(offset + FRICTION_OFFSET);
            outElement.speedFactor = unsafe.getFloat(offset + SPEED_FACTOR_OFFSET);
            outElement.jumpFactor = unsafe.getFloat(offset + JUMP_FACTOR_OFFSET);
            outElement.dynamicShape = unsafe.getByte(offset + DYNAMIC_SHAPE_OFFSET) != 0;
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, Block element) {
            unsafe.putInt(offset + BLOCK_ID_OFFSET, element.blockId);
            unsafe.putByte(offset + HAS_COLLISION_OFFSET, (byte) (element.hasCollision ? 1 : 0));
            unsafe.putByte(offset + RANDOMLY_TICKING_OFFSET, (byte) (element.randomlyTicking ? 1 : 0));
            unsafe.putFloat(offset + EXPLOSION_RESISTANCE_OFFSET, element.explosionResistance);
            unsafe.putFloat(offset + FRICTION_OFFSET, element.friction);
            unsafe.putFloat(offset + SPEED_FACTOR_OFFSET, element.speedFactor);
            unsafe.putFloat(offset + JUMP_FACTOR_OFFSET, element.jumpFactor);
            unsafe.putByte(offset + DYNAMIC_SHAPE_OFFSET, (byte) (element.dynamicShape ? 1 : 0));
        }
    }

    public static final class BlockStateMemory implements NativeTypeMemory<BlockState> {
        public static final NativeStructLayout LAYOUT;
        public static final long BLOCK_ID_OFFSET;
        public static final long AIR_OFFSET;
        public static final long SOLID_RENDER_OFFSET;
        public static final NativeStructLayout.StructField STATE_DATA_FIELD;

        private final StateDataMemory stateDataMemory = new StateDataMemory();

        static {
            NativeStructLayout.Builder builder = NativeStructLayout.builder();
            BLOCK_ID_OFFSET = builder.intField();
            AIR_OFFSET = builder.booleanField();
            SOLID_RENDER_OFFSET = builder.booleanField();
            STATE_DATA_FIELD = builder.structField(StateDataMemory.LAYOUT);
            LAYOUT = builder.build();
        }

        @Override
        public long sizeof() {
            return LAYOUT.sizeof();
        }

        @Override
        public void readFromMemory(Unsafe unsafe, long offset, BlockState outElement) {
            outElement.blockId = unsafe.getInt(offset + BLOCK_ID_OFFSET);
            outElement.air = unsafe.getByte(offset + AIR_OFFSET) != 0;
            outElement.solidRender = unsafe.getByte(offset + SOLID_RENDER_OFFSET) != 0;
            stateDataMemory.readFromMemory(unsafe, STATE_DATA_FIELD.address(offset), outElement.stateData);
        }

        @Override
        public void writeToMemory(Unsafe unsafe, long offset, BlockState element) {
            unsafe.putInt(offset + BLOCK_ID_OFFSET, element.blockId);
            unsafe.putByte(offset + AIR_OFFSET, (byte) (element.air ? 1 : 0));
            unsafe.putByte(offset + SOLID_RENDER_OFFSET, (byte) (element.solidRender ? 1 : 0));
            stateDataMemory.writeToMemory(unsafe, STATE_DATA_FIELD.address(offset), element.stateData);
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
