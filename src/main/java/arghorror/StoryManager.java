package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StoryManager {

    private static final Map<UUID, Integer> chapters = new HashMap<>();
    private static final Map<UUID, Integer> lastDay = new HashMap<>();
    private static final Map<BlockPos, BlockState> glitchBlocks = new HashMap<>();
    private static final Map<BlockPos, Long> glitchRestoreTick = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> bossBars = new HashMap<>();

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            UUID uuid = player.getUUID();
            if (!chapters.containsKey(uuid)) {
                chapters.put(uuid, 0);
                scheduleMessage(player, 60,  "\u00a78\u00a7l[SIGNAL_LOST]");
                scheduleMessage(player, 80,  "\u00a77A book has appeared in your inventory.");
                scheduleMessage(player, 100, GlitchMessages.CHAPTER_0_MSG);
                giveIntroBook(player);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                long now = level.getGameTime();

                // Restore glitch blocks
                glitchBlocks.entrySet().removeIf(entry -> {
                    BlockPos pos = entry.getKey();
                    Long restoreTick = glitchRestoreTick.get(pos);
                    if (restoreTick != null && now >= restoreTick) {
                        level.setBlock(pos, entry.getValue(), 3);
                        glitchRestoreTick.remove(pos);
                        return true;
                    }
                    return false;
                });

                for (ServerPlayer player : level.players()) {
                    UUID uuid = player.getUUID();
                    int chapter = chapters.getOrDefault(uuid, 0);
                    long dayTime = level.getDayTime() % 24000;
                    int currentDay = (int) (level.getDayTime() / 24000);
                    int prevDay = lastDay.getOrDefault(uuid, 0);

                    if (chapter == 0 && player.getY() < 50)
                        advanceChapter(player, 1, level);
                    if (chapter == 1 && player.isDeadOrDying())
                        advanceChapter(player, 2, level);
                    if (chapter == 2 && currentDay >= 7 && prevDay < 7)
                        advanceChapter(player, 3, level);
                    if (chapter == 3 && currentDay >= 14 && prevDay < 14)
                        advanceChapter(player, 4, level);
                    if (chapter == 4 && currentDay >= 21 && prevDay < 21)
                        advanceChapter(player, 5, level);

                    if (chapter >= 4 && dayTime >= 18000 && dayTime <= 18020)
                        glitchNearbyBlocks(player, level, now);
                    if (chapter >= 3 && dayTime >= 18000 && dayTime <= 18005)
                        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, false));

                    lastDay.put(uuid, currentDay);
                }
            }
        });
    }

    private static void advanceChapter(ServerPlayer player, int chapter, ServerLevel level) {
        UUID uuid = player.getUUID();
        chapters.put(uuid, chapter);
        MinecraftServer server = level.getServer();

        switch (chapter) {
            case 1 -> {
                server.getPlayerList().broadcastSystemMessage(
                    Component.literal("\u00a77[\u00a78DR_VALE\u00a77] joined the game"), false);
                scheduleMessage(player, 60,  "\u00a78[DR_VALE]: " + GlitchMessages.VALE_LOG_1);
                scheduleMessage(player, 120, "\u00a77[\u00a78DR_VALE\u00a77] left the game");
                level.playSound(null, player.blockPosition(),
                    SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 1.0f, 0.5f);
            }
            case 2 -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 2, false, false));
                scheduleMessage(player, 40,  GlitchMessages.VALE_FINAL_LOG);
                scheduleMessage(player, 80,  GlitchMessages.ARCHITECT_AWARE);
            }
            case 3 -> {
                scheduleMessage(player, 20, GlitchMessages.ARCHITECT_MSG_1);
                scheduleMessage(player, 60, GlitchMessages.ARCHITECT_MSG_2);
                level.playSound(null, player.blockPosition(),
                    SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 0.3f, 0.3f);
            }
            case 4 -> {
                scheduleMessage(player, 20, GlitchMessages.corruptName(player.getName().getString()));
                scheduleMessage(player, 80, GlitchMessages.CHAPTER_4_MSG);
            }
            case 5 -> {
                level.setWeatherParameters(0, 6000, true, true);
                ServerBossEvent bar = new ServerBossEvent(
                    Component.literal("H\u0337E\u0338 \u0335K\u0336N\u0334O\u0338W\u0337S\u0335 \u0338Y\u0336O\u0334U\u0335 \u0336A\u0337R\u0335E\u0334 \u0337H\u0338E\u0335R\u0336E\u0338"),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_20
                );
                bar.addPlayer(player);
                bossBars.put(uuid, bar);
                scheduleMessage(player, 20,  GlitchMessages.FINAL_MSG_1);
                scheduleMessage(player, 80,  GlitchMessages.FINAL_MSG_2);
                scheduleMessage(player, 160, GlitchMessages.FINAL_MSG_3);
                giveFinalLore(player);
                level.playSound(null, player.blockPosition(),
                    SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 0.5f, 0.3f);
            }
        }
    }

    private static void glitchNearbyBlocks(ServerPlayer player, ServerLevel level, long now) {
        BlockPos center = player.blockPosition();
        for (int i = 0; i < 3; i++) {
            int ox = (int)(Math.random() * 10) - 5;
            int oy = (int)(Math.random() * 4) - 2;
            int oz = (int)(Math.random() * 10) - 5;
            BlockPos pos = center.offset(ox, oy, oz);
            BlockState orig = level.getBlockState(pos);
            if (!orig.isAir() && !orig.is(Blocks.BEDROCK)) {
                glitchBlocks.put(pos, orig);
                glitchRestoreTick.put(pos, now + 40);
                level.setBlock(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
            }
        }
    }

    private static void scheduleMessage(ServerPlayer player, int delayTicks, String message) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return;
        int targetTick = server.getTickCount() + delayTicks;
        boolean[] fired = {false};
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            if (!fired[0] && s.getTickCount() >= targetTick) {
                fired[0] = true;
                if (!player.isRemoved()) {
                    player.sendSystemMessage(Component.literal(message));
                }
            }
        });
    }

    // Delivers intro lore as a series of chat messages (avoids Filterable/NBT API issues)
    private static void giveIntroBook(ServerPlayer player) {
        scheduleMessage(player, 20,  "\u00a78\u00a7l=================================================");
        scheduleMessage(player, 25,  "\u00a74\u00a7l         [ SIGNAL_LOST ]");
        scheduleMessage(player, 30,  "\u00a78\u00a7l=================================================");
        scheduleMessage(player, 40,  "\u00a77\u00a7oAuthor: DR. ORIN VALE");
        scheduleMessage(player, 50,  "\u00a77\u00a7o\"If you are reading this...");
        scheduleMessage(player, 55,  "\u00a77\u00a7o you are already inside it.\"");
        scheduleMessage(player, 65,  "\u00a78- - - - - - - - - - - - - - -");
        scheduleMessage(player, 75,  "\u00a77\u00a7oDay 1: The anomaly first appeared");
        scheduleMessage(player, 80,  "\u00a77\u00a7oat coordinates I dare not write.");
        scheduleMessage(player, 85,  "\u00a77\u00a7oThe world responded.");
        scheduleMessage(player, 90,  "\u00a77\u00a7oSomething watched me write this.");
        scheduleMessage(player, 100, "\u00a78\u00a7l=================================================");
    }

    private static void giveFinalLore(ServerPlayer player) {
        scheduleMessage(player, 200, "\u00a78\u00a7l=================================================");
        scheduleMessage(player, 210, "\u00a74\u00a7l     [ FINAL TRANSMISSION ]");
        scheduleMessage(player, 215, "\u00a78\u00a7l=================================================");
        scheduleMessage(player, 225, "\u00a74\u00a7oAuthor: THE ARCHITECT");
        scheduleMessage(player, 235, "\u00a74\u00a7oYOU WERE NEVER MEANT TO READ THIS FAR.");
        scheduleMessage(player, 245, "\u00a74\u00a7oThe simulation does not end. You do.");
        scheduleMessage(player, 255, "\u00a78- - - - - - - - - - - - - - -");
        scheduleMessage(player, 265, "\u00a74\u00a7oDR. VALE tried to warn you.");
        scheduleMessage(player, 275, "\u00a74\u00a7oHe could not leave either.");
        scheduleMessage(player, 285, "\u00a74\u00a7oNeither can you.");
        scheduleMessage(player, 295, "\u00a78\u00a7l=================================================");
    }

    public static int getChapter(UUID uuid) {
        return chapters.getOrDefault(uuid, 0);
    }
}
