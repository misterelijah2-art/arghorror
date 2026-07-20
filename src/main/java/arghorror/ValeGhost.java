package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;

/**
 * Vale's ghost: a particle trail that walks a short path and vanishes.
 * Spawns near the player, silent, never interacts.
 * Uses SOUL particles to trace a humanoid walking path.
 * Visible only briefly. The player may not even see it.
 */
public class ValeGhost {

    private static final Random RANDOM = new Random();
    private static int tick = 0;

    // Active ghost trails: list of (pos, step) to walk
    private static final List<GhostTrail> activeTrails = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;

            // Spawn a new ghost every 3-8 minutes per player
            if (tick % 3600 == 0) {
                for (ServerLevel level : server.getAllLevels()) {
                    for (ServerPlayer player : level.players()) {
                        int chapter = StoryManager.getChapter(player.getUUID());
                        if (chapter < 2) continue;
                        if (RANDOM.nextInt(3) != 0) continue;
                        spawnGhostTrail(player, level);
                    }
                }
            }

            // Advance active trails every 8 ticks (slow walk pace)
            if (tick % 8 == 0 && !activeTrails.isEmpty()) {
                for (ServerLevel level : server.getAllLevels()) {
                    Iterator<GhostTrail> iter = activeTrails.iterator();
                    while (iter.hasNext()) {
                        GhostTrail trail = iter.next();
                        if (trail.done()) { iter.remove(); continue; }
                        trail.step(level);
                    }
                }
            }
        });
    }

    private static void spawnGhostTrail(ServerPlayer player, ServerLevel level) {
        // Start 15-25 blocks away from player
        double angle = Math.toRadians(RANDOM.nextDouble() * 360);
        double dist = 15 + RANDOM.nextDouble() * 10;
        int sx = (int)(player.getX() + Math.cos(angle) * dist);
        int sz = (int)(player.getZ() + Math.sin(angle) * dist);
        int sy = level.getHeightmapPos(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            new BlockPos(sx, 0, sz)).getY();

        // Walk ~20 steps in a straight-ish direction
        double walkAngle = Math.toRadians(RANDOM.nextDouble() * 360);
        List<BlockPos> path = new ArrayList<>();
        double cx = sx, cz = sz;
        for (int i = 0; i < 20; i++) {
            cx += Math.cos(walkAngle) * 1.2 + (RANDOM.nextDouble() * 0.4 - 0.2);
            cz += Math.sin(walkAngle) * 1.2 + (RANDOM.nextDouble() * 0.4 - 0.2);
            int gy = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos((int)cx, 0, (int)cz)).getY();
            path.add(new BlockPos((int)cx, gy, (int)cz));
        }
        activeTrails.add(new GhostTrail(path));
    }

    static class GhostTrail {
        private final List<BlockPos> path;
        private int index = 0;
        private static final Random R = new Random();

        GhostTrail(List<BlockPos> path) { this.path = path; }
        boolean done() { return index >= path.size(); }

        void step(ServerLevel level) {
            BlockPos pos = path.get(index++);
            // Head particles
            level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 1.6, pos.getZ() + 0.5,
                2, 0.1, 0.05, 0.1, 0.01);
            // Body particles
            level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                3, 0.15, 0.1, 0.15, 0.01);
            // Foot particles
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                1, 0.2, 0, 0.2, 0.01);
        }
    }
}
