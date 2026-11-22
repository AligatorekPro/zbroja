package dev.alek.armorhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PlacementScreen extends Screen {
    private final Screen parent;
    private int selected = -1;
    private boolean dragging;
    private double grabX, grabY;

    public PlacementScreen(Screen parent) {
        super(Text.literal("Ustaw zbroję"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 90;
        addDrawableChild(new NeonButton(width / 2 - w - 3, 8, w, 20, Text.literal("Zapisz"), this::close));
        addDrawableChild(new NeonButton(width / 2 + 3, 8, w, 20, Text.literal("Reset"),
                () -> ArmorHudConfig.get().resetPositions()));
    }

    @Override
    public void renderBackground(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0x66100818);
        c.fill(width / 2, 0, width / 2 + 1, height, 0x44FF4DDB); // linia środka ekranu
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        super.render(c, mouseX, mouseY, delta);
        ArmorHudConfig cfg = ArmorHudConfig.get();
        ArmorHudRenderer.render(c, ArmorHudRenderer.samples(), width, height, cfg, selected);
        c.drawCenteredTextWithShadow(textRenderer,
                "Kliknij część zbroi i przeciągnij  |  kółko = rozmiar  |  strzałki = dokładnie", width / 2, 34, Theme.TEXT);
        c.drawCenteredTextWithShadow(textRenderer,
                "Części same przyciągają się do siebie (w tej samej linii i symetrycznie)  |  Esc = zapisz",
                width / 2, 46, Theme.DIM);
    }

    private int pieceAt(double mx, double my) {
        ArmorHudConfig cfg = ArmorHudConfig.get();
        for (int i = 3; i >= 0; i--) {
            float cx = ArmorHudRenderer.centerX(cfg, i, width);
            float cy = ArmorHudRenderer.centerY(cfg, i, height);
            if (Math.abs(mx - cx) <= ArmorHudRenderer.halfW(cfg) * cfg.scale
                    && Math.abs(my - cy) <= ArmorHudRenderer.halfH(cfg) * cfg.scale) {
                return i;
            }
        }
        return -1;
    }

    private void moveSelected(double mx, double my) {
        if (selected < 0) return;
        ArmorHudConfig cfg = ArmorHudConfig.get();
        float nx = (float) (mx + grabX - width / 2.0);
        float ny = (float) (height - (my + grabY));
        for (int j = 0; j < 4; j++) {
            if (j == selected) continue;
            if (Math.abs(cfg.dx[j] - nx) <= 4) nx = cfg.dx[j];
            else if (Math.abs(-cfg.dx[j] - nx) <= 4) nx = -cfg.dx[j];
            if (Math.abs(cfg.dy[j] - ny) <= 4) ny = cfg.dy[j];
        }
        if (Math.abs(nx) <= 4) nx = 0f;
        cfg.dx[selected] = Math.max(-width / 2f, Math.min(width / 2f, nx));
        cfg.dy[selected] = Math.max(0f, Math.min((float) height, ny));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0) {
            ArmorHudConfig cfg = ArmorHudConfig.get();
            int i = pieceAt(mouseX, mouseY);
            selected = i;
            if (i >= 0) {
                grabX = ArmorHudRenderer.centerX(cfg, i, width) - mouseX;
                grabY = ArmorHudRenderer.centerY(cfg, i, height) - mouseY;
                dragging = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging && button == 0) {
            moveSelected(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        ArmorHudConfig cfg = ArmorHudConfig.get();
        float s = cfg.scale + (verticalAmount > 0 ? 0.05f : -0.05f);
        cfg.scale = Math.max(0.5f, Math.min(3f, Math.round(s * 100f) / 100f));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ArmorHudConfig cfg = ArmorHudConfig.get();
        float step = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0 ? 5f : 1f;
        float ddx = 0, ddy = 0;
        if (keyCode == GLFW.GLFW_KEY_LEFT) ddx = -step;
        else if (keyCode == GLFW.GLFW_KEY_RIGHT) ddx = step;
        else if (keyCode == GLFW.GLFW_KEY_UP) ddy = step;
        else if (keyCode == GLFW.GLFW_KEY_DOWN) ddy = -step;
        if ((ddx != 0 || ddy != 0) && selected >= 0) {
            cfg.dx[selected] += ddx;
            cfg.dy[selected] = Math.max(0f, cfg.dy[selected] + ddy);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ArmorHudConfig.save();
        this.client.setScreen(parent);
    }
}
