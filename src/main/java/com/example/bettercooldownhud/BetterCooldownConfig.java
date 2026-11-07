package com.example.bettercooldownhud;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public final class BetterCooldownConfig {
    public static final int GRID = 11;
    public static final int ORIGIN = GRID / 2;          // centre cell, the (0,0) of the grid
    public static final int ORIGIN_INDEX = ORIGIN * GRID + ORIGIN;

    public static final int[] PALETTE = {
            0xFF3366FF, 0xFFFF3333, 0xFF33CC33, 0xFFFFE04D, 0xFFFF9A1F,
            0xFF33DDFF, 0xFFAA44FF, 0xFFFF66CC, 0xFFFFFFFF, 0xFF000000
    };

    private static final String HEADER = "Better Cooldown HUD settings. "
            + "shape: painted cell indices in paint order. "
            + "start: the cell where the cooldown starts (-1 = first painted cell). "
            + "crosshair_color / cooldown_color: ARGB hex, e.g. FF3366FF. "
            + "crosshair_size: 0 = default (1 px per cell), up to 4.";

    public static int crosshairColor = 0xFF3366FF; // blue
    public static int cooldownColor = 0xFFFF3333;  // red
    public static int crosshairSize = 0;
    public static int start = -1;
    public static final List<Integer> shape = new ArrayList<>();

    private BetterCooldownConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bettercooldownhud.properties");
    }

    public static void load() {
        Path path = path();
        Properties props = new Properties();
        props.setProperty("shape", "");
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
        start = parseInt(props.getProperty("start"), -1);
        crosshairColor = parseHex(props.getProperty("crosshair_color"), crosshairColor);
        cooldownColor = parseHex(props.getProperty("cooldown_color"), cooldownColor);
        crosshairSize = Math.max(0, Math.min(4, parseInt(props.getProperty("crosshair_size"), 0)));
    }

    public static void save() {
        Properties props = new Properties();
        StringBuilder cells = new StringBuilder();
        for (int i = 0; i < shape.size(); i++) {
            if (i > 0) {
                cells.append(',');
            }
            cells.append(shape.get(i));
        }
        props.setProperty("shape", cells.toString());
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

    /** The painted shape, or a default plus sign if nothing has been painted yet. */
    public static List<Integer> activeShape() {
        if (!shape.isEmpty()) {
            return shape;
        }
        List<Integer> plus = new ArrayList<>();
        plus.add(ORIGIN_INDEX);
        for (int d = 1; d <= ORIGIN; d++) {
            plus.add(ORIGIN_INDEX + d);
            plus.add(ORIGIN_INDEX - d);
            plus.add(ORIGIN_INDEX + d * GRID);
            plus.add(ORIGIN_INDEX - d * GRID);
        }
        return plus;
    }

    /** The cell the cooldown starts from: the clicked cell if it's still painted, otherwise the first cell. */
    public static int startCell() {
        List<Integer> cells = activeShape();
        if (start >= 0 && cells.contains(start)) {
            return start;
        }
        return cells.get(0);
    }

    /** Cells reachable from the start by touching sides, in order from the start outward. */
    private static List<Integer> reachFromStart() {
        List<Integer> cells = activeShape();
        Set<Integer> painted = new HashSet<>(cells);
        Set<Integer> seen = new HashSet<>();
        List<Integer> order = new ArrayList<>();
        Deque<Integer> queue = new ArrayDeque<>();

        int first = startCell();
        queue.add(first);
        seen.add(first);

        int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            order.add(cur);
            int x = cur % GRID;
            int y = cur / GRID;
            for (int[] s : steps) {
                int nx = x + s[0];
                int ny = y + s[1];
                if (nx < 0 || ny < 0 || nx >= GRID || ny >= GRID) {
                    continue;
                }
                int next = ny * GRID + nx;
                if (painted.contains(next) && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return order;
    }

    /** Full sweep order: connected cells from the start first, then any unconnected cells in paint order. */
    public static List<Integer> sweepOrder() {
        List<Integer> order = reachFromStart();
        Set<Integer> reached = new HashSet<>(order);
        for (int cell : activeShape()) {
            if (!reached.contains(cell)) {
                order.add(cell);
            }
        }
        return order;
    }

    /** True if the centre cell is painted and touches the rest of the drawing from the start. */
    public static boolean originConnected() {
        return reachFromStart().contains(ORIGIN_INDEX);
    }

    public static int currentColorCrosshair() {
        return crosshairColor;
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
