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

    // pozycje każdej części: [hełm, klata, spodnie, buty]
    // dx = przesunięcie w prawo od środka ekranu, dy = wysokość od dołu ekranu (piksele GUI)
    public static final float[] DEFAULT_DX = {-145f, -145f, 145f, 145f};
    public static final float[] DEFAULT_DY = {52f, 20f, 52f, 20f};

    public boolean enabled = true;
    public TextMode textMode = TextMode.PERCENT;
    public boolean showBar = true;
    public ColorScheme scheme = ColorScheme.NEON;
    public float scale = 1.0f;
    public float[] dx = DEFAULT_DX.clone();
    public float[] dy = DEFAULT_DY.clone();
    public int warnPercent = 10;

    // tło za każdą częścią
    public boolean bgEnabled = true;
    public int bgColor = 0x14081F;
    public int bgOpacity = 55;
    public boolean bgBorder = true;

    // auto-wymiana zbroi
    public boolean autoSwap = false;
    public int swapPercent = 10;        // wymiana gdy zostało <= tyle % wytrzymałości
    public int swapDelay = 8;           // opóźnienie między wymianami (ticki)
    public boolean swapBest = true;     // true = najlepszy zamiennik, false = pierwszy znaleziony
    public boolean swapNoReplacement = true; // zdejmij część nawet gdy nie ma zamiennika

    public void resetPositions() {
        dx = DEFAULT_DX.clone();
        dy = DEFAULT_DY.clone();
    }

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
        if (scheme == null) scheme = ColorScheme.NEON;
        scale = Math.max(0.5f, Math.min(3f, scale));
        warnPercent = Math.max(0, Math.min(50, warnPercent));
        if (dx == null || dy == null || dx.length != 4 || dy.length != 4) resetPositions();
        bgColor &= 0xFFFFFF;
        bgOpacity = Math.max(0, Math.min(100, bgOpacity));
        swapPercent = Math.max(1, Math.min(50, swapPercent));
        swapDelay = Math.max(2, Math.min(20, swapDelay));
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
