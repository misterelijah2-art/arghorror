package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.List;
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
                giveIntroBook(player);
                scheduleMessage(player, 100, GlitchMessages.CHAPTER_0_MSG);
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

                    if (chapter == 0 && player.getY() < 50) {
                        advanceChapter(player, 1, level);
                    }
                    if (chapter == 2 && currentDay >= 7 && prevDay < 7) {
                        advanceChapter(player, 3, level);
                    }
                    if (chapter == 3 && currentDay >= 14 && prevDay < 14) {
                        advanceChapter(player, 4, level);
                    }
                    if (chapter == 4 && currentDay >= 21 && prevDay < 21) {
                        advanceChapter(player, 5, level);
                    }
                    if (chapter >= 4 && dayTime >= 18000 && dayTime <= 18020) {
                        glitchNearbyBlocks(player, level, now);
                    }
                    if (chapter >= 3 && dayTime >= 18000 && dayTime <= 18005) {
                        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, false));
                    }

                    lastDay.put(uuid, currentDay);
                }
            }
        });

        // Chapter 2: death detection
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    UUID uuid = player.getUUID();
                    int chapter = chapters.getOrDefault(uuid, 0);
                    if (chapter == 1 && player.isDeadOrDying()) {
                        advanceChapter(player, 2, level);
                    }
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
                scheduleMessage(player, 60, "\u00a78[DR_VALE]: " + GlitchMessages.VALE_LOG_1);
                scheduleMessage(player, 120, "\u00a77[\u00a78DR_VALE\u00a77] left the game");
                level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(),
                    SoundSource.AMBIENT, 1.0f, 0.5f);
            }
            case 2 -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 2, false, false));
                scheduleMessage(player, 40, GlitchMessages.VALE_FINAL_LOG);
                scheduleMessage(player, 80, GlitchMessages.ARCHITECT_AWARE);
            }
            case 3 -> {
                scheduleMessage(player, 20, GlitchMessages.ARCHITECT_MSG_1);
                scheduleMessage(player, 60, GlitchMessages.ARCHITECT_MSG_2);
                level.playSound(null, player.blockPosition(), SoundEvents.WITHER_AMBIENT,
                    SoundSource.HOSTILE, 0.3f, 0.3f);
            }
            case 4 -> {
                scheduleMessage(player, 20, GlitchMessages.corruptName(player.getName().getString()));
                scheduleMessage(player, 80, GlitchMessages.CHAPTER_4_MSG);
            }
            case 5 -> {
                // Use ServerLevel's weather methods
                level.setWeatherParameters(0, 6000, true, true);
                ServerBossEvent bar = new ServerBossEvent(
                    Component.literal("H\u0337E\u0338 \u0335K\u0336N\u0334O\u0338W\u0337S\u0335 \u0338Y\u0336O\u0334U\u0335 \u0336A\u0337R\u0335E\u0334 \u0337H\u0338E\u0335R\u0336E\u0338"),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_20
                );
                bar.addPlayer(player);
                bossBars.put(uuid, bar);
                scheduleMessage(player, 20, GlitchMessages.FINAL_MSG_1);
                scheduleMessage(player, 80, GlitchMessages.FINAL_MSG_2);
                scheduleMessage(player, 160, GlitchMessages.FINAL_MSG_3);
                giveFinalBook(player);
                level.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH,
                    SoundSource.HOSTILE, 0.5f, 0.3f);
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
        MinecraftServer server = player.getServer();
        if (server == null) return;
        int targetTick = server.getTickCount() + delayTicks;
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            if (s.getTickCount() >= targetTick) {
                if (player.isAlive()) {
                    player.sendSystemMessage(Component.literal(message));
                }
                // Self-remove by using a flag — Fabric events don't unregister,
                // so we guard with a one-shot boolean via the closure
            }
        });
    }

    private static void giveIntroBook(ServerPlayer player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
            new net.minecraft.util.Unit() != null
                ? null : null, // placeholder — see below
            "SIGNAL_LOST",
            "DR. ORIN VALE",
            List.of(
                net.minecraft.network.chat.FilteredText.passThrough(
                    Component.literal("SIGNAL_LOST\n\nIf you are reading this... you are already inside it.\n\n- DR. VALE")),
                net.minecraft.network.chat.FilteredText.passThrough(
                    Component.literal("Day 1\nThe anomaly first appeared at coordinates I dare not write. The world responded. Something watched me write this."))
            ),
            false
        ));
        player.getInventory().add(book);
    }

    private static void giveFinalBook(ServerPlayer player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
            null,
            "FINAL TRANSMISSION",
            "THE ARCHITECT",
            List.of(
                net.minecraft.network.chat.FilteredText.passThrough(
                    Component.literal("YOU WERE NEVER MEANT TO READ THIS FAR.\n\nThe simulation does not end.\nYou do.")),
                net.minecraft.network.chat.FilteredText.passThrough(
                    Component.literal("DR. VALE tried to warn you.\nHe could not leave either.\n\nNeither can you."))
            ),
            false
        ));
        player.getInventory().add(book);
    }

    public static int getChapter(UUID uuid) {
        return chapters.getOrDefault(uuid, 0);
    }
}
