package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;

import java.util.*;

/**
 * The Architect watches everything. Silently tracks milestones and reacts
 * with messages proving it was always observing — no HUD, no numbers, just dread.
 */
public class WatcherSystem {

    private static final Map<UUID, Integer> blocksBroken   = new HashMap<>();
    private static final Map<UUID, Integer> animalsKilled  = new HashMap<>();
    private static final Map<UUID, Long>    distanceTraveled = new HashMap<>();
    private static final Map<UUID, Integer> nightsSlept    = new HashMap<>();
    private static final Map<UUID, Long>    lastPos        = new HashMap<>();
    // Tracks which milestones have already fired per player
    private static final Map<UUID, Set<String>> fired = new HashMap<>();

    public static void register() {
        // Track blocks broken
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (!(player instanceof ServerPlayer sp)) return;
            UUID uuid = sp.getUUID();
            int count = blocksBroken.merge(uuid, 1, Integer::sum);
            checkBlockMilestones(sp, count);
        });

        // Track distance + nights slept each tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    UUID uuid = player.getUUID();

                    // Distance
                    long packed = ((long)(int) player.getX() << 32) | ((int) player.getZ() & 0xFFFFFFFFL);
                    Long prev = lastPos.put(uuid, packed);
                    if (prev != null && prev != packed) {
                        int px = (int)(prev >> 32), pz = (int)(prev & 0xFFFFFFFFL);
                        double dx = player.getX() - px, dz = player.getZ() - pz;
                        long total = distanceTraveled.merge(uuid, (long) Math.sqrt(dx*dx + dz*dz), Long::sum);
                        checkDistanceMilestones(player, total);
                    }

                    // Sleeping detection
                    if (player.isSleeping()) {
                        int nights = nightsSlept.merge(uuid, 1, Integer::sum);
                        if (nights == 1 && hasFired(uuid, "sleep_1")) {
                            scheduleMsg(player, 40, "\u00a74[THE ARCHITECT]: Sleep. It won't help.");
                        }
                        if (nights == 3 && hasFired(uuid, "sleep_3")) {
                            scheduleMsg(player, 40, "\u00a74[THE ARCHITECT]: You keep trying to skip the dark. \u00a7oI am in the dark.");
                        }
                        if (nights == 7 && hasFired(uuid, "sleep_7")) {
                            scheduleMsg(player, 40, "\u00a74[THE ARCHITECT]: Seven nights. DR. VALE slept seven nights too. Then he stopped.");
                        }
                    }
                }
            }
        });

        // Track animals killed
        net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(
            (world, entity, killedEntity) -> {
                if (!(entity instanceof ServerPlayer sp)) return;
                if (!(killedEntity instanceof Animal)) return;
                UUID uuid = sp.getUUID();
                int count = animalsKilled.merge(uuid, 1, Integer::sum);
                checkAnimalMilestones(sp, count);
            }
        );
    }

    private static void checkBlockMilestones(ServerPlayer p, int count) {
        UUID uuid = p.getUUID();
        if (count == 64 && hasFired(uuid, "blocks_64"))
            scheduleMsg(p, 20, "\u00a78[SYSTEM]: Block modification logged. Entry #64.");
        if (count == 200 && hasFired(uuid, "blocks_200"))
            scheduleMsg(p, 20, "\u00a74[THE ARCHITECT]: You have broken 200 blocks. Why are you building? \u00a7oYou cannot stay.");
        if (count == 500 && hasFired(uuid, "blocks_500"))
            scheduleMsg(p, 20, "\u00a74[THE ARCHITECT]: 500. DR. VALE reached 500 on day 9. He started hearing things on day 10.");
        if (count == 1000 && hasFired(uuid, "blocks_1000")) {
            scheduleMsg(p, 20,  "\u00a74[THE ARCHITECT]: One thousand.");
            scheduleMsg(p, 60,  "\u00a74[THE ARCHITECT]: Do you feel it yet?");
            scheduleMsg(p, 120, "\u00a74[THE ARCHITECT]: \u00a7oHe did.");
        }
    }

    private static void checkDistanceMilestones(ServerPlayer p, long dist) {
        UUID uuid = p.getUUID();
        if (dist >= 500 && hasFired(uuid, "dist_500"))
            scheduleMsg(p, 20, "\u00a78[SYSTEM]: 500 blocks from spawn. Observation radius: nominal.");
        if (dist >= 2000 && hasFired(uuid, "dist_2000"))
            scheduleMsg(p, 20, "\u00a74[THE ARCHITECT]: Running doesn't change the coordinates that matter.");
        if (dist >= 5000 && hasFired(uuid, "dist_5000")) {
            scheduleMsg(p, 20, "\u00a74[THE ARCHITECT]: DR. VALE walked 5000 blocks looking for the edge.");
            scheduleMsg(p, 80, "\u00a74[THE ARCHITECT]: \u00a7oThere is no edge.");
        }
        if (dist >= 10000 && hasFired(uuid, "dist_10000")) {
            scheduleMsg(p, 20, "\u00a74\u00a7l[THE ARCHITECT]: STILL HERE.");
        }
    }

    private static void checkAnimalMilestones(ServerPlayer p, int count) {
        UUID uuid = p.getUUID();
        if (count == 1 && hasFired(uuid, "animal_1"))
            scheduleMsg(p, 20, "\u00a78[SYSTEM]: Organic entity terminated. Noted.");
        if (count == 10 && hasFired(uuid, "animal_10"))
            scheduleMsg(p, 20, "\u00a74[THE ARCHITECT]: Ten. They trusted you.");
        if (count == 25 && hasFired(uuid, "animal_25")) {
            scheduleMsg(p, 20, "\u00a74[THE ARCHITECT]: DR. VALE never killed anything. He said it felt wrong.");
            scheduleMsg(p, 60, "\u00a74[THE ARCHITECT]: \u00a7oHe was right.");
        }
    }

    /** Returns true if this milestone hasn't fired yet, and marks it as fired. */
    private static boolean hasFired(UUID uuid, String key) {
        return fired.computeIfAbsent(uuid, k -> new HashSet<>()).add(key);
    }

    private static void scheduleMsg(ServerPlayer player, int delayTicks, String msg) {
        StoryManager.scheduleMessage(player, delayTicks, msg);
    }
}
