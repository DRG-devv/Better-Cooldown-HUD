package com.example.bettercooldownhud;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

public class BetterCooldownHudClient implements ClientModInitializer {
    private static final Identifier RING = Identifier.of("bettercooldownhud", "ring");
    private static final RenderPipeline PIPELINE = RenderPipelines.GUI_TEXTURED;

    // Size of the ring image in pixels
    private static final int TEXTURE_SIZE = 128;

    // Distance from the image centre to the ring's centre line, in pixels
    private static final float RING_CENTRE_IN_TEXTURE = 56f;

    // Faint white ring shown behind the progress
    private static final int BASE_COLOR = 0x66FFFFFF;

    // Fine-tune in pixels after measuring; 0 means exactly on the crosshair centre
    private static final int FINE_X = 0;
    private static final int FINE_Y = 0;

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

        // Origin: the exact centre of the screen, where the crosshair sits
        int originX = client.getWindow().getScaledWidth() / 2;
        int originY = client.getWindow().getScaledHeight() / 2;

        // Scale the image so its centre line matches the radius setting
        int size = Math.round(BetterCooldownConfig.radius * TEXTURE_SIZE / RING_CENTRE_IN_TEXTURE) * 2;

        // Top-left corner of the image, measured from the origin
        int x = originX - size / 2 + FINE_X;
        int y = originY - size / 2 + FINE_Y;

        // Faint full ring behind
        context.drawGuiTexture(PIPELINE, RING, x, y, size, size, BASE_COLOR);

        // Coloured ring revealed from the bottom up, like the vanilla cooldown
        int visibleTex = Math.round(TEXTURE_SIZE * progress);
        if (visibleTex <= 0) {
            return;
        }
        int v = TEXTURE_SIZE - visibleTex;
        int visibleHeight = Math.round(size * progress);
        int fillColor = BetterCooldownConfig.currentColor() | 0xFF000000;

        context.drawGuiTexture(PIPELINE, RING, TEXTURE_SIZE, TEXTURE_SIZE, 0, v,
                x, y + size - visibleHeight, size, visibleHeight, fillColor);
    }
}
