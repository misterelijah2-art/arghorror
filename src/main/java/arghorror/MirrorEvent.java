package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;

/**
 * 4th wall break system. At chapter 4+, The Architect references:
 * - The player's real username
 * - Their actual coordinates
 * - Things WatcherSystem recorded (blocks broken, animals killed, distance)
 * - Time-specific details from this session
 *
 * Fires rarely. Each line should feel like a violation.
 */
public class MirrorEvent {

    private static final Random RANDOM = new Random();
    private static int tick = 0;
    private static final Map<UUID, Integer> eventsFired = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;
            if (tick % 1200 != 0) return; // check every 60 seconds

            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    int chapter = StoryManager.getChapter(player.getUUID());
                    if (chapter < 4) continue;
                    if (RANDOM.nextInt(4) != 0) continue;

                    fireMirrorEvent(player);
                }
            }
        });
    }

    public static void fireMirrorEvent(ServerPlayer player) {
        UUID uuid = player.getUUID();
        int fired = eventsFired.getOrDefault(uuid, 0);
        String name = player.getName().getString();
        int x = (int) player.getX();
        int y = (int) player.getY();
        int z = (int) player.getZ();

        // Escalating sequence of mirror events
        String msg = switch (fired % 12) {
            case 0  -> "\u00a74[THE ARCHITECT]: " + name + ". I know that name.";
            case 1  -> "\u00a74[THE ARCHITECT]: You are at " + x + ", " + y + ", " + z + ". I already knew that.";
            case 2  -> "\u00a74[THE ARCHITECT]: " + name + ". How long have you been awake today?";
            case 3  -> "\u00a78[SYSTEM]: OBSERVER IDENTIFIED. USERNAME: " + name.toUpperCase() + ". LOGGING SESSION.";
            case 4  -> "\u00a74[THE ARCHITECT]: The coordinates you are standing on right now \u00a7o(" + x + ", " + z + ")\u00a7r are the same coordinates DR. VALE was standing on when he stopped writing.";
            case 5  -> "\u00a74[THE ARCHITECT]: " + name + ". You moved " + x + " blocks east of where you started. He moved further. It didn't help him.";
            case 6  -> "\u00a78[NULL]: " + corrupt(name);
            case 7  -> "\u00a74[THE ARCHITECT]: I have been watching you since you were at Y:" + (y + RANDOM.nextInt(20) - 10) + ". You don't remember being there.";
            case 8  -> "\u00a74[THE ARCHITECT]: " + name + ". Close the game.";
            case 9  -> "\u00a74[THE ARCHITECT]: I said \u00a7oclose the game\u00a7r, " + name + ".";
            case 10 -> "\u00a78[SYSTEM]: SIMULATION INTEGRITY: " + (12 - RANDOM.nextInt(8)) + "%";
            default -> "\u00a74[THE ARCHITECT]: " + name + ". \u00a7oWhy are you still here.";
        };

        player.sendSystemMessage(Component.literal(msg));
        eventsFired.put(uuid, fired + 1);
    }

    private static String corrupt(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            sb.append(c).append('̷');
        }
        return sb.toString();
    }
}
