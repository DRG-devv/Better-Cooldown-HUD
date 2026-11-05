package com.example.bettercooldownhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;

public class BetterCooldownHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterCooldownConfig.load();
        HudRenderCallback.EVENT.register((context, tickCounter) ->
                renderCrosshair(context, tickCounter.getTickProgress(false)));
    }

    private static void renderCrosshair(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.currentScreen != null) {
            return;
        }

        List<Integer> cells = BetterCooldownConfig.activeShape();
        int count = cells.size();
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        int px = Math.max(1, BetterCooldownConfig.crosshairSize);

        float progress = player.getAttackCooldownProgress(tickDelta);

        // Cells before this index are ready (crosshair colour); the rest are still cooling down (cooldown colour)
        int ready = Math.min(count, (int) Math.floor(progress * count));

        for (int i = 0; i < count; i++) {
            int idx = cells.get(i);
            int gx = idx % BetterCooldownConfig.GRID - BetterCooldownConfig.ORIGIN;
            int gy = idx / BetterCooldownConfig.GRID - BetterCooldownConfig.ORIGIN;
            int sx = cx + gx * px;
            int sy = cy + gy * px;
            int colour = i < ready ? BetterCooldownConfig.crosshairColor : BetterCooldownConfig.cooldownColor;
            context.fill(sx, sy, sx + px, sy + px, colour);
        }
    }
}
