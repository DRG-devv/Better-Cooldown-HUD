package com.example.bettercooldownhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

public class BetterCooldownHudClient implements ClientModInitializer {
    // How many pixels each arm can fill, measured from the centre pixel
    private static final int ARM_LENGTH = 5;
    private static final int WHITE = 0xFFFFFFFF;

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

        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;

        // Default white crosshair: 9 px arms, 1 px thick
        context.fill(cx - 4, cy, cx + 5, cy + 1, WHITE);
        context.fill(cx, cy - 4, cx + 1, cy + 5, WHITE);

        float progress = player.getAttackCooldownProgress(tickDelta);

        // Fully charged: default crosshair only
        if (progress >= 1.0f || progress <= 0.0f) {
            return;
        }

        // Number of pixels filled on each arm, growing outward as the cooldown recharges
        int filled = Math.min(ARM_LENGTH, (int) Math.ceil(progress * ARM_LENGTH));
        int color = BetterCooldownConfig.currentColor();

        // Centre pixel
        context.fill(cx, cy, cx + 1, cy + 1, color);

        // Each arm, one pixel at a time from the centre outward
        for (int n = 1; n <= filled; n++) {
            context.fill(cx + n, cy, cx + n + 1, cy + 1, color);
            context.fill(cx - n, cy, cx - n + 1, cy + 1, color);
            context.fill(cx, cy + n, cx + 1, cy + n + 1, color);
            context.fill(cx, cy - n, cx + 1, cy - n + 1, color);
        }
    }
}
