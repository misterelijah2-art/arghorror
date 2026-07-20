package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class CorruptionSpread {

    private static final Set<BlockPos> corrupted = new HashSet<>();
    private static final Deque<BlockPos> spreadQueue = new ArrayDeque<>();
    private static int tickCounter = 0;
    private static final Random RANDOM = new Random();

    private static final BlockState[] CORRUPT_PALETTE = {
        Blocks.CRYING_OBSIDIAN.defaultBlockState(),
        Blocks.BLACKSTONE.defaultBlockState(),
        Blocks.SOUL_SAND.defaultBlockState(),
        Blocks.SOUL_SOIL.defaultBlockState(),
        Blocks.BASALT.defaultBlockState()
    };

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    int chapter = StoryManager.getChapter(player.getUUID());
                    if (chapter < 2) continue;

                    if (tickCounter % 200 == 0) {
                        seedCorruption(player.blockPosition().below(), level, chapter);
                    }

                    if (tickCounter % 100 == 0 && !spreadQueue.isEmpty()) {
                        int spreadCount = Math.min(3 + chapter, spreadQueue.size());
                        for (int i = 0; i < spreadCount; i++) {
                            BlockPos next = spreadQueue.poll();
                            if (next == null) break;
                            spreadFrom(next, level);
                        }
                    }
                }

                if (tickCounter % 6000 == 0) {
                    for (ServerPlayer player : level.players()) {
                        int chapter = StoryManager.getChapter(player.getUUID());
                        if (chapter >= 2 && corrupted.size() > 10 && RANDOM.nextInt(3) == 0) {
                            String[] remarks = {
                                "\u00a74[THE ARCHITECT]: The world remembers where you stood.",
                                "\u00a74[THE ARCHITECT]: You cannot clean it. I've seen others try.",
                                "\u00a74[THE ARCHITECT]: DR. VALE tried to outrun it. Look at where you are standing.",
                                "\u00a74[THE ARCHITECT]: It spreads because \u00a7oyou\u00a7r spread."
                            };
                            player.sendSystemMessage(Component.literal(
                                remarks[RANDOM.nextInt(remarks.length)]));
                        }
                    }
                }
            }
        });
    }

    private static void seedCorruption(BlockPos center, ServerLevel level, int chapter) {
        int radius = 2 + chapter;
        for (int attempt = 0; attempt < 5; attempt++) {
            int ox = RANDOM.nextInt(radius * 2) - radius;
            int oz = RANDOM.nextInt(radius * 2) - radius;
            BlockPos pos = center.offset(ox, 0, oz);
            if (canCorrupt(level.getBlockState(pos))) {
                corruptBlock(pos, level);
                spreadQueue.add(pos);
            }
        }
    }

    private static void spreadFrom(BlockPos origin, ServerLevel level) {
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        int[] dir = dirs[RANDOM.nextInt(dirs.length)];
        BlockPos next = origin.offset(dir[0], 0, dir[1]);
        if (canCorrupt(level.getBlockState(next))) {
            corruptBlock(next, level);
            spreadQueue.add(next);
            level.playSound(null, next, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 0.3f, 0.8f);
        }
    }

    private static void corruptBlock(BlockPos pos, ServerLevel level) {
        if (corrupted.add(pos)) {
            level.setBlock(pos, CORRUPT_PALETTE[RANDOM.nextInt(CORRUPT_PALETTE.length)], 3);
        }
    }

    private static boolean canCorrupt(BlockState state) {
        return !state.isAir()
            && !state.is(Blocks.BEDROCK)
            && !state.is(Blocks.CRYING_OBSIDIAN)
            && !state.is(Blocks.BLACKSTONE)
            && !state.is(Blocks.SOUL_SAND)
            && !state.is(Blocks.SOUL_SOIL)
            && !state.is(Blocks.BASALT)
            && state.isSolid();
    }
}
