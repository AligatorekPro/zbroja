package dev.alek.armorhud;

import dev.alek.armorhud.ArmorHudConfig.TextMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class ArmorHudRenderer {
    private ArmorHudRenderer() {}

    public static int cellWidth(ArmorHudConfig c) {
        switch (c.textMode) {
            case USES_MAX: return 50;
            case NONE: return 20;
            default: return 28;
        }
    }

    public static int cellHeight(ArmorHudConfig c) {
        boolean text = c.textMode != TextMode.NONE;
        return 16 + ((text || c.showBar) ? 1 : 0) + (c.showBar ? 4 : 0) + (text ? 9 : 0);
    }

    public static float centerX(ArmorHudConfig c, int i, int screenW) {
        return screenW / 2f + c.dx[i];
    }

    public static float centerY(ArmorHudConfig c, int i, int screenH) {
        return screenH - c.dy[i];
    }

    /** połowa szerokości / wysokości klikalnego pola (bez skali) */
    public static float halfW(ArmorHudConfig c) { return cellWidth(c) / 2f + 3f; }
    public static float halfH(ArmorHudConfig c) { return cellHeight(c) / 2f + 3f; }

    /** stacks: [hełm, klata, spodnie, buty]; selected = indeks podświetlanej części albo -1 */
    public static void render(DrawContext ctx, ItemStack[] stacks, int screenW, int screenH,
                              ArmorHudConfig c, int selected) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        MatrixStack m = ctx.getMatrices();
        for (int i = 0; i < 4; i++) {
            ItemStack s = stacks[i];
            if (s == null || s.isEmpty()) continue;
            m.push();
            m.translate(centerX(c, i, screenW), centerY(c, i, screenH), 0f);
            m.scale(c.scale, c.scale, 1f);
            cell(ctx, tr, s, c, i == selected);
            m.pop();
        }
    }

    private static void cell(DrawContext ctx, TextRenderer tr, ItemStack s, ArmorHudConfig c, boolean selected) {
        int cw = cellWidth(c);
        int ch = cellHeight(c);
        int x1 = Math.round(-cw / 2f) - 2;
        int y1 = Math.round(-ch / 2f) - 2;
        int x2 = x1 + cw + 4;
        int y2 = y1 + ch + 4;
        int ix = -8;
        int iy = Math.round(-ch / 2f);

        boolean dmg = s.isDamageable();
        float p = 1f;
        int left = 0, max = 0;
        if (dmg) {
            max = s.getMaxDamage();
            left = max - s.getDamage();
            p = Math.max(0f, Math.min(1f, left / (float) max));
        }
        int color = dmg ? c.colorFor(p) : c.scheme.mid;
        if (dmg && c.warnPercent > 0 && p * 100f <= c.warnPercent
                && (System.currentTimeMillis() / 300L) % 2L == 1L) {
            color = 0xFFFFFF;
        }

        // tło
        if (c.bgEnabled) {
            int a = Math.round(c.bgOpacity * 255f / 100f);
            ctx.fill(x1, y1, x2, y2, (a << 24) | (c.bgColor & 0xFFFFFF));
            if (c.bgBorder) Theme.outline(ctx, x1, y1, x2, y2, 0xB0000000 | color);
        }
        if (selected) Theme.glow(ctx, x1, y1, x2, y2, Theme.PINK, 2);

        ctx.drawItem(s, ix, iy);
        if (!dmg) return;

        if (c.showBar) {
            int by = iy + 17;
            ctx.fill(ix, by, ix + 16, by + 2, 0xFF000000);
            ctx.fill(ix, by, ix + Math.max(1, Math.round(16 * p)), by + 2, 0xFF000000 | color);
        }

        String t = text(c.textMode, left, max, p);
        if (t != null) {
            int tw = tr.getWidth(t);
            int tx = Math.round(-tw / 2f);
            int ty = iy + 17 + (c.showBar ? 4 : 0);
            ctx.drawText(tr, t, tx, ty, 0xFF000000 | color, true);
        }
    }

    private static String text(TextMode m, int left, int max, float p) {
        switch (m) {
            case PERCENT: return (int) Math.ceil(p * 100f) + "%";
            case USES: return Integer.toString(left);
            case USES_MAX: return left + "/" + max;
            default: return null;
        }
    }

    // ---- przykładowa zbroja do podglądu w menu ----
    private static ItemStack[] samples;

    public static ItemStack[] samples() {
        if (samples == null) {
            samples = new ItemStack[]{
                    mk(Items.NETHERITE_HELMET, 0.00f),
                    mk(Items.NETHERITE_CHESTPLATE, 0.35f),
                    mk(Items.NETHERITE_LEGGINGS, 0.70f),
                    mk(Items.NETHERITE_BOOTS, 0.93f)
            };
        }
        return samples;
    }

    private static ItemStack mk(Item item, float damaged) {
        ItemStack s = new ItemStack(item);
        s.setDamage(Math.round(s.getMaxDamage() * damaged));
        return s;
    }
}
