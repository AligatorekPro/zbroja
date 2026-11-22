package dev.alek.armorhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BackgroundScreen extends Screen {
    private static final String[] NAMES = {"Czarny", "Ciemny fiolet", "Granat", "Szary", "Neon różowy", "Neon fiolet"};
    private static final int[] COLORS = {0x000000, 0x14081F, 0x0A1030, 0x404040, 0xFF4DDB, 0xB14DFF};

    private final Screen parent;
    private int px, py, pw, ph;

    public BackgroundScreen(Screen parent) {
        super(Text.literal("Tło"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ArmorHudConfig cfg = ArmorHudConfig.get();
        int bw = 124, bh = 20, gx = 6, gy = 4;
        pw = bw * 2 + gx + 24;
        ph = 34 + 4 * (bh + gy) + 6;
        px = (width - pw) / 2;
        py = 6;
        int lx = px + 12;
        int rx = lx + bw + gx;
        int y = py + 30;

        int idx = -1;
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == (cfg.bgColor & 0xFFFFFF)) idx = i;
        }
        final int curIdx = idx;
        String colorName = idx >= 0 ? NAMES[idx] : "Własny";

        // rząd 1
        addDrawableChild(new NeonButton(lx, y, bw, bh,
                Text.literal("Tło: " + (cfg.bgEnabled ? "WŁĄCZONE" : "WYŁĄCZONE")),
                () -> { cfg.bgEnabled = !cfg.bgEnabled; clearAndInit(); }));
        addDrawableChild(new NeonButton(rx, y, bw, bh,
                Text.literal("Ramka: " + (cfg.bgBorder ? "TAK" : "NIE")),
                () -> { cfg.bgBorder = !cfg.bgBorder; clearAndInit(); }));
        y += bh + gy;

        // rząd 2
        addDrawableChild(new NeonButton(lx, y, bw, bh,
                Text.literal("Kolor: " + colorName),
                () -> {
                    cfg.bgColor = COLORS[(curIdx + 1) % COLORS.length];
                    clearAndInit();
                }));
        addDrawableChild(new NeonSlider(rx, y, bw, bh, 0f, 100f, 1f, cfg.bgOpacity,
                v -> "Przyciemnienie: " + Math.round(v) + "%",
                v -> cfg.bgOpacity = Math.round(v)));
        y += bh + gy;

        // rząd 3 (własny kolor RGB)
        addDrawableChild(new NeonSlider(lx, y, bw, bh, 0f, 255f, 1f, (cfg.bgColor >> 16) & 255,
                v -> "Czerwony: " + Math.round(v),
                v -> cfg.bgColor = (cfg.bgColor & 0x00FFFF) | (Math.round(v) << 16)));
        addDrawableChild(new NeonSlider(rx, y, bw, bh, 0f, 255f, 1f, (cfg.bgColor >> 8) & 255,
                v -> "Zielony: " + Math.round(v),
                v -> cfg.bgColor = (cfg.bgColor & 0xFF00FF) | (Math.round(v) << 8)));
        y += bh + gy;

        // rząd 4
        addDrawableChild(new NeonSlider(lx, y, bw, bh, 0f, 255f, 1f, cfg.bgColor & 255,
                v -> "Niebieski: " + Math.round(v),
                v -> cfg.bgColor = (cfg.bgColor & 0xFFFF00) | Math.round(v)));
        addDrawableChild(new NeonButton(rx, y, bw, bh, Text.literal("Wstecz"), this::close));
    }

    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fillGradient(px, py, px + pw, py + ph, Theme.BG_TOP, Theme.BG_BOTTOM);
        Theme.glow(c, px, py, px + pw, py + ph, Theme.PINK, 3);
        String title = "T Ł O";
        c.drawText(textRenderer, title, px + pw / 2 - textRenderer.getWidth(title) / 2, py + 8, Theme.PINK, true);
        String sub = "kolor i przyciemnienie za zbroją";
        c.drawText(textRenderer, sub, px + pw / 2 - textRenderer.getWidth(sub) / 2, py + 19, Theme.DIM, false);
    }

    @Override
    public void close() {
        ArmorHudConfig.save();
        this.client.setScreen(parent);
    }
}
