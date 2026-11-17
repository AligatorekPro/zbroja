package dev.alek.armorhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class NeonButton extends ClickableWidget {
    private final Runnable action;

    public NeonButton(int x, int y, int w, int h, Text message, Runnable action) {
        super(x, y, w, h, message);
        this.action = action;
    }

    @Override
    protected void renderWidget(DrawContext c, int mouseX, int mouseY, float delta) {
        boolean hov = this.isHovered();
        int x1 = getX(), y1 = getY(), x2 = x1 + getWidth(), y2 = y1 + getHeight();
        c.fill(x1, y1, x2, y2, hov ? Theme.BTN_HOVER : Theme.BTN);
        Theme.glow(c, x1, y1, x2, y2, hov ? Theme.PINK : Theme.PURPLE, hov ? 3 : 1);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        c.drawCenteredTextWithShadow(tr, getMessage(), x1 + getWidth() / 2, y1 + (getHeight() - 8) / 2,
                hov ? 0xFFFFFFFF : Theme.TEXT);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        action.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
