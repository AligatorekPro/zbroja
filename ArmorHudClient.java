package dev.alek.armorhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ArmorHudClient implements ClientModInitializer {
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        ArmorHudConfig.load();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Armor HUD - ustawienia", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "Armor HUD"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ConfigScreen(null));
                }
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ArmorHudConfig cfg = ArmorHudConfig.get();
            if (!cfg.enabled || mc.player == null || mc.options.hudHidden) return;
            if (mc.currentScreen instanceof PlacementScreen) return;

            ItemStack[] stacks;
            if (mc.currentScreen instanceof ConfigScreen) {
                stacks = ArmorHudRenderer.samples();
            } else {
                stacks = new ItemStack[]{
                        mc.player.getEquippedStack(EquipmentSlot.HEAD),
                        mc.player.getEquippedStack(EquipmentSlot.CHEST),
                        mc.player.getEquippedStack(EquipmentSlot.LEGS),
                        mc.player.getEquippedStack(EquipmentSlot.FEET)
                };
            }
            int w = ctx.getScaledWindowWidth();
            int h = ctx.getScaledWindowHeight();
            ArmorHudRenderer.render(ctx, stacks, cfg.posX * w, cfg.posY * h, cfg.scale, cfg);
        });
    }
}
