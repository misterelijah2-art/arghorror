package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;

/**
 * At high chapters, the player occasionally hears footsteps near them
 * when nothing is there — DR. VALE's ghost retracing his own path.
 * The sound always comes from a direction slightly off from where the
 * player is looking, so it's never directly visible.
 */
public class FootstepGhost {

    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 80 != 0) return; // check every 4 seconds

            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    int chapter = StoryManager.getChapter(player.getUUID());
                    if (chapter < 3) continue;

                    // Chance scales with chapter
                    int chance = switch (chapter) {
                        case 3 -> 25;
                        case 4 -> 15;
                        default -> 8;
                    };
                    if (RANDOM.nextInt(chance) != 0) continue;

                    // Place sound slightly off to the side of player's look direction
                    float yaw = player.getYRot() + 90 + (RANDOM.nextFloat() * 60 - 30);
                    double rad = Math.toRadians(yaw);
                    double dist = 4 + RANDOM.nextDouble() * 6;
                    double sx = player.getX() + Math.cos(rad) * dist;
                    double sz = player.getZ() + Math.sin(rad) * dist;
                    double sy = player.getY();
                    BlockPos soundPos = new BlockPos((int) sx, (int) sy, (int) sz);

                    // Alternate between step sounds for variety
                    var sound = switch (RANDOM.nextInt(4)) {
                        case 0 -> SoundEvents.STONE_STEP;
                        case 1 -> SoundEvents.GRAVEL_STEP;
                        case 2 -> SoundEvents.WOOD_STEP;
                        default -> SoundEvents.SAND_STEP;
                    };

                    level.playSound(null, soundPos, sound, SoundSource.PLAYERS, 0.4f, 0.8f + RANDOM.nextFloat() * 0.4f);

                    // Rare: double footsteps, like someone walking
                    if (RANDOM.nextInt(3) == 0) {
                        BlockPos soundPos2 = soundPos.offset(RANDOM.nextInt(3) - 1, 0, RANDOM.nextInt(3) - 1);
                        level.playSound(null, soundPos2, sound, SoundSource.PLAYERS, 0.3f, 0.8f + RANDOM.nextFloat() * 0.4f);
                    }
                }
            }
        });
    }
}
