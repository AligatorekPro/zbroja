package dev.alek.armorhud;

import net.minecraft.client.gui.DrawContext;

final class Theme {
    static final int BG_TOP = 0xF0120818;
    static final int BG_BOTTOM = 0xF0260B3A;
    static final int PINK = 0xFFFF4DDB;
    static final int PURPLE = 0xFFB14DFF;
    static final int TEXT = 0xFFF3E6FF;
    static final int DIM = 0xFF9A7BB8;
    static final int BTN = 0xFF1B0B2B;
    static final int BTN_HOVER = 0xFF2E1050;

    private Theme() {}

    static void outline(DrawContext c, int x1, int y1, int x2, int y2, int color) {
        c.fill(x1, y1, x2, y1 + 1, color);
        c.fill(x1, y2 - 1, x2, y2, color);
        c.fill(x1, y1, x1 + 1, y2, color);
        c.fill(x2 - 1, y1, x2, y2, color);
    }

    /** Ramka neonowa: 1px linia + poświata na zewnątrz. */
    static void glow(DrawContext c, int x1, int y1, int x2, int y2, int color, int radius) {
        outline(c, x1, y1, x2, y2, color);
        for (int i = 1; i <= radius; i++) {
            int a = 0x50 >> (i - 1);
            outline(c, x1 - i, y1 - i, x2 + i, y2 + i, (a << 24) | (color & 0xFFFFFF));
        }
    }
}
