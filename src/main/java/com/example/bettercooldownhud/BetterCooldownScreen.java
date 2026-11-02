package com.example.bettercooldownhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class BetterCooldownScreen extends Screen {
    private final Screen parent;
    private ButtonWidget alwaysButton;
    private ButtonWidget colorButton;

    public BetterCooldownScreen(Screen parent) {
        super(Text.literal("Better Cooldown HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = this.height / 2 - 80;

        alwaysButton = ButtonWidget.builder(alwaysText(), b -> {
            BetterCooldownConfig.alwaysShow = !BetterCooldownConfig.alwaysShow;
            b.setMessage(alwaysText());
        }).dimensions(x, y, 200, 20).build();
        addDrawableChild(alwaysButton);

        addDrawableChild(new Slider(x, y + 30, 200, 20, 4, 60,
                BetterCooldownConfig.radius,
                v -> BetterCooldownConfig.radius = (int) Math.round(v),
                v -> "Radius: " + (int) Math.round(v)));

        addDrawableChild(new Slider(x, y + 60, 200, 20, 1, 8,
                BetterCooldownConfig.thickness,
                v -> BetterCooldownConfig.thickness = (float) (Math.round(v * 10) / 10.0),
                v -> String.format("Thickness: %.1f", Math.round(v * 10) / 10.0)));

        colorButton = ButtonWidget.builder(colorText(), b -> {
            BetterCooldownConfig.colorIndex =
                    (BetterCooldownConfig.colorIndex + 1) % BetterCooldownConfig.COLOR_NAMES.length;
            b.setMessage(colorText());
        }).dimensions(x, y + 90, 200, 20).build();
        addDrawableChild(colorButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(x, y + 130, 200, 20).build());
    }

    private Text alwaysText() {
        return Text.literal("Always on: " + (BetterCooldownConfig.alwaysShow ? "ON" : "OFF"));
    }

    private Text colorText() {
        return Text.literal("Color: " + BetterCooldownConfig.COLOR_NAMES[BetterCooldownConfig.colorIndex]);
    }

    @Override
    public void close() {
        BetterCooldownConfig.save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    private static class Slider extends SliderWidget {
        private final double min;
        private final double max;
        private final Consumer<Double> setter;
        private final Function<Double, String> label;

        Slider(int x, int y, int width, int height, double min, double max, double current,
               Consumer<Double> setter, Function<Double, String> label) {
            super(x, y, width, height, Text.empty(), (current - min) / (max - min));
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.label = label;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label.apply(min + (max - min) * this.value)));
        }

        @Override
        protected void applyValue() {
            setter.accept(min + (max - min) * this.value);
        }
    }
}
