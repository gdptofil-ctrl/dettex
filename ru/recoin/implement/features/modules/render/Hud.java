/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render;

import java.awt.Color;
import java.util.List;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;

public class Hud
extends Module {
    private String cachedThemeName = "";
    private int cachedThemeColor = -9659651;
    private int cachedGradientStart = -9659651;
    private int cachedGradientEnd = -7569409;
    private int targetThemeColor = -9659651;
    private int targetGradientStart = -9659651;
    private int targetGradientEnd = -7569409;
    private long cachedThemeColorTime;
    private transient int appliedAccentColor = -9659651;
    public final SelectSetting hudLayoutSetting = new SelectSetting("Hud", "HUD style").value("Hud 1", "Hud 2", "Hud 3").selected("Hud 1");
    public final MultiSelectSetting interfaceSettings = new MultiSelectSetting("Elements", "Customize the interface elements").value("Watermark", "Hot Keys", "ServerHelper Binds", "Potions", "Staff", "Target Hud", "Armor", "Cooldowns", "\u041a\u0443\u043b\u0434\u0430\u0443\u043d\u044b \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u0430", "Inventory", "Notifications", "Media Player", "HotBar");
    public final SelectSetting languageSetting = new SelectSetting("Language", "Client interface language").value("\u0420\u0443\u0441\u0441\u043a\u0438\u0439", "English").selected("\u0420\u0443\u0441\u0441\u043a\u0438\u0439").visible(() -> false);
    public final MultiSelectSetting notificationSettings = new MultiSelectSetting("Notifications", "Choose when the notification will appear").value("Module Switch", "Staff Join", "Item Pick Up", "Item Cooldown", "Totem Pop", "Auto Armor", "Break Shield", "Potion Expire", "Low Durability").selected("Module Switch", "Item Cooldown").visible(() -> false);
    public final BooleanSetting targetRedOnHit = new BooleanSetting("Target Red On Hit", "Target HUD hit tint").setValue(true).visible(() -> false);
    public final BooleanSetting targetHitParticles = new BooleanSetting("Target Hit Particles", "Target HUD hit particles").setValue(true).visible(() -> false);
    public final BooleanSetting targetShowOnHover = new BooleanSetting("Target Show On Hover", "Show Target HUD while looking at an entity").setValue(true).visible(() -> false);
    public final BooleanSetting targetFunTimeDurability = new BooleanSetting("Target FunTime Durability", "Show FunTime armor durability bars").setValue(false).visible(() -> false);
    public final BooleanSetting potionsSide = new BooleanSetting("Potions Side", "Render effects as separate side blocks").visible(() -> false);
    public final BooleanSetting watermarkCentered = new BooleanSetting("Watermark Centered", "Place watermark at the top center").visible(() -> false);
    public final BooleanSetting watermarkSeparateElements = new BooleanSetting("Separate Watermark Elements", "Render watermark metrics as separate blocks").visible(() -> false);
    public final MultiSelectSetting watermarkMetrics = new MultiSelectSetting("Watermark Metrics", "Visible watermark metrics").value("BPS", "COORDS", "PING", "TIME", "TPS", "FPS").selected("BPS", "COORDS", "PING", "TIME", "TPS", "FPS").visible(() -> false);
    public final TextSetting watermarkNameSetting = new TextSetting("Watermark Name", "\u0418\u043c\u044f \u043a\u043b\u0438\u0435\u043d\u0442\u0430 \u0432 \u0432\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a\u0435").setText("DettexClient").setMax(24);
    public final BooleanSetting disableBlurSetting = new BooleanSetting("Disable Blur", "Disable interface blur");
    public final MultiSelectSetting disableBlurTargets = new MultiSelectSetting("Disable Blur In", "Choose where blur is disabled").value("HUD", "GUI").selected("HUD", "GUI").visible(() -> this.disableBlurSetting.isValue());
    public final BooleanSetting disableShadowsSetting = new BooleanSetting("Disable Shadows", "Disable interface shadows");
    public final MultiSelectSetting disableShadowsTargets = new MultiSelectSetting("Disable Shadows In", "Choose where shadows are disabled").value("HUD", "GUI").selected("HUD", "GUI").visible(() -> this.disableShadowsSetting.isValue());
    public final BooleanSetting disableOutlineSetting = new BooleanSetting("Disable Outline", "Disable interface outlines");
    public final MultiSelectSetting disableOutlineTargets = new MultiSelectSetting("Disable Outline In", "Choose where outlines are disabled").value("HUD", "GUI").selected("HUD", "GUI").visible(() -> this.disableOutlineSetting.isValue());
    public final ValueSetting guiScaleSetting = new ValueSetting("GUI Scale", "\u041c\u0430\u0441\u0448\u0442\u0430\u0431 \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430").range(0.6f, 1.4f).step(0.05f).setValue(0.85f).visible(() -> false);
    public final ValueSetting guiOpacitySetting = new ValueSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c GUI", "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430 \u043c\u0435\u043d\u044e").range(0.1f, 1.0f).step(0.05f).setValue(1.0f).visible(() -> false);
    public final ValueSetting shadowStrengthSetting = new ValueSetting("\u0421\u0438\u043b\u0430 \u0442\u0435\u043d\u0435\u0439", "\u0421\u0438\u043b\u0430 \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u044f \u0442\u0435\u043d\u0435\u0439 \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430").range(0.0f, 1.0f).step(0.05f).setValue(1.0f).visible(() -> false);
    public final BooleanSetting interface3DSetting = new BooleanSetting("3D Interface", "\u041e\u0442\u0434\u0430\u043b\u0435\u043d\u0438\u0435 \u0438 \u0440\u0430\u0441\u043f\u0430\u0434 GUI \u043d\u0430 \u0447\u0430\u0441\u0442\u0438\u0446\u044b \u043f\u0440\u0438 \u0437\u0430\u043a\u0440\u044b\u0442\u0438\u0438").setValue(true).visible(() -> false);
    public final ValueSetting hudOpacitySetting = new ValueSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c HUD", "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0444\u043e\u043d\u0430 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432 HUD").range(0.1f, 1.0f).step(0.05f).setValue(1.0f);
    public final ValueSetting hudBlurStrengthSetting = new ValueSetting("\u0421\u0438\u043b\u0430 \u0431\u043b\u044e\u0440\u0430 HUD", "\u0421\u0438\u043b\u0430 \u0440\u0430\u0437\u043c\u044b\u0442\u0438\u044f \u0444\u043e\u043d\u0430 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432 HUD").range(0.1f, 2.0f).step(0.05f).setValue(1.0f);
    public final ColorSetting hudBlurColorSetting = new ColorSetting("HUD Blur Color", "\u0426\u0432\u0435\u0442 \u0431\u043b\u044e\u0440\u0430 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432 HUD").setColor(-16777216).presets(-16777216, -15656921, -1, -9659651, -7569409, -12200449, -11147598, -37990);
    public final ColorSetting guiAccentColorSetting = new ColorSetting("GUI Accent Color", "\u0426\u0432\u0435\u0442 \u0438\u043a\u043e\u043d\u043e\u043a \u0438 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432 \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f GUI").setColor(-9659651).presets(-9659651, -7569409, -12200449, -11147598, -37990, -1).visible(() -> false);
    public final ColorSetting guiBackgroundColorSetting = new ColorSetting("GUI Background Color", "\u0426\u0432\u0435\u0442 \u0442\u043e\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u044f \u0431\u043b\u044e\u0440\u0430 GUI").setColor(-15656921).presets(-15656921, -15194824, -15260373, -14345685, -13950944, -14080232, -15193825, -14670804).visible(() -> false);
    public final SelectSetting themeSetting = new SelectSetting("Theme", "Select client color theme").value("Blue", "Deep Blue", "White", "Green", "Pink", "Purple", "Red", "Red-Black", "Orange", "Turquoise", "Cyberpunk", "Neon Violet", "Midnight Rose", "Toxic Lime", "Blood Moon", "Ocean Wave", "Lavender Dream", "Matrix", "Sunset", "Rainbow", "Gold", "Emerald", "Azure", "Arctic", "Amethyst", "Lime", "Cherry", "Rose", "Halloween", "Winter").selected("Blue");
    public final ButtonSetting openThemeEditor = new ButtonSetting("Theme Editor", "Open the theme editor column").setButtonName("\u041e\u0442\u043a\u0440\u044b\u0442\u044c").setRunnable(() -> {}).visible(() -> false);
    public final SelectSetting customThemeColorCount = new SelectSetting("Color Count", "How many colors custom theme uses").value("1", "2", "3", "4").selected("2").visible(() -> false);
    public final ColorSetting customThemeColor1 = new ColorSetting("Color 1", "First custom theme color").setColor(-9659651).visible(() -> false);
    public final ColorSetting customThemeColor2 = new ColorSetting("Color 2", "Second custom theme color").setColor(-7569409).visible(() -> false);
    public final ColorSetting customThemeColor3 = new ColorSetting("Color 3", "Third custom theme color").setColor(-12200449).visible(() -> false);
    public final ColorSetting customThemeColor4 = new ColorSetting("Color 4", "Fourth custom theme color").setColor(-11147598).visible(() -> false);
    public final ColorSetting customTextColor = new ColorSetting("Text Color", "Custom theme text color").setColor(-1644826).visible(() -> false);
    public final ColorSetting customPanelColor = new ColorSetting("Panel Color", "Custom theme panel color").setColor(-268303606).visible(() -> false);
    public final ColorSetting customOutlineColor = new ColorSetting("Outline Color", "Custom theme outline color").setColor(-12299142).visible(() -> false);
    public final ColorSetting customInactiveOutlineColor = new ColorSetting("Inactive Outline", "Outline color of disabled modules").setColor(-13616825).visible(() -> false);
    public final ColorSetting customActiveModuleColor = new ColorSetting("Active Module", "Background color of enabled modules").setColor(-14338218).visible(() -> false);
    public final ColorSetting customInactiveModuleColor = new ColorSetting("Inactive Module", "Background color of disabled modules").setColor(-15263199).visible(() -> false);
    public final ColorSetting customInactiveTextColor = new ColorSetting("Inactive Text", "Text color of disabled modules").setColor(-7169360).visible(() -> false);
    public final ColorSetting customSliderColor = new ColorSetting("Slider", "Active slider color").setColor(-9659651).visible(() -> false);
    public final ColorSetting customInactiveSliderColor = new ColorSetting("Inactive Slider", "Inactive slider color").setColor(-13616825).visible(() -> false);
    public final TextSetting customThemeName = new TextSetting("Custom Theme Name", "Name of the custom theme").setText("Custom").visible(() -> false);
    public final ColorSetting colorSetting = new ColorSetting("Global Interface Color", "Select the interface accent color").setColor(-13286168).presets(-9659651, -11698945, -12200449, -11147598, -10491011, -37990, -43691, -23178, -11930, -7569409);

    public static Hud getInstance() {
        return Instance.get(Hud.class);
    }

    public Hud() {
        super("Interface", ModuleCategory.RENDER);
        this.setup(this.languageSetting, this.hudLayoutSetting, this.interfaceSettings, this.openThemeEditor, this.guiScaleSetting, this.guiOpacitySetting, this.hudOpacitySetting, this.hudBlurStrengthSetting, this.hudBlurColorSetting, this.interface3DSetting, this.notificationSettings, this.targetRedOnHit, this.targetHitParticles, this.targetShowOnHover, this.targetFunTimeDurability, this.potionsSide, this.watermarkCentered, this.watermarkSeparateElements, this.watermarkMetrics, this.watermarkNameSetting, this.disableBlurSetting, this.disableBlurTargets, this.disableShadowsSetting, this.disableShadowsTargets, this.disableOutlineSetting, this.disableOutlineTargets, this.themeSetting, this.customThemeColorCount, this.customThemeColor1, this.customThemeColor2, this.customThemeColor3, this.customThemeColor4, this.customTextColor, this.customPanelColor, this.customOutlineColor, this.customInactiveOutlineColor, this.customActiveModuleColor, this.customInactiveModuleColor, this.customInactiveTextColor, this.customSliderColor, this.customInactiveSliderColor, this.customThemeName, this.shadowStrengthSetting, this.guiAccentColorSetting, this.guiBackgroundColorSetting);
    }

    public List<Setting> getGuiSettings() {
        return List.of(this.guiScaleSetting, this.guiOpacitySetting, this.shadowStrengthSetting, this.guiAccentColorSetting, this.guiBackgroundColorSetting, this.interface3DSetting, this.languageSetting);
    }

    public float getShadowStrength() {
        return this.shadowStrengthSetting != null ? this.shadowStrengthSetting.getValue() : 1.0f;
    }

    public boolean shouldDisableBlur(boolean hud, boolean gui) {
        return this.shouldDisableIn(this.disableBlurSetting, this.disableBlurTargets, hud, gui);
    }

    public boolean shouldDisableShadows(boolean hud, boolean gui) {
        return this.shouldDisableIn(this.disableShadowsSetting, this.disableShadowsTargets, hud, gui);
    }

    public boolean shouldDisableOutline(boolean hud, boolean gui) {
        return this.shouldDisableIn(this.disableOutlineSetting, this.disableOutlineTargets, hud, gui);
    }

    public boolean shouldUseSimpleHudPanels() {
        return this.shouldDisableBlur(true, false) && this.shouldDisableShadows(true, false) && this.shouldDisableOutline(true, false);
    }

    public float getHudScale() {
        return 1.0f;
    }

    public float updateHudScale(float delta) {
        return 1.0f;
    }

    public float getRenderedHudScale() {
        return 1.0f;
    }

    public float getClickGuiScale() {
        return this.guiScaleSetting.getValue();
    }

    public boolean isRussianLanguage() {
        return !this.languageSetting.isSelected("English");
    }

    /** Hud 3: the Hud 1 layout with neon-glass panels (rounder, darker glass, animated theme glow and outline). */
    public boolean isHud3() {
        return this.hudLayoutSetting != null && this.hudLayoutSetting.isSelected("Hud 3");
    }

    public boolean isHud2() {
        return this.hudLayoutSetting != null && this.hudLayoutSetting.isSelected("Hud 2");
    }

    private boolean shouldDisableIn(BooleanSetting setting, MultiSelectSetting targets, boolean hud, boolean gui) {
        return setting.isValue() && (hud && targets.isSelected("HUD") || gui && targets.isSelected("GUI"));
    }

    public int themeColor() {
        this.refreshThemeCache(System.currentTimeMillis());
        return this.cachedThemeColor;
    }

    public void syncThemeAccent() {
        int color = this.guiAccentColorSetting.getColor();
        if (color == this.appliedAccentColor) {
            return;
        }
        this.appliedAccentColor = color;
        if (color == this.themeColorFor(this.themeSetting.getSelected())) {
            return;
        }
        this.customThemeColorCount.setSelected("1");
        this.customThemeColor1.setColor(color);
        this.customOutlineColor.setColor(color);
        this.customActiveModuleColor.setColor(ColorUtil.overCol(-15263199, color, 0.25f));
        this.themeSetting.setSelected("Custom");
        this.cachedThemeName = "";
        this.colorSetting.setColor(color);
    }

    public void applyThemePreset(String name) {
        this.themeSetting.setSelected(name);
        int color = this.themeColorFor(name);
        this.guiAccentColorSetting.setColor(color);
        this.appliedAccentColor = this.guiAccentColorSetting.getColor();
        this.cachedThemeName = "";
        this.refreshThemeCache(System.currentTimeMillis());
    }

    public int themeGradientStart() {
        this.refreshThemeCache(System.currentTimeMillis());
        return this.cachedGradientStart;
    }

    public int themeGradientEnd() {
        this.refreshThemeCache(System.currentTimeMillis());
        return this.cachedGradientEnd;
    }

    public int themeTextColor() {
        return this.isCustomTheme() ? this.customTextColor.getColor() : -1644826;
    }

    public int themePanelColor() {
        return this.isCustomTheme() ? this.customPanelColor.getColor() : -268303606;
    }

    public int themeOutlineColor() {
        return this.isCustomTheme() ? this.customOutlineColor.getColor() : -12299142;
    }

    public int themeInactiveOutlineColor() {
        return this.isCustomTheme() ? this.customInactiveOutlineColor.getColor() : -13616825;
    }

    public int themeActiveModuleColor() {
        return this.isCustomTheme() ? this.customActiveModuleColor.getColor() : -14338218;
    }

    public int themeInactiveModuleColor() {
        return this.isCustomTheme() ? this.customInactiveModuleColor.getColor() : -15263199;
    }

    public int themeInactiveTextColor() {
        return this.isCustomTheme() ? this.customInactiveTextColor.getColor() : -7169360;
    }

    public int themeSliderColor() {
        return this.isCustomTheme() ? this.customSliderColor.getColor() : this.themeColor();
    }

    public int themeInactiveSliderColor() {
        return this.isCustomTheme() ? this.customInactiveSliderColor.getColor() : -13616825;
    }

    public int themeGradientStartFor(String name) {
        return this.themeGradientStartFor(name, System.currentTimeMillis());
    }

    public int themeGradientEndFor(String name) {
        return this.themeGradientEndFor(name, System.currentTimeMillis());
    }

    public int themeColorFor(String name) {
        if ("Custom".equals(name)) {
            return this.customThemeAccentColor();
        }
        return ColorUtil.overCol(this.themeGradientStartFor(name), this.themeGradientEndFor(name), 0.5f);
    }

    public int[] themeColorsFor(String name) {
        if ("Custom".equals(name)) {
            return this.customThemeColors();
        }
        return new int[]{this.themeGradientStartFor(name), this.themeGradientEndFor(name)};
    }

    public int themeFadeColor(int speed, int index) {
        if (!this.isCustomTheme()) {
            return ColorUtil.fade(speed, index, this.themeGradientStart(), this.themeGradientEnd());
        }
        int colorCount = this.getCustomThemeColorCount();
        if (colorCount == 1) {
            return this.customThemeColor(0);
        }
        float cycle = ((float)System.currentTimeMillis() / (float)speed + (float)index) % 360.0f / 360.0f;
        float scaled = cycle * (float)colorCount;
        int first = (int)Math.floor(scaled) % colorCount;
        int second = (first + 1) % colorCount;
        return ColorUtil.overCol(this.customThemeColor(first), this.customThemeColor(second), scaled - (float)Math.floor(scaled));
    }

    private void refreshThemeCache(long now) {
        this.syncThemeAccent();
        String theme = this.themeSetting.getSelected();
        boolean animated = "Sunset".equals(theme) || "Rainbow".equals(theme);
        boolean custom = "Custom".equals(theme);
        if (!theme.equals(this.cachedThemeName) || custom || animated && now - this.cachedThemeColorTime > 50L) {
            boolean switched = !theme.equals(this.cachedThemeName);
            this.cachedThemeName = theme;
            this.targetGradientStart = this.themeGradientStartFor(theme, now);
            this.targetGradientEnd = this.themeGradientEndFor(theme, now);
            this.targetThemeColor = custom ? this.customThemeAccentColor() : ColorUtil.overCol(this.targetGradientStart, this.targetGradientEnd, 0.5f);
            this.cachedThemeColorTime = now;
            if (switched) {
                this.cachedGradientStart = this.targetGradientStart;
                this.cachedGradientEnd = this.targetGradientEnd;
                this.cachedThemeColor = this.targetThemeColor;
            }
        }
        this.cachedGradientStart = ColorUtil.overCol(this.cachedGradientStart, this.targetGradientStart, 0.13f);
        this.cachedGradientEnd = ColorUtil.overCol(this.cachedGradientEnd, this.targetGradientEnd, 0.13f);
        this.cachedThemeColor = ColorUtil.overCol(this.cachedThemeColor, this.targetThemeColor, 0.13f);
        this.colorSetting.setColor(this.cachedThemeColor);
    }

    public int themeGradientStartFor(String name, long now) {
        return switch (name) {
            case "Custom" -> this.customThemeColor(0);
            case "Blue" -> -9659651;
            case "Deep Blue" -> -15053313;
            case "Green" -> -11147598;
            case "Pink" -> -37990;
            case "Purple" -> -7569409;
            case "Red" -> -43691;
            case "Red-Black" -> -4907218;
            case "Orange" -> -23178;
            case "Turquoise" -> -12200491;
            case "Azure" -> -12605441;
            case "Arctic" -> -6297601;
            case "Cyan" -> -12200449;
            case "Mint" -> -11147598;
            case "Emerald" -> -13707917;
            case "Lime" -> -5832883;
            case "Rose" -> -37990;
            case "Cherry" -> -43691;
            case "Coral" -> -32914;
            case "Peach" -> -19574;
            case "Amber" -> -22746;
            case "Gold" -> -11930;
            case "Violet" -> -7569409;
            case "Amethyst" -> -3703297;
            case "Steel" -> -6641736;
            case "White" -> -1577224;
            case "Cyberpunk" -> -6656;
            case "Neon Violet" -> -6487809;
            case "Midnight Rose" -> -13954987;
            case "Toxic Lime" -> -16711834;
            case "Blood Moon" -> -57817;
            case "Ocean Wave" -> -16099126;
            case "Lavender Dream" -> -5005861;
            case "Matrix" -> -15914984;
            case "Sunset" -> Color.HSBtoRGB(this.smoothCycle(now, 16000L, 0.015f, 0.085f), 0.68f, 1.0f) | 0xFF000000;
            case "Rainbow" -> Color.HSBtoRGB((float)(now % 18000L) / 18000.0f, 0.56f, 1.0f) | 0xFF000000;
            case "Halloween" -> 0xFFFF8A1E;
            case "Winter" -> 0xFF9FE2FF;
            default -> -9659651;
        };
    }

    public int themeGradientEndFor(String name, long now) {
        return switch (name) {
            case "Custom" -> this.customThemeColor(Math.max(0, this.getCustomThemeColorCount() - 1));
            case "Blue" -> -7569409;
            case "Deep Blue" -> -16376902;
            case "Green" -> -13707917;
            case "Pink" -> -3703297;
            case "Purple" -> -3703297;
            case "Red" -> -32914;
            case "Red-Black" -> -15069163;
            case "Orange" -> -11930;
            case "Turquoise" -> -11147598;
            case "Azure" -> -9659651;
            case "Arctic" -> -12200449;
            case "Cyan" -> -11147598;
            case "Mint" -> -13707917;
            case "Emerald" -> -5832883;
            case "Lime" -> -11930;
            case "Rose" -> -3703297;
            case "Cherry" -> -37990;
            case "Coral" -> -19574;
            case "Peach" -> -11930;
            case "Amber" -> -11930;
            case "Gold" -> -22746;
            case "Violet" -> -3703297;
            case "Amethyst" -> -37990;
            case "Steel" -> -9659651;
            case "White" -> -6297601;
            case "Cyberpunk" -> -16715521;
            case "Neon Violet" -> -65409;
            case "Midnight Rose" -> -54662;
            case "Toxic Lime" -> -16718337;
            case "Blood Moon" -> -35072;
            case "Ocean Wave" -> -14628457;
            case "Lavender Dream" -> -8333057;
            case "Matrix" -> -16711800;
            case "Sunset" -> {
                float hue = this.smoothCycle(now, 16000L, 0.015f, 0.085f) + 0.12f;
                yield Color.HSBtoRGB(hue - (float)Math.floor(hue), 0.72f, 1.0f) | 0xFF000000;
            }
            case "Rainbow" -> {
                float hue = (float)(now % 18000L) / 18000.0f + 0.18f;
                yield Color.HSBtoRGB(hue - (float)Math.floor(hue), 0.56f, 1.0f) | 0xFF000000;
            }
            case "Halloween" -> 0xFF9B4DFF;
            case "Winter" -> 0xFFF2FBFF;
            default -> -7569409;
        };
    }

    private boolean isCustomTheme() {
        return this.themeSetting.isSelected("Custom");
    }

    private int getCustomThemeColorCount() {
        try {
            return Math.max(1, Math.min(4, Integer.parseInt(this.customThemeColorCount.getSelected())));
        }
        catch (NumberFormatException ignored) {
            return 2;
        }
    }

    private int[] customThemeColors() {
        int count = this.getCustomThemeColorCount();
        int[] colors = new int[count];
        for (int i = 0; i < count; ++i) {
            colors[i] = this.customThemeColor(i);
        }
        return colors;
    }

    private int customThemeColor(int index) {
        return switch (Math.max(0, Math.min(3, index))) {
            case 0 -> this.customThemeColor1.getColor();
            case 1 -> this.customThemeColor2.getColor();
            case 2 -> this.customThemeColor3.getColor();
            default -> this.customThemeColor4.getColor();
        };
    }

    private int customThemeAccentColor() {
        int count = this.getCustomThemeColorCount();
        int color = this.customThemeColor(0);
        for (int i = 1; i < count; ++i) {
            color = ColorUtil.overCol(color, this.customThemeColor(i), 1.0f / (float)(i + 1));
        }
        return color;
    }

    private float smoothCycle(long periodMs, float min, float max) {
        return this.smoothCycle(System.currentTimeMillis(), periodMs, min, max);
    }

    private float smoothCycle(long now, long periodMs, float min, float max) {
        double phase = (double)(now % periodMs) / (double)periodMs;
        double wave = (Math.sin(phase * Math.PI * 2.0 - 1.5707963267948966) + 1.0) / 2.0;
        return min + (max - min) * (float)wave;
    }
}

