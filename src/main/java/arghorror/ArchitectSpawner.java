package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.*;

public class ArchitectSpawner {

    private static final Map<UUID, TheArchitectEntity> activeEntities = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static int tick = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;
            if (tick % 200 != 0) return;

            for (ServerLevel level : server.getAllLevels()) {
                long dayTime = level.getDayTime() % 24000;
                boolean isNightOrDusk = dayTime >= 13000 || dayTime < 6000;

                for (ServerPlayer player : level.players()) {
                    UUID uuid = player.getUUID();
                    int chapter = StoryManager.getChapter(uuid);
                    if (chapter < 1) continue;

                    TheArchitectEntity existing = activeEntities.get(uuid);
                    if (existing != null && (existing.isRemoved() || !existing.isAlive())) {
                        activeEntities.remove(uuid);
                        existing = null;
                    }
                    if (existing != null) continue;

                    boolean underground = !level.canSeeSky(player.blockPosition());
                    if (!isNightOrDusk && !underground) continue;

                    int chance = switch (chapter) {
                        case 1 -> 8;
                        case 2 -> 5;
                        case 3 -> 3;
                        default -> 1;
                    };
                    if (RANDOM.nextInt(chance) != 0) continue;

                    spawnArchitect(player, level);
                }
            }
        });
    }

    private static void spawnArchitect(ServerPlayer player, ServerLevel level) {
        double angle = Math.toRadians(player.getYRot() + 180 + (RANDOM.nextDouble() * 60 - 30));
        double dist = 16 + RANDOM.nextDouble() * 14;
        double sx = player.getX() + Math.cos(angle) * dist;
        double sz = player.getZ() + Math.sin(angle) * dist;

        BlockPos spawnPos = level.getHeightmapPos(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            new BlockPos((int) sx, (int) player.getY(), (int) sz)
        );

        TheArchitectEntity entity = ModEntities.THE_ARCHITECT.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (entity == null) return;

        entity.snapTo(spawnPos.getX() + 0.5, (double) spawnPos.getY(), spawnPos.getZ() + 0.5, 0f, 0f);
        level.addFreshEntity(entity);
        activeEntities.put(player.getUUID(), entity);
    }

    public static TheArchitectEntity getEntityFor(UUID playerUUID) {
        return activeEntities.get(playerUUID);
    }
}
