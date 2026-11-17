package dev.alek.armorhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PlacementScreen extends Screen {
    private final Screen parent;
    private boolean dragging;

    public PlacementScreen(Screen parent) {
        super(Text.literal("Ustaw zbroję"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 90;
        addDrawableChild(new NeonButton(width / 2 - w - 3, 8, w, 20, Text.literal("Zapisz"), this::close));
        addDrawableChild(new NeonButton(width / 2 + 3, 8, w, 20, Text.literal("Reset"), () -> {
            ArmorHudConfig cfg = ArmorHudConfig.get();
            cfg.posX = 0.5f;
            cfg.posY = 0.80f;
        }));
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
        ArmorHudRenderer.render(c, ArmorHudRenderer.samples(), cfg.posX * width, cfg.posY * height, cfg.scale, cfg);
        String hint = "Przeciągnij myszką  |  kółko = rozmiar  |  strzałki = dokładnie  |  Esc = zapisz";
        c.drawCenteredTextWithShadow(textRenderer, hint, width / 2, 34, Theme.TEXT);
    }

    private void move(double mx, double my) {
        ArmorHudConfig cfg = ArmorHudConfig.get();
        double x = mx;
        if (Math.abs(x - width / 2.0) < 4) x = width / 2.0; // magnes na środek
        cfg.posX = (float) Math.max(0, Math.min(1, x / width));
        cfg.posY = (float) Math.max(0, Math.min(1, my / height));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0) {
            dragging = true;
            move(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging && button == 0) {
            move(mouseX, mouseY);
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
        float dx = 0, dy = 0;
        if (keyCode == GLFW.GLFW_KEY_LEFT) dx = -step;
        else if (keyCode == GLFW.GLFW_KEY_RIGHT) dx = step;
        else if (keyCode == GLFW.GLFW_KEY_UP) dy = -step;
        else if (keyCode == GLFW.GLFW_KEY_DOWN) dy = step;
        if (dx != 0 || dy != 0) {
            cfg.posX = Math.max(0, Math.min(1, cfg.posX + dx / width));
            cfg.posY = Math.max(0, Math.min(1, cfg.posY + dy / height));
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
