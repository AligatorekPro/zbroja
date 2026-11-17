package dev.alek.armorhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArmorHudConfig {

    public enum TextMode {
        PERCENT("Procenty"), USES("Użycia"), USES_MAX("Użycia / max"), NONE("Brak tekstu");
        public final String label;
        TextMode(String label) { this.label = label; }
        public TextMode next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public enum Layout {
        SPLIT("Rozdzielony"), HORIZONTAL("Poziomy"), VERTICAL("Pionowy");
        public final String label;
        Layout(String label) { this.label = label; }
        public Layout next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public enum ColorScheme {
        NEON("Neonowy", 0x00FFC8, 0xB14DFF, 0xFF1F6B),
        PINK("Różowy", 0xFF8AE2, 0xC44DFF, 0xFF2A55),
        CLASSIC("Klasyczny", 0x55FF55, 0xFFFF55, 0xFF5555);
        public final String label;
        public final int high, mid, low;
        ColorScheme(String label, int high, int mid, int low) {
            this.label = label; this.high = high; this.mid = mid; this.low = low;
        }
        public ColorScheme next() { return values()[(ordinal() + 1) % values().length]; }
    }

    public boolean enabled = true;
    public TextMode textMode = TextMode.PERCENT;
    public boolean showBar = true;
    public Layout layout = Layout.SPLIT;
    public ColorScheme scheme = ColorScheme.NEON;
    public float scale = 1.0f;
    public float posX = 0.5f;
    public float posY = 0.80f;
    public int gap = 0;
    public int warnPercent = 10;

    public int colorFor(float p) {
        if (p >= 0.5f) return lerp(scheme.mid, scheme.high, (p - 0.5f) * 2f);
        return lerp(scheme.low, scheme.mid, p * 2f);
    }

    private static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round(((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = Math.round(((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = Math.round((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private void sanitize() {
        if (textMode == null) textMode = TextMode.PERCENT;
        if (layout == null) layout = Layout.SPLIT;
        if (scheme == null) scheme = ColorScheme.NEON;
        scale = Math.max(0.5f, Math.min(3f, scale));
        posX = Math.max(0f, Math.min(1f, posX));
        posY = Math.max(0f, Math.min(1f, posY));
        gap = Math.max(0, Math.min(500, gap));
        warnPercent = Math.max(0, Math.min(50, warnPercent));
    }

    // ---- zapis / odczyt ----
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ArmorHudConfig INSTANCE;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("armorhud.json");
    }

    public static ArmorHudConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                INSTANCE = GSON.fromJson(Files.readString(p), ArmorHudConfig.class);
            }
        } catch (Exception ignored) {
        }
        if (INSTANCE == null) INSTANCE = new ArmorHudConfig();
        INSTANCE.sanitize();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(get()));
        } catch (IOException ignored) {
        }
    }
}
