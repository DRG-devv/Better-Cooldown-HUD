package com.example.bettercooldownhud;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public final class BetterCooldownConfig {
    public static final int GRID = 11;
    public static final int ORIGIN = GRID / 2;
    public static final int ORIGIN_INDEX = ORIGIN * GRID + ORIGIN;

    public static final int[] PALETTE = {
            0xFF3366FF, 0xFFFF3333, 0xFF33CC33, 0xFFFFE04D, 0xFFFF9A1F,
            0xFF33DDFF, 0xFFAA44FF, 0xFFFF66CC, 0xFFFFFFFF, 0xFF000000
    };

    private static final String HEADER = "Better Cooldown HUD settings. "
            + "shape: painted cell indices. start: cell where the cooldown starts (-1 = centre). "
            + "crosshair_color / cooldown_color: ARGB hex, e.g. FF3366FF. "
            + "crosshair_size: 0 = default (1 px per cell), up to 4.";

    public static int crosshairColor = 0xFF3366FF;
    public static int cooldownColor = 0xFFFF3333;
    public static int crosshairSize = 0;
    public static int start = -1;
    public static final List<Integer> shape = new ArrayList<>();

    private BetterCooldownConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bettercooldownhud.properties");
    }

    /** The default plus sign (9 px across, centred on the origin), as grid cell indices. */
    public static List<Integer> defaultPlus() {
        List<Integer> plus = new ArrayList<>();
        plus.add(ORIGIN_INDEX);
        for (int d = 1; d <= 4; d++) {
            plus.add(ORIGIN_INDEX + d);
            plus.add(ORIGIN_INDEX - d);
            plus.add(ORIGIN_INDEX + d * GRID);
            plus.add(ORIGIN_INDEX - d * GRID);
        }
        return plus;
    }

    public static void load() {
        Path path = path();
        Properties props = new Properties();
        props.setProperty("shape", join(defaultPlus()));
        props.setProperty("start", "-1");
        props.setProperty("crosshair_color", "FF3366FF");
        props.setProperty("cooldown_color", "FFFF3333");
        props.setProperty("crosshair_size", "0");

        try {
            if (Files.exists(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    props.load(in);
                }
            } else {
                shape.clear();
                shape.addAll(defaultPlus());
                save();
                return;
            }
        } catch (Exception e) {
            // Use defaults if the file can't be read
        }

        shape.clear();
        for (String part : props.getProperty("shape", "").split(",")) {
            int idx = parseInt(part, -1);
            if (idx >= 0 && idx < GRID * GRID && !shape.contains(idx)) {
                shape.add(idx);
            }
        }
        if (shape.isEmpty()) {
            shape.addAll(defaultPlus());
        }
        start = parseInt(props.getProperty("start"), -1);
        crosshairColor = parseHex(props.getProperty("crosshair_color"), crosshairColor);
        cooldownColor = parseHex(props.getProperty("cooldown_color"), cooldownColor);
        crosshairSize = Math.max(0, Math.min(4, parseInt(props.getProperty("crosshair_size"), 0)));
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("shape", join(shape));
        props.setProperty("start", String.valueOf(start));
        props.setProperty("crosshair_color", String.format("%08X", crosshairColor));
        props.setProperty("cooldown_color", String.format("%08X", cooldownColor));
        props.setProperty("crosshair_size", String.valueOf(crosshairSize));

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

    /** The painted crosshair, or the default plus if nothing is painted. */
    public static List<Integer> activeShape() {
        return shape.isEmpty() ? defaultPlus() : shape;
    }

    /** The cell the cooldown starts from: the clicked cell if still painted, otherwise the centre. */
    public static int startCell() {
        List<Integer> cells = activeShape();
        if (start >= 0 && cells.contains(start)) {
            return start;
        }
        return cells.contains(ORIGIN_INDEX) ? ORIGIN_INDEX : cells.get(0);
    }

    /**
     * Every painted cell, ordered left to right (by column, then row), starting at the start cell.
     * After the rightmost column it wraps back to the leftmost.
     */
    public static List<Integer> sweepOrder() {
        List<Integer> sorted = new ArrayList<>(activeShape());
        sorted.sort(Comparator.comparingInt((Integer i) -> i % GRID).thenComparingInt(i -> i / GRID));

        int startPos = sorted.indexOf(startCell());
        List<Integer> order = new ArrayList<>(sorted.subList(startPos, sorted.size()));
        order.addAll(sorted.subList(0, startPos));
        return order;
    }

    private static String join(List<Integer> cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(cells.get(i));
        }
        return sb.toString();
    }

    private static int parseHex(String value, int fallback) {
        try {
            return (int) Long.parseLong(value.trim(), 16);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
