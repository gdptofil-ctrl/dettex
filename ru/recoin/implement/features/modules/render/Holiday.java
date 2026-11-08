package ru.recoin.implement.features.modules.render;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_243;
import net.minecraft.class_2394;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_4587;
import net.minecraft.class_5819;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;

/**
 * Holiday vibe: Halloween or Winter (or chosen by the date). Holiday theme for the HUD, snow caps or
 * pumpkin goo on HUD panels, snowflakes or bats flying over the screen, snow or spooky souls around
 * you in the world, holiday hats on every player and quiet holiday sounds.
 */
public final class Holiday
extends Module {
    public final SelectSetting mode = new SelectSetting("Holiday", "Which holiday").value("Auto", "Halloween", "Winter").selected("Auto");
    public final BooleanSetting theme = new BooleanSetting("Holiday Theme", "HUD colours of the holiday").setValue(true);
    public final BooleanSetting hudDecor = new BooleanSetting("HUD Decor", "Snow caps / pumpkin goo on HUD panels").setValue(true);
    public final BooleanSetting screen = new BooleanSetting("Screen Effects", "Snowflakes / bats flying over the screen").setValue(true);
    public final BooleanSetting world = new BooleanSetting("World Effects", "Snow / spooky souls and smoke around you").setValue(true);
    public final BooleanSetting hats = new BooleanSetting("Holiday Hats", "Every player wears a holiday hat").setValue(true);
    public final SelectSetting halloweenHat = new SelectSetting("Halloween Hat", "Hat for Halloween").value("Jack-o'-Lantern", "Witch Hat").selected("Jack-o'-Lantern");
    public final BooleanSetting sounds = new BooleanSetting("Holiday Sounds", "Quiet spooky or jingle sounds now and then").setValue(true);
    public final ValueSetting intensity = new ValueSetting("Intensity", "Amount of snow, bats and particles").setValue(1.0f).range(0.2f, 3.0f).step(0.1f);

    private final Random random = new Random();
    private final List<float[]> flakes = new ArrayList<>();   // x, y, size, speed, phase
    private final List<float[]> bats = new ArrayList<>();     // x, y, vx, phase, scale
    private String savedTheme;
    private String appliedTheme;
    private long lastFrame;
    private int soundIn = 200;
    private class_2394 snow;
    private class_2394 soul;
    private class_2394 smoke;

    public Holiday() {
        super("Holiday", "Holiday", ModuleCategory.RENDER);
        this.setup(this.mode, this.theme, this.hudDecor, this.screen, this.world, this.hats, this.halloweenHat, this.sounds, this.intensity);
    }

    public static Holiday getInstance() {
        return Instance.get(Holiday.class);
    }

    /** true = Halloween, false = Winter. Auto: Halloween from 1 Oct to 10 Nov, otherwise Winter. */
    public boolean halloween() {
        if (this.mode.isSelected("Halloween")) {
            return true;
        }
        if (this.mode.isSelected("Winter")) {
            return false;
        }
        LocalDate d = LocalDate.now();
        return d.getMonth() == Month.OCTOBER || d.getMonth() == Month.NOVEMBER && d.getDayOfMonth() <= 10;
    }

    // ------------------------------------------------------------------ hats

    /** Holiday hat every player wears (catalog name), or null. */
    public static String hatName(double squaredDistance) {
        Holiday h = Holiday.getInstance();
        if (h == null || !h.isState() || !h.hats.isValue() || squaredDistance > 64.0 * 64.0) {
            return null;
        }
        return h.halloween() ? h.halloweenHat.getSelected() : "Xmas Hat";
    }

    // ------------------------------------------------------------------ theme

    private void applyTheme() {
        Hud hud = Hud.getInstance();
        if (hud == null) {
            return;
        }
        String want = this.theme.isValue() ? (this.halloween() ? "Halloween" : "Winter") : null;
        if (want == null) {
            this.restoreTheme();
            return;
        }
        if (want.equals(this.appliedTheme) && want.equals(hud.themeSetting.getSelected())) {
            return;
        }
        if (this.savedTheme == null) {
            this.savedTheme = hud.themeSetting.getSelected();
        }
        hud.applyThemePreset(want);
        this.appliedTheme = want;
    }

    private void restoreTheme() {
        Hud hud = Hud.getInstance();
        if (hud != null && this.savedTheme != null && this.appliedTheme != null && this.appliedTheme.equals(hud.themeSetting.getSelected())) {
            hud.applyThemePreset(this.savedTheme);
        }
        this.savedTheme = null;
        this.appliedTheme = null;
    }

    @Override
    public void activate() {
        this.flakes.clear();
        this.bats.clear();
        this.applyTheme();
    }

    @Override
    public void deactivate() {
        this.restoreTheme();
        this.flakes.clear();
        this.bats.clear();
    }

    // ------------------------------------------------------------------ HUD decor (called by the blur renderer for every HUD panel)

    public static void decoratePanel(ShapeProperties shape) {
        Holiday h = Holiday.getInstance();
        if (h == null || !h.isState() || !h.hudDecor.isValue() || shape.getWidth() < 24.0f || shape.getHeight() < 10.0f) {
            return;
        }
        boolean halloween = h.halloween();
        class_4587 m = shape.getMatrix();
        float x = shape.getX();
        float y = shape.getY();
        float w = shape.getWidth();
        int main = halloween ? ColorUtil.getColor(255, 138, 30, 0.95f) : ColorUtil.getColor(250, 253, 255, 0.97f);
        int shade = halloween ? ColorUtil.getColor(155, 77, 255, 0.95f) : ColorUtil.getColor(205, 228, 245, 0.95f);
        // a cap along the top edge
        rect(m, x + 3.0f, y - 1.4f, w - 6.0f, 2.8f, 1.4f, main);
        // drips at stable spots that depend on the panel size, slowly growing and shrinking
        int count = Math.max(2, (int)(w / 26.0f));
        long seed = (long)(w * 31.0f) ^ (long)(shape.getHeight() * 17.0f);
        Random r = new Random(seed);
        double t = System.currentTimeMillis() / 1000.0;
        for (int i = 0; i < count; ++i) {
            float dx = x + 6.0f + r.nextFloat() * (w - 12.0f);
            float len = 1.6f + r.nextFloat() * 2.6f + (float)Math.sin(t * 0.8 + i * 1.7) * 0.5f;
            float dw = 1.6f + r.nextFloat() * 1.0f;
            rect(m, dx, y, dw, len, dw / 2.0f, i % 3 == 0 ? shade : main);
        }
        if (!halloween) {
            // a few snow lumps on the cap
            for (int i = 0; i < count; ++i) {
                float bx = x + 5.0f + r.nextFloat() * (w - 14.0f);
                rect(m, bx, y - 2.4f, 4.0f + r.nextFloat() * 3.0f, 2.2f, 1.1f, main);
            }
        }
    }

    private static void rect(class_4587 m, float x, float y, float w, float h, float round, int color) {
        QuickImports.rectangle.render(ShapeProperties.create(m, x, y, w, h).round(round).color(color).build());
    }

    // ------------------------------------------------------------------ screen effects

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (!this.screen.isValue()) {
            return;
        }
        class_332 context = event.getDrawContext();
        class_4587 m = MatrixStackCompat.from(context.method_51448());
        float sw = QuickImports.window.getScaledWidth();
        float sh = QuickImports.window.getScaledHeight();
        long now = System.nanoTime();
        float dt = this.lastFrame == 0L ? 0.016f : Math.min(0.1f, (now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        float amount = this.intensity.getValue();
        float alpha = Render2DUtil.getRenderAlpha();
        if (alpha <= 0.0f) {
            return;
        }
        if (this.halloween()) {
            this.flakes.clear();
            this.drawBats(m, sw, sh, dt, amount);
        } else {
            this.bats.clear();
            this.drawSnow(m, sw, sh, dt, amount);
        }
    }

    private void drawSnow(class_4587 m, float sw, float sh, float dt, float amount) {
        int target = (int)(70 * amount);
        while (this.flakes.size() < target) {
            this.flakes.add(new float[]{this.random.nextFloat() * sw, this.random.nextFloat() * sh - sh, 0.8f + this.random.nextFloat() * 2.2f, 12.0f + this.random.nextFloat() * 26.0f, this.random.nextFloat() * 6.28f});
        }
        while (this.flakes.size() > target) {
            this.flakes.remove(this.flakes.size() - 1);
        }
        double t = System.currentTimeMillis() / 1000.0;
        for (float[] f : this.flakes) {
            f[1] += f[3] * dt * (0.6f + f[2] * 0.25f);
            f[0] += (float)Math.sin(t * 0.9 + f[4]) * 8.0f * dt;
            if (f[1] > sh + 4.0f) {
                f[1] = -4.0f;
                f[0] = this.random.nextFloat() * sw;
            }
            int color = ColorUtil.getColor(255, 255, 255, 0.35f + f[2] * 0.18f);
            rect(m, f[0], f[1], f[2], f[2], f[2] / 2.0f, color);
        }
    }

    /** Bat as tiny pixel art: two flapping frames, 9x4 cells. */
    private static final String[][] BAT = {
            {"X.......X", "XX.XXX.XX", ".XXXXXXX.", "...X.X..."},
            {".........", "...XXX...", "XXXXXXXXX", "X..X.X..X"}};

    private void drawBats(class_4587 m, float sw, float sh, float dt, float amount) {
        int target = Math.max(1, (int)(4 * amount));
        while (this.bats.size() < target) {
            boolean left = this.random.nextBoolean();
            float speed = 40.0f + this.random.nextFloat() * 50.0f;
            this.bats.add(new float[]{left ? -30.0f - this.random.nextFloat() * sw * 0.5f : sw + 30.0f + this.random.nextFloat() * sw * 0.5f, 20.0f + this.random.nextFloat() * sh * 0.45f, left ? speed : -speed, this.random.nextFloat() * 6.28f, 1.2f + this.random.nextFloat() * 1.3f});
        }
        while (this.bats.size() > target) {
            this.bats.remove(this.bats.size() - 1);
        }
        double t = System.currentTimeMillis() / 1000.0;
        int body = ColorUtil.getColor(25, 12, 35, 0.92f);
        int eye = ColorUtil.getColor(255, 140, 30, 1.0f);
        for (float[] b : this.bats) {
            b[0] += b[2] * dt;
            float y = b[1] + (float)Math.sin(t * 2.2 + b[3]) * 10.0f;
            if (b[2] > 0 && b[0] > sw + 40.0f || b[2] < 0 && b[0] < -40.0f) {
                b[0] = b[2] > 0 ? -40.0f : sw + 40.0f;
                b[1] = 20.0f + this.random.nextFloat() * sh * 0.45f;
            }
            String[] frame = BAT[(int)((t * 7.0 + b[3]) % 2.0)];
            float cell = b[4];
            float x0 = b[0] - 4.5f * cell;
            for (int row = 0; row < frame.length; ++row) {
                for (int col = 0; col < frame[row].length(); ++col) {
                    if (frame[row].charAt(col) != 'X') continue;
                    QuickImports.rectangle.render(ShapeProperties.create(m, x0 + col * cell, y + row * cell, cell + 0.2f, cell + 0.2f).color(body).build());
                }
            }
            QuickImports.rectangle.render(ShapeProperties.create(m, x0 + 3.6f * cell, y + 1.2f * cell, cell * 0.5f, cell * 0.5f).color(eye).build());
            QuickImports.rectangle.render(ShapeProperties.create(m, x0 + 4.9f * cell, y + 1.2f * cell, cell * 0.5f, cell * 0.5f).color(eye).build());
        }
    }

    // ------------------------------------------------------------------ world effects and sounds

    @EventHandler
    public void onTick(TickEvent event) {
        this.applyTheme();
        class_310 mc = class_310.method_1551();
        class_638 level = mc.field_1687;
        class_746 player = mc.field_1724;
        if (level == null || player == null || mc.method_1493()) {
            return;
        }
        boolean halloween = this.halloween();
        if (this.world.isValue()) {
            if (this.snow == null) {
                this.snow = Holiday.particle("minecraft:snowflake");
                this.soul = Holiday.particle("minecraft:soul");
                this.smoke = Holiday.particle("minecraft:large_smoke");
            }
            class_243 pos = player.method_73189();
            int count = Math.round((halloween ? 2.5f : 8.0f) * this.intensity.getValue());
            for (int i = 0; i < count; ++i) {
                double x = pos.field_1352 + (this.random.nextDouble() * 2.0 - 1.0) * 14.0;
                double z = pos.field_1350 + (this.random.nextDouble() * 2.0 - 1.0) * 14.0;
                if (halloween) {
                    class_2394 p = this.random.nextInt(3) == 0 ? this.soul : this.smoke;
                    if (p != null) {
                        level.method_8406(p, x, pos.field_1351 + this.random.nextDouble() * 1.5, z, 0.0, 0.02, 0.0);
                    }
                } else if (this.snow != null) {
                    level.method_8406(this.snow, x, pos.field_1351 + 5.0 + this.random.nextDouble() * 8.0, z, (this.random.nextDouble() - 0.5) * 0.02, -0.03, (this.random.nextDouble() - 0.5) * 0.02);
                }
            }
        }
        if (this.sounds.isValue() && --this.soundIn <= 0) {
            this.soundIn = 400 + this.random.nextInt(600);   // every 20-50 seconds
            class_243 pos = player.method_73189();
            if (halloween) {
                String[] spooky = {"ambient.cave", "entity.bat.ambient", "entity.witch.ambient", "entity.zombie.ambient", "block.bell.resonate"};
                Holiday.play(spooky[this.random.nextInt(spooky.length)], pos.field_1352 + this.random.nextGaussian() * 6.0, pos.field_1351 + 1.0, pos.field_1350 + this.random.nextGaussian() * 6.0, 0.35f, 0.8f + this.random.nextFloat() * 0.3f);
            } else {
                // a little jingle: three bell notes going up
                float[] pitches = {1.0f, 1.26f, 1.5f};
                for (float pitch : pitches) {
                    Holiday.play("block.note_block.bell", pos.field_1352, pos.field_1351 + 1.0, pos.field_1350, 0.25f, pitch);
                }
                Holiday.play("block.amethyst_block.chime", pos.field_1352, pos.field_1351 + 1.0, pos.field_1350, 0.5f, 1.2f);
            }
        }
    }

    private static class_2394 particle(String id) {
        try {
            Object type = class_7923.field_41180.method_63535(class_2960.method_60654((String)id));
            return type instanceof class_2394 ? (class_2394)type : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static class_3419 ambient;

    private static void play(String id, double x, double y, double z, float volume, float pitch) {
        class_310 mc = class_310.method_1551();
        if (mc.method_1483() == null) {
            return;
        }
        if (ambient == null) {
            for (class_3419 c : class_3419.values()) {
                if ("ambient".equals(c.method_14840())) {
                    ambient = c;
                }
            }
            if (ambient == null) {
                ambient = class_3419.values()[0];
            }
        }
        class_3414 event = class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)id));
        mc.method_1483().method_4873((class_1113)new class_1109(event, ambient, volume, pitch, class_5819.method_43047(), x, y, z));
    }
}
