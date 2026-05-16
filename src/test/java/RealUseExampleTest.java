import net.sixik.javastructg.examples.RealUseExample;
import net.sixik.javastructg.structs.arrays.NativeObjectArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealUseExampleTest {

    @Test
    public void testBlockStateKeepsBlockIdAndStoresStateDataInline() {
        RealUseExample.BlockMemory blockMemory = new RealUseExample.BlockMemory();
        RealUseExample.BlockStateMemory blockStateMemory = new RealUseExample.BlockStateMemory();
        NativeObjectArray<RealUseExample.Block> blockRegistry = new NativeObjectArray<>(2, blockMemory);
        NativeObjectArray<RealUseExample.BlockState> states = new NativeObjectArray<>(1, blockStateMemory);
        try {
            blockRegistry.set(0, new RealUseExample.Block(0, true, false, 6.0f, 0.6f, 1.0f, 1.0f, false));
            blockRegistry.set(1, new RealUseExample.Block(1, true, true, 1.5f, 0.8f, 1.2f, 0.9f, true));

            RealUseExample.BlockState source = new RealUseExample.BlockState(
                    1,
                    false,
                    true,
                    new RealUseExample.StateData(7, true, false)
            );
            states.set(0, source);

            RealUseExample.BlockState out = new RealUseExample.BlockState();
            states.get(0, out);

            assertEquals(1, out.blockId);
            assertFalse(out.air);
            assertTrue(out.solidRender);
            assertEquals(7, out.stateData.lightEmission);
            assertTrue(out.stateData.waterlogged);
            assertFalse(out.stateData.replaceable);
            assertEquals(0.8f, RealUseExample.resolveFriction(blockRegistry, out));
        } finally {
            states.freeMemory();
            blockRegistry.freeMemory();
        }
    }

    @Test
    public void testTerrainGenerationFills32x32x8ChunkWithDeterministicNativeData() {
        try (RealUseExample.TerrainChunk terrain = RealUseExample.generateTerrain(123_456_789L)) {
            assertEquals(32, terrain.width());
            assertEquals(32, terrain.depth());
            assertEquals(8, terrain.height());
            assertEquals(7, terrain.paletteSize());

            RealUseExample.BlockState state = new RealUseExample.BlockState();
            RealUseExample.BlockState paletteState = new RealUseExample.BlockState();
            RealUseExample.Block block = new RealUseExample.Block();

            int[] counts = new int[6];
            long checksum = 0L;

            for (int z = 0; z < terrain.depth(); z++) {
                for (int x = 0; x < terrain.width(); x++) {
                    int surfaceY = terrain.surfaceHeight(x, z);
                    assertTrue(surfaceY >= 1 && surfaceY <= 6);

                    short surfaceStateId = terrain.getStateId(x, surfaceY, z);
                    assertTrue(surfaceStateId >= 0 && surfaceStateId < terrain.paletteSize());
                    terrain.getPaletteState(surfaceStateId, paletteState);
                    terrain.getState(x, surfaceY, z, state);
                    assertEquals(paletteState.blockId, state.blockId);
                    if (surfaceY >= RealUseExample.SEA_LEVEL) {
                        assertEquals(RealUseExample.GRASS_BLOCK_ID, state.blockId);
                        assertFalse(state.stateData.waterlogged);
                    } else {
                        assertEquals(RealUseExample.DIRT_BLOCK_ID, state.blockId);
                        assertTrue(state.stateData.waterlogged);
                    }

                    terrain.getState(x, terrain.height() - 1, z, state);
                    assertFalse(state.solidRender);

                    for (int y = 0; y < terrain.height(); y++) {
                        short stateId = terrain.getStateId(x, y, z);
                        assertTrue(stateId >= 0 && stateId < terrain.paletteSize());
                        terrain.getState(x, y, z, state);
                        counts[state.blockId]++;
                        checksum = (checksum * 1_315_423_911L)
                                + state.blockId
                                + (state.air ? 3 : 0)
                                + (state.solidRender ? 5 : 0)
                                + (state.stateData.lightEmission * 7L)
                                + (state.stateData.waterlogged ? 11 : 0)
                                + (state.stateData.replaceable ? 13 : 0);
                    }
                }
            }

            terrain.getState(10, terrain.surfaceHeight(10, 10), 10, state);
            terrain.getBlockById(state.blockId, block);
            assertEquals(block.friction, terrain.resolveFriction(state));

            assertEquals(3076, counts[RealUseExample.AIR_BLOCK_ID]);
            assertEquals(2036, counts[RealUseExample.STONE_BLOCK_ID]);
            assertEquals(1921, counts[RealUseExample.DIRT_BLOCK_ID]);
            assertEquals(1024, counts[RealUseExample.GRASS_BLOCK_ID]);
            assertEquals(132, counts[RealUseExample.WATER_BLOCK_ID]);
            assertEquals(3, counts[RealUseExample.LAVA_BLOCK_ID]);
            assertEquals(-347921800431973733L, checksum);
        }
    }
}
