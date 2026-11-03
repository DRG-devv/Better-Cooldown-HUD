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

    // Faint white ring shown behind the progress
    private static final int BASE_COLOR = 0x66FFFFFF;

    // Fine-tune the ring position in pixels (try -1, 0, or 1)
    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = 0;

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

        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;

        // The 128 px ring image has its centre line at 56 px from the middle,
        // so scale the image so that line matches the radius setting
        int size = Math.round(BetterCooldownConfig.radius * 128f / 56f) * 2;
        int x = cx - size / 2 + OFFSET_X;
        int y = cy - size / 2 + OFFSET_Y;

        // Dim base ring, then the coloured ring fading in as the cooldown recharges
        context.drawGuiTexture(PIPELINE, RING, x, y, size, size, BASE_COLOR);

        int alpha = Math.round(progress * 255f);
        int fg = (alpha << 24) | (BetterCooldownConfig.currentColor() & 0xFFFFFF);
        context.drawGuiTexture(PIPELINE, RING, x, y, size, size, fg);
    }
}
