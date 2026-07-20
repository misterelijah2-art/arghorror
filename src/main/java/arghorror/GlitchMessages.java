package arghorror;

import java.util.Random;

public class GlitchMessages {

    private static final Random RANDOM = new Random();

    public static final String CHAPTER_0_MSG =
        "\u00a78[SIGNAL_LOST]: T\u0337h\u0338e\u0337 \u0335w\u0336o\u0334r\u0338l\u0337d\u0335 \u0338r\u0336e\u0334m\u0335e\u0336m\u0337b\u0338e\u0335r\u0336s\u0338 \u0337y\u0334o\u0335u\u0336.";

    public static final String VALE_LOG_1 =
        "Day 3. The coordinates keep changing. I think the map is lying to me.";
    public static final String VALE_FINAL_LOG =
        "\u00a78[DR_VALE]: Day 31. I haven't slept. The blocks moved again. Something is building.";
    public static final String ARCHITECT_AWARE =
        "\u00a74[UNKNOWN]: I see you found his notes. Good. That saves time.";

    public static final String ARCHITECT_MSG_1 =
        "\u00a74[THE ARCHITECT]: You were not supposed to go this far.";
    public static final String ARCHITECT_MSG_2 =
        "\u00a74[THE ARCHITECT]: D\u0337R\u0338.\u0335 \u0337V\u0338A\u0334L\u0335E\u0336 \u0334t\u0337r\u0338i\u0335e\u0336d\u0334 \u0335t\u0336o\u0337 \u0334l\u0335e\u0336a\u0337v\u0338e\u0335.\u0334 \u0337H\u0338e\u0335 \u0334c\u0335o\u0336u\u0337l\u0334d\u0335n\u0336'\u0337t\u0338.";

    public static final String CHAPTER_4_MSG =
        "\u00a74[SYSTEM]: R\u0335E\u0337A\u0336L\u0338I\u0334T\u0337Y\u0336 \u0338C\u0337H\u0334E\u0335C\u0336K\u0338 \u0334F\u0337A\u0335I\u0338L\u0336E\u0334D\u0337";

    public static final String FINAL_MSG_1 =
        "\u00a74\u00a7l[THE ARCHITECT]: CHAPTER FINAL. YOU REACHED THE END OF THE RECORD.";
    public static final String FINAL_MSG_2 =
        "\u00a74[THE ARCHITECT]: T\u0335h\u0338e\u0337r\u0334e\u0335 \u0336i\u0338s\u0337 \u0334n\u0335o\u0336 \u0338e\u0337x\u0334i\u0335t\u0336.\u0338 \u0334T\u0337h\u0335e\u0336r\u0338e\u0334 \u0335w\u0336a\u0337s\u0338 \u0334n\u0335e\u0336v\u0338e\u0337r\u0334 \u0335a\u0336n\u0337 \u0338e\u0334x\u0335i\u0336t\u0337.";
    public static final String FINAL_MSG_3 =
        "\u00a74[THE ARCHITECT]: \u00a7lI HAVE BEEN WATCHING SINCE DAY ONE.";

    public static final String ZERO_SANITY_MSG =
        "y\u0337o\u0334u\u0335 \u0338c\u0337a\u0334n\u0335'\u0336t\u0338 \u0334t\u0337r\u0335u\u0336s\u0338t\u0334 \u0335w\u0336h\u0337a\u0338t\u0334 \u0335y\u0336o\u0337u\u0338 \u0334s\u0335e\u0336e\u0337";

    private static final String[] GLITCH_POOL = {
        "\u00a78h\u0338e\u0337 \u0335i\u0336s\u0338 \u0334b\u0337e\u0335h\u0336i\u0338n\u0334d\u0335 \u0336y\u0337o\u0338u\u0334",
        "\u00a78d\u0337o\u0336n\u0338'\u0334t\u0335 \u0336l\u0337o\u0338o\u0334k\u0335",
        "\u00a78t\u0338h\u0337e\u0335 \u0336w\u0334o\u0337r\u0338l\u0335d\u0336 \u0334i\u0337s\u0338 \u0335n\u0336o\u0334t\u0337 \u0338r\u0335e\u0336a\u0334l\u0337",
        "\u00a78s\u0335o\u0337m\u0334e\u0338t\u0336h\u0337i\u0335n\u0336g\u0338 \u0334w\u0337a\u0335t\u0336c\u0338h\u0334e\u0337s\u0335",
        "\u00a78D\u0334R\u0337_\u0335V\u0338A\u0336L\u0334E\u0337 \u0335W\u0336A\u0338S\u0334 \u0337H\u0335E\u0336R\u0338E\u0334",
        "\u00a78e\u0338r\u0337r\u0335o\u0336r\u0334:\u0337 \u0338e\u0335n\u0336t\u0334i\u0337t\u0338y\u0335 \u0336n\u0334o\u0337t\u0338 \u0335f\u0336o\u0334u\u0337n\u0338d\u0335",
        "\u00a78w\u0335h\u0337y\u0336 \u0338a\u0334r\u0337e\u0335 \u0336y\u0338o\u0334u\u0337 \u0335s\u0336t\u0338i\u0334l\u0337l\u0335 \u0336h\u0338e\u0334r\u0337e\u0335",
        "\u00a78[NULL]: \u0338\u0337\u0336\u0335\u0334",
        "\u00a78t\u0337h\u0335e\u0336 \u0338m\u0334a\u0337p\u0335 \u0336i\u0338s\u0334 \u0337w\u0335r\u0336o\u0338n\u0334g\u0337",
        "\u00a78i\u0335t\u0336 \u0338s\u0334e\u0337e\u0335s\u0336 \u0338y\u0334o\u0337u\u0335r\u0336 \u0338s\u0334c\u0337r\u0335e\u0336e\u0338n\u0334"
    };

    private static final String[] SYSTEM_GLITCH_POOL = {
        "MEMORY OVERFLOW AT 0x00000000",
        "ENTITY COUNT MISMATCH — expected 1, found 2",
        "CHUNK DATA CORRUPTED — rollback failed",
        "PLAYER RECORD NOT FOUND IN DATABASE",
        "WARNING: OBSERVER DETECTED",
        "SIMULATION INTEGRITY: 12%",
        "DR_VALE.LOG — ACCESS DENIED",
        "UNKNOWN ENTITY IN SECTOR 7",
        "PATHFINDING ERROR: DESTINATION OCCUPIED BY OBSERVER",
        "HEARTBEAT SIGNAL DETECTED — SOURCE: NULL",
        "WARNING: ENTITY theArchitect HAS NO REGISTERED SPAWN CONDITIONS",
        "TICK DESYNC — ENTITY POSITION UNDEFINED"
    };

    public static String randomGlitch() {
        return GLITCH_POOL[RANDOM.nextInt(GLITCH_POOL.length)];
    }

    public static String randomSystemGlitch() {
        return SYSTEM_GLITCH_POOL[RANDOM.nextInt(SYSTEM_GLITCH_POOL.length)];
    }

    public static String corruptName(String name) {
        StringBuilder sb = new StringBuilder("\u00a74[THE ARCHITECT]: I know your name. ");
        for (char c : name.toCharArray()) {
            sb.append(c).append('\u0337');
        }
        return sb.toString();
    }
}
