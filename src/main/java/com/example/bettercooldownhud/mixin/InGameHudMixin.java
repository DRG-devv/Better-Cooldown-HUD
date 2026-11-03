package com.example.bettercooldownhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void bettercooldownhud$replaceCrosshair(DrawContext context, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.currentScreen != null) {
            return;
        }

        // Skip the vanilla crosshair, which includes the attack ring, and draw a plain one
        ci.cancel();
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        int white = 0xFFFFFFFF;

        // Horizontal and vertical bars, 1 pixel thick, 9 pixels long
        context.fill(cx - 4, cy, cx + 5, cy + 1, white);
        context.fill(cx, cy - 4, cx + 1, cy + 5, white);
    }
}
