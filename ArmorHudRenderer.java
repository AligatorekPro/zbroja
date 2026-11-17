package dev.alek.armorhud;

import dev.alek.armorhud.ArmorHudConfig.Layout;
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

    /** stacks: [hełm, klata, spodnie, buty]; cx/cy = środek HUD-u w pikselach ekranu. */
    public static void render(DrawContext ctx, ItemStack[] stacks, float cx, float cy, float scale, ArmorHudConfig c) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        boolean side = c.layout == Layout.VERTICAL;
        int cw = cellWidth(c);
        int ch = cellHeight(c);
        float[] px = new float[4];
        float[] py = new float[4];

        switch (c.layout) {
            case HORIZONTAL:
                for (int i = 0; i < 4; i++) {
                    px[i] = -2f * cw + i * cw;
                    py[i] = -ch / 2f;
                }
                break;
            case VERTICAL: {
                int rowW = 19 + cw;
                int rowH = c.showBar ? 21 : 18;
                for (int i = 0; i < 4; i++) {
                    px[i] = -rowW / 2f;
                    py[i] = -2f * rowH + i * rowH;
                }
                break;
            }
            default: { // SPLIT: hełm+klata po lewej, spodnie+buty po prawej
                float half = c.gap / 2f;
                px[0] = -half - 2f * cw;
                px[1] = -half - cw;
                px[2] = half;
                px[3] = half + cw;
                for (int i = 0; i < 4; i++) py[i] = -ch / 2f;
                break;
            }
        }

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(cx, cy, 0f);
        m.scale(scale, scale, 1f);
        for (int i = 0; i < 4; i++) {
            ItemStack s = stacks[i];
            if (s == null || s.isEmpty()) continue;
            cell(ctx, tr, s, px[i], py[i], cw, side, c);
        }
        m.pop();
    }

    private static void cell(DrawContext ctx, TextRenderer tr, ItemStack s, float px, float py,
                             int cw, boolean side, ArmorHudConfig c) {
        int ix = Math.round(side ? px : px + (cw - 16) / 2f);
        int iy = Math.round(py);
        ctx.drawItem(s, ix, iy);
        if (!s.isDamageable()) return;

        int max = s.getMaxDamage();
        int left = max - s.getDamage();
        float p = Math.max(0f, Math.min(1f, left / (float) max));
        int color = c.colorFor(p);
        if (c.warnPercent > 0 && p * 100f <= c.warnPercent && (System.currentTimeMillis() / 300L) % 2L == 1L) {
            color = 0xFFFFFF;
        }

        if (c.showBar) {
            int by = iy + 17;
            ctx.fill(ix, by, ix + 16, by + 2, 0xFF000000);
            ctx.fill(ix, by, ix + Math.max(1, Math.round(16 * p)), by + 2, 0xFF000000 | color);
        }

        String t = text(c.textMode, left, max, p);
        if (t != null) {
            int tw = tr.getWidth(t);
            int tx, ty;
            if (side) {
                tx = ix + 19;
                ty = iy + 4;
            } else {
                tx = Math.round(px + cw / 2f - tw / 2f);
                ty = iy + 17 + (c.showBar ? 4 : 0);
            }
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
