package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;

/**
 * Environmental manipulation triggered by TheArchitect's proximity.
 * No chat messages. Pure world manipulation:
 * - Torches extinguish within 5 blocks of the entity
 * - Doors open by themselves
 * - Note blocks shift pitch
 * - Signs get text overwritten after multiple visits (via SignCorruptor)
 */
public class PresenceSystem {

    private static final Map<BlockPos, BlockState> extinguished = new HashMap<>();
    private static final Set<BlockPos> corruptedSigns = new HashSet<>();
    private static int tick = 0;
    private static final Random RANDOM = new Random();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;

            // Re-light extinguished torches after 30-90 seconds
            if (tick % 20 == 0) {
                for (ServerLevel level : server.getAllLevels()) {
                    extinguished.entrySet().removeIf(entry -> {
                        if (RANDOM.nextInt(180) == 0) {
                            level.setBlock(entry.getKey(), entry.getValue(), 3);
                            return true;
                        }
                        return false;
                    });
                }
            }
        });
    }

    /** Called every 60 watch-ticks by TheArchitectEntity */
    public static void onArchitectWatch(ServerPlayer player, BlockPos entityPos) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // Extinguish torches within 5 blocks of entity
        BlockPos.betweenClosed(
            entityPos.offset(-5, -2, -5),
            entityPos.offset(5, 2, 5)
        ).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)) {
                if (!extinguished.containsKey(pos)) {
                    extinguished.put(pos, state);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH,
                        SoundSource.BLOCKS, 0.5f, 0.9f + RANDOM.nextFloat() * 0.2f);
                }
            }
        });

        // Open nearby doors
        BlockPos.betweenClosed(
            entityPos.offset(-6, -1, -6),
            entityPos.offset(6, 3, 6)
        ).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.OAK_DOOR) || state.is(Blocks.SPRUCE_DOOR)
                    || state.is(Blocks.DARK_OAK_DOOR)) {
                if (state.hasProperty(BlockStateProperties.OPEN)
                        && !state.getValue(BlockStateProperties.OPEN)) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, true), 10);
                    level.playSound(null, pos, SoundEvents.WOODEN_DOOR_OPEN,
                        SoundSource.BLOCKS, 0.6f, 0.9f);
                }
            }
        });

        // Randomly trigger a note block nearby
        if (RANDOM.nextInt(4) == 0) {
            BlockPos.betweenClosed(
                player.blockPosition().offset(-8, -2, -8),
                player.blockPosition().offset(8, 2, 8)
            ).forEach(pos -> {
                if (level.getBlockState(pos).is(Blocks.NOTE_BLOCK) && RANDOM.nextInt(20) == 0) {
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(),
                        SoundSource.BLOCKS, 0.4f, 0.3f + RANDOM.nextFloat() * 0.2f);
                }
            });
        }
    }
}
