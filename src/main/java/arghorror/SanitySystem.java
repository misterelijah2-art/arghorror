package arghorror;

import arghorror.network.SanityPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
    private static int tickCounter = 0;

    public static void register() {
        // Register the packet type server-side
        PayloadTypeRegistry.playS2C().register(SanityPacket.TYPE, SanityPacket.CODEC);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 40 != 0) return;

            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    UUID uuid = player.getUUID();
                    int current = sanity.getOrDefault(uuid, MAX_SANITY);
                    long dayTime = level.getDayTime() % 24000;
                    boolean isNight = dayTime >= 13000 && dayTime <= 23000;
                    boolean isCave = player.getY() < 50 && !level.canSeeSky(player.blockPosition());
                    int chapter = StoryManager.getChapter(uuid);

                    int drain = 0;
                    if (isNight) drain += 1;
                    if (isCave) drain += 2;
                    if (chapter >= 3) drain += 1;
                    if (chapter >= 5) drain += 2;

                    int restore = (!isNight && !isCave) ? 1 : 0;
                    current = Math.max(0, Math.min(MAX_SANITY, current - drain + restore));
                    sanity.put(uuid, current);

                    applySanityEffects(player, current, level);

                    // Sync to client every 2 seconds
                    ServerPlayNetworking.send(player, new SanityPacket(current));
                }
            }
        });
    }

    private static void applySanityEffects(ServerPlayer player, int s, ServerLevel level) {
        if (s <= 60 && !player.hasEffect(MobEffects.DARKNESS)) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
        }
        if (s > 60 && player.hasEffect(MobEffects.DARKNESS)) {
            player.removeEffect(MobEffects.DARKNESS);
        }

        if (s <= 40 && RANDOM.nextInt(20) == 0) {
            level.playSound(null, player.blockPosition(),
                SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT,
                0.4f, 0.5f + RANDOM.nextFloat() * 0.3f);
        }

        if (s <= 20 && !player.hasEffect(MobEffects.NAUSEA)) {
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 1, false, false));
        }
        if (s > 20 && player.hasEffect(MobEffects.NAUSEA)) {
            player.removeEffect(MobEffects.NAUSEA);
        }

        if (s <= 20 && RANDOM.nextInt(30) == 0) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(GlitchMessages.randomGlitch()));
        }

        if (s == 0 && RANDOM.nextInt(100) == 0) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    "\u00a74[SYSTEM]: " + GlitchMessages.ZERO_SANITY_MSG));
        }
    }

    public static int getSanity(UUID uuid) {
        return sanity.getOrDefault(uuid, MAX_SANITY);
    }
}
