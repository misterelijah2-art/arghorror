package arghorror.client;

import net.fabricmc.api.ClientModInitializer;

public class ArghorrorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SanityHud.register();
    }
}
