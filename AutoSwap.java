package dev.alek.armorhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Auto-wymiana zbroi: gdy część ma <= X% wytrzymałości, ściąga ją i zakłada zamiennik z ekwipunku. */
public final class AutoSwap {
    private AutoSwap() {}

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    // numery slotów zbroi w PlayerScreenHandler: hełm 5, klata 6, spodnie 7, buty 8
    private static final int[] ARMOR_SCREEN_SLOT = {5, 6, 7, 8};

    private static int cooldown = 0;
    private static final ItemStack[] lastTried = {
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    private static float percentLeft(ItemStack s) {
        int max = s.getMaxDamage();
        if (max <= 0) return 1f;
        return Math.max(0f, Math.min(1f, (max - s.getDamage()) / (float) max));
    }

    public static void tick(MinecraftClient mc) {
        ArmorHudConfig cfg = ArmorHudConfig.get();
        if (!cfg.autoSwap) return;
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (p.isSpectator() || !p.isAlive()) return;
        if (p.currentScreenHandler != p.playerScreenHandler) return;
        if (!p.playerScreenHandler.getCursorStack().isEmpty()) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        for (int i = 0; i < 4; i++) {
            ItemStack worn = p.getEquippedStack(SLOTS[i]);
            if (worn.isEmpty() || !worn.isDamageable() || worn.isOf(Items.ELYTRA)) {
                lastTried[i] = ItemStack.EMPTY;
                continue;
            }
            if (percentLeft(worn) * 100f > cfg.swapPercent) {
                lastTried[i] = ItemStack.EMPTY;
                continue;
            }
            if (ItemStack.areEqual(worn, lastTried[i])) continue; // już próbowaliśmy, nic się nie zmieniło

            int syncId = p.playerScreenHandler.syncId;
            int repl = findReplacement(p, SLOTS[i], cfg);
            if (repl >= 0) {
                int invSlot = repl < 9 ? 36 + repl : repl;
                // wybierz zamiennik -> kliknij slot zbroi (zamiana) -> odłóż starą część na miejsce zamiennika
                mc.interactionManager.clickSlot(syncId, invSlot, 0, SlotActionType.PICKUP, p);
                mc.interactionManager.clickSlot(syncId, ARMOR_SCREEN_SLOT[i], 0, SlotActionType.PICKUP, p);
                mc.interactionManager.clickSlot(syncId, invSlot, 0, SlotActionType.PICKUP, p);
            } else if (cfg.swapNoReplacement) {
                // brak zamiennika: samo zdjęcie do ekwipunku (shift+klik)
                mc.interactionManager.clickSlot(syncId, ARMOR_SCREEN_SLOT[i], 0, SlotActionType.QUICK_MOVE, p);
            } else {
                continue;
            }
            lastTried[i] = worn.copy();
            cooldown = cfg.swapDelay;
            return; // jedna wymiana na raz
        }
    }

    /** Zwraca indeks w ekwipunku gracza (0-35) albo -1. */
    private static int findReplacement(ClientPlayerEntity p, EquipmentSlot slot, ArmorHudConfig cfg) {
        PlayerInventory inv = p.getInventory();
        int best = -1;
        float bestScore = -1f;
        for (int idx = 0; idx < 36; idx++) {
            ItemStack s = inv.getStack(idx);
            if (s.isEmpty() || !s.isDamageable() || s.isOf(Items.ELYTRA)) continue;
            EquippableComponent eq = s.get(DataComponentTypes.EQUIPPABLE);
            if (eq == null || eq.slot() != slot) continue;
            float pct = percentLeft(s) * 100f;
            if (pct <= cfg.swapPercent) continue; // zamiennik też prawie zepsuty
            // najlepszy = o największej bazowej wytrzymałości (netherite > diament > ...), potem % wytrzymałości
            float score = cfg.swapBest ? s.getMaxDamage() * 1000f + pct : 1000f - idx;
            if (score > bestScore) {
                bestScore = score;
                best = idx;
            }
        }
        return best;
    }
}
