package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SanitySystem {

    private static final Map<UUID, Integer> sanity = new HashMap<>();
    private static final int MAX_SANITY = 100;
    private static final Random RANDOM = new Random();

    // Sanity drains every N ticks
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            // Process sanity every 2 seconds
            if (tickCounter % 40 != 0) return;

            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    UUID uuid = player.getUUID();
                    int current = sanity.getOrDefault(uuid, MAX_SANITY);
                    long dayTime = level.getDayTime() % 24000;
                    boolean isNight = dayTime >= 13000 && dayTime <= 23000;
                    boolean isCave = player.getY() < 50 && !level.canSeeSky(player.blockPosition());
                    int chapter = StoryManager.getChapter(uuid);

                    // Drain sanity based on conditions
                    int drain = 0;
                    if (isNight) drain += 1;
                    if (isCave) drain += 2;
                    if (chapter >= 3) drain += 1;
                    if (chapter >= 5) drain += 2;

                    // Slowly restore sanity in daylight above ground
                    int restore = 0;
                    if (!isNight && !isCave) restore = 1;

                    current = Math.max(0, Math.min(MAX_SANITY, current - drain + restore));
                    sanity.put(uuid, current);

                    applySanityEffects(player, current, level, dayTime);
                }
            }
        });
    }

    private static void applySanityEffects(ServerPlayer player, int s, ServerLevel level, long dayTime) {
        // Low sanity: darkness
        if (s <= 60) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        }

        // Very low sanity: random ambient sounds
        if (s <= 40 && RANDOM.nextInt(20) == 0) {
            level.playSound(null, player.blockPosition(),
                SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                0.4f, 0.5f + RANDOM.nextFloat() * 0.3f);
        }

        // Critical sanity: nausea + glitch message
        if (s <= 20) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 1, false, false));
            if (RANDOM.nextInt(30) == 0) {
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                        GlitchMessages.randomGlitch()));
            }
        }

        // Zero sanity
        if (s == 0 && RANDOM.nextInt(100) == 0) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    "§4[SYSTEM]: " + GlitchMessages.ZERO_SANITY_MSG));
        }
    }

    public static int getSanity(UUID uuid) {
        return sanity.getOrDefault(uuid, MAX_SANITY);
    }
}
