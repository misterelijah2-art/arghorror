package arghorror;

import java.util.Random;

public class GlitchMessages {

    private static final Random RANDOM = new Random();

    // Chapter intro messages
    public static final String CHAPTER_0_MSG =
        "§8[SIGNAL_LOST]: " + "T̷h̸e̷ ̵w̶o̸r̷l̸d̵ ̴r̶e̸m̷e̵m̶b̵e̷r̸s̵ ̴y̵o̸u̷.";

    // DR. VALE logs
    public static final String VALE_LOG_1 =
        "Day 3. The coordinates keep changing. I think the map is lying to me.";
    public static final String VALE_FINAL_LOG =
        "§8[DR_VALE]: Day 31. I haven't slept. The blocks moved again. Something is building.";
    public static final String ARCHITECT_AWARE =
        "§4[UNKNOWN]: I see you found his notes. Good. That saves time.";

    // The Architect messages
    public static final String ARCHITECT_MSG_1 =
        "§4[THE ARCHITECT]: You were not supposed to go this far.";
    public static final String ARCHITECT_MSG_2 =
        "§4[THE ARCHITECT]: D̷R̸.̵ ̷V̸A̴L̵E̶ ̴t̸r̵i̴e̷d̵ ̸t̴o̷ ̵l̶e̸a̴v̷e̶.̵ ̴H̵e̸ ̷c̴o̸u̵l̸d̵n̶'̴t̷.";

    // Chapter 4
    public static final String CHAPTER_4_MSG =
        "§4[SYSTEM]: R̵E̷A̶L̸I̴T̵Y̷ ̶C̷H̴E̷C̵K̸ ̴F̷A̵I̸L̶E̵D̸";

    // Final chapter
    public static final String FINAL_MSG_1 =
        "§4§l[THE ARCHITECT]: CHAPTER FINAL. YOU REACHED THE END OF THE RECORD.";
    public static final String FINAL_MSG_2 =
        "§4[THE ARCHITECT]: T̵h̸e̷r̵e̸ ̷i̴s̵ ̶n̸o̷ ̵e̴x̷i̸t̵.̴ ̶T̷h̴e̵r̵e̶ ̸w̷a̴s̵ ̷n̷e̸v̶e̵r̸ ̴a̷n̷ ̶e̷x̴i̸t̷.";
    public static final String FINAL_MSG_3 =
        "§4[THE ARCHITECT]: §lI HAVE BEEN WATCHING SINCE DAY ONE.";

    // Zero sanity
    public static final String ZERO_SANITY_MSG =
        "y̷o̴u̵ ̸c̷a̶n̴'̵t̸ ̴t̵r̸u̷s̵t̴ ̶w̷h̸a̴t̵ ̶y̷o̴u̵ ̶s̵e̴e̷";

    // Random glitch messages
    private static final String[] GLITCH_POOL = {
        "§8h̸e̷ ̵i̶s̸ ̷b̴e̸h̶i̴n̵d̷ ̵y̸o̴u̶",
        "§8d̷o̶n̷'̴t̵ ̷l̵o̵o̷k̸",
        "§8t̸h̵e̷ ̶w̴o̸r̵l̷d̴ ̶i̷s̵ ̸n̴o̶t̷ ̵r̵e̸a̷l̴",
        "§8s̵o̷m̴e̸t̵h̶i̵n̷g̴ ̶w̷a̵t̸c̴h̵e̷s̸",
        "§8D̴R̷_̵V̸A̴L̶E̵ ̷W̴A̵S̶ ̸H̷E̴R̵E̸",
        "§8e̸r̵r̴o̷r̵:̸ ̶e̷n̴t̵i̸t̶y̷ ̵n̴o̸t̵ ̷f̴o̶u̵n̷d̸",
        "§8w̵h̷y̴ ̵a̸r̶e̷ ̴y̵o̶u̸ ̷s̵t̵i̷l̸l̴ ̶h̵e̸r̴e̷",
        "§8[NULL]: ̸̧̛̖̞͎̲̺̳̯̬͔̗̖͚̱̻̀͘͢͟",
        "§8t̷h̵e̸ ̴m̷a̶p̵ ̷i̴s̵ ̶w̷r̴o̸n̵g̷",
        "§8i̵t̶ ̷s̴e̵e̸s̷ ̵y̵o̶u̶r̴ ̸s̷c̵r̴e̶e̵n̷"
    };

    private static final String[] SYSTEM_GLITCH_POOL = {
        "MEMORY OVERFLOW AT 0x00000000",
        "ENTITY COUNT MISMATCH — expected 1, found 2",
        "CHUNK DATA CORRUPTED — rollback failed",
        "PLAYER RECORD NOT FOUND IN DATABASE",
        "WARNING: OBSERVER DETECTED",
        "SIMULATION INTEGRITY: 12%",
        "DR_VALE.LOG — ACCESS DENIED",
        "UNKNOWN ENTITY IN SECTOR 7"
    };

    public static String randomGlitch() {
        return GLITCH_POOL[RANDOM.nextInt(GLITCH_POOL.length)];
    }

    public static String randomSystemGlitch() {
        return SYSTEM_GLITCH_POOL[RANDOM.nextInt(SYSTEM_GLITCH_POOL.length)];
    }

    public static String corruptName(String name) {
        StringBuilder sb = new StringBuilder("§4[THE ARCHITECT]: I know your name. ");
        for (char c : name.toCharArray()) {
            sb.append(c);
            sb.append('̷');
        }
        return sb.toString();
    }
}
