package com.example.bettercooldownhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class BetterCooldownScreen extends Screen {
    private static final int SWATCH = 12;
    private static final int GRID = BetterCooldownConfig.GRID;
    private static final int YELLOW = 0xFFFFE04D;
    private static final int GREY = 0xFF888888;

    private final Screen parent;
    private int cell;
    private int gridX;
    private int gridY;
    private int rowY;
    private int leftX;
    private int rightX;
    private int buttonW;

    public BetterCooldownScreen(Screen parent) {
        super(Text.literal("Better Cooldown HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cell = Math.max(10, Math.min(22, (this.height - 200) / GRID));
        int gridPixels = cell * GRID;
        gridX = this.width / 2 - gridPixels / 2;
        gridY = 20;

        rowY = gridY + gridPixels + 12;
        buttonW = 130;
        leftX = this.width / 2 - buttonW - 4;
        rightX = this.width / 2 + 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("Crosshair"), b ->
                        BetterCooldownConfig.crosshairColor = nextColour(BetterCooldownConfig.crosshairColor))
                .dimensions(leftX, rowY, buttonW, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cooldown"), b ->
                        BetterCooldownConfig.cooldownColor = nextColour(BetterCooldownConfig.cooldownColor))
                .dimensions(rightX, rowY, buttonW, 20).build());

        addDrawableChild(ButtonWidget.builder(sizeText(), b -> {
            BetterCooldownConfig.crosshairSize = (BetterCooldownConfig.crosshairSize + 1) % 5;
            b.setMessage(sizeText());
        }).dimensions(leftX, rowY + 24, buttonW, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset to default"), b -> {
            BetterCooldownConfig.shape.clear();
            BetterCooldownConfig.shape.addAll(BetterCooldownConfig.defaultPlus());
            BetterCooldownConfig.start = -1;
        }).dimensions(rightX, rowY + 24, buttonW, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(this.width / 2 - 60, rowY + 48, 120, 20).build());
    }

    private Text sizeText() {
        int size = BetterCooldownConfig.crosshairSize;
        return Text.literal("Size: " + (size == 0 ? "Default" : String.valueOf(size)));
    }

    private static int nextColour(int current) {
        int[] palette = BetterCooldownConfig.PALETTE;
        int index = 0;
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] == current) {
                index = i;
                break;
            }
        }
        return palette[(index + 1) % palette.length];
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        List<Integer> shape = BetterCooldownConfig.activeShape();

        // Dark grid background
        for (int gy = 0; gy < GRID; gy++) {
            for (int gx = 0; gx < GRID; gx++) {
                int x = gridX + gx * cell;
                int y = gridY + gy * cell;
                context.fill(x, y, x + cell - 1, y + cell - 1, 0xFF0A0A0A);
            }
        }

        // The crosshair being edited, in the crosshair colour
        for (int idx : shape) {
            fillCell(context, idx, BetterCooldownConfig.crosshairColor);
        }

        // Grey outline on the centre cell (0,0)
        outlineCell(context, BetterCooldownConfig.ORIGIN_INDEX, GREY);

        // Yellow outline on the cell where the cooldown starts
        outlineCell(context, BetterCooldownConfig.startCell(), YELLOW);

        // Colour swatches beside the colour buttons
        drawSwatch(context, leftX + buttonW - SWATCH - 6, rowY + 4, BetterCooldownConfig.crosshairColor);
        drawSwatch(context, rightX + buttonW - SWATCH - 6, rowY + 4, BetterCooldownConfig.cooldownColor);

        int noteY = rowY + 74;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Left-click a painted cell: set where the cooldown starts (yellow)"),
                this.width / 2, noteY, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Left-click an empty cell: paint   |   Right-click: erase"),
                this.width / 2, noteY + 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("The cooldown sweeps left to right through every painted cell"),
                this.width / 2, noteY + 24, 0xFFFFFF);
    }

    private void fillCell(DrawContext context, int idx, int colour) {
        int x = gridX + (idx % GRID) * cell;
        int y = gridY + (idx / GRID) * cell;
        context.fill(x, y, x + cell - 1, y + cell - 1, colour);
    }

    private void outlineCell(DrawContext context, int idx, int colour) {
        int x = gridX + (idx % GRID) * cell;
        int y = gridY + (idx / GRID) * cell;
        context.fill(x - 1, y - 1, x + cell, y, colour);
        context.fill(x - 1, y + cell - 1, x + cell, y + cell, colour);
        context.fill(x - 1, y - 1, x, y + cell, colour);
        context.fill(x + cell - 1, y - 1, x + cell, y + cell, colour);
    }

    private static void drawSwatch(DrawContext context, int x, int y, int colour) {
        context.fill(x - 1, y - 1, x + SWATCH + 1, y + SWATCH + 1, 0xFF000000);
        context.fill(x, y, x + SWATCH, y + SWATCH, colour);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (handleClick(click.x(), click.y(), click.button() == 1, false)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (handleClick(click.x(), click.y(), click.button() == 1, true)) {
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    /**
     * Left click on an empty cell paints it. Left click on a painted cell sets the start (not while dragging).
     * Right click erases a cell, but never the last one.
     */
    private boolean handleClick(double mouseX, double mouseY, boolean right, boolean dragging) {
        int idx = cellAt(mouseX, mouseY);
        if (idx < 0) {
            return false;
        }

        List<Integer> shape = BetterCooldownConfig.shape;
        if (right) {
            if (shape.size() > 1 && shape.remove(Integer.valueOf(idx))) {
                if (idx == BetterCooldownConfig.start) {
                    BetterCooldownConfig.start = -1;
                }
            }
            return true;
        }

        if (shape.contains(idx)) {
            if (!dragging) {
                BetterCooldownConfig.start = idx;
            }
        } else {
            shape.add(idx);
        }
        return true;
    }

    private int cellAt(double mouseX, double mouseY) {
        int gx = (int) Math.floor((mouseX - gridX) / cell);
        int gy = (int) Math.floor((mouseY - gridY) / cell);
        if (gx < 0 || gy < 0 || gx >= GRID || gy >= GRID) {
            return -1;
        }
        return gy * GRID + gx;
    }

    @Override
    public void close() {
        BetterCooldownConfig.save();
        MinecraftClient.getInstance().setScreen(parent);
    }
}
