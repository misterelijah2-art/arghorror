package arghorror.client;

import arghorror.network.SanityPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class SanityHud {

    private static int clientSanity = 100;
    private static final ResourceLocation LAYER_ID =
        ResourceLocation.fromNamespaceAndPath("arghorror", "sanity_hud");

    public static void register() {
        // Receive sanity value from server
        ClientPlayNetworking.registerGlobalReceiver(SanityPacket.TYPE, (payload, context) -> {
            clientSanity = payload.sanity();
        });

        // Register HUD layer
        HudLayerRegistrationCallback.EVENT.register(layeredDraw -> {
            layeredDraw.addAfter(
                net.minecraft.client.gui.LayeredDraw.CHAT_PANEL,
                IdentifiedLayer.of(LAYER_ID, SanityHud::renderHud)
            );
        });
    }

    private static void renderHud(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int screenWidth  = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: bottom-left, just above hotbar
        int x = 10;
        int y = screenHeight - 55;

        // Label
        String label = "SANITY";
        // Pick color based on level
        int labelColor;
        if (clientSanity > 60)      labelColor = 0xAAAAAA; // grey
        else if (clientSanity > 30) labelColor = 0xFF8800; // orange
        else                        labelColor = 0xFF2222; // red

        graphics.drawString(mc.font, label, x, y, labelColor, false);

        // Bar background (dark)
        int barX = x;
        int barY = y + 10;
        int barWidth = 60;
        int barHeight = 4;
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x88000000);

        // Bar fill
        int fillWidth = (int)(barWidth * (clientSanity / 100.0f));
        int fillColor;
        if (clientSanity > 60)      fillColor = 0xFF55FF55; // green
        else if (clientSanity > 30) fillColor = 0xFFFF8800; // orange
        else                        fillColor = 0xFFFF2222; // red

        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);
        }

        // Numeric value — only show when sanity is low for extra dread
        if (clientSanity <= 30) {
            String val = clientSanity + "%";
            graphics.drawString(mc.font, val, barX + barWidth + 4, barY - 1, 0xFFFF2222, false);
        }
    }
}
