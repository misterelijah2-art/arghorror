package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/**
 * DR. VALE left echoes in the world. When a player stands still in the dark
 * for several seconds, they receive a fragment of his logs — as if he's still present.
 * Messages vary based on location context (underground, surface night, surface day).
 */
public class EchoSystem {

    private static final Map<UUID, Long>    stillSince   = new HashMap<>();
    private static final Map<UUID, Double[]> lastEchoPos = new HashMap<>();
    private static final Map<UUID, Long>    lastEchoTick = new HashMap<>();

    // Vale's underground echoes
    private static final String[] CAVE_ECHOES = {
        "\u00a78[DR_VALE]: I stood here too. Day 14. I thought if I didn't move, it couldn't find me.",
        "\u00a78[DR_VALE]: The cave goes deeper than the map says. I stopped mapping on day 6.",
        "\u00a78[DR_VALE]: I heard digging. There was nothing there. There is never anything there.",
        "\u00a78[DR_VALE]: The torch I placed here... I didn't place that torch.",
        "\u00a78[DR_VALE]: Something breathes down here. Not me. I've checked.",
        "\u00a78[DR_VALE]: Day 18. The ore is in the wrong place. All of it. It moved."
    };

    // Vale's surface night echoes
    private static final String[] NIGHT_ECHOES = {
        "\u00a78[DR_VALE]: Don't watch the treeline at night. I learned that.",
        "\u00a78[DR_VALE]: The stars look different tonight. I've been counting them. The number changes.",
        "\u00a78[DR_VALE]: Day 22. I saw my own shadow cast from two directions. No second light source.",
        "\u00a78[DR_VALE]: I built a wall. In the morning it was one block shorter. Every morning.",
        "\u00a78[DR_VALE]: It doesn't come from the dark. It \u00a7ois\u00a7r the dark."
    };

    // Vale's surface day echoes (rarer, more unsettling because the daylight offers no comfort)
    private static final String[] DAY_ECHOES = {
        "\u00a78[DR_VALE]: The sun rose at the wrong angle today.",
        "\u00a78[DR_VALE]: I found my own footprints leading somewhere I have never been.",
        "\u00a78[DR_VALE]: Day 27. I checked the coordinates. I am exactly where I started. I have been walking for three days.",
        "\u00a78[DR_VALE]: It watches better in the light. You can't look for something you can already see."
    };

    private static final Random RANDOM = new Random();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                long now = level.getGameTime();
                long dayTime = level.getDayTime() % 24000;
                boolean isNight = dayTime >= 13000 && dayTime <= 23000;

                for (ServerPlayer player : level.players()) {
                    UUID uuid = player.getUUID();
                    boolean isDark = !level.canSeeSky(player.blockPosition())
                        || level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, player.blockPosition()) < 4;

                    // Only trigger when underground OR at night surface
                    if (!isDark && !isNight) {
                        stillSince.remove(uuid);
                        continue;
                    }

                    Double[] lp = lastEchoPos.get(uuid);
                    double x = player.getX(), z = player.getZ();
                    boolean moved = lp == null || Math.abs(x - lp[0]) > 1.0 || Math.abs(z - lp[1]) > 1.0;

                    if (moved) {
                        stillSince.put(uuid, now);
                        lastEchoPos.put(uuid, new Double[]{x, z});
                        continue;
                    }

                    long still = now - stillSince.getOrDefault(uuid, now);
                    // Must be still for 5 seconds (100 ticks)
                    if (still < 100) continue;
                    // At least 30 seconds between echoes
                    long lastEcho = lastEchoTick.getOrDefault(uuid, 0L);
                    if (now - lastEcho < 600) continue;
                    // Random chance so it doesn't fire the instant the timer hits
                    if (RANDOM.nextInt(40) != 0) continue;

                    lastEchoTick.put(uuid, now);
                    boolean isCave = !level.canSeeSky(player.blockPosition());
                    String echo;
                    if (isCave) {
                        echo = CAVE_ECHOES[RANDOM.nextInt(CAVE_ECHOES.length)];
                    } else if (isNight) {
                        echo = NIGHT_ECHOES[RANDOM.nextInt(NIGHT_ECHOES.length)];
                    } else {
                        echo = DAY_ECHOES[RANDOM.nextInt(DAY_ECHOES.length)];
                    }
                    player.sendSystemMessage(Component.literal(echo));

                    // Small chance The Architect responds to the echo
                    if (RANDOM.nextInt(5) == 0) {
                        StoryManager.scheduleMessage(player, 60,
                            "\u00a74[THE ARCHITECT]: He wrote that for you. He knew you'd be here.");
                    }
                }
            }
        });
    }
}
