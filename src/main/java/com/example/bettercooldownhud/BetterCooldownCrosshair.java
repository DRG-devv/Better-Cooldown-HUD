package com.example.bettercooldownhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;

public final class BetterCooldownCrosshair {
    private BetterCooldownCrosshair() {}

    /** Draws the custom crosshair. The cooldown spreads outward from the centre, reaching every side at the same time. */
    public static void render(DrawContext context, MinecraftClient client, float tickDelta) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        List<Integer> cells = BetterCooldownConfig.activeShape();
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        int px = Math.max(1, BetterCooldownConfig.crosshairSize);

        float progress = player.getAttackCooldownProgress(tickDelta);

        // The farthest painted cell sets the scale; the spread reaches it at 100%
        double maxDist = 0;
        for (int idx : cells) {
            maxDist = Math.max(maxDist, BetterCooldownConfig.distanceFromOrigin(idx));
        }
        double reach = progress * maxDist;

        for (int idx : cells) {
            int gx = idx % BetterCooldownConfig.GRID - BetterCooldownConfig.ORIGIN;
            int gy = idx / BetterCooldownConfig.GRID - BetterCooldownConfig.ORIGIN;
            int sx = cx + gx * px;
            int sy = cy + gy * px;

            boolean ready = progress >= 1.0f
                    || (progress > 0.0f && BetterCooldownConfig.distanceFromOrigin(idx) <= reach);
            int colour = ready ? BetterCooldownConfig.crosshairColor : BetterCooldownConfig.cooldownColor;
            context.fill(sx, sy, sx + px, sy + px, colour);
        }
    }
}
