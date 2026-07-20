package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StoryManager {

    // Chapter progress per player
    private static final Map<UUID, Integer> chapters = new HashMap<>();
    // Day tracker
    private static final Map<UUID, Integer> lastDay = new HashMap<>();
    // Block restore tracking: position -> original state, restore tick
    private static final Map<BlockPos, BlockState> glitchBlocks = new HashMap<>();
    private static final Map<BlockPos, Long> glitchRestoreTick = new HashMap<>();
    // Boss bar
    private static final Map<UUID, ServerBossEvent> bossBars = new HashMap<>();

    public static void register() {
        // On player join: give the lore book (Chapter 0)
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
                // Restore glitch blocks
                long now = level.getGameTime();
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

                    // Chapter 1: first cave entry (Y < 50)
                    if (chapter == 0 && player.getY() < 50) {
                        advanceChapter(player, 1, level);
                    }

                    // Chapter 3: day 7
                    if (chapter == 2 && currentDay >= 7 && prevDay < 7) {
                        advanceChapter(player, 3, level);
                    }

                    // Chapter 4: day 14
                    if (chapter == 3 && currentDay >= 14 && prevDay < 14) {
                        advanceChapter(player, 4, level);
                    }

                    // Final chapter: day 21
                    if (chapter == 4 && currentDay >= 21 && prevDay < 21) {
                        advanceChapter(player, 5, level);
                    }

                    // Chapter 4 ongoing: glitch nearby blocks at midnight
                    if (chapter >= 4 && dayTime >= 18000 && dayTime <= 18020) {
                        glitchNearbyBlocks(player, level, now);
                    }

                    // Chapter 3 ongoing: wither pulse at midnight
                    if (chapter >= 3 && dayTime >= 18000 && dayTime <= 18005) {
                        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, false));
                    }

                    lastDay.put(uuid, currentDay);
                }
            }
        });

        // Chapter 2: trigger on death via respawn event — we use tick-based HP check
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

        switch (chapter) {
            case 1 -> {
                // Fake player join/leave
                player.server.getPlayerList().broadcastSystemMessage(
                    Component.literal("§7[§8DR_VALE§7] joined the game"), false);
                scheduleMessage(player, 60, "§8[DR_VALE]: " + GlitchMessages.VALE_LOG_1);
                scheduleMessage(player, 120, "§7[§8DR_VALE§7] left the game");
                // Ambient cave sound
                level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(),
                    SoundSource.AMBIENT, 1.0f, 0.5f);
            }
            case 2 -> {
                // World glitch on death
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 2, false, false));
                scheduleMessage(player, 40, GlitchMessages.VALE_FINAL_LOG);
                scheduleMessage(player, 80, GlitchMessages.ARCHITECT_AWARE);
            }
            case 3 -> {
                // The Architect speaks directly
                scheduleMessage(player, 20, GlitchMessages.ARCHITECT_MSG_1);
                scheduleMessage(player, 60, GlitchMessages.ARCHITECT_MSG_2);
                level.playSound(null, player.blockPosition(), SoundEvents.WITHER_AMBIENT,
                    SoundSource.HOSTILE, 0.3f, 0.3f);
            }
            case 4 -> {
                // Corrupted name message
                scheduleMessage(player, 20, GlitchMessages.corruptName(player.getName().getString()));
                scheduleMessage(player, 80, GlitchMessages.CHAPTER_4_MSG);
            }
            case 5 -> {
                // FINAL CHAPTER
                level.getLevelData().setThundering(true);
                level.getLevelData().setRaining(true);
                // Boss bar
                ServerBossEvent bar = new ServerBossEvent(
                    Component.literal("H̷E̸ ̵K̶N̴O̸W̷S̵ ̸Y̶O̴U̵ ̶A̷R̵E̴ ̷H̸E̵R̶E̸"),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_20
                );
                bar.addPlayer(player);
                bossBars.put(player.getUUID(), bar);
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
        player.server.tell(new net.minecraft.server.TickTask(player.server.getTickCount() + delayTicks, () -> {
            if (player.isAlive()) {
                player.sendSystemMessage(Component.literal(message));
            }
        }));
    }

    private static void giveIntroBook(ServerPlayer player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        var tag = book.getOrCreateTag();
        tag.putString("title", "SIGNAL_LOST");
        tag.putString("author", "DR. ORIN VALE");
        var pages = new net.minecraft.nbt.ListTag();
        pages.add(net.minecraft.nbt.StringTag.valueOf(
            net.minecraft.network.chat.Component.Serializer.toJson(
                Component.literal("SIGNAL_LOST\n\nIf you are reading this... you are already inside it.\n\n- DR. VALE"))));
        pages.add(net.minecraft.nbt.StringTag.valueOf(
            net.minecraft.network.chat.Component.Serializer.toJson(
                Component.literal("Day 1\nThe anomaly first appeared at coordinates I dare not write. The world responded. Something watched me write this."))));
        tag.put("pages", pages);
        player.getInventory().add(book);
    }

    private static void giveFinalBook(ServerPlayer player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        var tag = book.getOrCreateTag();
        tag.putString("title", "FINAL TRANSMISSION");
        tag.putString("author", "THE ARCHITECT");
        var pages = new net.minecraft.nbt.ListTag();
        pages.add(net.minecraft.nbt.StringTag.valueOf(
            net.minecraft.network.chat.Component.Serializer.toJson(
                Component.literal("YOU WERE NEVER MEANT TO READ THIS FAR.\n\nThe simulation does not end.\nYou do."))));
        pages.add(net.minecraft.nbt.StringTag.valueOf(
            net.minecraft.network.chat.Component.Serializer.toJson(
                Component.literal("DR. VALE tried to warn you.\nHe could not leave either.\n\nNeither can you."))));
        tag.put("pages", pages);
        player.getInventory().add(book);
    }

    public static int getChapter(UUID uuid) {
        return chapters.getOrDefault(uuid, 0);
    }
}
