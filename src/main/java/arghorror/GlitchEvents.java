package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Random;

public class GlitchEvents {

    private static final Random RANDOM = new Random();
    private static int tick = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;
            // Every ~30 seconds, maybe fire a glitch event
            if (tick % 600 != 0) return;

            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    int chapter = StoryManager.getChapter(player.getUUID());
                    if (chapter < 1) continue;

                    int roll = RANDOM.nextInt(100);

                    // Distant footstep sound
                    if (roll < 30) {
                        level.playSound(null, player.blockPosition(),
                            SoundEvents.SCULK_SENSOR_CLICKING, SoundSource.AMBIENT,
                            0.3f, 0.5f);
                    }
                    // Fake entity sound nearby
                    else if (roll < 55) {
                        level.playSound(null, player.blockPosition(),
                            SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE,
                            0.15f, 0.6f);
                    }
                    // Whisper-like cave sound
                    else if (roll < 75) {
                        level.playSound(null, player.blockPosition(),
                            SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                            0.2f, 0.3f);
                    }
                    // Cryptic chat message
                    else if (roll < 90 && chapter >= 2) {
                        player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(
                                GlitchMessages.randomGlitch()));
                    }
                    // Fake system ping (only high chapters)
                    else if (chapter >= 3) {
                        player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(
                                "§8[SYSTEM]: " + GlitchMessages.randomSystemGlitch()));
                    }
                }
            }
        });
    }
}
