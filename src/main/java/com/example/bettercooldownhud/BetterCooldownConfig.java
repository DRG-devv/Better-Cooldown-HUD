package com.example.bettercooldownhud;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class BetterCooldownConfig {
    public static final String[] COLOR_NAMES = {
            "Red", "Orange", "Yellow", "Green", "Cyan",
            "Blue", "Purple", "Pink", "White", "Black"
    };
    public static final int[] PALETTE = {
            0xFFFF3B3B, 0xFFFF9A1F, 0xFFFFE04D, 0xFF33CC33, 0xFF33DDFF,
            0xFF3366FF, 0xFFAA44FF, 0xFFFF66CC, 0xFFFFFFFF, 0xFF000000
    };

    private static final String HEADER = "Better Cooldown HUD settings. "
            + "always_show: true = ring always visible, false = hidden when ready. "
            + "radius: 4-60. thickness: 1.0-8.0. "
            + "color: Red, Orange, Yellow, Green, Cyan, Blue, Purple, Pink, White, Black.";

    public static boolean alwaysShow = false;
    public static int radius = 14;
    public static float thickness = 2.0f;
    public static int colorIndex = 3;

    private BetterCooldownConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bettercooldownhud.properties");
    }

    public static void load() {
        Path path = path();
        Properties props = new Properties();
        props.setProperty("always_show", "false");
        props.setProperty("radius", "14");
        props.setProperty("thickness", "2.0");
        props.setProperty("color", "Green");

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

        alwaysShow = Boolean.parseBoolean(props.getProperty("always_show", "false").trim());
        radius = clamp(parseInt(props, "radius", 14), 4, 60);
        thickness = clampF(parseFloat(props, "thickness", 2.0f), 1.0f, 8.0f);
        colorIndex = indexOfColor(props.getProperty("color", "Green").trim());
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("always_show", String.valueOf(alwaysShow));
        props.setProperty("radius", String.valueOf(radius));
        props.setProperty("thickness", String.valueOf(thickness));
        props.setProperty("color", COLOR_NAMES[colorIndex]);

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

    public static int currentColor() {
        return PALETTE[colorIndex];
    }

    private static int indexOfColor(String name) {
        for (int i = 0; i < COLOR_NAMES.length; i++) {
            if (COLOR_NAMES[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return 3;
    }

    private static int parseInt(Properties props, String key, int fallback) {
        try {
            return Integer.parseInt(props.getProperty(key).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static float parseFloat(Properties props, String key, float fallback) {
        try {
            return Float.parseFloat(props.getProperty(key).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clampF(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
