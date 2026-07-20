package arghorror;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Arghorror implements ModInitializer {
    public static final String MOD_ID = "arghorror";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        StoryManager.register();
        SanitySystem.register();
        GlitchEvents.register();
        LOGGER.info("SIGNAL_LOST initialized.");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
