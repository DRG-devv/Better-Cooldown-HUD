package com.example.bettercooldownhud;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class BetterCooldownConfig {
    private static final String HEADER = "Better Cooldown HUD settings. "
            + "radius: ring size in pixels (4-60). "
            + "always_show: true = ring always visible, false = only while recharging.";

    public static int radius = 14;
    public static boolean alwaysShow = false;

    private BetterCooldownConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bettercooldownhud.properties");
    }

    public static void load() {
        Path path = path();
        Properties props = new Properties();
        props.setProperty("radius", "14");
        props.setProperty("always_show", "false");

        try {
            if (Files.exists(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    props.load(in);
                }
            } else {
                save();
                return;
            }
        } catch (Exception e) {
            // Use defaults if the file can't be read
        }

        radius = clamp(parseInt(props, "radius", 14), 4, 60);
        alwaysShow = Boolean.parseBoolean(props.getProperty("always_show", "false").trim());
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("radius", String.valueOf(radius));
        props.setProperty("always_show", String.valueOf(alwaysShow));

        try {
            Path path = path();
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, HEADER);
            }
        } catch (Exception e) {
            // Settings still apply for this session if the file can't be written
        }
    }

    private static int parseInt(Properties props, String key, int fallback) {
        try {
            return Integer.parseInt(props.getProperty(key).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
