package com.example.bettercooldownhud.mixin;

import com.example.bettercooldownhud.BetterCooldownHudClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void bettercooldownhud$hideCrosshair(CallbackInfo ci) {
        if (BetterCooldownHudClient.isActive()) {
            ci.cancel();
        }
    }
}
