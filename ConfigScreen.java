package dev.alek.armorhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private int px, py, pw, ph;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Armor HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ArmorHudConfig cfg = ArmorHudConfig.get();
        int bw = 124, bh = 20, gx = 6, gy = 4;
        pw = bw * 2 + gx + 24;
        ph = 34 + 6 * (bh + gy) + 6;
        px = (width - pw) / 2;
        py = 6;
        int lx = px + 12;
        int rx = lx + bw + gx;
        int y = py + 30;

        // rząd 1
        addDrawableChild(new NeonButton(lx, y, bw, bh,
                Text.literal("Mod: " + (cfg.enabled ? "WŁĄCZONY" : "WYŁĄCZONY")),
                () -> { cfg.enabled = !cfg.enabled; clearAndInit(); }));
        addDrawableChild(new NeonButton(rx, y, bw, bh,
                Text.literal("Tekst: " + cfg.textMode.label),
                () -> { cfg.textMode = cfg.textMode.next(); clearAndInit(); }));
        y += bh + gy;

        // rząd 2
        addDrawableChild(new NeonButton(lx, y, bw, bh,
                Text.literal("Pasek: " + (cfg.showBar ? "TAK" : "NIE")),
                () -> { cfg.showBar = !cfg.showBar; clearAndInit(); }));
        addDrawableChild(new NeonButton(rx, y, bw, bh,
                Text.literal("Kolory: " + cfg.scheme.label),
                () -> { cfg.scheme = cfg.scheme.next(); clearAndInit(); }));
        y += bh + gy;

        // rząd 3
        addDrawableChild(new NeonButton(lx, y, bw, bh,
                Text.literal("Ustaw zbroję"),
                () -> this.client.setScreen(new PlacementScreen(this))));
        addDrawableChild(new NeonButton(rx, y, bw, bh,
                Text.literal("Tło..."),
                () -> this.client.setScreen(new BackgroundScreen(this))));
        y += bh + gy;

        // rząd 4 (suwaki)
        addDrawableChild(new NeonSlider(lx, y, bw, bh, 0.5f, 3f, 0.05f, cfg.scale,
                v -> String.format("Rozmiar: %.2f", v),
                v -> cfg.scale = v));
        addDrawableChild(new NeonSlider(rx, y, bw, bh, 0f, 50f, 1f, cfg.warnPercent,
                v -> Math.round(v) == 0 ? "Miganie: wyłączone" : "Miganie poniżej " + Math.round(v) + "%",
                v -> cfg.warnPercent = Math.round(v)));
        y += bh + gy;

        // rząd 5
        addDrawableChild(new NeonButton(lx, y, bw, bh,
                Text.literal("Auto-wymiana..."),
                () -> this.client.setScreen(new SwapScreen(this))));
        addDrawableChild(new NeonButton(rx, y, bw, bh,
                Text.literal("Reset pozycji"),
                () -> cfg.resetPositions()));
        y += bh + gy;

        // rząd 6
        addDrawableChild(new NeonButton(lx, y, bw * 2 + gx, bh, Text.literal("Gotowe"), this::close));
    }

    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fillGradient(px, py, px + pw, py + ph, Theme.BG_TOP, Theme.BG_BOTTOM);
        Theme.glow(c, px, py, px + pw, py + ph, Theme.PINK, 3);
        String title = "A R M O R   H U D";
        c.drawText(textRenderer, title, px + pw / 2 - textRenderer.getWidth(title) / 2, py + 8, Theme.PINK, true);
        String sub = "wytrzymałość zbroi";
        c.drawText(textRenderer, sub, px + pw / 2 - textRenderer.getWidth(sub) / 2, py + 19, Theme.DIM, false);
    }

    @Override
    public void close() {
        ArmorHudConfig.save();
        this.client.setScreen(parent);
    }
}
