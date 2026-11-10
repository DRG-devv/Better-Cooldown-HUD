package com.example.bettercooldownhud;

import net.fabricmc.api.ClientModInitializer;

public class BetterCooldownHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterCooldownConfig.load();
    }
}
