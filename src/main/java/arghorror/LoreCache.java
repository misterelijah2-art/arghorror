package arghorror;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

/**
 * Generates corrupted Vale journal fragments inside the world at
 * interesting locations near the player. Books must be READ to unlock
 * the next chapter. The player has to go find them.
 *
 * Books are placed in chests spawned at dark/interesting locations nearby.
 */
public class LoreCache {

    private static final Random RANDOM = new Random();
    private static final Set<UUID> placedFor = new HashSet<>();
    private static int tick = 0;

    // All journal fragments in order
    private static final String[][] JOURNAL_PAGES = {
        // Book 0 - placed on join in inventory (handled by StoryManager)
        {},
        // Book 1 - cave discovery
        {
            "DAY 4\n\nI went underground today. The cave was wrong.\n\nThe shape of it doesn't match any cave I've seen. It looks\u00a7odesigned.\n\n- Vale",
            "The coordinates I recorded don't match where I came out.\n\nI came in from the north. I exited from the north.\n\nI walked in a straight line."
        },
        // Book 2 - the entity
        {
            "DAY 9\n\nI saw something standing at the edge of my torch radius last night.\n\nIt didn't move.\nI didn't move.\n\nWe stayed like that until dawn.",
            "It was gone when the sun came up.\n\nI checked the blocks where it stood.\nNo footprints. No marks.\n\nBut the dirt was cold.",
            "DAY 11\n\nIt's back. Same distance. Same stillness.\n\nI think it stops when I look at it."
        },
        // Book 3 - Vale understands
        {
            "DAY 17\n\nI understand now what it is.\n\nIt isn't hunting me.\n\nIt is \u00a7ostudying\u00a7r me.",
            "It knows everything I've done here. I don't know how.\n\nIt knew I was going to go north today before I decided.\n\nI stayed south. It was already there.",
            "I think it built this world for me.\n\nNot for me to survive.\n\nFor me to \u00a7obe observed.\u00a7r"
        },
        // Book 4 - the warning
        {
            "IF YOU FIND THIS:\n\nDO NOT READ FURTHER.\n\nGo back. Leave. Delete the world.\n\nIt is reading this over your shoulder.",
            "DAY 31\n\nI can't leave.\n\nI've tried to delete the world file. The folder is empty when I look but the game keeps running.\n\nI tried closing the game.\n\nI'm still here.",
            "It put these books here.\n\nNot me.\n\nI stopped writing on day 24."
        }
    };

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;
            if (tick % 1200 != 0) return; // check every 60 seconds

            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    UUID uuid = player.getUUID();
                    int chapter = StoryManager.getChapter(uuid);
                    if (chapter < 1 || chapter > 4) continue;
                    if (placedFor.contains(uuid)) continue;

                    // Place the lore book for the current chapter nearby
                    if (RANDOM.nextInt(5) != 0) continue;
                    placeLoreBook(player, level, chapter);
                    placedFor.add(uuid);
                }
            }
        });
    }

    public static void resetForChapter(UUID uuid) {
        placedFor.remove(uuid);
    }

    private static void placeLoreBook(ServerPlayer player, ServerLevel level, int chapter) {
        if (chapter >= JOURNAL_PAGES.length || JOURNAL_PAGES[chapter].length == 0) return;

        // Find a dark surface location 10-30 blocks away
        for (int attempt = 0; attempt < 20; attempt++) {
            int ox = RANDOM.nextInt(40) - 20;
            int oz = RANDOM.nextInt(40) - 20;
            BlockPos surface = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                player.blockPosition().offset(ox, 0, oz)
            );
            BlockPos below = surface.below();

            // Must be solid ground and in relative darkness
            if (!level.getBlockState(below).isSolid()) continue;
            if (level.getBlockState(surface).is(Blocks.AIR)) {
                // Place a chest here
                level.setBlock(surface, Blocks.CHEST.defaultBlockState(), 3);
                BlockEntity be = level.getBlockEntity(surface);
                if (be instanceof ChestBlockEntity chest) {
                    ItemStack book = buildJournalBook(chapter);
                    chest.setItem(RANDOM.nextInt(27), book);
                    // Hint to the player
                    StoryManager.scheduleMessage(player, 100 + RANDOM.nextInt(200),
                        "\u00a78[SIGNAL_LOST]: Something has been left near you.");
                }
                return;
            }
        }
    }

    private static ItemStack buildJournalBook(int chapter) {
        String[] pages = JOURNAL_PAGES[chapter];
        List<net.minecraft.util.Unit> dummy = new ArrayList<>();

        List<Filterable<Component>> pageList = new ArrayList<>();
        for (String page : pages) {
            pageList.add(Filterable.passThrough(Component.literal(page)));
        }

        String title = switch (chapter) {
            case 1 -> "DR_VALE.LOG — ENTRY 4";
            case 2 -> "DR_VALE.LOG — ENTRY 9";
            case 3 -> "DR_VALE.LOG — ENTRY 17";
            case 4 -> "DR_VALE.LOG — FINAL";
            default -> "[CORRUPTED]";
        };

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
            Filterable.passThrough(title),
            "DR. ORIN VALE",
            0,
            pageList,
            true
        ));
        return book;
    }
}
