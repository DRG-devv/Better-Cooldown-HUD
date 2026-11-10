package com.example.bettercooldownhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;

public final class BetterCooldownCrosshair {
    private BetterCooldownCrosshair() {}

    /** Draws the custom crosshair, with the cooldown sweep. Called from the HUD mixin in place of vanilla's. */
    public static void render(DrawContext context, MinecraftClient client, float tickDelta) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        List<Integer> cells = BetterCooldownConfig.sweepOrder();
        int count = cells.size();
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        int px = Math.max(1, BetterCooldownConfig.crosshairSize);

        float progress = player.getAttackCooldownProgress(tickDelta);

        // Cells earlier in the sweep are ready (crosshair colour); the rest are still cooling down (cooldown colour)
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
