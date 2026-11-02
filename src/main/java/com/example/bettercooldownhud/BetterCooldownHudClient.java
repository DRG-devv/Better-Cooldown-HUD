package com.example.bettercooldownhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

public class BetterCooldownHudClient implements ClientModInitializer {
    private static final int GREY = 0x66666666;
    private static final float TWO_PI = (float) (Math.PI * 2.0);

    @Override
    public void onInitializeClient() {
        BetterCooldownConfig.load();
        HudRenderCallback.EVENT.register((context, tickCounter) ->
                renderRing(context, tickCounter.getTickProgress(false)));
    }

    /** True when the ring is drawn, so the vanilla crosshair can be hidden. */
    public static boolean isActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.world != null && client.currentScreen == null;
    }

    private static void renderRing(DrawContext context, float tickDelta) {
        if (!isActive()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        float progress = player.getAttackCooldownProgress(tickDelta);
        if (!BetterCooldownConfig.alwaysShow && progress >= 1.0f) {
            return;
        }

        // Exact center of the screen, including half pixels, so the ring is centered
        float cx = client.getWindow().getScaledWidth() / 2.0f;
        float cy = client.getWindow().getScaledHeight() / 2.0f;
        float radius = BetterCooldownConfig.radius;
        float half = BetterCooldownConfig.thickness / 2.0f;
        int filled = BetterCooldownConfig.currentColor();

        int minX = (int) Math.floor(cx - radius - half - 1);
        int maxX = (int) Math.ceil(cx + radius + half + 1);
        int minY = (int) Math.floor(cy - radius - half - 1);
        int maxY = (int) Math.ceil(cy + radius + half + 1);

        for (int py = minY; py < maxY; py++) {
            for (int px = minX; px < maxX; px++) {
                float dx = px + 0.5f - cx;
                float dy = py + 0.5f - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                // How far this pixel sits inside the ring edge (anti-aliasing)
                float edge = half - Math.abs(dist - radius);
                float coverage = Math.min(1.0f, edge + 0.5f);
                if (coverage <= 0.0f) {
                    continue;
                }

                // Clockwise angle from the top, 0 to 1
                float angle = (float) Math.atan2(dx, -dy);
                if (angle < 0.0f) {
                    angle += TWO_PI;
                }
                float fraction = angle / TWO_PI;

                int base = fraction < progress ? filled : GREY;
                int alpha = (int) (((base >>> 24) & 0xFF) * coverage);
                int color = (alpha << 24) | (base & 0xFFFFFF);
                context.fill(px, py, px + 1, py + 1, color);
            }
        }
    }
}
