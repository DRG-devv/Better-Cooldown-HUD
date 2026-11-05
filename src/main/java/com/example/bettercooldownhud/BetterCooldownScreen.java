package com.example.bettercooldownhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class BetterCooldownScreen extends Screen {
    private final Screen parent;
    private int cell;
    private int gridX;
    private int gridY;

    public BetterCooldownScreen(Screen parent) {
        super(Text.literal("Better Cooldown HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int grid = BetterCooldownConfig.GRID;
        cell = Math.max(3, Math.min(10, (this.height - 150) / grid));
        int gridPixels = cell * grid;
        gridX = this.width / 2 - gridPixels / 2;
        gridY = 24;

        int rowY = gridY + gridPixels + 10;
        int w = 110;
        int left = this.width / 2 - w - 3;
        int right = this.width / 2 + 3;

        addDrawableChild(ButtonWidget.builder(crosshairText(), b -> {
            BetterCooldownConfig.crosshairColor = nextColour(BetterCooldownConfig.crosshairColor);
            b.setMessage(crosshairText());
        }).dimensions(left, rowY, w, 20).build());

        addDrawableChild(ButtonWidget.builder(cooldownText(), b -> {
            BetterCooldownConfig.cooldownColor = nextColour(BetterCooldownConfig.cooldownColor);
            b.setMessage(cooldownText());
        }).dimensions(right, rowY, w, 20).build());

        addDrawableChild(ButtonWidget.builder(sizeText(), b -> {
            BetterCooldownConfig.crosshairSize = (BetterCooldownConfig.crosshairSize + 1) % 5;
            b.setMessage(sizeText());
        }).dimensions(left, rowY + 24, w, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), b -> BetterCooldownConfig.shape.clear())
                .dimensions(right, rowY + 24, w, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(this.width / 2 - 60, rowY + 48, 120, 20).build());
    }

    private Text crosshairText() {
        return Text.literal("Crosshair: #" + String.format("%06X", BetterCooldownConfig.crosshairColor & 0xFFFFFF));
    }

    private Text cooldownText() {
        return Text.literal("Cooldown: #" + String.format("%06X", BetterCooldownConfig.cooldownColor & 0xFFFFFF));
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

        int grid = BetterCooldownConfig.GRID;
        List<Integer> shape = BetterCooldownConfig.shape;

        // Empty grid background
        for (int gy = 0; gy < grid; gy++) {
            for (int gx = 0; gx < grid; gx++) {
                int x = gridX + gx * cell;
                int y = gridY + gy * cell;
                context.fill(x, y, x + cell - 1, y + cell - 1, 0x33FFFFFF);
            }
        }

        // Painted cells in the crosshair colour
        for (int idx : shape) {
            int x = gridX + (idx % grid) * cell;
            int y = gridY + (idx / grid) * cell;
            context.fill(x, y, x + cell - 1, y + cell - 1, BetterCooldownConfig.crosshairColor);
        }

        // Yellow outline on the first pixel drawn, where the cooldown starts
        if (!shape.isEmpty()) {
            int first = shape.get(0);
            int x = gridX + (first % grid) * cell;
            int y = gridY + (first / grid) * cell;
            int yellow = 0xFFFFE04D;
            context.fill(x - 1, y - 1, x + cell, y, yellow);
            context.fill(x - 1, y + cell - 1, x + cell, y + cell, yellow);
            context.fill(x - 1, y - 1, x, y + cell, yellow);
            context.fill(x + cell - 1, y - 1, x + cell, y + cell, yellow);
        }

        int noteY = gridY + cell * grid + 10 + 76;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("First pixel drawn (yellow) = where the attack cooldown starts"),
                this.width / 2, noteY, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (paintAt(mouseX, mouseY, button == 1)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (paintAt(mouseX, mouseY, button == 1)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    /** Left click or drag paints a cell (appended in paint order); right click erases it. */
    private boolean paintAt(double mouseX, double mouseY, boolean erase) {
        int grid = BetterCooldownConfig.GRID;
        int gx = (int) Math.floor((mouseX - gridX) / cell);
        int gy = (int) Math.floor((mouseY - gridY) / cell);
        if (gx < 0 || gy < 0 || gx >= grid || gy >= grid) {
            return false;
        }

        int idx = gy * grid + gx;
        List<Integer> shape = BetterCooldownConfig.shape;
        if (erase) {
            shape.remove(Integer.valueOf(idx));
        } else if (!shape.contains(idx)) {
            shape.add(idx);
        }
        return true;
    }

    @Override
    public void close() {
        BetterCooldownConfig.save();
        MinecraftClient.getInstance().setScreen(parent);
    }
}
