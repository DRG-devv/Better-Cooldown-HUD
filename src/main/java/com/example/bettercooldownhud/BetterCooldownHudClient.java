package com.example.bettercooldownhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

public class BetterCooldownHudClient implements ClientModInitializer {
    private static final int RADIUS = 14;
    private static final int GREY = 0xAA444444;
    private static final int GREEN = 0xFF33CC33;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((context, tickCounter) ->
                renderRing(context, tickCounter.getTickProgress(false)));
    }

    private static void renderRing(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.currentScreen != null) {
            return;
        }

        float progress = player.getAttackCooldownProgress(tickDelta);
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;

        // Draw one dot per degree, starting at the top and going clockwise
        for (int deg = 0; deg < 360; deg++) {
            int color = deg < progress * 360f ? GREEN : GREY;
            double rad = Math.toRadians(deg - 90);
            int x = cx + (int) Math.round(Math.cos(rad) * RADIUS);
            int y = cy + (int) Math.round(Math.sin(rad) * RADIUS);
            context.fill(x - 1, y - 1, x + 1, y + 1, color);
        }
    }
}
