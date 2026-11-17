package dev.alek.armorhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class NeonSlider extends ClickableWidget {
    private final float min, max, step;
    private float value;
    private final Consumer<Float> onChange;
    private final Function<Float, String> label;
    private boolean dragging;

    public NeonSlider(int x, int y, int w, int h, float min, float max, float step, float value,
                      Function<Float, String> label, Consumer<Float> onChange) {
        super(x, y, w, h, Text.empty());
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
        this.label = label;
        this.onChange = onChange;
    }

    private void setFromMouse(double mouseX) {
        float t = (float) ((mouseX - (getX() + 2)) / (getWidth() - 4));
        t = Math.max(0f, Math.min(1f, t));
        float v = min + t * (max - min);
        v = Math.round(v / step) * step;
        v = Math.max(min, Math.min(max, v));
        value = v;
        onChange.accept(v);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isMouseOver(mouseX, mouseY)) {
            dragging = true;
            setFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            setFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(DrawContext c, int mouseX, int mouseY, float delta) {
        int x1 = getX(), y1 = getY(), x2 = x1 + getWidth(), y2 = y1 + getHeight();
        boolean hov = this.isHovered() || dragging;
        c.fill(x1, y1, x2, y2, Theme.BTN);
        float t = (value - min) / (max - min);
        int fillX = x1 + 1 + Math.round((getWidth() - 2) * t);
        c.fillGradient(x1 + 1, y1 + 1, fillX, y2 - 1, 0xAAB14DFF, 0xAAFF4DDB);
        c.fill(Math.max(x1 + 1, fillX - 1), y1, Math.min(x2 - 1, fillX + 1), y2, Theme.PINK);
        Theme.glow(c, x1, y1, x2, y2, hov ? Theme.PINK : Theme.PURPLE, hov ? 3 : 1);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        c.drawCenteredTextWithShadow(tr, label.apply(value), x1 + getWidth() / 2, y1 + (getHeight() - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
