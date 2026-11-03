package com.example.bettercooldownhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BetterCooldownHudClient implements ClientModInitializer {
    private static final int GREY = 0x66666666;
    private static final float TWO_PI = (float) (Math.PI * 2.0);

    /** One ring pixel with its clockwise position from the top (0 to 1). */
    private record Dot(int x, int y, float fraction) {}

    // Precomputed ring, rebuilt only when size, radius, or thickness changes
    private static final List<Dot> dots = new ArrayList<>();
    private static int builtWidth = -1;
    private static int builtHeight = -1;
    private static int builtRadius = -1;
    private static float builtThickness = -1f;

    @Override
    public void onInitializeClient() {
        BetterCooldownConfig.load();
        HudRenderCallback.EVENT.register((context, tickCounter) ->
                renderRing(context, tickCounter.getTickProgress(false)));
    }

    private static void renderRing(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.currentScreen != null) {
            return;
        }

        float progress = player.getAttackCooldownProgress(tickDelta);
        if (!BetterCooldownConfig.alwaysShow && progress >= 1.0f) {
            return;
        }

        ensureBuilt(client);

        // Dots are sorted clockwise, so the first N dots are the charged part
        int filledCount = (int) (progress * dots.size());
        int filledColor = BetterCooldownConfig.currentColor();

        for (int i = 0; i < dots.size(); i++) {
            Dot d = dots.get(i);
            context.fill(d.x(), d.y(), d.x() + 1, d.y() + 1, i < filledCount ? filledColor : GREY);
        }
    }

    private static void ensureBuilt(MinecraftClient client) {
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int radius = BetterCooldownConfig.radius;
        float thickness = BetterCooldownConfig.thickness;

        if (width == builtWidth && height == builtHeight
                && radius == builtRadius && thickness == builtThickness) {
            return;
        }

        dots.clear();
        int cx = width / 2;
        int cy = height / 2;
        float half = thickness / 2.0f;
        int reach = (int) Math.ceil(radius + half) + 1;

        for (int py = cy - reach; py <= cy + reach; py++) {
            for (int px = cx - reach; px <= cx + reach; px++) {
                // Measure from the pixel center so the ring is symmetric around the crosshair
                float dx = px + 0.5f - cx;
                float dy = py + 0.5f - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                // Hard edge: a pixel is either in the ring or not, so the edge stays sharp
                if (Math.abs(dist - radius) > half) {
                    continue;
                }

                float angle = (float) Math.atan2(dx, -dy);
                if (angle < 0.0f) {
                    angle += TWO_PI;
                }
                dots.add(new Dot(px, py, angle / TWO_PI));
            }
        }

        dots.sort(Comparator.comparingDouble(Dot::fraction));

        builtWidth = width;
        builtHeight = height;
        builtRadius = radius;
        builtThickness = thickness;
    }
}
