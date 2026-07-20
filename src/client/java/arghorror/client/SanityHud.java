package arghorror.client;

import arghorror.network.SanityPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SanityHud {

    private static int clientSanity = 100;

    public static void register() {
        // Receive sanity value from server
        ClientPlayNetworking.registerGlobalReceiver(SanityPacket.TYPE, (payload, context) -> {
            clientSanity = payload.sanity();
        });

        // Register HUD render callback
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> renderHud(graphics));
    }

    private static void renderHud(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int x = 10;
        int y = screenHeight - 55;

        int labelColor;
        if (clientSanity > 60)      labelColor = 0xAAAAAA;
        else if (clientSanity > 30) labelColor = 0xFF8800;
        else                        labelColor = 0xFF2222;

        graphics.drawString(mc.font, "SANITY", x, y, labelColor, false);

        int barY = y + 10;
        int barWidth = 60;
        int barHeight = 4;
        graphics.fill(x, barY, x + barWidth, barY + barHeight, 0x88000000);

        int fillWidth = (int)(barWidth * (clientSanity / 100.0f));
        int fillColor;
        if (clientSanity > 60)      fillColor = 0xFF55FF55;
        else if (clientSanity > 30) fillColor = 0xFFFF8800;
        else                        fillColor = 0xFFFF2222;

        if (fillWidth > 0) {
            graphics.fill(x, barY, x + fillWidth, barY + barHeight, fillColor);
        }

        if (clientSanity <= 30) {
            graphics.drawString(mc.font, clientSanity + "%", x + barWidth + 4, barY - 1, 0xFFFF2222, false);
        }
    }
}
