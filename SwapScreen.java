package dev.alek.armorhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SwapScreen extends Screen {
    private final Screen parent;
    private int px, py, pw, ph;

    public SwapScreen(Screen parent) {
        super(Text.literal("Auto-wymiana"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ArmorHudConfig cfg = ArmorHudConfig.get();
        int bw = 124, bh = 20, gx = 6, gy = 4;
        pw = bw * 2 + gx + 24;
        ph = 34 + 3 * (bh + gy) + 22;
        px = (width - pw) / 2;
        py = 6;
        int lx = px + 12;
        int rx = lx + bw + gx;
        int y = py + 30;

        // rząd 1
        addDrawableChild(new NeonButton(lx, y, bw, bh,
                Text.literal("Auto-wymiana: " + (cfg.autoSwap ? "WŁĄCZONA" : "WYŁĄCZONA")),
                () -> { cfg.autoSwap = !cfg.autoSwap; clearAndInit(); }));
        addDrawableChild(new NeonButton(rx, y, bw, bh,
                Text.literal("Zamiennik: " + (cfg.swapBest ? "najlepszy" : "pierwszy")),
                () -> { cfg.swapBest = !cfg.swapBest; clearAndInit(); }));
        y += bh + gy;

        // rząd 2
        addDrawableChild(new NeonSlider(lx, y, bw, bh, 1f, 50f, 1f, cfg.swapPercent,
                v -> "Wymień przy: " + Math.round(v) + "%",
                v -> cfg.swapPercent = Math.round(v)));
        addDrawableChild(new NeonSlider(rx, y, bw, bh, 2f, 20f, 1f, cfg.swapDelay,
                v -> "Opóźnienie: " + Math.round(v) + " tick",
                v -> cfg.swapDelay = Math.round(v)));
        y += bh + gy;

        // rząd 3
        addDrawableChild(new NeonButton(lx, y, bw, bh,
                Text.literal("Bez zamiennika: " + (cfg.swapNoReplacement ? "zdejmij" : "zostaw")),
                () -> { cfg.swapNoReplacement = !cfg.swapNoReplacement; clearAndInit(); }));
        addDrawableChild(new NeonButton(rx, y, bw, bh, Text.literal("Wstecz"), this::close));
    }

    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fillGradient(px, py, px + pw, py + ph, Theme.BG_TOP, Theme.BG_BOTTOM);
        Theme.glow(c, px, py, px + pw, py + ph, Theme.PINK, 3);
        String title = "A U T O - W Y M I A N A";
        c.drawText(textRenderer, title, px + pw / 2 - textRenderer.getWidth(title) / 2, py + 8, Theme.PINK, true);
        String sub = "ściąga zniszczoną zbroję i zakłada inną z ekwipunku";
        c.drawText(textRenderer, sub, px + pw / 2 - textRenderer.getWidth(sub) / 2, py + 19, Theme.DIM, false);
        String warn = "Sprawdź, czy regulamin serwera na to pozwala";
        c.drawText(textRenderer, warn, px + pw / 2 - textRenderer.getWidth(warn) / 2, py + ph - 14, Theme.DIM, false);
    }

    @Override
    public void close() {
        ArmorHudConfig.save();
        this.client.setScreen(parent);
    }
}
