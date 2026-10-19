/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  org.joml.Matrix3x2fStack
 */
package ru.recoin.implement.screens.menu.components.implement.other;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Matrix3x2fStack;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.MultiColorSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.common.util.render.ShapeShaderRenderer;
import ru.recoin.common.util.render.pipeline.ThemeWavePipeline;
import ru.recoin.core.Main;
import ru.recoin.core.client.ProfileAvatar;
import ru.recoin.core.client.UserProfile;
import ru.recoin.implement.features.modules.movement.fireworkbooster.BoosterSectionHeaderSetting;
import ru.recoin.implement.features.modules.player.AutoBuy;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.cosmetics.CosmeticWeaponModels;
import ru.recoin.implement.screens.autobuy.AutoBuyScreen;
import ru.recoin.implement.screens.menu.AutoCrystalLocalization;
import ru.recoin.implement.screens.menu.ConfigPanel;
import ru.recoin.implement.screens.menu.MenuControls;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.MenuScreen;
import ru.recoin.implement.screens.menu.MenuSearch;
import ru.recoin.implement.screens.menu.MenuStyle;
import ru.recoin.implement.screens.menu.ModuleDescriptionProvider;
import ru.recoin.implement.screens.menu.ModuleSettingLocalizer;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.other.SearchComponent;
import ru.recoin.implement.screens.menu.components.implement.window.AbstractWindow;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.ColorWindow;

public class DropdownMenuComponent
extends AbstractComponent {
    private static final ThemeWavePipeline THEME_WAVE = new ThemeWavePipeline();
    private static final int TEXT = -986118;
    private static final int MUTED = -7301470;
    private static final String ICON_CHECK = "\ue05a";
    private static final String ICON_CROSS = "\ue05c";
    private static final float GAP = 8.0f;
    private static final float HEADER_CARD_H = 27.0f;
    private static final float HEADER_H = 27.0f;
    private static final float ROW_H = 30.0f;
    private static final float ROW_GAP = 8.0f;
    private static final float BOOLEAN_CHIP_ROW_H = 15.0f;
    private static final float BOOLEAN_CHIP_MAX_W = 94.0f;
    private static final float COSMETIC_TILE_SIZE = 28.0f;
    private static final float COSMETIC_TILE_STEP = 32.0f;
    private static final float PAD = 11.0f;
    private static final long THEME_EDITOR_ANIMATION_NANOS = 360000000L;
    private static final double CHIP_SELECTION_SMOOTH = 1.75;
    private static final Set<String> HIDDEN_MODULES = Set.of("AutoFarm", "AncientFarmer", "AutoEnd", "AutoInventory", "AutoTrade", "CreeperFarm");
    private static final Map<String, String> TRANSLATION_CACHE = new HashMap<String, String>();
    private final SearchComponent search;
    private final Map<ModuleCategory, List<Module>> modules = new EnumMap<ModuleCategory, List<Module>>(ModuleCategory.class);
    private final Map<String, Float> scroll = new HashMap<String, Float>();
    private final Map<String, Float> smoothScroll = new HashMap<String, Float>();
    private final ConfigPanel configs = new ConfigPanel();
    private String appliedSearch = "";
    private final Set<Module> searchExpanded = new HashSet<Module>();
    private final Map<ModuleCategory, Bounds> panels = new EnumMap<ModuleCategory, Bounds>(ModuleCategory.class);
    private final Map<Module, Bounds> moduleBounds = new HashMap<Module, Bounds>();
    private final Map<Module, Bounds> visibleModuleBounds = new HashMap<Module, Bounds>();
    private final Map<Module, Bounds> settingsIconBounds = new HashMap<Module, Bounds>();
    private final Map<Module, Float> expandAnimation = new HashMap<Module, Float>();
    private final Map<String, Float> navigationHover = new HashMap<String, Float>();
    private final Map<String, Float> navigationSelection = new HashMap<String, Float>();
    private final Map<String, FontRenderer> navigationIconFonts = new HashMap<String, FontRenderer>();
    private long navigationFrameNanos;
    private float navigationHoverBlend;
    private final Map<Module, Float> bindChipAnimation = new HashMap<Module, Float>();
    private final Map<Module, Float> moduleBindWidthAnimation = new HashMap<Module, Float>();
    private final Map<Module, ModuleMotion> moduleMotions = new HashMap<Module, ModuleMotion>();
    private List<Module> moduleLayoutOrder = List.of();
    private String moduleLayoutView = "";
    private Module raisedFavoriteModule;
    private final Map<Setting, Boolean> stateIconCache = new HashMap<Setting, Boolean>();
    private final Map<Setting, Float> stateIconAnimation = new HashMap<Setting, Float>();
    private final Map<Setting, Animation> profileToggleAnimation = new HashMap<Setting, Animation>();
    private final Map<BindSetting, Float> settingBindAnimation = new HashMap<BindSetting, Float>();
    private final Map<BindSetting, Float> settingBindWidthAnimation = new HashMap<BindSetting, Float>();
    private final Map<ValueSetting, Float> sliderAnimation = new HashMap<ValueSetting, Float>();
    private final Map<Setting, Map<String, Float>> chipSelectionAnimation = new HashMap<Setting, Map<String, Float>>();
    private final Map<String, Float> themeCardHover = new HashMap<String, Float>();
    private final Map<String, Float> cosmeticItemHover = new HashMap<String, Float>();
    private final Map<Setting, Bounds> settingBounds = new HashMap<Setting, Bounds>();
    private final Map<Setting, List<ChipBounds>> chipBounds = new HashMap<Setting, List<ChipBounds>>();
    private final List<ThemeBounds> themeBounds = new ArrayList<ThemeBounds>();
    private final List<CosmeticItemBounds> cosmeticItemBounds = new ArrayList<CosmeticItemBounds>();
    private final Set<Module> expanded = new HashSet<Module>();
    private ValueSetting draggingValue;
    private final Map<Setting, Float> actionHover = new IdentityHashMap<Setting, Float>();
    private TextSetting editingText;
    private BindSetting bindingSetting;
    private Module bindingModule;
    private String clickedTheme = "";
    private String pendingTheme = "";
    private long themeClickTime;
    private long themeWaveStart = -1L;
    private int themeWaveColor = -1;
    private boolean themeEditorOpen;
    private float themeEditorAnimation;
    private float themeEditorAnimationStart;
    private float themeEditorAnimationTarget;
    private long themeEditorAnimationStartNanos;
    private float themeEditorScroll;
    private float cosmeticsScroll;
    private float cosmeticsScrollTarget;
    private Bounds themeEditorPanel;
    private Bounds themeEditorList;
    private Bounds cosmeticsPageBounds;
    private Bounds themePlusBounds;
    private final Map<Integer, Bounds> themeColorCountBounds = new HashMap<Integer, Bounds>();
    private Bounds themeCloseBounds;
    private Module hoveredModule;
    private Module describedModule;
    private float descriptionAlpha;
    private AutoBuyScreen autoBuyPage;
    private Bounds embeddedAutoBuyBounds;
    private Bounds themePageBounds;
    private float themePageScroll;
    private String transitionPage = "";
    private long transitionStartNanos;
    private final Map<ModuleCategory, Bounds> navigation = new EnumMap<ModuleCategory, Bounds>(ModuleCategory.class);
    private final Map<Module, Bounds> favoriteBounds = new HashMap<Module, Bounds>();
    private final Set<Module> favorites = new HashSet<Module>();
    private final Map<Object, Float> starColors = new HashMap<Object, Float>();
    private final Map<Object, Long> starClicks = new HashMap<Object, Long>();
    private final Map<Object, Float> starColorStarts = new HashMap<Object, Float>();
    private final Map<Module, Long> moduleHoverStart = new HashMap<Module, Long>();
    private String section = "";
    private final Map<ModuleCategory, List<Module>> filteredCache = new EnumMap<ModuleCategory, List<Module>>(ModuleCategory.class);
    private String filteredQuery;
    private String filteredSection;
    private int filteredFavoritesHash;
    private boolean filteredCacheValid;
    private static final List<ModuleCategory> RENDER_CATEGORIES = List.of(ModuleCategory.COMBAT, ModuleCategory.MOVEMENT, ModuleCategory.RENDER, ModuleCategory.PLAYER, ModuleCategory.MISC);
    private static final List<String> EXTRA_NAVIGATION_NAMES = List.of("AutoBuy", "Configs", "Scripts", "Cosmetics");
    private Bounds logoButton;
    private long logoClickNanos;
    private Bounds profileButton;
    private Bounds profileSettingsButton;
    private Bounds profileSettingsPanel;
    private Bounds profileSettingsContent;
    private final Map<Setting, Bounds> profileControlBounds = new LinkedHashMap<Setting, Bounds>();
    private final Map<ColorSetting, Long> profileColorClicks = new HashMap<ColorSetting, Long>();
    private final List<ProfileChoiceBounds> profileChoices = new ArrayList<ProfileChoiceBounds>();
    private final Set<Setting> expandedProfileSettings = new HashSet<Setting>();
    private float profileScroll;
    private float profileScrollTarget;
    private long profileScrollTime;
    private float profileOffsetX;
    private float profileOffsetY;
    private float profileDragX;
    private float profileDragY;
    private boolean draggingProfilePanel;
    private float profileContentHeight;
    private boolean profileOpen;
    private float profileOpenAnimation;
    private float profileOpenAnimationStart;
    private float profileOpenAnimationTarget;
    private long profileOpenAnimationStartNanos;
    private ValueSetting draggingProfileValue;
    private float scaleBeforeDrag = 0.85f;
    private final Map<String, Bounds> extraNavigation = new LinkedHashMap<String, Bounds>();
    private int lastMouseX;
    private int lastMouseY;

    public DropdownMenuComponent(SearchComponent search) {
        this.search = search;
    }

    public void rebuildIfNeeded() {
        this.profileOpen = false;
        this.profileOpenAnimation = 0.0f;
        this.profileOpenAnimationStart = 0.0f;
        this.profileOpenAnimationTarget = 0.0f;
        this.profileOpenAnimationStartNanos = 0L;
        this.draggingProfileValue = null;
        this.draggingProfilePanel = false;
        this.moduleMotions.clear();
        this.moduleLayoutOrder = List.of();
        this.moduleLayoutView = "";
        this.raisedFavoriteModule = null;
        this.profileColorClicks.clear();
        this.modules.clear();
        this.filteredCache.clear();
        this.filteredCacheValid = false;
        for (ModuleCategory category : ModuleCategory.values()) {
            this.modules.put(category, new ArrayList());
            this.scroll.putIfAbsent("category:" + category.name(), Float.valueOf(0.0f));
            this.smoothScroll.putIfAbsent("category:" + category.name(), Float.valueOf(0.0f));
        }
        if (Main.getInstance() == null || Main.getInstance().getModuleProvider() == null) {
            return;
        }
        for (Module module2 : Main.getInstance().getModuleProvider().getModules()) {
            if (HIDDEN_MODULES.contains(module2.getName())) continue;
            this.modules.computeIfAbsent(module2.getCategory(), ignored -> new ArrayList()).add(module2);
        }
        HashSet availableModules = new HashSet();
        this.modules.values().forEach(availableModules::addAll);
        this.expanded.retainAll(availableModules);
        this.expandAnimation.keySet().retainAll(availableModules);
        Comparator<Module> alphabetic = Comparator.comparing(module -> module.getVisibleName().toLowerCase(Locale.ROOT));
        this.modules.values().forEach(list -> list.sort(alphabetic));
    }

    public float getAppliedGuiScale() {
        Hud hud = Hud.getInstance();
        return this.draggingProfileValue == hud.guiScaleSetting || this.draggingValue == hud.guiScaleSetting ? this.scaleBeforeDrag : hud.getClickGuiScale();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        boolean selected;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (this.modules.isEmpty()) {
            this.rebuildIfNeeded();
        }
        this.updateSearchExpansion();
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.panels.clear();
        this.moduleBounds.clear();
        this.visibleModuleBounds.clear();
        this.settingsIconBounds.clear();
        this.settingBounds.clear();
        this.chipBounds.clear();
        this.themeBounds.clear();
        this.cosmeticItemBounds.clear();
        this.themeColorCountBounds.clear();
        this.embeddedAutoBuyBounds = null;
        this.themePageBounds = null;
        this.cosmeticsPageBounds = null;
        this.hoveredModule = null;
        this.navigation.clear();
        this.favoriteBounds.clear();
        this.extraNavigation.clear();
        blur.setColorlessMode(true);
        blur.setShadowEnabled(false);
        blur.setStyledBlurRadius(48.0f);
        MenuStyle.shadow(matrix, this.x, this.y, this.width, this.height, 16.0f);
        float backgroundAlpha = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(backgroundAlpha * MenuStyle.backgroundOpacity());
        try {
            float scale = Math.abs(matrix.method_23760().method_23761().m00());
            blur.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(16.0f * scale).color(MenuStyle.surface()).build());
        }
        finally {
            Render2DUtil.setRenderAlpha(backgroundAlpha);
            blur.resetStyledBlurRadius();
            blur.setColorlessMode(false);
        }
        MenuStyle.fill(matrix, this.x, this.y, this.width, this.height, 16.0f, MenuStyle.surface());
        this.drawImmediateOutline(matrix, this.x, this.y, this.width, this.height, 16.0f, MenuStyle.border());
        MenuStyle.separator(matrix, this.x + 137.0f, this.y, this.height, true);
        MenuStyle.separator(matrix, this.x, this.y + 47.0f, this.width, false);
        this.logoButton = new Bounds(this.x + 49.0f, this.y + 7.0f, 38.0f, 34.0f);
        this.drawAnimatedLogo(context, matrix, this.x + 57.0f, this.y + 14.0f);
        MenuStyle.font(8.0f).drawString(matrix, MenuDescriptionLocalizer.isRussian() ? "\u0413\u041b\u0410\u0412\u041d\u041e\u0415" : "MAIN", this.x + 19.0f, this.y + 62.0f, -7301470);
        List<ModuleCategory> categories = RENDER_CATEGORIES;
        long now = System.nanoTime();
        float frameSeconds = this.navigationFrameNanos == 0L ? 0.016666668f : Math.min(0.05f, (float)(now - this.navigationFrameNanos) / 1.0E9f);
        this.navigationFrameNanos = now;
        this.navigationHoverBlend = 1.0f - (float)Math.exp(-frameSeconds / 0.04f);
        for (int index = 0; index < categories.size(); ++index) {
            float ny = this.y + 77.0f + (float)(index * 26);
            this.navigation.put(categories.get(index), this.navigationBounds(this.x + 12.0f, ny));
        }
        int extraIndex = 0;
        for (String name : EXTRA_NAVIGATION_NAMES) {
            float ny = this.y + 253.0f + (float)(extraIndex++ * 24);
            this.extraNavigation.put(name, this.navigationBounds(this.x + 12.0f, ny));
        }
        Bounds favoritesNavigation = this.navigationBounds(this.x + 12.0f, this.y + 207.0f);
        this.extraNavigation.put("Favorites", favoritesNavigation);
        this.drawNavigationHover(matrix, "Favorites", favoritesNavigation, mouseX, mouseY);
        this.drawNavigationLabel(matrix, favoritesNavigation, "\ue0ab", this.sectionName("Favorites"), "Favorites".equals(this.section));
        MenuStyle.font(8.0f).drawString(matrix, MenuDescriptionLocalizer.isRussian() ? "\u041f\u0420\u041e\u0427\u0415\u0415" : "OTHER", this.x + 19.0f, this.y + 238.0f, -7301470);
        for (int index = 0; index < categories.size(); ++index) {
            ModuleCategory category = categories.get(index);
            selected = this.section.isEmpty() && MenuScreen.INSTANCE.category == category;
            this.drawNavigationHover(matrix, category.name(), this.navigation.get((Object)category), mouseX, mouseY);
            this.drawNavigationLabel(matrix, this.navigation.get((Object)category), this.categoryIcon(category), category.getReadableName(), selected);
        }
        for (String name : EXTRA_NAVIGATION_NAMES) {
            selected = name.equals(this.section);
            this.drawNavigationHover(matrix, name, this.extraNavigation.get(name), mouseX, mouseY);
            this.drawNavigationLabel(matrix, this.extraNavigation.get(name), this.sectionIcon(name), name, selected);
        }
        this.renderProfile(context, matrix, mouseX, mouseY);
        float headerAlpha = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(headerAlpha * (1.0f - this.search.getExpansion()));
        float nextX = this.x + 154.0f;
        String headerIcon = this.section.isEmpty() ? this.categoryIcon(MenuScreen.INSTANCE.category) : this.sectionIcon(this.section);
        String headerText = this.section.isEmpty() ? MenuScreen.INSTANCE.category.getReadableName() : this.sectionName(this.section);
        FontRenderer headerFont = MenuStyle.medium(10.0f);
        FontRenderer.TextBounds headerBounds = headerFont.getStringBounds("H");
        float headerY = this.y + 18.0f;
        float headerCenter = headerY + headerBounds.y() + headerBounds.height() / 2.0f;
        FontRenderer iconFont = Fonts.getSize(22, Fonts.Type.COMBINED_ICONS);
        FontRenderer.TextBounds iconBounds = iconFont.getStringBounds(headerIcon);
        iconFont.drawString(matrix, headerIcon, nextX - iconBounds.x(), headerCenter - iconBounds.y() - iconBounds.height() / 2.0f, this.accent());
        headerFont.drawString(matrix, headerText, nextX + 14.0f, headerY, -986118);
        Render2DUtil.setRenderAlpha(headerAlpha);
        String page = this.section.isEmpty() ? MenuScreen.INSTANCE.category.name() : this.section;
        float pageAlpha = this.pageTransitionAlpha(page);
        float previousAlpha = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(previousAlpha * pageAlpha);
        try {
            if ("Configs".equals(this.section)) {
                this.configs.render(context, matrix, this.x + 149.0f, this.y + 59.0f, 479.0f, this.height - 73.0f, mouseX, mouseY, this.search.getText());
            } else if (!this.search.getText().isBlank()) {
                this.renderPanel(context, matrix, MenuScreen.INSTANCE.category, this.x + 149.0f, this.y + 59.0f, 479.0f, this.height - 73.0f, mouseX, mouseY, delta, 0.0f);
            } else if ("Scripts".equals(this.section)) {
                MenuStyle.medium(10.0f).drawCenteredString(matrix, MenuDescriptionLocalizer.isRussian() ? "\u0412 \u0440\u0430\u0437\u0440\u0430\u0431\u043e\u0442\u043a\u0435" : "In development", this.x + 388.5f, this.y + 59.0f + (this.height - 73.0f) / 2.0f - 5.0f, -7301470);
            } else if ("Themes".equals(this.section)) {
                this.themeEditorPanel = null;
                this.renderThemeGallery(matrix, this.x + 149.0f, this.y + 57.0f, 479.0f, this.height - 69.0f, mouseX, mouseY);
            } else if ("AutoBuy".equals(this.section)) {
                this.themeEditorPanel = null;
                this.renderAutoBuyPage(context, mouseX, mouseY, delta, (int)(this.x + 149.0f), (int)(this.y + 59.0f), 479, (int)this.height - 73);
            } else if ("Cosmetics".equals(this.section)) {
                this.themeEditorPanel = null;
                this.renderCosmeticsPage(context, matrix, mouseX, mouseY, (int)(this.x + 149.0f), (int)(this.y + 59.0f), 479, (int)this.height - 73);
            } else {
                this.themeEditorPanel = null;
                this.renderPanel(context, matrix, MenuScreen.INSTANCE.category, this.x + 149.0f, this.y + 59.0f, 479.0f, this.height - 73.0f, mouseX, mouseY, delta, 18.0f * (1.0f - pageAlpha));
            }
        }
        finally {
            Render2DUtil.setRenderAlpha(previousAlpha);
        }
    }

    private void drawLogo(class_4587 matrix, float lx, float ly, int size, int color) {
        Fonts.getSize(size * 2, Fonts.Type.COMBINED_ICONS).drawString(matrix, "\ue0c6", lx, ly, Render2DUtil.applyRenderAlpha(color));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawAnimatedLogo(class_332 context, class_4587 matrix, float lx, float ly) {
        float t = class_3532.method_15363((float)((float)(System.nanoTime() - this.logoClickNanos) / 2.8E8f), (float)0.0f, (float)1.0f);
        float scale = 1.0f - 0.4f * (float)Math.sin((double)t * Math.PI * 2.0) * (float)Math.exp(-t * 2.0f);
        FontRenderer.TextBounds bounds = Fonts.getSize(44, Fonts.Type.COMBINED_ICONS).getStringBounds("\ue0c6");
        float cx = lx + bounds.x() + bounds.width() / 2.0f;
        float cy = ly + bounds.y() + bounds.height() / 2.0f + Render2DUtil.getTextYOffset();
        context.method_51448().pushMatrix();
        try {
            context.method_51448().translate(cx, cy);
            context.method_51448().scale(scale, scale);
            context.method_51448().translate(-cx, -cy);
            this.drawLogo(matrix, lx, ly, 22, this.accent());
        }
        finally {
            context.method_51448().popMatrix();
        }
    }

    private static float boundScale(float progress, float minimum) {
        float t = class_3532.method_15363((float)progress, (float)0.0f, (float)1.0f) - 1.0f;
        float eased = 1.0f + 2.70158f * t * t * t + 1.70158f * t * t;
        return minimum + (1.0f - minimum) * eased;
    }

    private void drawStar(class_4587 matrix, float sx, float sy, int color) {
        this.drawCenteredGlyph(matrix, Fonts.getSize(22, Fonts.Type.COMBINED_ICONS), "\ue0ab", sx + 5.5f, sy + 5.5f, color);
    }

    private void drawCenteredGlyph(class_4587 matrix, FontRenderer font, String text, float centerX, float centerY, int color) {
        FontRenderer.TextBounds bounds = font.getStringBounds(text);
        font.drawString(matrix, text, centerX - bounds.x() - bounds.width() / 2.0f, centerY - bounds.y() - bounds.height() / 2.0f - Render2DUtil.getTextYOffset(), color);
    }

    private static float clickBounce(long started, long now) {
        if (started == 0L) {
            return 1.0f;
        }
        float t = class_3532.method_15363((float)((float)(now - started) / 2.8E8f), (float)0.0f, (float)1.0f);
        return 1.0f - 0.4f * (float)Math.sin((double)t * Math.PI * 2.0) * (float)Math.exp(-t * 2.0f);
    }

    private void drawAnimatedStar(class_332 context, class_4587 matrix, Object key, float sx, float sy, boolean selected) {
        float color = this.starColors.getOrDefault(key, Float.valueOf(selected ? 1.0f : 0.0f)).floatValue();
        float t = class_3532.method_15363((float)((float)(System.nanoTime() - this.starClicks.getOrDefault(key, 0L)) / 2.8E8f), (float)0.0f, (float)1.0f);
        float colorT = class_3532.method_15363((float)(t * 280.0f / 160.0f), (float)0.0f, (float)1.0f);
        colorT = colorT * colorT * (3.0f - 2.0f * colorT);
        color = class_3532.method_16439((float)colorT, (float)this.starColorStarts.getOrDefault(key, Float.valueOf(color)).floatValue(), (float)(selected ? 1.0f : 0.0f));
        this.starColors.put(key, Float.valueOf(color));
        float scale = DropdownMenuComponent.clickBounce(this.starClicks.getOrDefault(key, 0L), System.nanoTime());
        context.method_51448().pushMatrix();
        context.method_51448().translate(sx + 5.5f, sy + 5.5f);
        context.method_51448().scale(scale, scale);
        context.method_51448().translate(-sx - 5.5f, -sy - 5.5f);
        this.drawStar(matrix, sx, sy, ColorUtil.overCol(-5394245, -14277, color));
        context.method_51448().popMatrix();
    }

    private void drawNavigationLabel(class_4587 matrix, Bounds row, String icon, String label, boolean selected) {
        float centerY = this.navigationPixel(row.y + row.height / 2.0f, false);
        float textOffset = Render2DUtil.getTextYOffset();
        FontRenderer iconFont = this.navigationIconFonts.computeIfAbsent(icon, value -> {
            FontRenderer base = Fonts.getSize(22, Fonts.Type.COMBINED_ICONS);
            FontRenderer.TextBounds bounds = base.getStringBounds((String)value);
            float extent = Math.max(bounds.width(), bounds.height());
            return Fonts.create(264.0f / Math.max(1.0f, extent), Fonts.Type.COMBINED_ICONS.getType());
        });
        FontRenderer.TextBounds iconBounds = iconFont.getStringBounds(icon);
        iconFont.drawString(matrix, icon, this.navigationPixel(row.x + 12.0f, true) - iconBounds.x() - iconBounds.width() / 2.0f, centerY - iconBounds.y() - iconBounds.height() / 2.0f - textOffset, selected ? this.accent() : -7301470);
        FontRenderer labelFont = MenuStyle.medium(10.0f);
        FontRenderer.TextBounds labelBounds = labelFont.getStringBounds(label);
        FontRenderer.TextBounds capBounds = labelFont.getStringBounds("H");
        labelFont.drawString(matrix, label, this.navigationPixel(row.x + 25.0f, true) - labelBounds.x(), this.navigationPixel(centerY - capBounds.height() / 2.0f, false) - capBounds.y() - textOffset, selected ? -986118 : -7301470);
    }

    private Bounds navigationBounds(float rowX, float rowY) {
        float left = this.navigationPixel(rowX, true);
        float top = this.navigationPixel(rowY, false);
        return new Bounds(left, top, this.navigationPixel(rowX + 115.0f, true) - left, this.navigationPixel(rowY + 24.0f, false) - top);
    }

    private float navigationPixel(float coordinate, boolean horizontal) {
        float translation;
        class_332 context = Render2DUtil.getActiveContext();
        if (context == null) {
            return coordinate;
        }
        Matrix3x2fStack transform = context.method_51448();
        if (Math.abs(transform.m01()) > 1.0E-4f || Math.abs(transform.m10()) > 1.0E-4f) {
            return coordinate;
        }
        float scale = horizontal ? transform.m00() : transform.m11();
        float f = translation = horizontal ? transform.m20() : transform.m21();
        if (Math.abs(scale) < 1.0E-4f) {
            return coordinate;
        }
        double pixels = class_310.method_1551().method_22683().method_4495();
        return (float)(((double)Math.round((double)(coordinate * scale + translation) * pixels) / pixels - (double)translation) / (double)scale);
    }

    private void drawNavigationHover(class_4587 matrix, String key, Bounds bounds, int mouseX, int mouseY) {
        float target = bounds.hover(mouseX, mouseY) ? 1.0f : 0.0f;
        float previous = this.navigationHover.getOrDefault(key, Float.valueOf(0.0f)).floatValue();
        float alpha = previous + (target - previous) * this.navigationHoverBlend;
        this.navigationHover.put(key, Float.valueOf(alpha));
        boolean selected = this.section.isEmpty() ? MenuScreen.INSTANCE.category.name().equals(key) : this.section.equals(key);
        float selection = this.navigationSelection.getOrDefault(key, Float.valueOf(0.0f)).floatValue();
        selection += ((selected ? 1.0f : 0.0f) - selection) * this.navigationHoverBlend;
        this.navigationSelection.put(key, Float.valueOf(selection));
        float strength = Math.max(alpha * 0.07f, selection * 0.13f);
        if (strength > 0.001f) {
            MenuStyle.fill(matrix, bounds.x, bounds.y, bounds.width, bounds.height, 5.0f, ColorUtil.multAlpha(-7301470, strength));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderProfile(class_332 context, class_4587 matrix, int mouseX, int mouseY) {
        MenuStyle.separator(matrix, this.x, this.y + this.height - 49.0f, 137.0f, false);
        this.profileButton = new Bounds(this.x + 10.0f, this.y + this.height - 40.0f, 119.0f, 33.0f);
        this.profileSettingsButton = new Bounds(this.x + 102.0f, this.y + this.height - 40.0f, 27.0f, 33.0f);
        MenuStyle.fill(matrix, this.profileButton.x, this.profileButton.y, this.profileButton.width, this.profileButton.height, 7.0f, MenuStyle.moduleBackground());
        this.drawProfileAvatar(context, this.x + 12.0f, this.y + this.height - 35.0f, 24.0f, 12.0f);
        MenuStyle.medium(9.0f).drawString(matrix, MenuStyle.fitMedium(UserProfile.getUsername(), 56.0f, 9.0f), this.x + 43.0f, this.y + this.height - 35.0f, -986118);
        MenuStyle.font(7.0f).drawString(matrix, MenuStyle.fit(UserProfile.getRole(), 56.0f, 7.0f), this.x + 43.0f, this.y + this.height - 22.0f, -7301470);
        float anim = this.updateProfileOpenAnimation();
        int arrowColor = ColorUtil.overCol(-7301470, this.accent(), anim);
        float cx = this.profileSettingsButton.x + this.profileSettingsButton.width / 2.0f;
        float cy = this.profileSettingsButton.y + this.profileSettingsButton.height / 2.0f;
        FontRenderer arrowFont = MenuStyle.font(14.0f);
        FontRenderer.TextBounds arrowBounds = arrowFont.getStringBounds("\u203a");
        context.method_51448().pushMatrix();
        try {
            context.method_51448().rotateAbout(anim * (float)Math.PI / 2.0f, cx, cy);
            arrowFont.drawString(matrix, "\u203a", cx - arrowBounds.x() - arrowBounds.width() / 2.0f, cy - arrowBounds.y() - arrowBounds.height() / 2.0f - Render2DUtil.getTextYOffset(), arrowColor);
        }
        finally {
            context.method_51448().popMatrix();
        }
    }

    public float updateProfileOpenAnimation() {
        long now = System.nanoTime();
        if (this.profileOpenAnimationStartNanos == 0L) {
            this.profileOpenAnimationStart = this.profileOpenAnimation = (this.profileOpenAnimationTarget = this.profileOpen ? 1.0f : 0.0f);
            this.profileOpenAnimationStartNanos = now;
            return this.profileOpenAnimation;
        }
        float distance = Math.abs(this.profileOpenAnimationTarget - this.profileOpenAnimationStart);
        long duration = Math.max(120000000L, (long)(2.0E8f * Math.max(0.35f, distance)));
        float progress = class_3532.method_15363((float)((float)(now - this.profileOpenAnimationStartNanos) / (float)duration), (float)0.0f, (float)1.0f);
        float inv = 1.0f - progress;
        float eased = 1.0f - inv * inv * inv;
        this.profileOpenAnimation = class_3532.method_16439((float)eased, (float)this.profileOpenAnimationStart, (float)this.profileOpenAnimationTarget);
        if (progress >= 1.0f) {
            this.profileOpenAnimation = this.profileOpenAnimationTarget;
        }
        return this.profileOpenAnimation;
    }

    private void drawProfileAvatar(class_332 context, float px, float py, float size, float round) {
        class_2960 texture = ProfileAvatar.getTexture();
        if (UserProfile.isDefaultAvatar()) {
            Render2DUtil.drawRoundedTexture(context, texture, px, py, size, round, 0.0f, 0.0f, 736, 736, 736, 1226, -1);
        } else {
            Render2DUtil.drawRoundedTexture(context, texture, px, py, size, round, -1);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean renderProfileSettingsOverlay(class_332 context, int mouseX, int mouseY) {
        float anim = this.updateProfileOpenAnimation();
        if (!this.profileOpen && anim <= 0.001f) {
            return false;
        }
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        float panelX = this.x + 143.0f + this.profileOffsetX;
        float panelHeight = Math.min(316.0f, this.height - 16.0f);
        float panelY = this.y + this.height - panelHeight - 8.0f + this.profileOffsetY;
        float pivotX = panelX;
        float pivotY = panelY + panelHeight;
        float scale = DropdownMenuComponent.boundScale(anim, 0.84f);
        float prevAlpha = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(prevAlpha * anim);
        context.method_51448().pushMatrix();
        context.method_51448().translate(pivotX, pivotY);
        context.method_51448().scale(scale, scale);
        context.method_51448().translate(-pivotX, -pivotY);
        matrix = MatrixStackCompat.from(context.method_51448());
        try {
            this.renderProfileSettingsPanel(context, matrix, mouseX, mouseY);
        }
        finally {
            context.method_51448().popMatrix();
            Render2DUtil.setRenderAlpha(prevAlpha);
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderProfileSettingsPanel(class_332 context, class_4587 matrix, int mouseX, int mouseY) {
        float panelX = this.x + 143.0f + this.profileOffsetX;
        float panelWidth = 114.0f;
        float panelHeight = Math.min(316.0f, this.height - 16.0f);
        float panelY = this.y + this.height - panelHeight - 8.0f + this.profileOffsetY;
        float contentX = panelX + 10.0f;
        float contentWidth = panelWidth - 20.0f;
        float centerX = panelX + panelWidth / 2.0f;
        this.profileSettingsPanel = new Bounds(panelX, panelY, panelWidth, panelHeight);
        this.profileSettingsContent = new Bounds(contentX, panelY + 80.0f, contentWidth, panelHeight - 90.0f);
        this.profileControlBounds.clear();
        this.profileChoices.clear();
        blur.setColorlessMode(true);
        blur.setShadowEnabled(false);
        blur.setStyledBlurRadius(90.0f);
        float backgroundAlpha = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(backgroundAlpha * MenuStyle.backgroundOpacity());
        try {
            blur.setLiveMode(false);
            blur.render(ShapeProperties.create(matrix, panelX, panelY, panelWidth, panelHeight).round(12.0f).color(MenuStyle.surface()).build());
        }
        finally {
            Render2DUtil.setRenderAlpha(backgroundAlpha);
            blur.resetStyledBlurRadius();
            blur.setColorlessMode(false);
        }
        MenuStyle.fill(matrix, panelX, panelY, panelWidth, panelHeight, 12.0f, ColorUtil.multAlpha(-1610414838, MenuStyle.backgroundOpacity()));
        MenuStyle.outline(matrix, panelX, panelY, panelWidth, panelHeight, 12.0f, MenuStyle.border());
        MenuStyle.fill(matrix, centerX - 15.0f, panelY + 10.0f, 30.0f, 30.0f, 10.0f, MenuStyle.card());
        this.drawProfileAvatar(context, centerX - 14.0f, panelY + 11.0f, 28.0f, 9.0f);
        MenuStyle.medium(9.5f).drawCenteredString(matrix, MenuStyle.fitMedium(UserProfile.getUsername(), contentWidth, 9.5f), centerX, panelY + 45.0f, -986118);
        MenuStyle.font(7.5f).drawCenteredString(matrix, MenuStyle.fit(UserProfile.getSubscriptionEndLabel(), contentWidth, 7.5f), centerX, panelY + 59.0f, -7301470);
        MenuStyle.separator(matrix, contentX, panelY + 72.0f, contentWidth, false);
        Hud hud = Hud.getInstance();
        List<Setting> settings = hud.getGuiSettings();
        this.profileContentHeight = 0.0f;
        for (Setting setting : settings) {
            this.profileContentHeight += this.profileSettingHeight(setting);
        }
        float maxScroll = Math.max(0.0f, this.profileContentHeight - this.profileSettingsContent.height);
        this.profileScrollTarget = class_3532.method_15363((float)this.profileScrollTarget, (float)(-maxScroll), (float)0.0f);
        long now = System.nanoTime();
        float elapsed = this.profileScrollTime == 0L ? 0.0f : Math.min(0.05f, (float)(now - this.profileScrollTime) / 1.0E9f);
        this.profileScrollTime = now;
        this.profileScroll = class_3532.method_16439((float)(1.0f - (float)Math.exp(-14.0f * elapsed)), (float)this.profileScroll, (float)this.profileScrollTarget);
        if (Math.abs(this.profileScrollTarget - this.profileScroll) < 0.05f) {
            this.profileScroll = this.profileScrollTarget;
        }
        this.profileScroll = class_3532.method_15363((float)this.profileScroll, (float)(-maxScroll), (float)0.0f);
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), this.profileSettingsContent.x, this.profileSettingsContent.y, this.profileSettingsContent.width, this.profileSettingsContent.height);
        try {
            float rowY = this.profileSettingsContent.y + this.profileScroll;
            for (Setting setting : settings) {
                float rowHeight = this.profileSettingHeight(setting);
                if (rowY + rowHeight > this.profileSettingsContent.y && rowY < this.profileSettingsContent.y + this.profileSettingsContent.height) {
                    this.drawProfileSetting(matrix, hud, setting, contentX, rowY, contentWidth, mouseX, mouseY);
                }
                rowY += rowHeight;
            }
        }
        finally {
            scissor.pop();
        }
        if (maxScroll > 0.0f) {
            float thumbHeight = Math.max(14.0f, this.profileSettingsContent.height * this.profileSettingsContent.height / this.profileContentHeight);
            float thumbY = this.profileSettingsContent.y - this.profileScroll / maxScroll * (this.profileSettingsContent.height - thumbHeight);
            MenuStyle.fill(matrix, panelX + panelWidth - 5.0f, thumbY, 2.0f, thumbHeight, 1.0f, ColorUtil.multAlpha(this.accent(), 0.7f));
        }
    }

    private float profileSettingHeight(Setting setting) {
        if (setting instanceof ValueSetting) {
            return 32.0f;
        }
        if (setting instanceof ColorSetting || setting instanceof BooleanSetting) {
            return 28.0f;
        }
        return 40.0f + (this.expandedProfileSettings.contains(setting) ? (float)this.profileOptions(setting).size() * 20.0f : 0.0f);
    }

    private List<String> profileOptions(Setting setting) {
        if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting multi = (MultiSelectSetting)setting;
            return multi.getList();
        }
        if (setting instanceof SelectSetting) {
            SelectSetting select = (SelectSetting)setting;
            ArrayList<String> options = new ArrayList<String>(select.getList());
            if (setting == Hud.getInstance().themeSetting && !options.contains("Custom")) {
                options.add("Custom");
            }
            return options;
        }
        return List.of();
    }

    private String profileSettingName(Setting setting) {
        return switch (setting.getName()) {
            case "GUI Scale" -> "\u041c\u0430\u0441\u0448\u0442\u0430\u0431 GUI";
            case "GUI Accent Color" -> "\u0410\u043a\u0446\u0435\u043d\u0442";
            case "GUI Background Color" -> "\u0424\u043e\u043d";
            case "HUD Blur Color" -> "\u0426\u0432\u0435\u0442 \u0431\u043b\u044e\u0440\u0430 HUD";
            case "3D Interface" -> "3D-\u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f";
            case "Language" -> "\u042f\u0437\u044b\u043a";
            case "Disable Blur" -> "\u0411\u0435\u0437 \u0431\u043b\u044e\u0440\u0430";
            case "Disable Blur In" -> "\u0423\u0431\u0440\u0430\u0442\u044c \u0431\u043b\u044e\u0440 \u0432";
            case "Disable Shadows" -> "\u0411\u0435\u0437 \u0442\u0435\u043d\u0435\u0439";
            case "Disable Shadows In" -> "\u0423\u0431\u0440\u0430\u0442\u044c \u0442\u0435\u043d\u0438 \u0432";
            case "Disable Outline" -> "\u0411\u0435\u0437 \u043e\u0431\u0432\u043e\u0434\u043a\u0438";
            case "Disable Outline In" -> "\u0423\u0431\u0440\u0430\u0442\u044c \u043e\u0431\u0432\u043e\u0434\u043a\u0443 \u0432";
            case "Theme" -> "\u0422\u0435\u043c\u0430";
            case "Elements" -> "\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u044b HUD";
            default -> setting.getName();
        };
    }

    private void drawProfileSetting(class_4587 matrix, Hud hud, Setting setting, float sx, float sy, float sw, int mouseX, int mouseY) {
        String label = this.profileSettingName(setting);
        if (setting instanceof ValueSetting) {
            ValueSetting value = (ValueSetting)setting;
            String amount = Math.round(value.getValue() * 100.0f) + "%";
            float amountWidth = MenuStyle.medium(7.5f).getStringWidth(amount);
            MenuStyle.font(7.5f).drawString(matrix, MenuStyle.fit(label, sw - amountWidth - 4.0f, 7.5f), sx, sy + 2.0f, -7301470);
            MenuStyle.medium(7.5f).drawString(matrix, amount, sx + sw - amountWidth, sy + 2.0f, -986118);
            Bounds track = new Bounds(sx, sy + 15.0f, sw, 10.0f);
            this.profileControlBounds.put(setting, track);
            float progress = class_3532.method_15363((float)((value.getValue() - value.getMin()) / Math.max(0.001f, value.getMax() - value.getMin())), (float)0.0f, (float)1.0f);
            progress = MenuControls.sliderProgress(this.sliderAnimation.getOrDefault(value, Float.valueOf(progress)).floatValue(), progress);
            this.sliderAnimation.put(value, Float.valueOf(progress));
            MenuControls.slider(matrix, sx, track.y + 5.0f, sw, progress);
        } else if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            Bounds swatch = new Bounds(sx + sw - 18.0f, sy + 3.0f, 18.0f, 18.0f);
            this.profileControlBounds.put(setting, swatch);
            MenuStyle.font(8.0f).drawString(matrix, MenuStyle.fit(label, sw - 27.0f, 8.0f), sx, sy + 7.0f, -986118);
            this.drawColorControl(matrix, swatch, color);
        } else if (setting instanceof BooleanSetting) {
            BooleanSetting bool = (BooleanSetting)setting;
            this.profileControlBounds.put(setting, new Bounds(sx, sy, sw, 24.0f));
            MenuStyle.font(8.0f).drawString(matrix, MenuStyle.fit(label, sw - 27.0f, 8.0f), sx, sy + 7.0f, -986118);
            float active = MenuStyle.toggleProgress(this.profileToggleAnimation, bool, bool.isValue());
            this.drawToggle(matrix, sx + sw - 23.0f, sy + 6.0f, bool.isValue(), active);
        } else {
            String object;
            MenuStyle.font(8.0f).drawString(matrix, MenuStyle.fit(label, sw, 8.0f), sx, sy + 2.0f, -986118);
            if (setting instanceof SelectSetting) {
                SelectSetting select = (SelectSetting)setting;
                object = DropdownMenuComponent.optionName(setting.getName(), select.getSelected());
            } else {
                object = ((MultiSelectSetting)setting).getSelected().size() + " выбрано";
            }
            String selected = object;
            Bounds header = new Bounds(sx, sy + 15.0f, sw, 19.0f);
            this.profileControlBounds.put(setting, header);
            MenuStyle.fill(matrix, header.x, header.y, sw, header.height, 4.0f, MenuStyle.cardHover());
            MenuStyle.font(8.0f).drawString(matrix, MenuStyle.fit(selected, sw - 20.0f, 8.0f), sx + 5.0f, header.y + 5.0f, -986118);
            boolean expanded = this.expandedProfileSettings.contains(setting);
            MenuStyle.font(10.0f).drawString(matrix, expanded ? "\u2039" : "\u203a", sx + sw - 11.0f, header.y + 3.0f, this.accent());
            if (expanded) {
                float optionY = sy + 36.0f;
                for (String option : this.profileOptions(setting)) {
                    boolean bl;
                    Bounds optionBounds = new Bounds(sx, optionY, sw, 18.0f);
                    this.profileChoices.add(new ProfileChoiceBounds(setting, option, optionBounds));
                    if (setting instanceof SelectSetting) {
                        SelectSetting select = (SelectSetting)setting;
                        bl = select.isSelected(option);
                    } else {
                        bl = ((MultiSelectSetting)setting).isSelected(option);
                    }
                    boolean active = bl;
                    int fill = active ? ColorUtil.multAlpha(this.accent(), 0.25f) : MenuStyle.card();
                    MenuStyle.fill(matrix, sx, optionY, sw, 18.0f, 4.0f, fill);
                    MenuStyle.font(8.0f).drawString(matrix, MenuStyle.fit(DropdownMenuComponent.optionName(setting.getName(), option), sw - 10.0f, 8.0f), sx + 5.0f, optionY + 4.0f, active ? -986118 : -7301470);
                    optionY += 20.0f;
                }
            }
        }
    }

    private void drawColorControl(class_4587 matrix, Bounds bounds, ColorSetting color) {
        float bounce = DropdownMenuComponent.clickBounce(this.profileColorClicks.getOrDefault(color, 0L), System.nanoTime());
        MenuControls.color(matrix, bounds.x, bounds.y, color.getColor(), bounce);
    }

    private void applyProfileValue(double mouseX) {
        Bounds bounds = this.profileControlBounds.get(this.draggingProfileValue);
        if (bounds != null) {
            this.applyValueDrag(this.draggingProfileValue, mouseX, bounds.x + 4.0f, bounds.width - 8.0f);
        }
    }

    private void clickProfileSetting(double mouseX, double mouseY, int button) {
        if (this.profileSettingsContent == null || !this.profileSettingsContent.hover(mouseX, mouseY)) {
            return;
        }
        if (button == 0) {
            for (ProfileChoiceBounds profileChoiceBounds : this.profileChoices) {
                if (!profileChoiceBounds.bounds.hover(mouseX, mouseY)) continue;
                Setting setting = profileChoiceBounds.setting;
                if (setting instanceof SelectSetting) {
                    SelectSetting select = (SelectSetting)setting;
                    select.setSelected(profileChoiceBounds.value);
                    this.expandedProfileSettings.remove(select);
                } else {
                    setting = profileChoiceBounds.setting;
                    if (setting instanceof MultiSelectSetting) {
                        MultiSelectSetting multi = (MultiSelectSetting)setting;
                        if (multi.isSelected(profileChoiceBounds.value)) {
                            multi.getSelected().remove(profileChoiceBounds.value);
                        } else {
                            multi.getSelected().add(profileChoiceBounds.value);
                        }
                    }
                }
                return;
            }
        }
        for (Map.Entry entry : this.profileControlBounds.entrySet()) {
            if (!((Bounds)entry.getValue()).hover(mouseX, mouseY)) continue;
            Setting setting = (Setting)entry.getKey();
            if (setting instanceof ValueSetting) {
                ValueSetting value = (ValueSetting)setting;
                if (button == 0) {
                    if (value == Hud.getInstance().guiScaleSetting) {
                        this.scaleBeforeDrag = value.getValue();
                    }
                    this.draggingProfileValue = value;
                    this.applyProfileValue(mouseX);
                } else if (button == 2) {
                    value.setValue(value.getDefaultValue());
                }
            } else if (button == 0) {
                if (setting instanceof BooleanSetting) {
                    BooleanSetting bool = (BooleanSetting)setting;
                    bool.setValue(!bool.isValue());
                } else if (setting instanceof ColorSetting) {
                    ColorSetting color = (ColorSetting)setting;
                    this.profileColorClicks.put(color, System.nanoTime());
                    this.openColorWindow(color, (Bounds)entry.getValue());
                } else if (!this.expandedProfileSettings.add(setting)) {
                    this.expandedProfileSettings.remove(setting);
                }
            }
            return;
        }
    }

    private String sectionIcon(String name) {
        return switch (name) {
            case "AutoBuy" -> "\ue058";
            case "Cosmetics" -> "\ue01b";
            case "Themes" -> "\ue005";
            case "Scripts" -> "\ue005";
            case "Favorites" -> "\ue0ab";
            case "Configs" -> "\ue037";
            default -> "";
        };
    }

    private String sectionName(String name) {
        if (name.equals("Configs") && MenuDescriptionLocalizer.isRussian()) {
            return "\u041a\u043e\u043d\u0444\u0438\u0433\u0438";
        }
        return name;
    }

    private float pageTransitionAlpha(String page) {
        long now = System.nanoTime();
        if (!page.equals(this.transitionPage)) {
            this.transitionPage = page;
            this.transitionStartNanos = now;
        }
        float progress = class_3532.method_15363((float)((float)(now - this.transitionStartNanos) / 2.6E8f), (float)0.0f, (float)1.0f);
        return this.smoothStep(0.0f, 1.0f, progress);
    }

    private void renderAutoBuyPage(class_332 context, int mouseX, int mouseY, float delta, int px, int py, int pw, int ph) {
        AutoBuy autoBuy = AutoBuy.getInstance();
        if (autoBuy == null) {
            return;
        }
        if (this.autoBuyPage == null) {
            this.autoBuyPage = new AutoBuyScreen(autoBuy);
        }
        this.embeddedAutoBuyBounds = new Bounds(px, py, pw, ph);
        this.autoBuyPage.renderEmbedded(context, mouseX, mouseY, delta, px, py, pw, ph);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderCosmeticsPage(class_332 context, class_4587 matrix, int mouseX, int mouseY, int px, int py, int pw, int ph) {
        Module module = this.modules.values().stream().flatMap(Collection::stream).filter(candidate -> candidate.getName().equalsIgnoreCase("Cosmetics")).findFirst().orElse(null);
        if (module == null) {
            return;
        }
        this.cosmeticsPageBounds = new Bounds(px, py, pw, ph);
        this.drawImmediateRect(matrix, px, py, pw, 24.0f, 8.0f, 0.0f, MenuStyle.card());
        this.drawImmediateOutline(matrix, px, py, pw, 24.0f, 8.0f, MenuStyle.border());
        context.method_51427(new class_1799((class_1935)class_1802.field_8833), px + 8, py + 4);
        MenuStyle.font(11.0f).drawString(matrix, MenuStyle.fit(module.getVisibleName(), (float)pw - 64.0f, 11.0f), (float)px + 29.0f, (float)py + 7.5f, -986118);
        this.drawToggle(matrix, (float)(px + pw) - 31.0f, (float)py + 6.0f, module.isState(), module.getAnimation().getOutput().floatValue());
        this.moduleBounds.put(module, new Bounds(px, py, pw, 24.0f));
        float settingsX = (float)px + 8.0f;
        float settingsY = (float)py + 32.0f + this.cosmeticsScroll;
        float settingsW = (float)pw - 16.0f;
        float contentHeight = this.cosmeticSettingListHeight(module.settings(), settingsW);
        float maxScroll = Math.max(0.0f, contentHeight - ((float)ph - 30.0f));
        this.cosmeticsScrollTarget = class_3532.method_15363((float)this.cosmeticsScrollTarget, (float)(-maxScroll), (float)0.0f);
        this.cosmeticsScroll = class_3532.method_15363((float)MathUtil.interpolateSmooth(1.7, this.cosmeticsScroll, this.cosmeticsScrollTarget), (float)(-maxScroll), (float)0.0f);
        settingsY = (float)py + 32.0f + this.cosmeticsScroll;
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), (float)px + 3.0f, (float)py + 30.0f, (float)pw - 6.0f, (float)ph - 33.0f);
        context.method_44379((int)((float)px + 3.0f), (int)((float)py + 30.0f), (int)((float)(px + pw) - 3.0f), (int)((float)(py + ph) - 3.0f));
        try {
            this.drawCosmeticSettingList(context, matrix, module.settings(), settingsX, settingsY, settingsW, mouseX, mouseY, (float)py + 30.0f, py + ph);
        }
        finally {
            context.method_44380();
            scissor.pop();
        }
        if ((float)mouseY >= (float)py + 30.0f && mouseY <= py + ph) {
            this.cosmeticItemBounds.stream().filter(item -> item.bounds.hover(mouseX, mouseY)).findFirst().ifPresent(item -> context.method_51438(class_310.method_1551().field_1772, (class_2561)class_2561.method_43470((String)item.value), mouseX, mouseY));
        }
        if (maxScroll > 0.0f) {
            float trackY = (float)py + 31.0f;
            float trackH = (float)ph - 37.0f;
            float thumbH = Math.max(20.0f, trackH * trackH / (trackH + maxScroll));
            float thumbY = trackY + -this.cosmeticsScroll / maxScroll * (trackH - thumbH);
            this.drawImmediateRect(matrix, (float)(px + pw) - 4.5f, trackY, 2.5f, trackH, 1.25f, 0.0f, 1612720695);
            this.drawImmediateRect(matrix, (float)(px + pw) - 4.5f, thumbY, 2.5f, thumbH, 1.25f, 0.0f, ColorUtil.multAlpha(this.accent(), 0.8f));
        }
    }

    private float drawCosmeticSettingList(class_332 context, class_4587 matrix, List<Setting> source, float sx, float sy, float sw, int mouseX, int mouseY, float visibleTop, float visibleBottom) {
        float cy = sy;
        for (Setting setting : source) {
            if (!setting.isVisible()) continue;
            float sh = this.cosmeticSettingHeight(setting, sw);
            if (cy + sh > visibleTop && cy < visibleBottom) {
                this.settingBounds.put(setting, new Bounds(sx, cy, sw, sh));
                if (setting instanceof GroupSetting) {
                    GroupSetting group = (GroupSetting)setting;
                    this.drawCosmeticGroup(context, matrix, group, sx, cy, sw, mouseX, mouseY, visibleTop, visibleBottom);
                } else if (setting instanceof SelectSetting) {
                    SelectSetting select = (SelectSetting)setting;
                    this.drawCosmeticSelect(context, matrix, select, sx, cy, sw, mouseX, mouseY, visibleTop, visibleBottom);
                } else {
                    this.drawSetting(context, matrix, setting, sx, cy, sw, sh, mouseX, mouseY, visibleTop, visibleBottom);
                }
            }
            cy += sh;
        }
        return cy;
    }

    private void drawCosmeticGroup(class_332 context, class_4587 matrix, GroupSetting group, float sx, float sy, float sw, int mouseX, int mouseY, float visibleTop, float visibleBottom) {
        class_2960 groupIcon = this.cosmeticGroupCustomIcon(group.getName());
        if (groupIcon != null) {
            if (sy + 18.0f >= visibleTop && sy <= visibleBottom) {
                ShapeShaderRenderer.renderRoundTexture(ShapeProperties.create(matrix, sx + 1.0f, sy + 3.0f, 15.0, 15.0).round(3.0f).quality(12.0f).build(), groupIcon, -1, 0.0f, 0.0f, 1.0f, 1.0f);
            }
        } else {
            class_1799 icon = this.cosmeticGroupIcon(group.getName());
            if (!icon.method_7960() && sy + 18.0f >= visibleTop && sy <= visibleBottom) {
                context.method_51427(icon, (int)sx, (int)sy + 2);
            }
        }
        MenuStyle.font(10.0f).drawString(matrix, MenuStyle.fit(DropdownMenuComponent.settingName(group.getName()), sw - 26.0f, 11.0f), sx + 20.0f, sy + 6.6f, -986118);
        if (group.isCheckbox()) {
            this.drawStateIcon(matrix, group, group.isValue(), sx + sw - 7.0f, sy + 8.5f);
        }
        if (group.isCheckbox() && !group.isValue()) {
            return;
        }
        this.drawCosmeticSettingList(context, matrix, group.getSubSettings(), sx + 20.0f, sy + 21.0f, sw - 20.0f, mouseX, mouseY, visibleTop, visibleBottom);
    }

    private void drawCosmeticSelect(class_332 context, class_4587 matrix, SelectSetting select, float sx, float sy, float sw, int mouseX, int mouseY, float visibleTop, float visibleBottom) {
        List<String> values2 = select.getList() == null ? List.of() : select.getList();
        int selectedIndex = values2.indexOf(select.getSelected());
        MenuStyle.font(9.0f).drawString(matrix, DropdownMenuComponent.settingName(select.getName()), sx, sy + 4.5f, -986118);
        this.drawCount(matrix, Math.max(0, selectedIndex + 1) + " \u0438\u0437 " + values2.size(), sx + sw, sy + 5.0f);
        int columns = this.cosmeticGridColumns(sw);
        float gridY = sy + 17.0f;
        for (int index = 0; index < values2.size(); ++index) {
            String value = (String)values2.get(index);
            int column = index % columns;
            int row = index / columns;
            float tileX = sx + (float)column * 32.0f;
            float tileY = gridY + (float)row * 32.0f;
            if (tileY + 28.0f < visibleTop || tileY > visibleBottom) continue;
            Bounds bounds = new Bounds(tileX, tileY, 28.0f, 28.0f);
            this.cosmeticItemBounds.add(new CosmeticItemBounds(select, value, bounds));
            boolean selected = Objects.equals(select.getSelected(), value);
            boolean hovered = bounds.hover(mouseX, mouseY);
            String animationKey = select.getName() + "\u0000" + value;
            float target = selected ? 1.0f : (hovered ? 0.42f : 0.0f);
            float highlight = MathUtil.interpolateSmooth(1.5, this.cosmeticItemHover.getOrDefault(animationKey, Float.valueOf(0.0f)).floatValue(), target);
            this.cosmeticItemHover.put(animationKey, Float.valueOf(highlight));
            int slotBg = selected ? ColorUtil.multAlpha(this.accent(), 0.25f) : (hovered ? 1612390958 : 940709408);
            int outline = ColorUtil.overCol(-14473423, this.accent(), highlight);
            this.drawImmediateRect(matrix, tileX, tileY, 28.0f, 28.0f, 6.0f, 0.2f, slotBg);
            if (highlight > 0.01f) {
                rectangle.render(ShapeProperties.create(matrix, tileX - 1.0f, tileY - 1.0f, 30.0, 30.0).round(7.0f).softness(3.5f).quality(20.0f).color(ColorUtil.multAlpha(this.accent(), 0.14f * highlight)).build());
            }
            this.drawImmediateOutline(matrix, tileX, tileY, 28.0f, 28.0f, 6.0f, 0.4f, outline);
            class_2960 customIcon = this.getCosmeticCustomIcon(select, value, index);
            if (customIcon != null) {
                ShapeProperties iconShape = ShapeProperties.create(matrix, tileX + 4.5f, tileY + 4.5f, 19.0, 19.0).round(4.0f).build();
                ShapeShaderRenderer.renderRoundTexture(iconShape, customIcon, -1, 0.0f, 0.0f, 1.0f, 1.0f);
                continue;
            }
            class_1799 preview = this.cosmeticPreviewStack(select, value, index);
            if (preview.method_7960()) continue;
            context.method_51427(preview, (int)tileX + 6, (int)tileY + 6);
        }
    }

    private float cosmeticSettingListHeight(List<Setting> source, float width) {
        float height = 0.0f;
        for (Setting setting : source) {
            if (!setting.isVisible()) continue;
            height += this.cosmeticSettingHeight(setting, width);
        }
        return height;
    }

    private float cosmeticSettingHeight(Setting setting, float width) {
        if (setting instanceof GroupSetting) {
            GroupSetting group = (GroupSetting)setting;
            float height = 21.0f;
            if (!group.isCheckbox() || group.isValue()) {
                height += this.cosmeticSettingListHeight(group.getSubSettings(), width - 20.0f);
            }
            return height;
        }
        if (setting instanceof SelectSetting) {
            SelectSetting select = (SelectSetting)setting;
            int count = select.getList() == null ? 0 : select.getList().size();
            int columns = this.cosmeticGridColumns(width);
            int rows = (count + columns - 1) / columns;
            return 17.0f + Math.max(0.0f, (float)rows * 32.0f - 4.0f);
        }
        return this.settingHeight(setting, width);
    }

    private int cosmeticGridColumns(float width) {
        return Math.max(1, (int)((width + 4.0f) / 32.0f));
    }

    private class_2960 cosmeticGroupCustomIcon(String group) {
        return switch (group) {
            case "\u041a\u0440\u044b\u043b\u044c\u044f" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/wing_angel.png");
            case "\u0420\u044e\u043a\u0437\u0430\u043a\u0438" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_frog.png");
            case "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u043c\u043e\u0434\u0435\u043b\u0438" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/model_beer.png");
            default -> null;
        };
    }

    private class_2960 getCosmeticCustomIcon(SelectSetting select, String value, int index) {
        String name = select.getName();
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u043a\u0440\u044b\u043b\u044c\u0435\u0432")) {
            return switch (value) {
                case "\u041a\u0440\u044b\u043b\u044c\u044f \u0430\u043d\u0433\u0435\u043b\u0430" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/wing_angel.png");
                case "\u041a\u0440\u044b\u043b\u044c\u044f \u0432\u0443\u043b\u043a\u0430\u043d\u0430" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/wing_vulcano.png");
                case "\u041a\u0440\u044b\u043b\u044c\u044f \u0434\u0440\u0430\u043a\u043e\u043d\u0447\u0438\u043a\u0430" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/wing_dragon.png");
                default -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/wing_angel.png");
            };
        }
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u0440\u044e\u043a\u0437\u0430\u043a\u0430")) {
            return switch (value) {
                case "Frog Backpack" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_frog.png");
                case "Backpack 1" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_1.png");
                case "Backpack 2" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_2.png");
                case "Backpack 3" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_3.png");
                case "Backpack 4" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_4.png");
                case "Backpack 5" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_5.png");
                case "Backpack 6" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_6.png");
                case "Backpack 7" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_7.png");
                case "Backpack 8" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_8.png");
                case "Backpack 9" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_9.png");
                default -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/backpack_1.png");
            };
        }
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u0438\u0433\u0440\u043e\u043a\u0430")) {
            return switch (value) {
                case "Sahur" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/model_sahur.png");
                case "Beer" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/model_beer.png");
                case "CrazyRabbit" -> class_2960.method_60655((String)"recoin", (String)"textures/cosmetics/model_crazyrabbit.png");
                default -> null;
            };
        }
        return null;
    }

    private class_1799 cosmeticGroupIcon(String group) {
        return switch (group) {
            case "\u041c\u0435\u0447\u0438" -> new class_1799((class_1935)class_1802.field_8802);
            case "\u0411\u0443\u043b\u0430\u0432\u044b" -> new class_1799((class_1935)class_1802.field_49814);
            case "\u0410\u0440\u0431\u0430\u043b\u0435\u0442" -> new class_1799((class_1935)class_1802.field_8399);
            case "\u041f\u0438\u0442\u043e\u043c\u0446\u044b" -> new class_1799((class_1935)class_1802.field_8777);
            case "\u041a\u0440\u044b\u043b\u044c\u044f" -> new class_1799((class_1935)class_1802.field_8833);
            case "\u0420\u044e\u043a\u0437\u0430\u043a\u0438" -> new class_1799((class_1935)class_1802.field_8106);
            case "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u043c\u043e\u0434\u0435\u043b\u0438" -> new class_1799((class_1935)class_1802.field_8575);
            default -> class_1799.field_8037;
        };
    }

    private class_1799 cosmeticPreviewStack(SelectSetting select, String value, int index) {
        String name = select.getName();
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u043c\u0435\u0447\u0430")) {
            return CosmeticWeaponModels.apply(new class_1799((class_1935)class_1802.field_22022), value, false);
        }
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u0431\u0443\u043b\u0430\u0432\u044b")) {
            return CosmeticWeaponModels.apply(new class_1799((class_1935)class_1802.field_49814), value, true);
        }
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u0430\u0440\u0431\u0430\u043b\u0435\u0442\u0430")) {
            return CosmeticWeaponModels.applyCrossbow(new class_1799((class_1935)class_1802.field_8399), value);
        }
        if (name.equals("\u041f\u0438\u0442\u043e\u043c\u0435\u0446")) {
            return switch (value) {
                case "\u0416\u0430\u0431\u0430" -> new class_1799((class_1935)class_1802.field_8777);
                case "\u0420\u043e\u0431\u043e\u0442" -> new class_1799((class_1935)class_1802.field_8620);
                case "\u0421\u043e\u0432\u0430" -> new class_1799((class_1935)class_1802.field_8153);
                case "\u0427\u0435\u043a\u0443\u0448\u043a\u0430" -> new class_1799((class_1935)class_1802.field_20417);
                case "\u041a\u043e\u0437\u043b\u0438\u043a" -> new class_1799((class_1935)class_1802.field_39057);
                default -> new class_1799((class_1935)class_1802.field_8618);
            };
        }
        if (name.equals("\u0412\u0438\u0434 \u0436\u0430\u0431\u044b")) {
            return new class_1799((class_1935)class_1802.field_37535);
        }
        if (name.equals("\u0422\u0438\u043f \u0440\u043e\u0431\u043e\u0442\u0430")) {
            return switch (index % 4) {
                case 1 -> new class_1799((class_1935)class_1802.field_27071);
                case 2 -> new class_1799((class_1935)class_1802.field_8494);
                case 3 -> new class_1799((class_1935)class_1802.field_8793);
                default -> new class_1799((class_1935)class_1802.field_8773);
            };
        }
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u043a\u0440\u044b\u043b\u044c\u0435\u0432")) {
            return new class_1799((class_1935)class_1802.field_8833);
        }
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u0440\u044e\u043a\u0437\u0430\u043a\u0430")) {
            return switch (index % 5) {
                case 1 -> new class_1799((class_1935)class_1802.field_8466);
                case 2 -> new class_1799((class_1935)class_1802.field_16307);
                case 3 -> new class_1799((class_1935)class_1802.field_8545);
                case 4 -> new class_1799((class_1935)class_1802.field_27023);
                default -> new class_1799((class_1935)class_1802.field_8106);
            };
        }
        if (name.equals("\u041c\u043e\u0434\u0435\u043b\u044c \u0438\u0433\u0440\u043e\u043a\u0430")) {
            return switch (value) {
                case "Beer" -> new class_1799((class_1935)class_1802.field_20417);
                case "CrazyRabbit" -> new class_1799((class_1935)class_1802.field_8073);
                default -> new class_1799((class_1935)class_1802.field_8575);
            };
        }
        return class_1799.field_8037;
    }

    private void renderThemeGallery(class_4587 matrix, float px, float py, float pw, float ph, int mouseX, int mouseY) {
        Hud hud = Hud.getInstance();
        List<String> themes = hud.themeSetting.getList();
        this.themePageBounds = new Bounds(px, py, pw, ph);
        int columns = 3;
        float gap = 8.0f;
        float cardWidth = (pw - gap * (float)(columns - 1) - 8.0f) / (float)columns;
        float cardHeight = 79.0f;
        float rowGap = 8.0f;
        int rows = (themes.size() + columns - 1) / columns;
        float maxScroll = Math.max(0.0f, (float)rows * (cardHeight + rowGap) - ph + rowGap);
        this.themePageScroll = class_3532.method_15363((float)this.themePageScroll, (float)(-maxScroll), (float)0.0f);
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), px, py, pw, ph);
        for (int i = 0; i < themes.size(); ++i) {
            String theme = themes.get(i);
            int column = i % columns;
            int row = i / columns;
            float cardX = px + (float)column * (cardWidth + gap);
            float cardY = py + 4.0f + (float)row * (cardHeight + rowGap) + this.themePageScroll;
            if (cardY + cardHeight < py || cardY > py + ph) continue;
            boolean selected = hud.themeSetting.isSelected(theme);
            boolean hovered = new Bounds(cardX, cardY, cardWidth, cardHeight).hover(mouseX, mouseY);
            float hover = MathUtil.interpolateSmooth(1.6, this.themeCardHover.getOrDefault(theme, Float.valueOf(0.0f)).floatValue(), hovered ? 1.0f : 0.0f);
            this.themeCardHover.put(theme, Float.valueOf(hover));
            int accent = hud.themeColorFor(theme);
            this.drawImmediateRect(matrix, cardX, cardY, cardWidth, cardHeight, 7.0f, 0.0f, ColorUtil.overCol(-720233944, ColorUtil.multAlpha(accent, 0.18f), hover));
            int startCol = hud.themeGradientStartFor(theme, System.currentTimeMillis());
            int endCol = hud.themeGradientEndFor(theme, System.currentTimeMillis());
            MenuStyle.gradient(matrix, cardX + 4.0f, cardY + 4.0f, cardWidth - 8.0f, 48.0f, 5.0f, startCol, endCol);
            int outline = selected ? ColorUtil.multAlpha(accent, 0.95f) : -1523893678;
            this.drawImmediateOutline(matrix, cardX, cardY, cardWidth, cardHeight, 7.0f, outline);
            MenuStyle.font(9.0f).drawString(matrix, MenuStyle.fit(theme, cardWidth - 36.0f, 9.0f), cardX + 7.0f, cardY + 61.0f, selected ? -986118 : -3946543);
            String number = Integer.toString(i + 1);
            MenuStyle.font(8.0f).drawString(matrix, number, cardX + cardWidth - MenuStyle.font(8.0f).getStringWidth(number) - 7.0f, cardY + 62.0f, selected ? accent : -7301470);
            this.themeBounds.add(new ThemeBounds(theme, new Bounds(cardX, cardY, cardWidth, cardHeight)));
        }
        scissor.pop();
        if (maxScroll > 0.0f) {
            float trackX = px + pw + 6.0f;
            float thumbH = Math.max(22.0f, ph * ph / (ph + maxScroll));
            float thumbY = py + -this.themePageScroll / maxScroll * (ph - thumbH);
            this.drawImmediateRect(matrix, trackX, py, 2.5f, ph, 1.25f, 0.0f, -14670285);
            this.drawImmediateRect(matrix, trackX, thumbY, 2.5f, thumbH, 1.25f, 0.0f, -10191194);
        }
    }

    public void renderThemeWave(class_332 context) {
    }

    private void renderThemeEditor(class_4587 matrix, float px, float py, float pw, float ph, int mouseX, int mouseY) {
        Hud hud = Hud.getInstance();
        this.themeEditorPanel = new Bounds(px, py, pw, ph);
        this.drawThemeEditorSurface(matrix, px, py, pw, ph);
        this.drawThemeEditorHeader(matrix, px, py, pw);
        float innerX = px + 10.0f;
        float innerW = pw - 20.0f;
        float rowY = py + 27.0f + 5.0f;
        rowY = this.drawEditorColor(matrix, hud.customThemeColor1, "\u041e\u0441\u043d\u043e\u0432\u043d\u043e\u0439", innerX, rowY, innerW);
        rowY = this.drawEditorColor(matrix, hud.customOutlineColor, "\u0410\u043a\u0442\u0438\u0432\u043d\u0430\u044f \u043e\u0431\u0432\u043e\u0434\u043a\u0430", innerX, rowY, innerW);
        rowY = this.drawEditorColor(matrix, hud.customInactiveOutlineColor, "\u041d\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u0430\u044f \u043e\u0431\u0432\u043e\u0434\u043a\u0430", innerX, rowY, innerW);
        rowY = this.drawEditorColor(matrix, hud.customActiveModuleColor, "\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u043c\u043e\u0434\u0443\u043b\u0438", innerX, rowY, innerW);
        rowY = this.drawEditorColor(matrix, hud.customInactiveModuleColor, "\u041d\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u043c\u043e\u0434\u0443\u043b\u0438", innerX, rowY, innerW);
        rowY = this.drawEditorColor(matrix, hud.customTextColor, "\u0422\u0435\u043a\u0441\u0442", innerX, rowY, innerW);
        rowY = this.drawEditorColor(matrix, hud.customInactiveTextColor, "\u041d\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0442\u0435\u043a\u0441\u0442", innerX, rowY, innerW);
        rowY = this.drawEditorColor(matrix, hud.customSliderColor, "\u0421\u043b\u0430\u0439\u0434\u0435\u0440", innerX, rowY, innerW);
        rowY = this.drawEditorColor(matrix, hud.customInactiveSliderColor, "\u041d\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0441\u043b\u0430\u0439\u0434\u0435\u0440", innerX, rowY, innerW);
        MenuStyle.font(7.0f).drawString(matrix, "\u0422\u0435\u043c\u044b \u00b7 famelov", innerX, rowY += 9.0f, -7301470);
        rowY += 9.0f;
        float dot = 10.0f;
        float dotGap = 5.0f;
        float dotX = innerX;
        String hoveredTheme = null;
        for (String theme : hud.themeSetting.getList()) {
            boolean selected = hud.themeSetting.isSelected(theme);
            rectangle.render(ShapeProperties.create(matrix, dotX, rowY, dot, dot).round(dot / 2.0f).thickness(selected ? 1.0f : 0.0f).outlineColor(selected ? -986118 : 0).color(hud.themeColorFor(theme)).build());
            Bounds dotBounds = new Bounds(dotX, rowY, dot, dot);
            if (dotBounds.hover(mouseX, mouseY)) {
                hoveredTheme = theme;
            }
            this.themeBounds.add(new ThemeBounds(theme, dotBounds));
            dotX += dot + dotGap;
        }
        rowY += 18.0f;
        if (hoveredTheme != null) {
            String displayName = "Custom".equals(hoveredTheme) ? Objects.requireNonNullElse(hud.customThemeName.getText(), "Custom") : hoveredTheme;
            MenuStyle.font(7.0f).drawString(matrix, displayName + " \u00b7 famelov", innerX, rowY - 6.0f, -7301470);
        }
        Object name = Objects.requireNonNullElse(hud.customThemeName.getText(), "");
        if (this.editingText == hud.customThemeName) {
            name = (String)name + "_";
        }
        this.drawImmediateRect(matrix, innerX, rowY, innerW, 13.0f, 3.0f, 0.3f, 857481776);
        MenuStyle.font(8.0f).drawString(matrix, (String)(((String)name).isBlank() ? "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0442\u0435\u043c\u044b" : name), innerX + 5.0f, rowY + 3.7f, ((String)name).isBlank() ? -7301470 : -986118);
        this.settingBounds.put(hud.customThemeName, new Bounds(innerX, rowY, innerW, 13.0f));
        this.themePlusBounds = new Bounds(innerX, rowY += 17.0f, innerW, 14.0f);
        this.drawImmediateRect(matrix, innerX, rowY, innerW, 14.0f, 3.0f, 0.3f, ColorUtil.multAlpha(this.accent(), 0.28f));
        MenuStyle.medium(8.0f).drawCenteredString(matrix, "\u0421\u043e\u0437\u0434\u0430\u0442\u044c", innerX + innerW / 2.0f, rowY + 4.0f, -986118);
        this.themeEditorList = null;
    }

    private Bounds drawThemeModeButton(class_4587 matrix, String label, float bx, float by, float bw, boolean selected) {
        rectangle.render(ShapeProperties.create(matrix, bx, by, bw, 13.0).round(4.0f).color(selected ? ColorUtil.multAlpha(this.accent(), 0.35f) : 1209210677).build());
        MenuStyle.font(8.0f).drawCenteredString(matrix, label, bx + bw / 2.0f, by + 4.1f, selected ? -1 : -7629912);
        return new Bounds(bx, by, bw, 13.0f);
    }

    private float drawEditorColor(class_4587 matrix, ColorSetting setting, String label, float bx, float by, float bw) {
        MenuStyle.font(8.0f).drawString(matrix, label, bx, by + 3.6f, -7366741);
        float swatchX = bx + bw - 25.0f;
        rectangle.render(ShapeProperties.create(matrix, swatchX, by + 1.0f, 25.0, 10.0).round(4.0f).thickness(1.0f).outlineColor(Hud.getInstance().themeOutlineColor()).color(setting.getColor()).build());
        this.settingBounds.put(setting, new Bounds(swatchX, by, 25.0f, 12.0f));
        return by + 13.0f;
    }

    private void drawThemeEditorHeader(class_4587 matrix, float px, float py, float pw) {
        rectangle.render(ShapeProperties.create(matrix, px + 1.0f, py + 1.0f, pw - 2.0f, 26.0).round(7.0f).color(-15658473).build());
        MenuStyle.separator(matrix, px + 9.0f, py + 27.0f - 1.0f, pw - 18.0f, false);
        MenuStyle.medium(11.0f).drawCenteredString(matrix, "Theme Editor", px + pw / 2.0f, py + 10.3f, -986118);
        this.themeCloseBounds = new Bounds(px + pw - 19.0f, py + 6.0f, 13.0f, 13.0f);
        Fonts.getMenuSize(13, Fonts.Type.BOLD).drawCenteredString(matrix, "x", px + pw - 12.5f, py + 10.2f, -7432534);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawThemeEditorSurface(class_4587 matrix, float x, float y, float w, float h) {
        boolean wasHud = Render2DUtil.isHudDecorationActive();
        boolean wasGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.render(ShapeProperties.create(matrix, x, y, w, h).round(8.0f).softness(5.0f).color(-184022771).build());
        }
        finally {
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(wasHud);
            Render2DUtil.setGuiDecorationActive(wasGui);
        }
        rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(8.0f).thickness(1.0f).outlineColor(-1523893678).color(-385349363).build());
    }

    public void toggleThemeEditor() {
        this.updateThemeEditorAnimation();
        this.themeEditorOpen = !this.themeEditorOpen;
        this.section = this.themeEditorOpen ? "Themes" : "";
        this.themeEditorAnimationStart = this.themeEditorAnimation;
        this.themeEditorAnimationTarget = this.themeEditorOpen ? 1.0f : 0.0f;
        this.themeEditorAnimationStartNanos = System.nanoTime();
        this.themeEditorScroll = 0.0f;
    }

    public boolean isThemeEditorOpen() {
        return this.themeEditorOpen;
    }

    public float updateThemeEditorAnimation() {
        long now = System.nanoTime();
        if (this.themeEditorAnimationStartNanos == 0L) {
            this.themeEditorAnimationStart = this.themeEditorAnimation = (this.themeEditorAnimationTarget = this.themeEditorOpen ? 1.0f : 0.0f);
            this.themeEditorAnimationStartNanos = now;
            return this.themeEditorAnimation;
        }
        float distance = Math.abs(this.themeEditorAnimationTarget - this.themeEditorAnimationStart);
        long duration = Math.max(120000000L, (long)(3.6E8f * distance));
        float progress = class_3532.method_15363((float)((float)(now - this.themeEditorAnimationStartNanos) / (float)duration), (float)0.0f, (float)1.0f);
        float eased = progress * progress * (3.0f - 2.0f * progress);
        this.themeEditorAnimation = class_3532.method_16439((float)eased, (float)this.themeEditorAnimationStart, (float)this.themeEditorAnimationTarget);
        if (progress >= 1.0f) {
            this.themeEditorAnimation = this.themeEditorAnimationTarget;
        }
        return this.themeEditorAnimation;
    }

    private void renderPanel(class_332 context, class_4587 matrix, ModuleCategory category, float px, float py, float pw, float ph, int mouseX, int mouseY, float delta, float entranceOffset) {
        ModuleMotion motion;
        this.panels.put(category, new Bounds(px, py, pw, ph));
        float viewY = py;
        float viewH = ph;
        float cardWidth = (pw - 8.0f) / 2.0f;
        List<Module> list = this.filtered(category);
        String view = category.name() + ":" + this.section + ":" + this.search.getText().trim().toLowerCase(Locale.ROOT);
        if (!this.moduleLayoutView.equals(view)) {
            this.moduleMotions.clear();
            this.moduleLayoutOrder = List.of();
            this.moduleLayoutView = view;
            this.raisedFavoriteModule = null;
        }
        boolean reorder = !this.moduleLayoutOrder.isEmpty() && !this.moduleLayoutOrder.equals(list);
        long now = System.nanoTime();
        float[] columns = new float[]{0.0f, 0.0f};
        for (int i = 0; i < list.size(); ++i) {
            Module module = list.get(i);
            int column = i % 2;
            float expand = this.updateExpand(module, this.expanded.contains(module) && this.hasSettings(module));
            float targetX = (float)column * (cardWidth + 8.0f);
            float targetY = columns[column];
            ModuleMotion motion2 = this.moduleMotions.get(module);
            if (motion2 == null) {
                motion2 = new ModuleMotion(targetX, targetY);
                this.moduleMotions.put(module, motion2);
            }
            motion2.moveTo(targetX, targetY, now, reorder);
            int n = column;
            columns[n] = columns[n] + (38.0f + (expand > 0.002f ? (this.settingsHeight(module, cardWidth) + 4.0f) * expand : 0.0f));
        }
        this.moduleLayoutOrder = list;
        this.moduleMotions.keySet().retainAll(list);
        float max = Math.max(0.0f, Math.max(columns[0], columns[1]) - 8.0f - viewH);
        String scrollKey = this.scrollKey(category);
        float current = class_3532.method_15363((float)this.scroll.getOrDefault(scrollKey, Float.valueOf(0.0f)).floatValue(), (float)(-max), (float)0.0f);
        this.scroll.put(scrollKey, Float.valueOf(current));
        float smooth = MathUtil.interpolateSmooth(2.0, this.smoothScroll.getOrDefault(scrollKey, Float.valueOf(current)).floatValue(), current);
        this.smoothScroll.put(scrollKey, Float.valueOf(smooth));
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), px - 3.0f, py, pw + 6.0f, ph);
        ModuleMotion raised = this.moduleMotions.get(this.raisedFavoriteModule);
        if (raised == null || !raised.isMoving()) {
            this.raisedFavoriteModule = null;
        }
        boolean layered = this.moduleMotions.values().stream().anyMatch(ModuleMotion::isMoving);
        List<Module> paintOrder = list;
        if (this.raisedFavoriteModule != null) {
            paintOrder = new ArrayList<Module>(list);
            paintOrder.remove(this.raisedFavoriteModule);
            paintOrder.add(this.raisedFavoriteModule);
        }
        ArrayList<FontRenderer.TextOcclusion> occlusions = new ArrayList<FontRenderer.TextOcclusion>();
        if (layered) {
            for (Module module : paintOrder) {
                motion = this.moduleMotions.get(module);
                float expand = this.expandAnimation.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
                float moduleHeight = 30.0f + (expand > 0.002f ? (this.settingsHeight(module, cardWidth) + 4.0f) * expand : 0.0f);
                occlusions.add(this.moduleTextOcclusion(context, px + motion.x, viewY + smooth + entranceOffset + motion.y, cardWidth, moduleHeight));
            }
        }
        for (int i = 0; i < paintOrder.size(); ++i) {
            Module module;
            module = paintOrder.get(i);
            motion = this.moduleMotions.get(module);
            this.drawModule(context, matrix, module, px + motion.x, viewY + smooth + entranceOffset + motion.y, cardWidth, mouseX, mouseY, delta, viewY, viewY + viewH, layered, layered ? occlusions.subList(i + 1, occlusions.size()) : List.of());
        }
        if (list.isEmpty()) {
            MenuStyle.medium(10.0f).drawCenteredString(matrix, MenuDescriptionLocalizer.isRussian() ? "\u041c\u043e\u0434\u0443\u043b\u0438 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b" : "No modules found", px + pw / 2.0f, py + ph / 2.0f - 5.0f, -7301470);
        }
        scissor.pop();
        if (max > 0.0f) {
            float thumb = Math.max(24.0f, viewH * viewH / (viewH + max));
            this.drawImmediateRect(matrix, px + pw + 5.0f, py, 1.0f, viewH, 0.5f, 0.0f, 1210657845);
            this.drawImmediateRect(matrix, px + pw + 5.0f, py + -smooth / max * (viewH - thumb), 1.0f, thumb, 0.5f, 0.0f, -12301993);
        }
    }

    private float drawModule(class_332 context, class_4587 matrix, Module module, float mx, float my, float mw, int mouseX, int mouseY, float delta, float visibleTop, float visibleBottom, boolean layered, List<FontRenderer.TextOcclusion> occlusions) {
        boolean hovered;
        boolean configurable = this.hasSettings(module);
        float expand = this.expandAnimation.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
        boolean windowHovered = windowManager.isHover(mouseX, mouseY);
        boolean bl = hovered = !windowHovered && MathUtil.isHovered(mouseX, mouseY, mx, my, mw, 30.0);
        if (hovered && my + 30.0f > visibleTop && my < visibleBottom) {
            this.hoveredModule = module;
            this.moduleHoverStart.putIfAbsent(module, System.currentTimeMillis());
        } else {
            this.moduleHoverStart.remove(module);
        }
        float active = module.getAnimation().getOutput().floatValue();
        float settingsVisibleH = expand > 0.002f ? this.settingsHeight(module, mw) * expand : 0.0f;
        float moduleH = 30.0f + (settingsVisibleH > 0.0f ? settingsVisibleH + 4.0f * expand : 0.0f);
        if (my + moduleH <= visibleTop || my >= visibleBottom) {
            this.updateBindChip(module, this.bindingModule == module);
            return my + moduleH + 8.0f;
        }
        if (layered) {
            FontRenderer.flushQueuedText(context);
            context.method_71048();
        }
        this.drawImmediateRect(matrix, mx, my, mw, moduleH, 10.0f, 0.0f, MenuStyle.moduleBackground());
        boolean binding = this.bindingModule == module;
        float bindAnimation = this.updateBindChip(module, binding);
        String bind = binding ? "..." : (module.getKey() == -1 ? "NONE" : StringUtil.getBindName(module.getKey()));
        bind = MenuStyle.fit(bind, 48.0f, 7.5f);
        float targetWidth = MenuControls.bindWidth(bind);
        float chipWidth = MathUtil.interpolateSmooth(1.0, this.moduleBindWidthAnimation.getOrDefault(module, Float.valueOf(targetWidth)).floatValue(), targetWidth);
        this.moduleBindWidthAnimation.put(module, Float.valueOf(chipWidth));
        float bindRight = mx + mw - (configurable ? 78.0f : 59.0f);
        float chipX = bindRight - chipWidth;
        float centerY = my + 15.0f;
        FontRenderer nameFont = MenuStyle.medium(11.0f);
        String name = MenuStyle.fitMedium(module.getVisibleName(), Math.max(0.0f, bindRight - Math.max(chipWidth, targetWidth) - mx - 18.0f), 11.0f);
        FontRenderer.TextBounds nameBounds = nameFont.getStringBounds(name);
        FontRenderer.TextBounds capBounds = nameFont.getStringBounds("H");
        MenuSearch.draw(matrix, nameFont, name, mx + 10.0f - nameBounds.x(), centerY - capBounds.y() - capBounds.height() / 2.0f - Render2DUtil.getTextYOffset(), -986118, this.search.getText());
        MenuControls.bind(matrix, bindRight, centerY, chipWidth, bind, bindAnimation);
        if (configurable) {
            this.drawCenteredGlyph(matrix, Fonts.getMenuSize(24, Fonts.Type.COMBINED_ICONS), "\ue0ad", mx + mw - 64.0f, centerY, this.expanded.contains(module) ? this.accent() : -7301470);
            float top = Math.max(my + 3.0f, visibleTop);
            float bottom = Math.min(my + 27.0f, visibleBottom);
            if (bottom > top) {
                this.settingsIconBounds.put(module, new Bounds(mx + mw - 73.0f, top, 18.0f, bottom - top));
            }
        }
        this.drawAnimatedStar(context, matrix, module, mx + mw - 50.0f, my + 9.5f, this.favorites.contains(module));
        this.favoriteBounds.put(module, new Bounds(mx + mw - 54.0f, Math.max(my + 3.0f, visibleTop), 19.0f, Math.max(0.0f, Math.min(my + 23.0f, visibleBottom) - Math.max(my + 3.0f, visibleTop))));
        this.drawToggle(matrix, mx + mw - 32.0f, my + 9.0f, module.isState(), active);
        Bounds bounds = new Bounds(mx, my, mw, 30.0f);
        this.moduleBounds.put(module, bounds);
        float clippedTop = Math.max(bounds.y, visibleTop);
        float clippedBottom = Math.min(bounds.y + bounds.height, visibleBottom);
        if (clippedBottom > clippedTop) {
            this.visibleModuleBounds.put(module, new Bounds(bounds.x, clippedTop, bounds.width, clippedBottom - clippedTop));
        }
        if (settingsVisibleH > 0.0f) {
            this.drawSettings(context, matrix, module, mx, my + 30.0f + 2.0f, mw, mouseX, mouseY, delta, expand, visibleTop, visibleBottom);
        }
        if (layered) {
            FontRenderer.flushQueuedText(context, occlusions);
            context.method_71048();
        }
        return my + moduleH + 8.0f;
    }

    private FontRenderer.TextOcclusion moduleTextOcclusion(class_332 context, float x, float y, float width, float height) {
        Matrix3x2fStack transform = context.method_51448();
        float left = x * transform.m00() + y * transform.m10() + transform.m20();
        float top = x * transform.m01() + y * transform.m11() + transform.m21();
        float dx = width * transform.m00();
        float dy = width * transform.m01();
        float ex = height * transform.m10();
        float ey = height * transform.m11();
        float minX = Math.min(Math.min(left, left + dx), Math.min(left + ex, left + dx + ex));
        float minY = Math.min(Math.min(top, top + dy), Math.min(top + ey, top + dy + ey));
        float maxX = Math.max(Math.max(left, left + dx), Math.max(left + ex, left + dx + ex));
        float maxY = Math.max(Math.max(top, top + dy), Math.max(top + ey, top + dy + ey));
        return new FontRenderer.TextOcclusion(minX, minY, maxX - minX, maxY - minY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderHoveredDescription(class_332 context) {
        Module eligible;
        boolean windowActive;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        boolean bl = windowActive = !windowManager.getWindows().isEmpty() && windowManager.isHover(this.lastMouseX, this.lastMouseY);
        if (windowActive) {
            this.hoveredModule = null;
        }
        long hoverStart = this.moduleHoverStart.getOrDefault(this.hoveredModule, System.currentTimeMillis());
        Module module = eligible = this.hoveredModule == null || windowActive || System.currentTimeMillis() - hoverStart < 1000L ? null : this.hoveredModule;
        if (this.descriptionAlpha < 0.01f) {
            this.describedModule = eligible;
        }
        float target = this.describedModule != null && this.describedModule == eligible ? 1.0f : 0.0f;
        this.descriptionAlpha = MathUtil.interpolateSmooth(2.8, this.descriptionAlpha, target);
        if (this.descriptionAlpha < 0.01f || this.describedModule == null) {
            return;
        }
        String description = ModuleDescriptionProvider.describe(this.describedModule);
        Bounds bounds = this.visibleModuleBounds.get(this.describedModule);
        if (bounds == null) {
            return;
        }
        FontRenderer font = MenuStyle.font(9.0f);
        float tooltipWidth = 137.33333f;
        ArrayList<String> lines = new ArrayList<String>();
        String line = "";
        for (String word : description.split("\\s+")) {
            String candidate;
            String string = candidate = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && font.getStringWidth(candidate) > tooltipWidth - 16.0f) {
                lines.add(line);
                line = word;
                continue;
            }
            line = candidate;
        }
        if (!line.isEmpty()) {
            lines.add(line);
        }
        float tx = bounds.x + bounds.width + 6.0f;
        MenuScreen screen = MenuScreen.INSTANCE;
        float scale = Math.max(0.001f, screen.getSmoothConfiguredScale() * screen.getViewportScale());
        float center = this.x + this.width / 2.0f;
        float rightEdge = center + ((float)window.getScaledWidth() - center) / scale;
        tx = Math.min(tx, rightEdge - tooltipWidth - 8.0f);
        float ty = Math.min(bounds.y, this.y + this.height - ((float)lines.size() * 13.0f + 36.0f));
        float previous = Render2DUtil.getRenderAlpha();
        Render2DUtil.setRenderAlpha(previous * this.descriptionAlpha);
        context.method_51448().pushMatrix();
        float tooltipScale = DropdownMenuComponent.boundScale(this.descriptionAlpha, 0.88f);
        context.method_51448().translate(tx, ty);
        context.method_51448().scale(tooltipScale, tooltipScale);
        context.method_51448().translate(-tx, -ty);
        matrix = MatrixStackCompat.from(context.method_51448());
        try {
            float tooltipHeight = (float)lines.size() * 13.0f + 36.0f;
            blur.setLiveMode(false);
            blur.setColorlessMode(true);
            blur.setStyledBlurRadius(90.0f);
            try {
                blur.render(ShapeProperties.create(matrix, tx, ty, tooltipWidth, tooltipHeight).round(6.0f).color(MenuStyle.surface()).build());
            }
            finally {
                blur.setLiveMode(false);
                blur.setColorlessMode(false);
                blur.resetStyledBlurRadius();
            }
            MenuStyle.fill(matrix, tx, ty, tooltipWidth, tooltipHeight, 6.0f, ColorUtil.multAlpha(-1476262904, MenuStyle.backgroundOpacity()));
            Fonts.getMenuSize(18, Fonts.Type.COMBINED_ICONS).drawString(matrix, "\ue037", tx + 8.0f, ty + 8.0f, -7301470);
            MenuStyle.medium(9.0f).drawString(matrix, MenuDescriptionLocalizer.isRussian() ? "\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435" : "Description", tx + 23.0f, ty + 8.0f, -986118);
            for (int i = 0; i < lines.size(); ++i) {
                font.drawString(matrix, (String)lines.get(i), tx + 8.0f, ty + 28.0f + (float)(i * 13), -986118);
            }
        }
        finally {
            context.method_51448().popMatrix();
            Render2DUtil.setRenderAlpha(previous);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private float drawSettings(class_332 context, class_4587 matrix, Module module, float sx, float sy, float sw, int mouseX, int mouseY, float delta, float expand, float visibleTop, float visibleBottom) {
        float h = this.settingsHeight(module, sw);
        sx += 11.0f;
        sw -= 22.0f;
        float visibleH = h * expand;
        float settingsTop = Math.max(sy - 1.0f, visibleTop);
        float settingsBottom = Math.min(sy + visibleH + 1.0f, visibleBottom);
        if (settingsBottom <= settingsTop) {
            return sy + visibleH + 6.0f * expand;
        }
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), sx - 4.0f, settingsTop, sw + 8.0f, settingsBottom - settingsTop);
        try {
            this.drawSettingList(context, matrix, module.settings(), sx, sy, sw, mouseX, mouseY, settingsTop, settingsBottom);
        }
        finally {
            scissor.pop();
        }
        return sy + visibleH + 6.0f * expand;
    }

    private float drawSettingList(class_332 context, class_4587 matrix, List<Setting> source, float sx, float sy, float sw, int mouseX, int mouseY, float visibleTop, float visibleBottom) {
        float cy = sy;
        for (Setting setting : source) {
            if (!this.settingVisible(setting)) continue;
            float sh = this.settingHeight(setting, sw);
            if (cy + sh > visibleTop && cy < visibleBottom) {
                this.settingBounds.put(setting, new Bounds(sx, cy, sw, sh));
                this.drawSetting(context, matrix, setting, sx, cy, sw, sh, mouseX, mouseY, visibleTop, visibleBottom);
            }
            cy += sh;
        }
        return cy;
    }

    private void drawSetting(class_332 context, class_4587 matrix, Setting setting, float sx, float sy, float sw, float sh, int mouseX, int mouseY, float visibleTop, float visibleBottom) {
        if (setting instanceof BoosterSectionHeaderSetting) {
            this.drawSimpleRow(matrix, DropdownMenuComponent.settingName(setting.getName()), "", sx, sy, sw, false);
        } else if (setting instanceof ValueSetting) {
            ValueSetting value = (ValueSetting)setting;
            this.drawValue(matrix, value, sx, sy, sw, mouseX);
        } else if (setting instanceof BooleanSetting) {
            BooleanSetting bool = (BooleanSetting)setting;
            this.drawBoolean(matrix, bool, sx, sy, sw);
        } else if (setting instanceof SelectSetting) {
            SelectSetting select = (SelectSetting)setting;
            this.drawSelect(matrix, select, sx, sy, sw);
        } else if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting multi = (MultiSelectSetting)setting;
            this.drawMulti(matrix, multi, sx, sy, sw);
        } else if (setting instanceof BindSetting) {
            BindSetting bind = (BindSetting)setting;
            this.drawBind(matrix, bind, sx, sy, sw, mouseX, mouseY);
        } else if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            this.drawColor(matrix, color, sx, sy, sw);
        } else if (setting instanceof MultiColorSetting) {
            MultiColorSetting multiColor = (MultiColorSetting)setting;
            this.drawMultiColor(context, matrix, multiColor, sx, sy, sw, mouseX);
        } else if (setting instanceof GroupSetting) {
            GroupSetting group = (GroupSetting)setting;
            this.drawGroup(context, matrix, group, sx, sy, sw, mouseX, mouseY, visibleTop, visibleBottom);
        } else if (setting instanceof ButtonSetting) {
            ButtonSetting button = (ButtonSetting)setting;
            this.drawActionRow(matrix, setting, DropdownMenuComponent.settingName(setting.getName()), this.buttonName(button), sx, sy, sw, mouseX, mouseY);
        } else if (setting instanceof TextSetting) {
            TextSetting text = (TextSetting)setting;
            Object value = Objects.requireNonNullElse(text.getText(), "");
            if (this.editingText == text) {
                value = (String)value + "_";
            }
            this.drawSimpleRow(matrix, DropdownMenuComponent.settingName(setting.getName()), (String)value, sx, sy, sw, false);
        } else {
            this.drawSimpleRow(matrix, DropdownMenuComponent.settingName(setting.getName()), "", sx, sy, sw, false);
        }
    }

    private void drawValue(class_4587 matrix, ValueSetting value, float sx, float sy, float sw, int mouseX) {
        if (this.draggingValue == value) {
            this.applyValueDrag(value, mouseX, sx + 4.0f, sw - 8.0f);
        }
        String text = value.isInteger() ? String.valueOf((int)value.getValue()) : BigDecimal.valueOf(value.getValue()).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        float valueW = MenuStyle.font(9.0f).getStringWidth(text) + 7.0f;
        this.drawSearchLabel(matrix, DropdownMenuComponent.settingName(value.getName()), sw - valueW - 4.0f, 9.0f, sx, sy + 4.7f, -986118);
        this.drawImmediateRect(matrix, sx + sw - valueW, sy + 2.2f, valueW, 11.0f, 3.0f, 0.35f, MenuStyle.cardHover());
        this.drawImmediateOutline(matrix, sx + sw - valueW, sy + 2.2f, valueW, 11.0f, 3.0f, ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.52f));
        MenuStyle.font(9.0f).drawCenteredString(matrix, text, sx + sw - valueW / 2.0f, sy + 2.5f, -986118);
        float trackY = sy + 19.5f;
        float targetPercent = value.getMax() <= value.getMin() ? 0.0f : class_3532.method_15363((float)((value.getValue() - value.getMin()) / (value.getMax() - value.getMin())), (float)0.0f, (float)1.0f);
        float percent = this.sliderAnimation.getOrDefault(value, Float.valueOf(targetPercent)).floatValue();
        percent = MenuControls.sliderProgress(percent, targetPercent);
        this.sliderAnimation.put(value, Float.valueOf(percent));
        MenuControls.slider(matrix, sx, trackY + 1.0f, sw, percent);
    }

    private void drawBoolean(class_4587 matrix, BooleanSetting bool, float sx, float sy, float sw) {
        this.drawSearchLabel(matrix, DropdownMenuComponent.settingName(bool.getName()), sw - 20.0f, 9.0f, sx, sy + 4.5f, -986118);
        this.drawStateIcon(matrix, bool, bool.isValue(), sx + sw - 7.0f, sy + 8.5f);
    }

    private void drawSelect(class_4587 matrix, SelectSetting select, float sx, float sy, float sw) {
        this.drawSearchLabel(matrix, DropdownMenuComponent.settingName(select.getName()), sw - 42.0f, 9.0f, sx, sy + 2.0f, -986118);
        this.drawCount(matrix, "1 \u0438\u0437 " + select.getList().size(), sx + sw, sy + 5.0f);
        this.drawChips(matrix, select, select.getList(), List.of(select.getSelected()), sx, sy + 15.0f, sw, false);
    }

    private void drawMulti(class_4587 matrix, MultiSelectSetting multi, float sx, float sy, float sw) {
        this.drawSearchLabel(matrix, DropdownMenuComponent.settingName(multi.getName()), sw - 42.0f, 9.0f, sx, sy + 2.0f, -986118);
        this.drawCount(matrix, multi.getSelected().size() + " \u0438\u0437 " + multi.getList().size(), sx + sw, sy + 5.0f);
        this.drawChips(matrix, multi, multi.getList(), multi.getSelected(), sx, sy + 15.0f, sw, true);
    }

    private void drawBind(class_4587 matrix, BindSetting bind, float sx, float sy, float sw, int mouseX, int mouseY) {
        String key = this.bindingSetting == bind ? "..." : (bind.getKey() == -1 ? "NONE" : StringUtil.getBindName(bind.getKey()));
        key = MenuStyle.fit(key, 48.0f, 7.5f);
        float targetWidth = MenuControls.bindWidth(key);
        float valueWidth = MathUtil.interpolateSmooth(1.0, this.settingBindWidthAnimation.getOrDefault(bind, Float.valueOf(targetWidth)).floatValue(), targetWidth);
        this.settingBindWidthAnimation.put(bind, Float.valueOf(valueWidth));
        float chipX = sx + sw - valueWidth;
        float target = this.bindingSetting == bind ? 1.0f : 0.0f;
        float progress = MathUtil.interpolateSmooth(1.5, this.settingBindAnimation.getOrDefault(bind, Float.valueOf(0.0f)).floatValue(), target);
        this.settingBindAnimation.put(bind, Float.valueOf(progress));
        this.drawSearchLabel(matrix, DropdownMenuComponent.settingName(bind.getName()), Math.max(0.0f, sw - valueWidth - 4.0f), 9.0f, sx, sy + 4.5f, -986118);
        MenuControls.bind(matrix, sx + sw, sy + 10.5f, valueWidth, key, progress);
    }

    private void drawColor(class_4587 matrix, ColorSetting color, float sx, float sy, float sw) {
        this.drawSearchLabel(matrix, DropdownMenuComponent.settingName(color.getName()), sw - 27.0f, 9.0f, sx, sy + 4.5f, -986118);
        MenuControls.color(matrix, sx + sw - 18.0f, sy + 1.5f, color.getColor(), DropdownMenuComponent.clickBounce(this.profileColorClicks.getOrDefault(color, 0L), System.nanoTime()));
    }

    private void drawMultiColor(class_332 context, class_4587 matrix, MultiColorSetting multiColor, float sx, float sy, float sw, int mouseX) {
        float cy = sy;
        for (ColorSetting color : multiColor.getAllColors()) {
            if (!this.settingVisible(color)) continue;
            this.settingBounds.put(color, new Bounds(sx, cy, sw, 21.0f));
            this.drawColor(matrix, color, sx, cy, sw);
            cy += 21.0f;
        }
    }

    private void drawGroup(class_332 context, class_4587 matrix, GroupSetting group, float sx, float sy, float sw, int mouseX, int mouseY, float visibleTop, float visibleBottom) {
        boolean cosmeticsGroup = "Cosmetics".equals(this.section);
        int labelX = (int)sx;
        float labelWidth = sw - 22.0f;
        if (cosmeticsGroup) {
            class_2960 customGroupIcon = this.cosmeticGroupCustomIcon(group.getName());
            if (customGroupIcon != null) {
                if (sy + 18.0f >= visibleTop && sy <= visibleBottom) {
                    ShapeShaderRenderer.renderRoundTexture(ShapeProperties.create(matrix, sx + 1.0f, sy + 3.0f, 15.0, 15.0).round(3.0f).quality(12.0f).build(), customGroupIcon, -1, 0.0f, 0.0f, 1.0f, 1.0f);
                }
                labelX += 20;
                labelWidth -= 20.0f;
            } else {
                class_1799 icon;
                switch (group.getName()) {
                    case "\u041c\u0435\u0447\u0438": {
                        icon = new class_1799((class_1935)class_1802.field_22022);
                        break;
                    }
                    case "\u0411\u0443\u043b\u0430\u0432\u044b": {
                        icon = new class_1799((class_1935)class_1802.field_49814);
                        break;
                    }
                    case "\u041f\u0438\u0442\u043e\u043c\u0446\u044b": {
                        icon = new class_1799((class_1935)class_1802.field_8618);
                        break;
                    }
                    default: {
                        icon = class_1799.field_8037;
                    }
                }
                if (!icon.method_7960()) {
                    if (sy + 18.0f >= visibleTop && sy <= visibleBottom) {
                        context.method_51427(icon, (int)sx, (int)sy + 2);
                    }
                    labelX += 20;
                    labelWidth -= 20.0f;
                }
            }
        }
        this.drawSearchLabel(matrix, DropdownMenuComponent.settingName(group.getName()), labelWidth, 9.0f, labelX, sy + 4.5f, -986118);
        if (group.isCheckbox()) {
            this.drawStateIcon(matrix, group, group.isValue(), sx + sw - 7.0f, sy + 8.5f);
        }
        if (group.isCheckbox() && !group.isValue() && !this.settingMatchesSearch(group)) {
            return;
        }
        float childX = sx + 6.0f;
        float childW = sw - 6.0f;
        this.drawSettingList(context, matrix, group.getSubSettings(), childX, sy + 21.0f, childW, mouseX, mouseY, visibleTop, visibleBottom);
    }

    private void drawSimpleRow(class_4587 matrix, String name, String value, float sx, float sy, float sw, boolean active) {
        float valueWidth = (value = MenuStyle.fit(value, sw * 0.52f, 9.0f)).isEmpty() ? 0.0f : MenuStyle.font(9.0f).getStringWidth(value) + 12.0f;
        float labelWidth = value.isEmpty() ? sw : Math.max(0.0f, sw - valueWidth - 4.0f);
        this.drawSearchLabel(matrix, name, labelWidth, 9.0f, sx, sy + 4.5f, -986118);
        if (!value.isEmpty()) {
            float w = valueWidth;
            rectangle.render(ShapeProperties.create(matrix, sx + sw - w, sy + 3.0f, w, 15.0).round(5.0f).quality(16.0f).color(active ? ColorUtil.multAlpha(this.accent(), 0.35f) : MenuStyle.cardHover()).build());
            this.drawImmediateOutline(matrix, sx + sw - w, sy + 3.0f, w, 15.0f, 5.0f, active ? ColorUtil.multAlpha(this.accent(), 0.7f) : ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.55f));
            MenuStyle.font(9.0f).drawCenteredString(matrix, value, sx + sw - w / 2.0f, sy + 4.5f, -986118);
        }
    }

    private void drawActionRow(class_4587 matrix, Setting setting, String name, String value, float sx, float sy, float sw, int mouseX, int mouseY) {
        float valueWidth = MenuStyle.font(9.0f).getStringWidth(value) + 12.0f;
        float x = sx + sw - valueWidth;
        float y = sy + 3.0f;
        boolean hovered = new Bounds(x, y, valueWidth, 15.0f).hover(mouseX, mouseY);
        float state = MathUtil.interpolateSmooth(2.2, this.actionHover.getOrDefault(setting, Float.valueOf(0.0f)).floatValue(), hovered ? 1.0f : 0.0f);
        this.actionHover.put(setting, Float.valueOf(state));
        this.drawSearchLabel(matrix, name, Math.max(0.0f, sw - valueWidth - 4.0f), 9.0f, sx, sy + 4.5f, -986118);
        int fill = ColorUtil.overCol(MenuStyle.cardHover(), ColorUtil.multAlpha(this.accent(), 0.32f), state);
        int outline = ColorUtil.overCol(ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.55f), ColorUtil.multAlpha(this.accent(), 0.7f), state);
        this.drawImmediateRect(matrix, x, y, valueWidth, 15.0f, 5.0f, 0.2f, fill);
        this.drawImmediateOutline(matrix, x, y, valueWidth, 15.0f, 5.0f, outline);
        MenuStyle.font(9.0f).drawCenteredString(matrix, value, x + valueWidth / 2.0f, sy + 4.5f, -986118);
    }

    private String buttonName(ButtonSetting setting) {
        String name = setting.getButtonName();
        if (name == null || name.isBlank()) {
            return MenuDescriptionLocalizer.isRussian() ? "\u0417\u0430\u043f\u0443\u0441\u043a" : "Run";
        }
        return DropdownMenuComponent.optionName(name);
    }

    private void drawChips(class_4587 matrix, Setting owner, List<String> values2, List<String> selected, float sx, float sy, float sw, boolean allowMany) {
        ArrayList<ChipBounds> chips = new ArrayList<ChipBounds>();
        Map<String, Float> animations = this.chipSelectionAnimation.computeIfAbsent(owner, ignored -> new HashMap());
        float cx = sx;
        float cy = sy;
        for (String value : values2) {
            boolean active;
            float target;
            float selection;
            String shown = MenuStyle.fit(DropdownMenuComponent.optionName(owner != null ? owner.getName() : null, value), 63.0f, 8.0f);
            float cw = Math.min(72.0f, MenuStyle.font(8.0f).getStringWidth(shown) + 9.0f);
            if (cx + cw > sx + sw) {
                cx = sx;
                cy += 15.0f;
            }
            if (Math.abs((selection = MathUtil.interpolateSmooth(1.75, animations.getOrDefault(value, Float.valueOf(target = (active = selected.stream().anyMatch(value::equalsIgnoreCase)) ? 1.0f : 0.0f)).floatValue(), target)) - target) < 0.001f) {
                selection = target;
            }
            animations.put(value, Float.valueOf(selection));
            int inactiveColor = 1614297920;
            int activeColor = ColorUtil.multAlpha(this.accent(), 0.32f);
            int chipColor = ColorUtil.overCol(inactiveColor, activeColor, selection);
            this.drawImmediateRect(matrix, cx, cy + 0.5f, cw, 12.0f, 4.0f, 0.2f, chipColor);
            int inactiveOutline = 1969646711;
            int activeOutline = ColorUtil.multAlpha(this.accent(), 0.42f);
            int chipOutline = active ? activeOutline : inactiveOutline;
            this.drawImmediateOutline(matrix, cx, cy + 0.5f, cw, 12.0f, 4.0f, 0.5f, chipOutline);
            int chipText = ColorUtil.overCol(-7170142, -920577, selection);
            MenuStyle.font(8.0f).drawCenteredString(matrix, shown, cx + cw / 2.0f, cy + 1.5f, chipText);
            chips.add(new ChipBounds(value, new Bounds(cx, cy + 0.5f, cw, 12.0f), allowMany));
            cx += cw + 3.0f;
        }
        this.chipBounds.put(owner, chips);
    }

    private void drawCount(class_4587 matrix, String value, float right, float y) {
        MenuStyle.font(8.0f).drawString(matrix, value, right - MenuStyle.font(8.0f).getStringWidth(value), y - 0.2f, -7301470);
    }

    private void drawModuleOutline(class_4587 matrix, float x, float y, float w, float h, int color) {
        this.drawImmediateOutline(matrix, x, y, w, h, 6.0f, color);
    }

    private void drawImmediateRect(class_4587 matrix, float x, float y, float w, float h, float round, float softness, int color) {
        MenuStyle.fill(matrix, x, y, w, h, round, color);
    }

    private void drawImmediateOutline(class_4587 matrix, float x, float y, float w, float h, float round, int color) {
        MenuStyle.outline(matrix, x, y, w, h, round, 0.65f, color);
    }

    private void drawImmediateOutline(class_4587 matrix, float x, float y, float w, float h, float round, float thickness, int color) {
        MenuStyle.outline(matrix, x, y, w, h, round, thickness, color);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawPanel(class_4587 matrix, float x, float y, float w, float h) {
        boolean wasHud = Render2DUtil.isHudDecorationActive();
        boolean wasGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.render(ShapeProperties.create(matrix, x, y, w, h).round(8.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(-268435456).build());
        }
        finally {
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(wasHud);
            Render2DUtil.setGuiDecorationActive(wasGui);
        }
        rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(8.0f).quality(16.0f).color(-2046754296).build());
    }

    private void drawHeader(class_4587 matrix, ModuleCategory category, float px, float py, float pw) {
        int color = -4735528;
        MenuStyle.medium(12.0f).drawString(matrix, category.getReadableName(), px + 13.0f, py + 9.5f, color);
        Fonts.getMenuSize(25, Fonts.Type.COMBINED_ICONS).drawString(matrix, this.categoryIcon(category), px + pw - 27.0f, py + 6.9f, this.accent());
    }

    private void drawStateIcon(class_4587 matrix, Setting setting, boolean enabled, float centerX, float y) {
        Boolean cached = this.stateIconCache.put(setting, enabled);
        if (cached != null && cached != enabled) {
            this.stateIconAnimation.put(setting, Float.valueOf(0.0f));
        }
        float progress = this.stateIconAnimation.getOrDefault(setting, Float.valueOf(1.0f)).floatValue();
        if ((progress = MathUtil.interpolateSmooth(1.75, progress, 1.0f)) > 0.985f) {
            progress = 1.0f;
        }
        this.stateIconAnimation.put(setting, Float.valueOf(progress));
        float ease = 1.0f - (float)Math.pow(1.0f - progress, 3.0);
        float bounce = (float)Math.sin((double)ease * Math.PI) * 0.16f;
        int size = Math.round(10.0f + 3.0f * ease + 2.0f * bounce);
        int color = ColorUtil.multAlpha(enabled ? -12197777 : -41370, 0.32f + 0.68f * ease);
        String marker = enabled ? ICON_CHECK : ICON_CROSS;
        float box = 10.5f + bounce;
        float boxX = centerX - box / 2.0f;
        float boxY = y - 4.2f;
        this.drawImmediateRect(matrix, boxX, boxY, box, box, 3.0f, 0.35f, 1109666616);
        this.drawImmediateOutline(matrix, boxX, boxY, box, box, 3.0f, ColorUtil.multAlpha(Hud.getInstance().themeOutlineColor(), 0.52f));
        float iconX = centerX;
        float iconY = y - 1.3f * (1.0f - ease);
        if (enabled) {
            iconX += 0.6f;
            iconY -= 2.25f;
        } else {
            iconX += 1.25f;
            iconY -= 2.25f;
        }
        Fonts.getMenuSize(size, Fonts.Type.COMBINED_ICONS).drawCenteredString(matrix, marker, iconX, iconY, color);
    }

    private void drawToggle(class_4587 matrix, float x, float y, boolean state, float active) {
        MenuStyle.toggle(matrix, x, y, active);
    }

    private void drawSettingsIcon(class_4587 matrix, float x, float y) {
        Fonts.getSize(14, Fonts.Type.COMBINED_ICONS).drawString(matrix, "\ue037", x, y, -2762524);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean insidePanel;
        if (button == 0 && this.logoButton != null && this.logoButton.hover(mouseX, mouseY)) {
            this.logoClickNanos = System.nanoTime();
            return true;
        }
        if (this.profileOpen && this.profileSettingsPanel != null && this.profileSettingsPanel.hover(mouseX, mouseY)) {
            if (button == 0 && mouseY < (double)(this.profileSettingsPanel.y + 72.0f)) {
                this.draggingProfilePanel = true;
                this.profileDragX = (float)mouseX - this.profileSettingsPanel.x;
                this.profileDragY = (float)mouseY - this.profileSettingsPanel.y;
                this.draggingProfileValue = null;
                this.profileScrollTarget = this.profileScroll;
            } else {
                this.profileScrollTarget = this.profileScroll;
                this.clickProfileSetting(mouseX, mouseY, button);
            }
            return true;
        }
        if (button == 0 && this.profileSettingsButton != null && this.profileSettingsButton.hover(mouseX, mouseY)) {
            this.updateProfileOpenAnimation();
            this.profileOpen = !this.profileOpen;
            this.profileOpenAnimationStart = this.profileOpenAnimation;
            this.profileOpenAnimationTarget = this.profileOpen ? 1.0f : 0.0f;
            this.profileOpenAnimationStartNanos = System.nanoTime();
            this.draggingProfileValue = null;
            this.draggingProfilePanel = false;
            this.profileSettingsContent = null;
            this.profileControlBounds.clear();
            this.profileChoices.clear();
            if (this.profileOpen) {
                this.profileScroll = 0.0f;
                this.profileScrollTarget = 0.0f;
                this.profileScrollTime = 0L;
                this.draggingValue = null;
                this.editingText = null;
                this.bindingModule = null;
                this.bindingSetting = null;
            }
            return true;
        }
        if (this.profileOpen) {
            this.updateProfileOpenAnimation();
            this.profileOpen = false;
            this.profileOpenAnimationStart = this.profileOpenAnimation;
            this.profileOpenAnimationTarget = 0.0f;
            this.profileOpenAnimationStartNanos = System.nanoTime();
            this.draggingProfileValue = null;
            this.draggingProfilePanel = false;
        }
        if (this.profileButton != null && this.profileButton.hover(mouseX, mouseY)) {
            return true;
        }
        if (button == 0 && this.bindingModule == null && this.bindingSetting == null) {
            for (Map.Entry<ModuleCategory, Bounds> entry2 : this.navigation.entrySet()) {
                if (!entry2.getValue().hover(mouseX, mouseY)) continue;
                MenuScreen.INSTANCE.setCategory(entry2.getKey());
                this.section = "";
                this.moduleHoverStart.clear();
                this.themeEditorOpen = false;
                return true;
            }
            for (Map.Entry<String, Bounds> entry3 : this.extraNavigation.entrySet()) {
                if (!entry3.getValue().hover(mouseX, mouseY)) continue;
                this.section = entry3.getKey();
                if (this.section.equals("Configs")) {
                    this.configs.open();
                }
                this.search.reset();
                this.themeEditorOpen = false;
                return true;
            }
            for (Map.Entry entry4 : this.favoriteBounds.entrySet()) {
                if (this.coveredByRaisedModule((Module)entry4.getKey(), mouseX, mouseY) || !((Bounds)entry4.getValue()).hover(mouseX, mouseY)) continue;
                if (!this.favorites.add((Module)entry4.getKey())) {
                    this.favorites.remove(entry4.getKey());
                }
                this.raisedFavoriteModule = (Module)entry4.getKey();
                this.starColorStarts.put(entry4.getKey(), this.starColors.getOrDefault(entry4.getKey(), Float.valueOf(this.favorites.contains(entry4.getKey()) ? 0.0f : 1.0f)));
                this.starClicks.put(entry4.getKey(), System.nanoTime());
                return true;
            }
        }
        if ("Configs".equals(this.section) && this.configs.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.bindingModule != null) {
            this.bindingModule.setKey(button);
            this.bindingModule = null;
            return true;
        }
        if (this.bindingSetting != null) {
            this.bindingSetting.setKey(button);
            this.bindingSetting = null;
            return true;
        }
        if ("AutoBuy".equals(this.section) && this.embeddedAutoBuyBounds != null && this.embeddedAutoBuyBounds.hover(mouseX, mouseY) && this.autoBuyPage != null) {
            this.autoBuyPage.mouseClickedEmbedded(mouseX, mouseY, button);
            return true;
        }
        if (button == 0) {
            if ("Themes".equals(this.section)) {
                for (ThemeBounds themeBounds : this.themeBounds) {
                    if (!themeBounds.bounds.hover(mouseX, mouseY)) continue;
                    this.clickedTheme = themeBounds.name;
                    this.themeClickTime = System.currentTimeMillis();
                    Hud.getInstance().themeSetting.setSelected(themeBounds.name);
                    return true;
                }
            }
            if (this.themeEditorOpen && this.themeCloseBounds != null && this.themeCloseBounds.hover(mouseX, mouseY)) {
                this.section = "";
                this.themeEditorOpen = false;
                return true;
            }
            if (this.themeEditorOpen && this.themePlusBounds != null && this.themePlusBounds.hover(mouseX, mouseY)) {
                Hud.getInstance().themeSetting.setSelected("Custom");
                return true;
            }
            if (this.themeEditorOpen) {
                for (Map.Entry entry5 : this.themeColorCountBounds.entrySet()) {
                    if (!((Bounds)entry5.getValue()).hover(mouseX, mouseY)) continue;
                    Hud.getInstance().customThemeColorCount.setSelected(String.valueOf(entry5.getKey()));
                    Hud.getInstance().themeSetting.setSelected("Custom");
                    return true;
                }
                for (ThemeBounds themeBounds : this.themeBounds) {
                    if (!themeBounds.bounds.hover(mouseX, mouseY)) continue;
                    this.clickedTheme = themeBounds.name;
                    this.themeClickTime = System.currentTimeMillis();
                    Hud.getInstance().themeSetting.setSelected(themeBounds.name);
                    return true;
                }
            }
        }
        if (button == 0 && "Cosmetics".equals(this.section) && this.cosmeticsPageBounds != null && new Bounds(this.cosmeticsPageBounds.x + this.cosmeticsPageBounds.width - 31.0f, this.cosmeticsPageBounds.y + 6.0f, 23.0f, 12.0f).hover(mouseX, mouseY)) {
            this.modules.values().stream().flatMap(Collection::stream).filter(module -> module.getName().equalsIgnoreCase("Cosmetics")).findFirst().ifPresent(Module::switchState);
            return true;
        }
        boolean bl = insidePanel = this.panels.values().stream().anyMatch(panel -> panel.hover(mouseX, mouseY)) || this.themeEditorPanel != null && this.themeEditorPanel.hover(mouseX, mouseY) || this.themePageBounds != null && this.themePageBounds.hover(mouseX, mouseY) || this.cosmeticsPageBounds != null && this.cosmeticsPageBounds.hover(mouseX, mouseY);
        if (!insidePanel) {
            this.bindingSetting = null;
            this.bindingModule = null;
            return false;
        }
        if (button == 0) {
            for (Map.Entry<Module, Bounds> entry6 : this.settingsIconBounds.entrySet()) {
                if (this.coveredByRaisedModule(entry6.getKey(), mouseX, mouseY) || !entry6.getValue().hover(mouseX, mouseY)) continue;
                this.toggle(entry6.getKey());
                this.bindingModule = null;
                return true;
            }
            if ("Cosmetics".equals(this.section)) {
                for (CosmeticItemBounds cosmeticItemBounds : this.cosmeticItemBounds) {
                    if (!cosmeticItemBounds.bounds.hover(mouseX, mouseY) || this.cosmeticsPageBounds == null || !(mouseY >= (double)(this.cosmeticsPageBounds.y + 30.0f)) || !(mouseY <= (double)(this.cosmeticsPageBounds.y + this.cosmeticsPageBounds.height))) continue;
                    cosmeticItemBounds.setting.setSelected(cosmeticItemBounds.value);
                    return true;
                }
            }
        }
        List<Map.Entry<Setting, Bounds>> list = this.settingBounds.entrySet().stream().filter(entry -> !this.coveredByRaisedModule(null, mouseX, mouseY) && ((Bounds)entry.getValue()).hover(mouseX, mouseY)).sorted(Comparator.comparingDouble(entry -> ((Bounds)entry.getValue()).width * ((Bounds)entry.getValue()).height)).toList();
        for (Map.Entry entry7 : list) {
            Setting setting = (Setting)entry7.getKey();
            if (!this.clickSetting(setting, mouseX, mouseY, button)) continue;
            return true;
        }
        for (Map.Entry<Module, Bounds> entry8 : this.visibleModuleBounds.entrySet()) {
            if (this.coveredByRaisedModule(entry8.getKey(), mouseX, mouseY) || !entry8.getValue().hover(mouseX, mouseY)) continue;
            if (button == 2) {
                this.bindingModule = entry8.getKey();
                return true;
            }
            if (button == 1) {
                this.toggle(entry8.getKey());
                this.bindingModule = null;
                return true;
            }
            if (button != 0) continue;
            Bounds card = this.moduleBounds.get(entry8.getKey());
            if (this.bindingModule != entry8.getKey() && card != null && new Bounds(card.x + card.width - 32.0f, card.y + 9.0f, 23.0f, 12.0f).hover(mouseX, mouseY)) {
                entry8.getKey().switchState();
            }
            return true;
        }
        this.bindingSetting = null;
        return false;
    }

    private boolean coveredByRaisedModule(Module module, double mouseX, double mouseY) {
        if (this.raisedFavoriteModule == null || this.raisedFavoriteModule == module) {
            return false;
        }
        Bounds raised = this.visibleModuleBounds.get(this.raisedFavoriteModule);
        return raised != null && raised.hover(mouseX, mouseY);
    }

    private boolean clickSetting(Setting setting, double mouseX, double mouseY, int button) {
        Bounds b = this.settingBounds.get(setting);
        if (setting instanceof ValueSetting) {
            ValueSetting value = (ValueSetting)setting;
            if (button == 2) {
                float currentPercent = value.getMax() <= value.getMin() ? 0.0f : class_3532.method_15363((float)((value.getValue() - value.getMin()) / (value.getMax() - value.getMin())), (float)0.0f, (float)1.0f);
                this.sliderAnimation.put(value, Float.valueOf(currentPercent));
                value.setValue(value.getDefaultValue());
                this.draggingValue = null;
                return true;
            }
            if (button != 0) {
                return false;
            }
            if (value == Hud.getInstance().guiScaleSetting) {
                this.scaleBeforeDrag = value.getValue();
            }
            this.draggingValue = value;
            this.applyValueDrag(value, mouseX, b.x + 4.0f, b.width - 8.0f);
            return true;
        }
        if (button != 0) {
            return false;
        }
        if (setting instanceof BooleanSetting) {
            BooleanSetting bool = (BooleanSetting)setting;
            bool.setValue(!bool.isValue());
            return true;
        }
        if (setting instanceof SelectSetting) {
            SelectSetting select = (SelectSetting)setting;
            for (ChipBounds chip : this.chipBounds.getOrDefault(setting, List.of())) {
                if (!chip.bounds.hover(mouseX, mouseY)) continue;
                select.setSelected(chip.value);
                return true;
            }
        }
        if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting multi = (MultiSelectSetting)setting;
            for (ChipBounds chip : this.chipBounds.getOrDefault(setting, List.of())) {
                if (!chip.bounds.hover(mouseX, mouseY)) continue;
                if (multi.isSelected(chip.value)) {
                    multi.getSelected().remove(chip.value);
                } else {
                    multi.getSelected().add(chip.value);
                }
                return true;
            }
        }
        if (setting instanceof BindSetting) {
            BindSetting bind = (BindSetting)setting;
            this.bindingSetting = this.bindingSetting == bind ? null : bind;
            return true;
        }
        if (setting instanceof ColorSetting) {
            ColorSetting color = (ColorSetting)setting;
            this.profileColorClicks.put(color, System.nanoTime());
            this.openColorWindow(color, b);
            return true;
        }
        if (setting instanceof GroupSetting) {
            GroupSetting group = (GroupSetting)setting;
            if (group.isCheckbox() && new Bounds(b.x, b.y + 3.0f, b.width, 15.0f).hover(mouseX, mouseY)) {
                group.setValue(!group.isValue());
                return true;
            }
            return false;
        }
        if (setting instanceof TextSetting) {
            TextSetting text;
            this.editingText = text = (TextSetting)setting;
            if (text.getText() == null) {
                text.setText("");
            }
            return true;
        }
        if (setting instanceof ButtonSetting) {
            ButtonSetting buttonSetting = (ButtonSetting)setting;
            String buttonName = this.buttonName(buttonSetting);
            float valueWidth = MenuStyle.font(9.0f).getStringWidth(buttonName) + 12.0f;
            if (b == null || !new Bounds(b.x + b.width - valueWidth, b.y + 3.0f, valueWidth, 15.0f).hover(mouseX, mouseY)) {
                return false;
            }
            if (buttonSetting.getRunnable() != null) {
                buttonSetting.getRunnable().run();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Bounds b;
        if (this.draggingProfilePanel && button == 0) {
            float panelHeight = Math.min(316.0f, this.height - 16.0f);
            this.profileOffsetX = (float)mouseX - this.profileDragX - this.x - 143.0f;
            this.profileOffsetY = (float)mouseY - this.profileDragY - this.y - this.height + panelHeight + 8.0f;
            return true;
        }
        if (this.draggingProfileValue != null) {
            this.applyProfileValue(mouseX);
            return true;
        }
        if ("AutoBuy".equals(this.section) && this.autoBuyPage != null && this.autoBuyPage.mouseDraggedEmbedded(mouseX, mouseY)) {
            return true;
        }
        if (this.draggingValue != null && (b = this.settingBounds.get(this.draggingValue)) != null) {
            this.applyValueDrag(this.draggingValue, mouseX, b.x + 4.0f, b.width - 8.0f);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (this.draggingProfilePanel || this.draggingProfileValue != null)) {
            this.draggingProfilePanel = false;
            this.draggingProfileValue = null;
            return true;
        }
        this.draggingProfileValue = null;
        if ("AutoBuy".equals(this.section) && this.autoBuyPage != null && this.autoBuyPage.mouseReleasedEmbedded()) {
            return true;
        }
        this.draggingValue = null;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if ("Configs".equals(this.section) && this.configs.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }
        if (this.profileOpen && this.profileSettingsPanel != null && this.profileSettingsPanel.hover(mouseX, mouseY)) {
            if (!this.draggingProfilePanel && this.draggingProfileValue == null && this.profileSettingsContent != null) {
                float maxScroll = Math.max(0.0f, this.profileContentHeight - this.profileSettingsContent.height);
                this.profileScrollTarget = class_3532.method_15363((float)(this.profileScrollTarget + (float)amount * 22.0f), (float)(-maxScroll), (float)0.0f);
            }
            return true;
        }
        if ("AutoBuy".equals(this.section) && this.embeddedAutoBuyBounds != null && this.embeddedAutoBuyBounds.hover(mouseX, mouseY) && this.autoBuyPage != null) {
            return this.autoBuyPage.mouseScrolledEmbedded(mouseX, mouseY, amount);
        }
        if ("Cosmetics".equals(this.section) && this.cosmeticsPageBounds != null && this.cosmeticsPageBounds.hover(mouseX, mouseY)) {
            Module module = this.modules.values().stream().flatMap(Collection::stream).filter(candidate -> candidate.getName().equalsIgnoreCase("Cosmetics")).findFirst().orElse(null);
            if (module != null) {
                float max = Math.max(0.0f, this.cosmeticSettingListHeight(module.settings(), this.cosmeticsPageBounds.width - 16.0f) - (this.cosmeticsPageBounds.height - 29.0f));
                this.cosmeticsScrollTarget = class_3532.method_15363((float)(this.cosmeticsScrollTarget + (float)amount * 22.0f), (float)(-max), (float)0.0f);
            }
            return true;
        }
        if ("Themes".equals(this.section) && this.themePageBounds != null && this.themePageBounds.hover(mouseX, mouseY)) {
            int count = Hud.getInstance().themeSetting.getList().size();
            int rows = (count + 2) / 3;
            float max = Math.max(0.0f, (float)rows * 87.0f - this.themePageBounds.height + 8.0f);
            this.themePageScroll = class_3532.method_15363((float)(this.themePageScroll + (float)amount * 18.0f), (float)(-max), (float)0.0f);
            return true;
        }
        if (this.themeEditorOpen && this.themeEditorList != null && this.themeEditorList.hover(mouseX, mouseY)) {
            float contentH = (float)(Hud.getInstance().themeSetting.getList().size() - 1) * 14.0f;
            float max = Math.max(0.0f, contentH - this.themeEditorList.height);
            this.themeEditorScroll = class_3532.method_15363((float)(this.themeEditorScroll + (float)amount * 18.0f), (float)(-max), (float)0.0f);
            return true;
        }
        for (Map.Entry<ModuleCategory, Bounds> entry : this.panels.entrySet()) {
            if (!entry.getValue().hover(mouseX, mouseY)) continue;
            ModuleCategory category = entry.getKey();
            float max = Math.max(0.0f, this.contentHeight(category) - entry.getValue().height);
            String key = this.scrollKey(category);
            this.scroll.put(key, Float.valueOf(class_3532.method_15363((float)(this.scroll.getOrDefault(key, Float.valueOf(0.0f)).floatValue() + (float)amount * 22.0f), (float)(-max), (float)0.0f)));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ("Configs".equals(this.section) && this.configs.keyPressed(keyCode, modifiers)) {
            return true;
        }
        if (this.profileOpen && keyCode == 256) {
            this.updateProfileOpenAnimation();
            this.profileOpen = false;
            this.profileOpenAnimationStart = this.profileOpenAnimation;
            this.profileOpenAnimationTarget = 0.0f;
            this.profileOpenAnimationStartNanos = System.nanoTime();
            this.draggingProfileValue = null;
            this.draggingProfilePanel = false;
            return true;
        }
        if ("AutoBuy".equals(this.section) && this.autoBuyPage != null && this.autoBuyPage.keyPressedEmbedded(keyCode, modifiers)) {
            return true;
        }
        if (this.editingText != null) {
            if (keyCode == 256 || keyCode == 257 || keyCode == 335) {
                this.editingText = null;
                return true;
            }
            if (keyCode == 259) {
                String value = Objects.requireNonNullElse(this.editingText.getText(), "");
                if (!value.isEmpty()) {
                    this.editingText.setText(value.substring(0, value.length() - 1));
                }
                return true;
            }
            if (keyCode == 261) {
                this.editingText.setText("");
                return true;
            }
            return false;
        }
        if (this.bindingModule != null) {
            int key = keyCode == 261 || keyCode == 256 || keyCode == 259 ? -1 : keyCode;
            this.bindingModule.setKey(key);
            this.bindingModule = null;
            return true;
        }
        if (this.bindingSetting != null) {
            int key = keyCode == 261 || keyCode == 256 || keyCode == 259 ? -1 : keyCode;
            this.bindingSetting.setKey(key);
            this.bindingSetting = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        int max;
        if ("Configs".equals(this.section) && !SearchComponent.typing && this.configs.charTyped(chr)) {
            return true;
        }
        if ("AutoBuy".equals(this.section) && this.autoBuyPage != null && this.autoBuyPage.charTypedEmbedded(chr)) {
            return true;
        }
        if (this.editingText == null || Character.isISOControl(chr)) {
            return false;
        }
        String value = Objects.requireNonNullElse(this.editingText.getText(), "");
        int n = max = this.editingText.getMax() > 0 ? this.editingText.getMax() : Integer.MAX_VALUE;
        if (value.length() < max) {
            this.editingText.setText(value + chr);
        }
        return true;
    }

    private void applyValueDrag(ValueSetting value, double mouseX, float x, float w) {
        float percent = class_3532.method_15363((float)(((float)mouseX - x) / Math.max(1.0f, w)), (float)0.0f, (float)1.0f);
        float next = value.getMin() + (value.getMax() - value.getMin()) * percent;
        if (value.getStep() > 0.0f) {
            next = value.getMin() + (float)Math.round((next - value.getMin()) / value.getStep()) * value.getStep();
        }
        if (value.isInteger()) {
            next = (int)next;
        }
        value.setValue(next);
    }

    private void openColorWindow(ColorSetting color, Bounds bounds) {
        AbstractWindow existing = null;
        for (AbstractWindow window : windowManager.getWindows()) {
            if (!(window instanceof ColorWindow)) continue;
            existing = window;
            break;
        }
        if (existing != null) {
            windowManager.delete(existing);
            if (((ColorWindow)existing).getSetting() == color) {
                return;
            }
        }
        float pickerX = bounds.x + bounds.width + 6.0f;
        float pickerY = bounds.y - 12.0f;
        if (pickerX + 150.0f > (float)window.getScaledWidth()) {
            pickerX = bounds.x - 156.0f;
        }
        if (pickerY + 165.0f > (float)window.getScaledHeight()) {
            pickerY = (float)window.getScaledHeight() - 170.0f;
        }
        if (pickerY < 10.0f) {
            pickerY = 10.0f;
        }
        windowManager.add(new ColorWindow(color).position(pickerX, pickerY).size(150.0f, 165.0f).draggable(true));
    }

    private float updateExpand(Module module, boolean open) {
        float target = open ? 1.0f : 0.0f;
        float current = this.expandAnimation.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
        float next = MathUtil.interpolateSmooth(0.75, current, target);
        if (Math.abs(next - target) < 0.0015f) {
            next = target;
        }
        if (next <= 0.0015f && !open) {
            this.expandAnimation.remove(module);
        } else {
            this.expandAnimation.put(module, Float.valueOf(next));
        }
        return next;
    }

    private float updateBindChip(Module module, boolean binding) {
        float target = binding ? 1.0f : 0.0f;
        float current = this.bindChipAnimation.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
        float next = MathUtil.interpolateSmooth(1.1, current, target);
        if (Math.abs(next - target) < 0.002f) {
            next = target;
        }
        if (next <= 0.0f && !binding) {
            this.bindChipAnimation.remove(module);
        } else {
            this.bindChipAnimation.put(module, Float.valueOf(next));
        }
        return next;
    }

    private float smoothStep(float edgeStart, float edgeEnd, float value) {
        float t = class_3532.method_15363((float)((value - edgeStart) / (edgeEnd - edgeStart)), (float)0.0f, (float)1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private float contentHeight(ModuleCategory category) {
        float column0 = 0.0f;
        float column1 = 0.0f;
        float settingsWidth = 235.5f;
        List<Module> list = this.filtered(category);
        for (int i = 0; i < list.size(); ++i) {
            Module module = list.get(i);
            float expand = this.expandAnimation.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
            float add = 38.0f + (expand > 0.002f ? (this.settingsHeight(module, settingsWidth) + 4.0f) * expand : 0.0f);
            if (i % 2 == 0) {
                column0 += add;
            } else {
                column1 += add;
            }
        }
        return Math.max(0.0f, Math.max(column0, column1) - 8.0f);
    }

    private float settingsHeight(Module module, float width) {
        return this.settingListHeight(module.settings(), width - 22.0f);
    }

    private float settingListHeight(List<Setting> source, float w) {
        float h = 0.0f;
        for (Setting setting : source) {
            if (!this.settingVisible(setting)) continue;
            h += this.settingHeight(setting, w);
        }
        return h;
    }

    private float settingHeight(Setting setting, float w) {
        if (setting instanceof ValueSetting) {
            return 34.0f;
        }
        if (setting instanceof SelectSetting) {
            SelectSetting select = (SelectSetting)setting;
            return 17.0f + (float)this.chipRows(select, select.getList(), w) * 15.0f;
        }
        if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting multi = (MultiSelectSetting)setting;
            return 17.0f + (float)this.chipRows(multi, multi.getList(), w) * 15.0f;
        }
        if (setting instanceof MultiColorSetting) {
            MultiColorSetting multiColor = (MultiColorSetting)setting;
            float h = 0.0f;
            for (ColorSetting color : multiColor.getAllColors()) {
                if (!this.settingVisible(color)) continue;
                h += 21.0f;
            }
            return h;
        }
        if (setting instanceof GroupSetting) {
            GroupSetting group = (GroupSetting)setting;
            float h = 21.0f;
            if (!group.isCheckbox() || group.isValue() || this.settingMatchesSearch(group)) {
                h += this.settingListHeight(group.getSubSettings(), w - 6.0f);
            }
            return h;
        }
        return 21.0f;
    }

    private int chipRows(Setting owner, List<String> values2, float w) {
        int rows = 1;
        float x = 0.0f;
        for (String value : values2) {
            String shown = MenuStyle.fit(DropdownMenuComponent.optionName(owner.getName(), value), 63.0f, 8.0f);
            float cw = Math.min(72.0f, MenuStyle.font(8.0f).getStringWidth(shown) + 9.0f);
            if (x > 0.0f && x + cw > w) {
                ++rows;
                x = 0.0f;
            }
            x += cw + 3.0f;
        }
        return rows;
    }

    private void toggle(Module module) {
        if (!this.expanded.remove(module) && this.hasSettings(module)) {
            this.expanded.add(module);
        }
    }

    private boolean hasSettings(Module module) {
        for (Setting setting : module.settings()) {
            if (!this.settingVisible(setting)) continue;
            return true;
        }
        return false;
    }

    private List<Module> filtered(ModuleCategory category) {
        String q = this.search.getText().trim().toLowerCase(Locale.ROOT);
        int favoritesHash = this.favorites.hashCode();
        if (!this.filteredCacheValid || !q.equals(this.filteredQuery) || !Objects.equals(this.section, this.filteredSection) || favoritesHash != this.filteredFavoritesHash) {
            this.filteredCache.clear();
            this.filteredQuery = q;
            this.filteredSection = this.section;
            this.filteredFavoritesHash = favoritesHash;
            this.filteredCacheValid = true;
        }
        List<Module> cached = this.filteredCache.get(category);
        if (cached != null) {
            return cached;
        }
        List<Module> computed = this.computeFiltered(category, q);
        this.filteredCache.put(category, computed);
        return computed;
    }

    private List<Module> computeFiltered(ModuleCategory category, String q) {
        if (!q.isEmpty()) {
            Stream<Module> stream = this.modules.values().stream().flatMap(Collection::stream).distinct();
            if ("Favorites".equals(this.section)) {
                stream = stream.filter(this.favorites::contains);
            }
            return stream.filter(module -> MenuSearch.matches(module.getVisibleName() + " " + module.getName(), q) || module.settings().stream().anyMatch(this::settingMatchesSearch)).sorted(Comparator.comparing((Module module) -> !this.favorites.contains(module)).thenComparing(module -> module.getVisibleName().toLowerCase(Locale.ROOT))).toList();
        }
        List list = this.section.isEmpty() ? this.modules.getOrDefault((Object)category, List.of()) : this.modules.values().stream().flatMap(Collection::stream).filter(module -> switch (this.section) {
            case "AutoBuy" -> module.getName().toLowerCase(Locale.ROOT).contains("autobuy");
            case "Cosmetics" -> module.getName().equalsIgnoreCase("Cosmetics");
            case "Favorites" -> this.favorites.contains(module);
            default -> false;
        }).toList();
        return list.stream().sorted(Comparator.comparing(module -> !this.favorites.contains(module))).toList();
    }

    private String scrollKey(ModuleCategory category) {
        String base = this.section.isEmpty() ? "category:" + category.name() : "section:" + this.section;
        String query = this.search.getText().strip().toLowerCase(Locale.ROOT);
        return query.isEmpty() ? base : base + ":search:" + query;
    }

    private boolean settingMatchesSearch(Setting setting) {
        GroupSetting group;
        MultiSelectSetting multi;
        SelectSetting select;
        String query = this.search.getText().strip();
        if (query.isEmpty()) {
            return false;
        }
        if (MenuSearch.matches(setting.getName() + " " + DropdownMenuComponent.settingName(setting.getName()) + " " + this.searchAliases(setting.getName()), query)) {
            return true;
        }
        if (setting instanceof SelectSetting && (select = (SelectSetting)setting).getList().stream().anyMatch(value -> MenuSearch.matches(value + " " + DropdownMenuComponent.optionName(setting.getName(), value), query))) {
            return true;
        }
        if (setting instanceof MultiSelectSetting && (multi = (MultiSelectSetting)setting).getList().stream().anyMatch(value -> MenuSearch.matches(value + " " + DropdownMenuComponent.optionName(setting.getName(), value), query))) {
            return true;
        }
        if (setting instanceof MultiColorSetting) {
            MultiColorSetting colors = (MultiColorSetting)setting;
            return colors.getAllColors().stream().anyMatch(this::settingMatchesSearch);
        }
        return setting instanceof GroupSetting && (group = (GroupSetting)setting).getSubSettings().stream().anyMatch(this::settingMatchesSearch);
    }

    private String searchAliases(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        String aliases = "";
        if (lower.contains("distance") || lower.contains("range")) {
            aliases = (String)aliases + " \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438\u0435";
        }
        if (lower.contains("\u0434\u0438\u0441\u0442\u0430\u043d\u0446") || lower.contains("\u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438")) {
            aliases = (String)aliases + " distance range";
        }
        if (lower.contains("rotation")) {
            aliases = (String)aliases + " \u0440\u043e\u0442\u0430\u0446\u0438\u044f \u043f\u043e\u0432\u043e\u0440\u043e\u0442";
        }
        if (lower.contains("speed")) {
            aliases = (String)aliases + " \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c";
        }
        if (lower.contains("delay")) {
            aliases = (String)aliases + " \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0430";
        }
        if (lower.equals("\u0440\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0443\u0434\u0430\u0440\u044b")) {
            aliases = (String)aliases + " \u0440\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0430\u0446\u0438\u044f \u043a\u0440\u0438\u0442\u043e\u0432 randomize crits randomization";
        }
        return aliases;
    }

    private boolean settingVisible(Setting setting) {
        Hud hud = Hud.getInstance();
        if (hud != null && (setting == hud.guiScaleSetting || setting == hud.guiOpacitySetting || setting == hud.shadowStrengthSetting || setting == hud.guiAccentColorSetting || setting == hud.guiBackgroundColorSetting || setting == hud.interface3DSetting || setting == hud.languageSetting)) {
            return false;
        }
        return setting.isVisible() || this.settingMatchesSearch(setting);
    }

    private void updateSearchExpansion() {
        String query = this.search.getText().strip();
        if (query.equals(this.appliedSearch)) {
            return;
        }
        this.expanded.removeAll(this.searchExpanded);
        this.searchExpanded.clear();
        this.appliedSearch = query;
        if (query.isEmpty()) {
            return;
        }
        this.modules.values().stream().flatMap(Collection::stream).forEach(module -> {
            if (module.settings().stream().anyMatch(this::settingMatchesSearch) && this.expanded.add((Module)module)) {
                this.searchExpanded.add((Module)module);
            }
        });
    }

    private void drawSearchLabel(class_4587 matrix, String text, float width, float size, float x, float y, int color) {
        if (!this.search.getText().isBlank() && !MenuSearch.matches(text, this.search.getText()) && MenuSearch.matches(this.searchAliases(text), this.search.getText())) {
            color = this.accent();
        }
        MenuSearch.draw(matrix, MenuStyle.font(size), MenuStyle.fit(text, width, size), x, y, color, this.search.getText());
    }

    public static String settingName(String name) {
        String translated;
        String additional = ModuleSettingLocalizer.label(name);
        if (additional != null) {
            return additional;
        }
        if (MenuDescriptionLocalizer.isRussian() && (translated = AutoCrystalLocalization.settingName(name)) != null) {
            return translated;
        }
        if (!MenuDescriptionLocalizer.isRussian()) {
            return switch (name) {
                case "\u0421\u0438\u043b\u0430 \u0431\u043b\u044e\u0440\u0430 HUD" -> "HUD Blur Strength";
                case "\u0420\u0435\u0436\u0438\u043c \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f" -> "Action Mode";
                case "\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u044f \u0432 \u0447\u0430\u0442" -> "Actions in Chat";
                case "\u0410\u043d\u0442\u0438 \u0441\u043f\u0430\u043c" -> "Anti Spam";
                case "\u0420\u0430\u0437\u043b\u043e\u0436\u0438\u0442\u044c \u0441\u0435\u0442" -> "Apply Layout";
                case "\u0420\u0435\u0436\u0438\u043c \u043f\u043e\u0438\u0441\u043a\u0430 \u0442\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u0438" -> "Area Search Mode";
                case "\u0411\u0440\u043e\u043d\u044f" -> "Armor";
                case "\u0410\u0432\u0442\u043e /event delay" -> "Auto /event delay";
                case "\u0410\u0432\u0442\u043e \u0435\u0434\u0430 \u0438 \u0438\u043d\u0432\u0438\u0437" -> "Auto Food & Invis";
                case "\u041e\u0442\u0447\u0451\u0442\u044b AutoSell" -> "AutoSell Reports";
                case "\u041e\u0442\u0445\u043e\u0434\u0438\u0442\u044c \u043f\u043e\u0441\u043b\u0435 \u043b\u0443\u0442\u0430" -> "Back Away After Loot";
                case "\u0411\u0430\u0437\u043e\u0432\u0430\u044f \u0430\u043d\u0430\u0440\u0445\u0438\u044f" -> "Base Anarchy";
                case "\u0426\u0432\u0435\u0442 \u043f\u043e\u043b\u0435\u0437\u043d\u044b\u0445 \u0437\u0435\u043b\u0438\u0439" -> "Buff Potion Color";
                case "\u041a\u0430\u043b\u044c\u043a\u0443\u043b\u044f\u0442\u043e\u0440" -> "Calculator";
                case "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430" -> "Delay";
                case "\u0420\u0430\u0434\u0438\u0443\u0441 \u0434\u0430\u043d\u0436\u0430" -> "Dungeon Radius";
                case "\u0427\u0430\u0440\u044b" -> "Enchants";
                case "\u042d\u043d\u0434-\u0430\u043d\u0430\u0440\u0445\u0438\u044f" -> "End Anarchy";
                case "\u0412\u0430\u0440\u043f \u0432 \u042d\u043d\u0434\u0435" -> "End Warp";
                case "\u0410\u043d\u0430\u0440\u0445\u0438\u0438 \u0434\u043b\u044f \u0444\u0430\u0440\u043c\u0430" -> "Farm Anarchies";
                case "\u041e\u0431\u0445\u043e\u0434 Funtime" -> "FunTime Bypass";
                case "\u0411\u0435\u0441\u043a\u043e\u043d\u0435\u0447\u043d\u044b\u0439 \u0447\u0430\u0442" -> "Infinite Chat";
                case "\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b" -> "Items";
                case "\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430" -> "Layout";
                case "\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043e\u043a" -> "Layout Editor";
                case "\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0434\u043b\u044f \u043b\u0443\u0442\u0430" -> "Loot Items";
                case "\u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442\u044b \u043b\u0443\u0442\u0430" -> "Loot Priorities";
                case "\u0411\u0443\u043b\u0430\u0432\u0430" -> "Mace";
                case "\u0410\u0432\u0442\u043e \u0431\u0443\u043b\u0430\u0432\u0430", "\u0410\u0432\u0442\u043e \u0411\u0443\u043b\u0430\u0432\u0430" -> "Auto Mace";
                case "\u041e\u0441\u043d\u043e\u0432\u043d\u0430\u044f \u0430\u043d\u0430\u0440\u0445\u0438\u044f" -> "Main Anarchy";
                case "\u0420\u0435\u0436\u0438\u043c" -> "Mode";
                case "\u0411\u0435\u0437 \u043f\u043e\u0432\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0439" -> "No Damage";
                case "\u0422\u043e\u043b\u044c\u043a\u043e \u0432 AFK" -> "Only in AFK";
                case "Only Through Partial Blocks" -> "Only Through Non-Full Blocks";
                case "\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u043c\u0435\u043d\u044e \u0437\u0435\u043b\u0438\u0439" -> "Open Potion Menu";
                case "\u0412\u044b\u043f\u043e\u043b\u043d\u044f\u0442\u044c \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f" -> "Perform Actions";
                case "\u0421\u043f\u043e\u0441\u043e\u0431 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f \u0437\u0435\u043b\u0438\u0439" -> "Potion Throw Mode";
                case "\u0417\u0432\u0443\u043a \u043f\u0440\u0438 \u043f\u043e\u043a\u0443\u043f\u043a\u0435" -> "Purchase Sound";
                case "\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0443\u0434\u0430\u0440\u044b" -> "Randomize Crits";
                case "\u0420\u0435\u0430\u0433\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u043d\u0430 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e\u0441\u0442\u044c" -> "React to Warning";
                case "\u0420\u0435\u043f\u043e\u0440\u0442\u0438\u0442\u044c \u043e\u0431\u0438\u0434\u0447\u0438\u043a\u043e\u0432" -> "Report Killers";
                case "\u041e\u0442\u043b\u0430\u0434\u043a\u0430" -> "Debug";
                case "\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u0440\u0435\u0441\u0443\u0440\u0441\u043e\u0432" -> "Resource Anarchy";
                case "\u0423\u0447\u0438\u0442\u044b\u0432\u0430\u0442\u044c \u0431\u0430\u043b\u0430\u043d\u0441" -> "Respect Balance";
                case "\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u043f\u0440\u043e\u0434\u0430\u0436\u0438" -> "Sell Anarchy";
                case "\u041f\u0440\u043e\u0434\u0430\u0436\u0430 \u043f\u043e \u0441\u0440.\u0446\u0435\u043d\u0435" -> "Sell at Avg Price";
                case "\u0420\u0435\u0436\u0438\u043c \u0441\u0435\u0440\u0432\u0435\u0440\u0430" -> "Server Mode";
                case "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0448\u0430\u043d\u0441 \u043f\u043e\u043f\u0430\u0434\u0430\u043d\u0438\u044f" -> "Show Hit Chance";
                case "\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a" -> "Source";
                case "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c" -> "Speed";
                case "\u0417\u0435\u043b\u044c\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438 (\u0431\u0440\u0430\u0442\u044c \u0438 \u043f\u0438\u0442\u044c)" -> "Speed Potions";
                case "\u0421\u0432\u0430\u043f\u0430\u0442\u044c \u0430\u043d\u0430\u0440\u0445\u0438\u0438" -> "Swap Anarchies";
                case "\u0422\u0435\u0433\u0438" -> "Tags";
                case "\u0411\u0440\u0430\u0442\u044c \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c" -> "Take Speed";
                case "\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u043c\u0443\u0441\u043e\u0440\u0430" -> "Trash Anarchy";
                case "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c" -> "Use Speed";
                case "\u0411\u0435\u0433\u0430\u0442\u044c \u0440\u044f\u0434\u043e\u043c" -> "Walk Nearby";
                default -> name;
            };
        }
        return switch (name) {
            case "AI Debug Log" -> "AI \u043b\u043e\u0433 \u0434\u0435\u0431\u0430\u0433\u0430";
            case "AI Human Misses" -> "AI \u043f\u0440\u043e\u043c\u0430\u0445\u0438";
            case "HUD Blur Color" -> "\u0426\u0432\u0435\u0442 \u0431\u043b\u044e\u0440\u0430 HUD";
            case "ML Rotation" -> "ML \u0440\u043e\u0442\u0430\u0446\u0438\u044f";
            case "Pitch" -> "Pitch";
            case "Yaw" -> "Yaw";
            case "Animation Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438";
            case "Bright" -> "\u042f\u0440\u043a\u043e\u0441\u0442\u044c";
            case "Auto Mace" -> "\u0410\u0432\u0442\u043e \u0411\u0443\u043b\u0430\u0432\u0430";
            case "Auto Disable" -> "\u0410\u0432\u0442\u043e \u0432\u044b\u043a\u043b.";
            case "AutoDebuff" -> "\u0410\u0432\u0442\u043e \u0434\u0435\u0431\u0430\u0444\u0444";
            case "Auto Deposit" -> "\u0410\u0432\u0442\u043e \u0434\u0435\u043f\u043e\u0437\u0438\u0442";
            case "Auto Food And Invisibility" -> "\u0410\u0432\u0442\u043e \u0435\u0434\u0430 \u0438 \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u044c";
            case "Auto Emeralds" -> "\u0410\u0432\u0442\u043e \u0438\u0437\u0443\u043c\u0440\u0443\u0434\u044b";
            case "Auto Loot" -> "\u0410\u0432\u0442\u043e \u043b\u0443\u0442";
            case "Weapons" -> "\u041e\u0440\u0443\u0436\u0438\u0435";
            case "Auto Experience" -> "\u0410\u0432\u0442\u043e \u043e\u043f\u044b\u0442";
            case "Auto Open" -> "\u0410\u0432\u0442\u043e \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u0435";
            case "Auto Repair" -> "\u0410\u0432\u0442\u043e \u043f\u043e\u0447\u0438\u043d\u043a\u0430";
            case "Auto Tp Accept" -> "\u0410\u0432\u0442\u043e \u043f\u0440\u0438\u043d\u044f\u0442\u0438\u0435 \u0422\u041f";
            case "Auto Clan Accept" -> "\u0410\u0432\u0442\u043e \u043f\u0440\u0438\u043d\u044f\u0442\u0438\u0435 \u0432 \u043a\u043b\u0430\u043d";
            case "Auto Jump" -> "\u0410\u0432\u0442\u043e \u043f\u0440\u044b\u0436\u043e\u043a";
            case "Auto Shulker" -> "\u0410\u0432\u0442\u043e \u0448\u0430\u043b\u043a\u0435\u0440";
            case "Auto ReParse" -> "\u0410\u0432\u0442\u043e-\u043f\u0430\u0440\u0441\u0438\u043d\u0433 \u0446\u0435\u043d";
            case "Auto Fix Slowdown" -> "\u0410\u0432\u0442\u043e-\u0444\u0438\u043a\u0441 \u0437\u0430\u0432\u0438\u0441\u0430\u043d\u0438\u044f";
            case "Farm Anarchies" -> "\u0410\u043d\u0430\u0440\u0445\u0438\u0438 \u0434\u043b\u044f \u0444\u0430\u0440\u043c\u0430";
            case "Anarchy" -> "\u0410\u043d\u0430\u0440\u0445\u0438\u044f";
            case "Base Anarchy" -> "\u0410\u043d\u0430\u0440\u0445\u0438\u044f \u0431\u0430\u0437\u044b";
            case "Animate" -> "\u0410\u043d\u0438\u043c\u0430\u0446\u0438\u0438";
            case "Color Animation" -> "\u0410\u043d\u0438\u043c\u0430\u0446\u0438\u044f \u0446\u0432\u0435\u0442\u0430";
            case "Particle Color Animation" -> "\u0410\u043d\u0438\u043c\u0430\u0446\u0438\u044f \u0446\u0432\u0435\u0442\u0430 \u0447\u0430\u0441\u0442\u0438\u0446";
            case "Balatro" -> "\u0411\u0430\u043b\u0430\u0442\u0440\u043e";
            case "No Level 3" -> "\u0411\u0435\u0437 3 \u0443\u0440\u043e\u0432\u043d\u044f";
            case "No Inventory" -> "\u0411\u0435\u0437 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f";
            case "No Combined" -> "\u0411\u0435\u0437 \u043a\u043e\u043c\u0431\u043e";
            case "No Firework Chestplate" -> "\u0411\u0435\u0437 \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0430 \u0432 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a\u0435";
            case "No Thorns" -> "\u0411\u0435\u0437 \u0448\u0438\u043f\u043e\u0432";
            case "No Silk Touch" -> "\u0411\u0435\u0437 \u0448\u0451\u043b\u043a\u043e\u0432\u043e\u0433\u043e \u043a\u0430\u0441\u0430\u043d\u0438\u044f";
            case "Anti Fly" -> "\u0411\u0438\u043d\u0434 \u0430\u043d\u0442\u0438 \u0444\u043b\u0430\u044f";
            case "Gods Aura" -> "\u0411\u0438\u043d\u0434 \u0431\u043e\u0436\u044c\u0435\u0439 \u0430\u0443\u0440\u044b";
            case "Throw" -> "\u0411\u0438\u043d\u0434 \u0431\u0440\u043e\u0441\u043a\u0430";
            case "Explosive Trap" -> "\u0411\u0438\u043d\u0434 \u0432\u0437\u0440\u044b\u0432\u043d\u043e\u0439 \u0442\u0440\u0430\u043f\u043a\u0438";
            case "Explosive Thing" -> "\u0411\u0438\u043d\u0434 \u0432\u0437\u0440\u044b\u0432\u0447\u0430\u0442\u043a\u0438";
            case "Disorientation" -> "\u0411\u0438\u043d\u0434 \u0434\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u0438";
            case "Menu Bind" -> "\u0411\u0438\u043d\u0434 \u043c\u0435\u043d\u044e";
            case "Default Trap" -> "\u0411\u0438\u043d\u0434 \u043e\u0431\u044b\u0447\u043d\u043e\u0439 \u0442\u0440\u0430\u043f\u043a\u0438";
            case "Fire Charge" -> "\u0411\u0438\u043d\u0434 \u043e\u0433\u043d\u0435\u043d\u043d\u043e\u0433\u043e \u0448\u0430\u0440\u0430";
            case "Plast" -> "\u0411\u0438\u043d\u0434 \u043f\u043b\u0430\u0441\u0442\u0430";
            case "Search Key" -> "\u0411\u0438\u043d\u0434 \u043f\u043e\u0438\u0441\u043a\u0430 \u043d\u0430 AH";
            case "Experience Scroll" -> "\u0411\u0438\u043d\u0434 \u0441\u0432\u0438\u0442\u043a\u0430 \u043e\u043f\u044b\u0442\u0430";
            case "SnowBall" -> "\u0411\u0438\u043d\u0434 \u0441\u043d\u0435\u0436\u043a\u0430";
            case "Message Bind" -> "\u0411\u0438\u043d\u0434 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f";
            case "Stun" -> "\u0411\u0438\u043d\u0434 \u0441\u0442\u0430\u043d\u0430";
            case "Trap" -> "\u0411\u0438\u043d\u0434 \u0442\u0440\u0430\u043f\u043a\u0438";
            case "Toggle Lead" -> "\u0411\u0438\u043d\u0434 \u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u044f";
            case "Clear Dust" -> "\u0411\u0438\u043d\u0434 \u044f\u0432\u043d\u043e\u0439 \u043f\u044b\u043b\u0438";
            case "Hit Invisible" -> "\u0411\u0438\u0442\u044c \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u043a";
            case "Attack Invisible" -> "\u0411\u0438\u0442\u044c \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u044b\u0445";
            case "Blocks" -> "\u0411\u043b\u043e\u043a\u0438";
            case "Quick Start" -> "\u0411\u044b\u0441\u0442\u0440\u044b\u0439 \u0441\u0442\u0430\u0440\u0442";
            case "Watermark Centered" -> "\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a \u043f\u043e \u0446\u0435\u043d\u0442\u0440\u0443";
            case "Wind Charge" -> "\u0412\u0435\u0442\u0440\u043e\u0432\u043e\u0439 \u0437\u0430\u0440\u044f\u0434";
            case "Target ESP" -> "\u0412\u0438\u0437\u0443\u0430\u043b\u0438\u0437\u0430\u0446\u0438\u044f \u0446\u0435\u043b\u0438";
            case "Active Module" -> "\u0412\u043a\u043b\u044e\u0447\u0435\u043d\u043d\u044b\u0439 \u043c\u043e\u0434\u0443\u043b\u044c";
            case "Appearance" -> "\u0412\u043d\u0435\u0448\u043d\u0438\u0439 \u0432\u0438\u0434";
            case "Time" -> "\u0412\u0440\u0435\u043c\u044f";
            case "Source Life Time" -> "\u0412\u0440\u0435\u043c\u044f \u0436\u0438\u0437\u043d\u0438 \u0438\u0441\u0442\u043e\u0447\u043d\u0438\u043a\u0430";
            case "Sky Time" -> "\u0412\u0440\u0435\u043c\u044f \u043d\u0435\u0431\u0430";
            case "Off Hand X" -> "\u0412\u0442\u043e\u0440\u0430\u044f \u0440\u0443\u043a\u0430 X";
            case "Off Hand Y" -> "\u0412\u0442\u043e\u0440\u0430\u044f \u0440\u0443\u043a\u0430 Y";
            case "Off Hand Z" -> "\u0412\u0442\u043e\u0440\u0430\u044f \u0440\u0443\u043a\u0430 Z";
            case "Second item" -> "\u0412\u0442\u043e\u0440\u043e\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442";
            case "Collector Items" -> "\u0412\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b";
            case "Unload Gunpowder" -> "\u0412\u044b\u0433\u0440\u0443\u0436\u0430\u0442\u044c \u043f\u043e\u0440\u043e\u0445";
            case "Disable On World" -> "\u0412\u044b\u043a\u043b. \u043f\u0440\u0438 \u0441\u043c\u0435\u043d\u0435 \u043c\u0438\u0440\u0430";
            case "Disable on World Change" -> "\u0412\u044b\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u043f\u0440\u0438 \u0441\u043c\u0435\u043d\u0435 \u043c\u0438\u0440\u0430";
            case "Inactive Module" -> "\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u043d\u044b\u0439 \u043c\u043e\u0434\u0443\u043b\u044c";
            case "Height" -> "\u0412\u044b\u0441\u043e\u0442\u0430";
            case "Mace Height" -> "\u0412\u044b\u0441\u043e\u0442\u0430 \u0431\u0443\u043b\u0430\u0432\u044b";
            case "Aim Height" -> "\u0412\u044b\u0441\u043e\u0442\u0430 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u044f";
            case "Fall Height" -> "\u0412\u044b\u0441\u043e\u0442\u0430 \u043f\u0430\u0434\u0435\u043d\u0438\u044f";
            case "Vertical Range" -> "\u0412\u044b\u0441\u043e\u0442\u0430 \u0441\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u044f";
            case "Spawn Height" -> "\u0412\u044b\u0441\u043e\u0442\u0430 \u0441\u043f\u0430\u0432\u043d\u0430";
            case "Geometrics" -> "\u0413\u0435\u043e\u043c\u0435\u0442\u0440\u0438\u044f";
            case "Volume" -> "\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c";
            case "Range" -> "\u0414\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c";
            case "Find Range" -> "\u0414\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u043f\u043e\u0438\u0441\u043a\u0430";
            case "Move Head" -> "\u0414\u0432\u0438\u0433\u0430\u0442\u044c \u0433\u043e\u043b\u043e\u0432\u0443";
            case "Engine" -> "\u0414\u0432\u0438\u0436\u043e\u043a";
            case "Dual Shader" -> "\u0414\u0432\u043e\u0439\u043d\u043e\u0439 \u0448\u0435\u0439\u0434\u0435\u0440";
            case "Dynamic Cooldown" -> "\u0414\u0438\u043d\u0430\u043c\u0438\u0447\u0435\u0441\u043a\u0430\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c";
            case "Dynamic" -> "\u0414\u0438\u043d\u0430\u043c\u0438\u0447\u0435\u0441\u043a\u0438\u0439";
            case "Dispersion" -> "\u0414\u0438\u0441\u043f\u0435\u0440\u0441\u0438\u044f";
            case "Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f";
            case "TNT Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f TNT";
            case "Attack Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u0430\u0442\u0430\u043a\u0438";
            case "Creeper Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043a\u0440\u0438\u043f\u0435\u0440\u0430";
            case "Crystal Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u0430";
            case "Link Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u0441\u0432\u044f\u0437\u0435\u0439";
            case "Follow Range" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u0441\u043b\u0435\u0434\u043e\u0432\u0430\u043d\u0438\u044f";
            case "Target Pearl Trigger Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u0441\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u044f";
            case "Fog Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u0442\u0443\u043c\u0430\u043d\u0430";
            case "Trail Length" -> "\u0414\u043b\u0438\u043d\u0430 \u0441\u043b\u0435\u0434\u0430";
            case "Duration" -> "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c";
            case "Swing Duration" -> "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0443\u0434\u0430\u0440\u0430";
            case "Friend Add" -> "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0434\u0440\u0443\u0433\u0430";
            case "Additional Distance" -> "\u0414\u043e\u043f. \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f";
            case "Extra Aim Distance" -> "\u0414\u043e\u043f. \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u044f";
            case "Extra Delay" -> "\u0414\u043e\u043f. \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0430";
            case "Friends" -> "\u0414\u0440\u0443\u0437\u044c\u044f";
            case "Smoke" -> "\u0414\u044b\u043c";
            case "Animals" -> "\u0416\u0438\u0432\u043e\u0442\u043d\u044b\u0435";
            case "Delayed Box" -> "\u0417\u0430\u0434\u0435\u0440\u0436\u0430\u043d\u043d\u044b\u0439 \u0431\u043e\u043a\u0441";
            case "Maximum Separation" -> "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u043e\u0442\u0440\u044b\u0432";
            case "Show Box" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0431\u043e\u043a\u0441";
            case "Delay" -> "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430";
            case "Delay (ms)" -> "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 (\u043c\u0441)";
            case "Potion Delay" -> "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u0432\u0430\u0440\u043a\u0438";
            case "Action Delay" -> "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0439";
            case "Point Dwell" -> "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043d\u0430 \u0442\u043e\u0447\u043a\u0435";
            case "Fill", "Filled" -> "\u0417\u0430\u043b\u0438\u0432\u043a\u0430";
            case "Detect Auction Slowdown" -> "\u0417\u0430\u043c\u0435\u0440 \u043b\u0430\u0433\u043e\u0432 \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0430";
            case "Freeze" -> "\u0417\u0430\u043c\u043e\u0440\u043e\u0437\u043a\u0430";
            case "Wind Charge Under Self" -> "\u0417\u0430\u0440\u044f\u0434 \u043f\u043e\u0434 \u0441\u0435\u0431\u044f";
            case "Replant" -> "\u0417\u0430\u0441\u0430\u0436\u0438\u0432\u0430\u0442\u044c";
            case "Armor Enchants" -> "\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u0438\u044f \u0431\u0440\u043e\u043d\u0438";
            case "Sword Enchants" -> "\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u0438\u044f \u043c\u0435\u0447\u0430";
            case "Purchase Sound" -> "\u0417\u0432\u0443\u043a \u043f\u043e\u043a\u0443\u043f\u043a\u0438";
            case "Elytra Health" -> "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0430\u0445";
            case "Potion To Brew" -> "\u0417\u0435\u043b\u044c\u0435 \u0434\u043b\u044f \u0432\u0430\u0440\u043a\u0438";
            case "Potions" -> "\u0417\u0435\u043b\u044c\u044f";
            case "Combiner Potions" -> "\u0417\u0435\u043b\u044c\u044f \u043a\u043e\u043c\u0431\u0430\u0439\u043d\u0435\u0440\u0430";
            case "Potions Side" -> "\u0417\u0435\u043b\u044c\u044f \u0441\u0431\u043e\u043a\u0443";
            case "Speed Potions (take and drink)" -> "\u0417\u0435\u043b\u044c\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438 (\u0431\u0440\u0430\u0442\u044c \u0438 \u043f\u0438\u0442\u044c)";
            case "Ignore" -> "\u0418\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c";
            case "Ignore Hunger" -> "\u0418\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0433\u043e\u043b\u043e\u0434";
            case "Ignore Blindness" -> "\u0418\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0441\u043b\u0435\u043f\u043e\u0442\u0443";
            case "Ignore Naked" -> "\u0418\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0433\u043e\u043b\u044b\u0445";
            case "Ignore The Walls" -> "\u0418\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0441\u0442\u0435\u043d\u044b";
            case "Players" -> "\u0418\u0433\u0440\u043e\u043a\u0438";
            case "Name" -> "\u0418\u043c\u044f";
            case "Custom Theme Name" -> "\u0418\u043c\u044f \u0442\u0435\u043c\u044b";
            case "Smoke Intensity" -> "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0434\u044b\u043c\u0430";
            case "Glow Intensity" -> "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f";
            case "Blend Intensity" -> "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0441\u043c\u0435\u0448\u0438\u0432\u0430\u043d\u0438\u044f";
            case "Snow Intensity" -> "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0441\u043d\u0435\u0433\u0430";
            case "Click Interval" -> "\u0418\u043d\u0442\u0435\u0440\u0432\u0430\u043b \u043a\u043b\u0438\u043a\u043e\u0432";
            case "Look Away Interval" -> "\u0418\u043d\u0442\u0435\u0440\u0432\u0430\u043b \u043e\u0442\u0432\u043e\u0434\u0430";
            case "ReParse Every (min)" -> "\u0418\u043d\u0442\u0435\u0440\u0432\u0430\u043b \u043f\u0430\u0440\u0441\u0438\u043d\u0433\u0430 (\u043c\u0438\u043d)";
            case "Parser Discount (%)" -> "\u0421\u043a\u0438\u0434\u043a\u0430 \u043f\u0430\u0440\u0441\u0435\u0440\u0430 (%)";
            case "Auto Point" -> "\u0418\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f \u043e\u0431 \u0438\u0432\u0435\u043d\u0442\u0435";
            case "Distortion" -> "\u0418\u0441\u043a\u0430\u0436\u0435\u043d\u0438\u0435";
            case "Use Bonemeal" -> "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u043a\u043e\u0441\u0442\u043d\u0443\u044e \u043c\u0443\u043a\u0443";
            case "Use Speed" -> "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c";
            case "Use Chests" -> "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0441\u0443\u043d\u0434\u0443\u043a\u0438";
            case "Use FireWork" -> "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a";
            case "Sources" -> "\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a\u0438";
            case "Sword Relist Cooldown" -> "\u041a\u0414 \u043c\u0435\u0447\u0435\u0439 \u043d\u0430 AH";
            case "Relist Cooldown" -> "\u041a\u0414 \u043f\u0435\u0440\u0435\u0432\u044b\u0441\u0442\u0430\u0432\u043b\u0435\u043d\u0438\u044f";
            case "Rescan Cooldown (sec)" -> "\u041a\u0414 \u043f\u043e\u0432\u0442\u043e\u0440\u043d\u043e\u0433\u043e \u0441\u043a\u0430\u043d\u0430 (\u0441\u0435\u043a)";
            case "Confirm Cooldown (ms)" -> "\u041a\u0414 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u044f (\u043c\u0441)";
            case "Buy Cooldown (ms)" -> "\u041a\u0414 \u043f\u043e\u043a\u0443\u043f\u043a\u0438 (\u043c\u0441)";
            case "Camera Clip" -> "\u041a\u0430\u043c\u0435\u0440\u0430 \u0441\u043a\u0432\u043e\u0437\u044c \u0431\u043b\u043e\u043a\u0438";
            case "Custom Ratio" -> "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u043e\u0435 \u0441\u043e\u043e\u0442\u043d\u043e\u0448\u0435\u043d\u0438\u0435";
            case "Custom Sky Color" -> "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0439 \u0446\u0432\u0435\u0442 \u043d\u0435\u0431\u0430";
            case "Quality" -> "\u041a\u0430\u0447\u0435\u0441\u0442\u0432\u043e";
            case "Bind", "Key" -> "\u041a\u043b\u0430\u0432\u0438\u0448\u0430";
            case "Item use key" -> "\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f";
            case "Button" -> "\u041a\u043d\u043e\u043f\u043a\u0430 \u043c\u044b\u0448\u0438";
            case "Fold Items Button" -> "\u041a\u043d\u043e\u043f\u043a\u0430 \u0441\u043b\u043e\u0436\u0438\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b";
            case "Oscillate X" -> "\u041a\u043e\u043b\u0435\u0431\u0430\u043d\u0438\u044f X";
            case "Oscillate Y" -> "\u041a\u043e\u043b\u0435\u0431\u0430\u043d\u0438\u044f Y";
            case "Count" -> "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e";
            case "Source Amount" -> "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0438\u0441\u0442\u043e\u0447\u043d\u0438\u043a\u0430";
            case "Sell Quantity" -> "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u043f\u0440\u043e\u0434\u0430\u0436\u0438";
            case "Firefly Count" -> "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0441\u0432\u0435\u0442\u043b\u044f\u0447\u043a\u043e\u0432";
            case "Geometrics Count" -> "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0444\u0438\u0433\u0443\u0440";
            case "Color Count" -> "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0446\u0432\u0435\u0442\u043e\u0432";
            case "Regular Count" -> "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0447\u0430\u0441\u0442\u0438\u0446";
            case "Bloom Particle Count" -> "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0447\u0430\u0441\u0442\u0438\u0446";
            case "Sprint Control" -> "\u041a\u043e\u043d\u0442\u0440\u043e\u043b\u044c \u0441\u043f\u0440\u0438\u043d\u0442\u0430";
            case "Inactive Outline" -> "\u041a\u043e\u043d\u0442\u0443\u0440 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u043d\u043e\u0433\u043e";
            case "Coordinates" -> "\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b";
            case "Down" -> "\u041a\u043e\u043f\u0430\u0442\u044c \u0432\u043d\u0438\u0437";
            case "Correction", "Move correction" -> "\u041a\u043e\u0440\u0440\u0435\u043a\u0446\u0438\u044f \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f";
            case "Target Red On Hit" -> "\u041a\u0440\u0430\u0441\u043d\u044b\u0439 \u043f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435";
            case "Craft All" -> "\u041a\u0440\u0430\u0444\u0442\u0438\u0442\u044c \u0432\u0441\u0435";
            case "Crits Only Space" -> "\u041a\u0440\u0438\u0442\u044b \u0442\u043e\u043b\u044c\u043a\u043e \u0441 \u043f\u0440\u043e\u0431\u0435\u043b\u043e\u043c";
            case "Line Of Sight" -> "\u041b\u0438\u043d\u0438\u044f \u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u0438";
            case "Break Shield", "Shield Break" -> "\u041b\u043e\u043c\u0430\u0442\u044c \u0449\u0438\u0442";
            case "Max CPS" -> "\u041c\u0430\u043a\u0441. CPS";
            case "Pitch Max" -> "\u041c\u0430\u043a\u0441. Pitch";
            case "Max Distance" -> "\u041c\u0430\u043a\u0441. \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f";
            case "Max Delay" -> "\u041c\u0430\u043a\u0441. \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0430";
            case "Max Health" -> "\u041c\u0430\u043a\u0441. \u0437\u0434\u043e\u0440\u043e\u0432\u044c\u0435";
            case "Wait Chest Up To (sec)" -> "\u041c\u0430\u043a\u0441. \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u0435 \u0441\u0443\u043d\u0434\u0443\u043a\u0430 (\u0441\u0435\u043a)";
            case "Max Links" -> "\u041c\u0430\u043a\u0441. \u0441\u0432\u044f\u0437\u0435\u0439";
            case "Pitch Speed Max" -> "\u041c\u0430\u043a\u0441. \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c Pitch";
            case "Yaw Speed Max" -> "\u041c\u0430\u043a\u0441. \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c Yaw";
            case "Max Damage" -> "\u041c\u0430\u043a\u0441. \u0443\u0440\u043e\u043d";
            case "Max Price" -> "\u041c\u0430\u043a\u0441. \u0446\u0435\u043d\u0430";
            case "Max Brightness" -> "\u041c\u0430\u043a\u0441. \u044f\u0440\u043a\u043e\u0441\u0442\u044c";
            case "Scale" -> "\u041c\u0430\u0441\u0448\u0442\u0430\u0431";
            case "Collector Menu" -> "\u041c\u0435\u043d\u044e \u0441\u0431\u043e\u0440\u0449\u0438\u043a\u0430";
            case "Swap Anarchies" -> "\u041c\u0435\u043d\u044f\u0442\u044c \u0430\u043d\u0430\u0440\u0445\u0438\u0438";
            case "Watermark Metrics" -> "\u041c\u0435\u0442\u0440\u0438\u043a\u0438 \u0432\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a\u0430";
            case "Micro Movement" -> "\u041c\u0438\u043a\u0440\u043e \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f";
            case "Micro Movements" -> "\u041c\u0438\u043a\u0440\u043e\u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f";
            case "Min CPS" -> "\u041c\u0438\u043d. CPS";
            case "Pitch Min" -> "\u041c\u0438\u043d. Pitch";
            case "Min Delay" -> "\u041c\u0438\u043d. \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0430";
            case "Minimum Durability" -> "\u041c\u0438\u043d. \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c";
            case "Pitch Speed Min" -> "\u041c\u0438\u043d. \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c Pitch";
            case "Yaw Speed Min" -> "\u041c\u0438\u043d. \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c Yaw";
            case "Min Brightness" -> "\u041c\u0438\u043d. \u044f\u0440\u043a\u043e\u0441\u0442\u044c";
            case "Minimalist HUD" -> "\u041c\u0438\u043d\u0438\u043c\u0430\u043b\u0438\u0441\u0442\u0438\u0447\u043d\u044b\u0439 HUD";
            case "Mobs" -> "\u041c\u043e\u0431\u044b";
            case "Multi-Point", "Multipoint" -> "\u041c\u0443\u043b\u044c\u0442\u0438\u043f\u043e\u0438\u043d\u0442";
            case "Direction" -> "\u041d\u0430\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435";
            case "Attack Setting", "Attack setting" -> "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0430\u0442\u0430\u043a\u0438";
            case "Player Settings" -> "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0438\u0433\u0440\u043e\u043a\u043e\u0432";
            case "World Setting" -> "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u043c\u0438\u0440\u0430";
            case "Chroma Saturation" -> "\u041d\u0430\u0441\u044b\u0449\u0435\u043d\u043d\u043e\u0441\u0442\u044c \u0445\u0440\u043e\u043c\u044b";
            case "No Attack In Containers" -> "\u041d\u0435 \u0431\u0438\u0442\u044c \u0432 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u0430\u0445";
            case "No Attack When Eat" -> "\u041d\u0435 \u0431\u0438\u0442\u044c \u0432\u043e \u0432\u0440\u0435\u043c\u044f \u0435\u0434\u044b";
            case "No Aim in Inventory" -> "\u041d\u0435 \u043d\u0430\u0432\u043e\u0434\u0438\u0442\u044c\u0441\u044f \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435";
            case "Don't Swap While Eating" -> "\u041d\u0435 \u0441\u0432\u0430\u043f\u0430\u0442\u044c \u0432\u043e \u0432\u0440\u0435\u043c\u044f \u0435\u0434\u044b";
            case "Inactive Slider" -> "\u041d\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u043f\u043e\u043b\u0437\u0443\u043d\u043e\u043a";
            case "Invisible", "Invisibles" -> "\u041d\u0435\u0432\u0438\u0434\u0438\u043c\u044b\u0435";
            case "Invisible Armor" -> "\u041d\u0435\u0432\u0438\u0434\u0438\u043c\u044b\u0435 \u0432 \u0431\u0440\u043e\u043d\u0435";
            case "Opacity" -> "\u041d\u0435\u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c";
            case "Outline" -> "\u041e\u0431\u0432\u043e\u0434\u043a\u0430";
            case "Bypass" -> "\u041e\u0431\u0445\u043e\u0434";
            case "Web Vertical Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u0432\u0435\u0440\u0445/\u0432\u043d\u0438\u0437";
            case "Regular" -> "\u041e\u0431\u044b\u0447\u043d\u044b\u0435";
            case "Free Hotbar" -> "\u041e\u0441\u0432\u043e\u0431\u043e\u0436\u0434\u0430\u0442\u044c \u0445\u043e\u0442\u0431\u0430\u0440";
            case "Main Hand X" -> "\u041e\u0441\u043d\u043e\u0432\u043d\u0430\u044f \u0440\u0443\u043a\u0430 X";
            case "Main Hand Y" -> "\u041e\u0441\u043d\u043e\u0432\u043d\u0430\u044f \u0440\u0443\u043a\u0430 Y";
            case "Main Hand Z" -> "\u041e\u0441\u043d\u043e\u0432\u043d\u0430\u044f \u0440\u0443\u043a\u0430 Z";
            case "Main Color" -> "\u041e\u0441\u043d\u043e\u0432\u043d\u043e\u0439 \u0446\u0432\u0435\u0442";
            case "Main Shader" -> "\u041e\u0441\u043d\u043e\u0432\u043d\u043e\u0439 \u0448\u0435\u0439\u0434\u0435\u0440";
            case "Look Away" -> "\u041e\u0442\u0432\u043e\u0434\u0438\u0442\u044c \u043f\u0440\u0438\u0446\u0435\u043b";
            case "Aim Reset" -> "\u041e\u0442\u0432\u043e\u0434\u043a\u0430 \u043f\u0440\u0438\u0446\u0435\u043b\u0430";
            case "UnPress Shield" -> "\u041e\u0442\u0436\u0438\u043c\u0430\u0442\u044c \u0449\u0438\u0442";
            case "Disable Blur In" -> "\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u0431\u043b\u044e\u0440 \u0432";
            case "Disable in PvP" -> "\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u0432 PvP";
            case "Disable Outline In" -> "\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u043e\u0431\u0432\u043e\u0434\u043a\u0443 \u0432";
            case "Disable Shadows In" -> "\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u0442\u0435\u043d\u0438 \u0432";
            case "Disable Blur" -> "\u041e\u0442\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0431\u043b\u044e\u0440";
            case "Disable Outline" -> "\u041e\u0442\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u043e\u0431\u0432\u043e\u0434\u043a\u0443";
            case "Disable Shadows" -> "\u041e\u0442\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0442\u0435\u043d\u0438";
            case "Cancel Packets" -> "\u041e\u0442\u043c\u0435\u043d\u044f\u0442\u044c \u043f\u0430\u043a\u0435\u0442\u044b";
            case "Indent" -> "\u041e\u0442\u0441\u0442\u0443\u043f";
            case "Attack Indent" -> "\u041e\u0442\u0441\u0442\u0443\u043f \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0438";
            case "Retreat After Loot" -> "\u041e\u0442\u0445\u043e\u0434\u0438\u0442\u044c \u043f\u043e\u0441\u043b\u0435 \u043b\u0443\u0442\u0430";
            case "Passwords Folder" -> "\u041f\u0430\u043f\u043a\u0430 \u043f\u0430\u0440\u043e\u043b\u0435\u0439";
            case "Effect Parameters" -> "\u041f\u0430\u0440\u0430\u043c\u0435\u0442\u0440\u044b \u044d\u0444\u0444\u0435\u043a\u0442\u0430";
            case "Password" -> "\u041f\u0430\u0440\u043e\u043b\u044c";
            case "First item" -> "\u041f\u0435\u0440\u0432\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442";
            case "Toggle" -> "\u041f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435";
            case "Smoothness" -> "\u041f\u043b\u0430\u0432\u043d\u043e\u0441\u0442\u044c";
            case "Overlay Lerp" -> "\u041f\u043b\u0430\u0432\u043d\u043e\u0441\u0442\u044c \u043d\u0430\u043b\u043e\u0436\u0435\u043d\u0438\u044f";
            case "Star Density" -> "\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c \u0437\u0432\u0451\u0437\u0434";
            case "Rotate" -> "\u041f\u043e\u0432\u043e\u0440\u0430\u0447\u0438\u0432\u0430\u0442\u044c";
            case "Pick Up" -> "\u041f\u043e\u0434\u0431\u0438\u0440\u0430\u0442\u044c \u043b\u0443\u0442";
            case "Confirmation" -> "\u041f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u0435";
            case "Show Distance" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044e";
            case "Show Health" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0437\u0434\u043e\u0440\u043e\u0432\u044c\u0435";
            case "Show Name" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0438\u043c\u044f";
            case "Draw FOV", "Draw FOV radius" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u043e\u043b\u0435 \u0437\u0440\u0435\u043d\u0438\u044f";
            case "Target Show On Hover" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u0438 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u0438";
            case "FOV" -> "\u041f\u043e\u043b\u0435 \u0437\u0440\u0435\u043d\u0438\u044f";
            case "Rotation FOV" -> "\u041f\u043e\u043b\u0435 \u0437\u0440\u0435\u043d\u0438\u044f \u0440\u043e\u0442\u0430\u0446\u0438\u0438";
            case "Slider" -> "\u041f\u043e\u043b\u0437\u0443\u043d\u043e\u043a";
            case "Stripes" -> "\u041f\u043e\u043b\u043e\u0441\u044b";
            case "Refill To" -> "\u041f\u043e\u043f\u043e\u043b\u043d\u044f\u0442\u044c \u0434\u043e";
            case "Loot Items" -> "\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0434\u043b\u044f \u043b\u0443\u0442\u0430";
            case "Refraction" -> "\u041f\u0440\u0435\u043b\u043e\u043c\u043b\u0435\u043d\u0438\u0435";
            case "Zoom" -> "\u041f\u0440\u0438\u0431\u043b\u0438\u0436\u0435\u043d\u0438\u0435";
            case "Priority" -> "\u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442";
            case "Loot Priority" -> "\u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442 \u043b\u0443\u0442\u0430";
            case "Block Check" -> "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u0431\u043b\u043e\u043a\u043e\u0432";
            case "Ray Trace" -> "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u043b\u0443\u0447\u043e\u043c";
            case "Fall Check" -> "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u043f\u0430\u0434\u0435\u043d\u0438\u044f";
            case "Wall Check" -> "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u0441\u0442\u0435\u043d";
            case "Advanced" -> "\u041f\u0440\u043e\u0434\u0432\u0438\u043d\u0443\u0442\u044b\u0435";
            case "Alpha" -> "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c";
            case "Misses" -> "\u041f\u0440\u043e\u043c\u0430\u0445\u0438";
            case "Durability" -> "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c";
            case "Target FunTime Durability" -> "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c FunTime";
            case "Wind Charge Jump" -> "\u041f\u0440\u044b\u0436\u043e\u043a \u0441 \u0437\u0430\u0440\u044f\u0434\u043e\u043c";
            case "EXP Bottle" -> "\u041f\u0443\u0437\u044b\u0440\u0451\u043a \u043e\u043f\u044b\u0442\u0430";
            case "Radius" -> "\u0420\u0430\u0434\u0438\u0443\u0441";
            case "Villager Radius" -> "\u0420\u0430\u0434\u0438\u0443\u0441 \u0436\u0438\u0442\u0435\u043b\u0435\u0439";
            case "Search Radius" -> "\u0420\u0430\u0434\u0438\u0443\u0441 \u043f\u043e\u0438\u0441\u043a\u0430";
            case "Glow Radius" -> "\u0420\u0430\u0434\u0438\u0443\u0441 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f";
            case "Farm Radius", "Scan Radius" -> "\u0420\u0430\u0434\u0438\u0443\u0441 \u0441\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u044f";
            case "Spawn Radius" -> "\u0420\u0430\u0434\u0438\u0443\u0441 \u0441\u043f\u0430\u0432\u043d\u0430";
            case "Chunk Radius" -> "\u0420\u0430\u0434\u0438\u0443\u0441 \u0447\u0430\u043d\u043a\u043e\u0432";
            case "Noise" -> "\u0420\u0430\u0437\u0431\u0440\u043e\u0441";
            case "Noise Range" -> "\u0420\u0430\u0437\u0431\u0440\u043e\u0441 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0438";
            case "Separate Watermark Elements" -> "\u0420\u0430\u0437\u0434\u0435\u043b\u044f\u0442\u044c \u0432\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a";
            case "Size" -> "\u0420\u0430\u0437\u043c\u0435\u0440";
            case "Off Hand Size" -> "\u0420\u0430\u0437\u043c\u0435\u0440 \u0432\u0442\u043e\u0440\u043e\u0439 \u0440\u0443\u043a\u0438";
            case "Main Hand Size" -> "\u0420\u0430\u0437\u043c\u0435\u0440 \u043e\u0441\u043d\u043e\u0432\u043d\u043e\u0439 \u0440\u0443\u043a\u0438";
            case "Blur Size" -> "\u0420\u0430\u0437\u043c\u0435\u0440 \u0440\u0430\u0437\u043c\u044b\u0442\u0438\u044f";
            case "Firefly Scale" -> "\u0420\u0430\u0437\u043c\u0435\u0440 \u0441\u0432\u0435\u0442\u043b\u044f\u0447\u043a\u043e\u0432";
            case "Geometrics Scale" -> "\u0420\u0430\u0437\u043c\u0435\u0440 \u0444\u0438\u0433\u0443\u0440";
            case "Regular Scale" -> "\u0420\u0430\u0437\u043c\u0435\u0440 \u0447\u0430\u0441\u0442\u0438\u0446";
            case "Background Blur" -> "\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435 \u0444\u043e\u043d\u0430";
            case "Pitch Random" -> "\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0430\u0446\u0438\u044f Pitch";
            case "Yaw Random" -> "\u0420\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0430\u0446\u0438\u044f Yaw";
            case "XZ Expand" -> "\u0420\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u0435 XZ";
            case "Y Expand" -> "\u0420\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u0435 Y";
            case "Theme Editor" -> "\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 \u0442\u0435\u043c";
            case "Mode" -> "\u0420\u0435\u0436\u0438\u043c";
            case "PvP Mode" -> "\u0420\u0435\u0436\u0438\u043c PvP";
            case "Attack Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0430\u0442\u0430\u043a\u0438";
            case "Dual Shader Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0434\u0432\u043e\u0439\u043d\u043e\u0433\u043e \u0448\u0435\u0439\u0434\u0435\u0440\u0430";
            case "Multipoint Mode" -> "\u0420\u0435\u0436\u0438\u043c \u043c\u0443\u043b\u044c\u0442\u0438\u043f\u043e\u0438\u043d\u0442\u0430";
            case "Item Mode" -> "\u0420\u0435\u0436\u0438\u043c \u043e\u0431\u0445\u043e\u0434\u0430";
            case "Count Mode" -> "\u0420\u0435\u0436\u0438\u043c \u043f\u043e\u0434\u0441\u0447\u0451\u0442\u0430";
            case "Work Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0440\u0430\u0431\u043e\u0442\u044b";
            case "Glow Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f";
            case "Blend Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0441\u043c\u0435\u0448\u0438\u0432\u0430\u043d\u0438\u044f";
            case "Stack Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0441\u0442\u0430\u043a\u0430";
            case "Point Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0442\u043e\u0447\u0435\u043a";
            case "Color Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0446\u0432\u0435\u0442\u0430";
            case "Particle Color Mode" -> "\u0420\u0435\u0436\u0438\u043c \u0446\u0432\u0435\u0442\u0430 \u0447\u0430\u0441\u0442\u0438\u0446";
            case "Emerald Reserve" -> "\u0420\u0435\u0437\u0435\u0440\u0432 \u0438\u0437\u0443\u043c\u0440\u0443\u0434\u043e\u0432";
            case "Report Griefers" -> "\u0420\u0435\u043f\u043e\u0440\u0442\u0438\u0442\u044c \u0433\u0440\u0438\u0444\u0435\u0440\u043e\u0432";
            case "Camera Rotation" -> "\u0420\u043e\u0442\u0430\u0446\u0438\u044f \u043a\u0430\u043c\u0435\u0440\u044b";
            case "Ores" -> "\u0420\u0443\u0434\u044b";
            case "Sprint Reset" -> "\u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430";
            case "Reset Points" -> "\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0438";
            case "Elytra Swap" -> "\u0421\u0432\u0430\u043f \u044d\u043b\u0438\u0442\u0440";
            case "Firefly" -> "\u0421\u0432\u0435\u0442\u043b\u044f\u0447\u043a\u0438";
            case "Glow" -> "\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435";
            case "Free Look" -> "\u0421\u0432\u043e\u0431\u043e\u0434\u043d\u044b\u0439 \u043e\u0431\u0437\u043e\u0440";
            case "Custom Delay" -> "\u0421\u0432\u043e\u044f \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0430";
            case "Server" -> "\u0421\u0435\u0440\u0432\u0435\u0440";
            case "Network" -> "\u0421\u0435\u0442\u044c";
            case "Glow Power" -> "\u0421\u0438\u043b\u0430 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f";
            case "Keep Sprint Strength" -> "\u0421\u0438\u043b\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u044f";
            case "Shader 1 Strength" -> "\u0421\u0438\u043b\u0430 \u0448\u0435\u0439\u0434\u0435\u0440\u0430 1";
            case "Shader 2 Strength" -> "\u0421\u0438\u043b\u0430 \u0448\u0435\u0439\u0434\u0435\u0440\u0430 2";
            case "HitBox Sync" -> "\u0421\u0438\u043d\u0445\u0440. \u0445\u0438\u0442\u0431\u043e\u043a\u0441\u0430";
            case "Through Blocks" -> "\u0421\u043a\u0432\u043e\u0437\u044c \u0431\u043b\u043e\u043a\u0438";
            case "Through Walls" -> "\u0421\u043a\u0432\u043e\u0437\u044c \u0441\u0442\u0435\u043d\u044b";
            case "Store Drop" -> "\u0421\u043a\u043b\u0430\u0434\u044b\u0432\u0430\u0442\u044c \u0434\u043e\u0431\u044b\u0447\u0443";
            case "Wheelie Bind" -> "\u0411\u0438\u043d\u0434 \u0435\u0437\u0434\u044b \u043d\u0430 \u0437\u0430\u0434\u043d\u0435\u043c \u043a\u043e\u043b\u0435\u0441\u0435";
            case "Bike Top Speed" -> "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0431\u0430\u0439\u043a\u0430";
            case "Bike Acceleration" -> "\u0420\u0430\u0437\u0433\u043e\u043d \u0431\u0430\u0439\u043a\u0430";
            case "Bike Manual Gears" -> "\u0420\u0443\u0447\u043d\u044b\u0435 \u043f\u0435\u0440\u0435\u0434\u0430\u0447\u0438";
            case "Bike Gear Up" -> "\u041f\u0435\u0440\u0435\u0434\u0430\u0447\u0430 \u0432\u044b\u0448\u0435";
            case "Bike Gear Down" -> "\u041f\u0435\u0440\u0435\u0434\u0430\u0447\u0430 \u043d\u0438\u0436\u0435";
            case "Bike Dashboard" -> "\u041f\u0440\u0438\u0431\u043e\u0440\u043d\u0430\u044f \u043f\u0430\u043d\u0435\u043b\u044c";
            case "Bike Particles" -> "\u041f\u044b\u043b\u044c \u0438 \u0431\u0440\u044b\u0437\u0433\u0438";
            case "Bike Tire Tracks" -> "\u0421\u043b\u0435\u0434\u044b \u0448\u0438\u043d";
            case "Bike Handlebar View" -> "\u0412\u0438\u0434 \u043e\u0442 \u0440\u0443\u043b\u044f";
            case "Bike Camera Shake" -> "\u0422\u0440\u044f\u0441\u043a\u0430 \u043a\u0430\u043c\u0435\u0440\u044b";
            case "Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c";
            case "Acceleration" -> "\u0423\u0441\u043a\u043e\u0440\u0435\u043d\u0438\u0435";
            case "Speed 1" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c 1";
            case "Speed 2" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c 2";
            case "Attack Pitch Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0430\u0442\u0430\u043a\u0438 Pitch";
            case "Attack Yaw Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0430\u0442\u0430\u043a\u0438 Yaw";
            case "Balatro Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0431\u0430\u043b\u0430\u0442\u0440\u043e";
            case "Return Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u043e\u0437\u0432\u0440\u0430\u0442\u0430";
            case "Gradient Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0433\u0440\u0430\u0434\u0438\u0435\u043d\u0442\u0430";
            case "Overlay Aim Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0434\u043e\u0432\u043e\u0434\u043a\u0438";
            case "Smoke Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0434\u044b\u043c\u0430";
            case "Inventory Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f";
            case "Source Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0438\u0441\u0442\u043e\u0447\u043d\u0438\u043a\u0430";
            case "Button Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043a\u043d\u043e\u043f\u043e\u043a";
            case "Oscillate Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043a\u043e\u043b\u0435\u0431\u0430\u043d\u0438\u0439";
            case "Stripe Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u043e\u043b\u043e\u0441";
            case "Ghost Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u043e\u0432";
            case "Point Switch Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0441\u043c\u0435\u043d\u044b \u0442\u043e\u0447\u0435\u043a";
            case "Snow Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0441\u043d\u0435\u0433\u0430";
            case "Chest Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0441\u0443\u043d\u0434\u0443\u043a\u043e\u0432";
            case "Tab Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0442\u0430\u0431\u0430";
            case "Chroma Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0445\u0440\u043e\u043c\u044b";
            case "Hide Anarchy" -> "\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u043d\u043e\u043c\u0435\u0440 \u0430\u043d\u0430\u0440\u0445\u0438\u0438";
            case "Hide Screen" -> "\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u044d\u043a\u0440\u0430\u043d";
            case "Stealth Mode (Warden)" -> "\u0421\u043a\u0440\u044b\u0442\u043d\u044b\u0439 \u0440\u0435\u0436\u0438\u043c (\u0412\u0430\u0440\u0434\u0435\u043d)";
            case "Hide Hand Texture" -> "\u0421\u043a\u0440\u044b\u0442\u044c \u0442\u0435\u043a\u0441\u0442\u0443\u0440\u0443 \u0440\u0443\u043a\u0438";
            case "Pitch Follow" -> "\u0421\u043b\u0435\u0434\u043e\u0432\u0430\u043d\u0438\u0435 Pitch";
            case "Restore Word" -> "\u0421\u043b\u043e\u0432\u043e \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u044f";
            case "Slots" -> "\u0421\u043b\u043e\u0442\u044b";
            case "Pitch Offset" -> "\u0421\u043c\u0435\u0449\u0435\u043d\u0438\u0435 Pitch";
            case "Yaw Offset" -> "\u0421\u043c\u0435\u0449\u0435\u043d\u0438\u0435 Yaw";
            case "Side Point Offset" -> "\u0421\u043c\u0435\u0449\u0435\u043d\u0438\u0435 \u0431\u043e\u043a\u043e\u0432\u044b\u0445 \u0442\u043e\u0447\u0435\u043a";
            case "Collect Loot After Death" -> "\u0421\u043e\u0431\u0438\u0440\u0430\u0442\u044c \u043b\u0443\u0442 \u043f\u043e\u0441\u043b\u0435 \u0441\u043c\u0435\u0440\u0442\u0438";
            case "Ratio" -> "\u0421\u043e\u043e\u0442\u043d\u043e\u0448\u0435\u043d\u0438\u0435";
            case "Keep Enchanted Totems" -> "\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0442\u044c \u0437\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435 \u0442\u043e\u0442\u0435\u043c\u044b";
            case "Keep Sprint" -> "\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0442\u044c \u0441\u043f\u0440\u0438\u043d\u0442";
            case "Prediction" -> "\u041f\u0440\u0435\u0434\u0438\u043a\u0442";
            case "Spawn" -> "\u0421\u043f\u0430\u0432\u043d";
            case "Solid" -> "\u0421\u043f\u043b\u043e\u0448\u043d\u043e\u0439";
            case "Lag Statistics In Chat" -> "\u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430 \u0437\u0430\u0434\u0435\u0440\u0436\u0435\u043a \u0432 \u0447\u0430\u0442\u0435";
            case "Glass" -> "\u0421\u0442\u0435\u043a\u043b\u043e";
            case "Style" -> "\u0421\u0442\u0438\u043b\u044c";
            case "Cube Type" -> "\u0421\u0442\u0438\u043b\u044c \u043a\u0443\u0431\u0430";
            case "Ghost Type" -> "\u0421\u0442\u0438\u043b\u044c \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u043e\u0432";
            case "Consumables Timer" -> "\u0422\u0430\u0439\u043c\u0435\u0440 \u0440\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u043e\u0432";
            case "Attack Timing" -> "\u0422\u0430\u0439\u043c\u0438\u043d\u0433 \u0443\u0434\u0430\u0440\u0430";
            case "AutoMessage Text" -> "\u0422\u0435\u043a\u0441\u0442 \u0430\u0432\u0442\u043e-\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f";
            case "Inactive Text" -> "\u0422\u0435\u043a\u0441\u0442 \u043d\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0433\u043e";
            case "Theme" -> "\u0422\u0435\u043c\u0430";
            case "Ticks" -> "\u0422\u0438\u043a\u0438";
            case "Type" -> "\u0422\u0438\u043f";
            case "Swing Type" -> "\u0422\u0438\u043f \u0432\u0437\u043c\u0430\u0445\u0430";
            case "Leave Type" -> "\u0422\u0438\u043f \u0432\u044b\u0445\u043e\u0434\u0430";
            case "Correction Type" -> "\u0422\u0438\u043f \u043a\u043e\u0440\u0440\u0435\u043a\u0446\u0438\u0438";
            case "Stealer Type" -> "\u0422\u0438\u043f \u043b\u0443\u0442\u0430";
            case "Aim Type" -> "\u0422\u0438\u043f \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u044f";
            case "Rotation Type" -> "\u0422\u0438\u043f \u0440\u043e\u0442\u0430\u0446\u0438\u0438";
            case "Entity Type" -> "\u0422\u0438\u043f \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439";
            case "Crop Type" -> "\u0422\u0438\u043f \u0443\u0440\u043e\u0436\u0430\u044f";
            case "Target Type" -> "\u0422\u0438\u043f \u0446\u0435\u043b\u0435\u0439";
            case "Particle Type" -> "\u0422\u0438\u043f \u0447\u0430\u0441\u0442\u0438\u0446";
            case "Bot Token" -> "\u0422\u043e\u043a\u0435\u043d \u0431\u043e\u0442\u0430";
            case "Line Width" -> "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u043e\u0431\u0432\u043e\u0434\u043a\u0438";
            case "X Only" -> "\u0422\u043e\u043b\u044c\u043a\u043e X";
            case "Only Yaw" -> "\u0422\u043e\u043b\u044c\u043a\u043e Yaw";
            case "Only PvP" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u0432 PvP";
            case "Armored Only", "Only Armored" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u0432 \u0431\u0440\u043e\u043d\u0435";
            case "Only Friends" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u0434\u0440\u0443\u0437\u044c\u044f";
            case "Enchanted Only" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u0437\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435";
            case "Only Players", "Players Only" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u0438\u0433\u0440\u043e\u043a\u0438";
            case "Only Critical" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u043a\u0440\u0438\u0442\u044b";
            case "Only Through Partial Blocks" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u0447\u0435\u0440\u0435\u0437 \u043d\u0435\u043f\u043e\u043b\u043d\u044b\u0435 \u0431\u043b\u043e\u043a\u0438";
            case "Only Ground" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u043d\u0430 \u0437\u0435\u043c\u043b\u0435";
            case "Correct Movement" -> "\u041a\u043e\u0440\u0440\u0435\u043a\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0435";
            case "Only Attack Aura" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u0441 Attack Aura";
            case "Only Weapon", "Weapon Only" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u0441 \u043e\u0440\u0443\u0436\u0438\u0435\u043c";
            case "Route Point" -> "\u0422\u043e\u0447\u043a\u0430 \u043c\u0430\u0440\u0448\u0440\u0443\u0442\u0430";
            case "Trap+Plast" -> "\u0422\u0440\u0430\u043f\u043a\u0430 + \u043f\u043b\u0430\u0441\u0442";
            case "Notifications" -> "\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f";
            case "Look Away Angle" -> "\u0423\u0433\u043e\u043b \u043e\u0442\u0432\u043e\u0434\u0430";
            case "Hold" -> "\u0423\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435";
            case "Enhance" -> "\u0423\u043b\u0443\u0447\u0448\u0430\u0442\u044c \u0437\u0435\u043b\u044c\u044f";
            case "Forward Lead" -> "\u0423\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0435";
            case "Head Lead" -> "\u0423\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0435 \u043f\u043e \u0433\u043e\u043b\u043e\u0432\u0435";
            case "Triggers" -> "\u0423\u0441\u043b\u043e\u0432\u0438\u044f";
            case "Alpha Factor" -> "\u0424\u0430\u043a\u0442\u043e\u0440 \u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u0438";
            case "Physics" -> "\u0424\u0438\u0437\u0438\u043a\u0430";
            case "Particle Physics" -> "\u0424\u0438\u0437\u0438\u043a\u0430 \u0447\u0430\u0441\u0442\u0438\u0446";
            case "Count Filter" -> "\u0424\u0438\u043b\u044c\u0442\u0440 \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u0430";
            case "Warden Home" -> "\u0425\u043e\u0443\u043c \u0432\u0430\u0440\u0434\u0435\u043d\u0430";
            case "Storage" -> "\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435";
            case "Chroma" -> "\u0425\u0440\u043e\u043c\u0430";
            case "Chromatic Aberration" -> "\u0425\u0440\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0430\u044f \u0430\u0431\u0435\u0440\u0440\u0430\u0446\u0438\u044f";
            case "Client Color", "Color" -> "\u0426\u0432\u0435\u0442";
            case "Color 1" -> "\u0426\u0432\u0435\u0442 1";
            case "Color 2" -> "\u0426\u0432\u0435\u0442 2";
            case "Color 3" -> "\u0426\u0432\u0435\u0442 3";
            case "Color 4" -> "\u0426\u0432\u0435\u0442 4";
            case "Smoke Color" -> "\u0426\u0432\u0435\u0442 \u0434\u044b\u043c\u0430";
            case "Global Interface Color" -> "\u0426\u0432\u0435\u0442 \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430";
            case "Sky Color" -> "\u0426\u0432\u0435\u0442 \u043d\u0435\u0431\u0430";
            case "Outline Color" -> "\u0426\u0432\u0435\u0442 \u043e\u0431\u0432\u043e\u0434\u043a\u0438";
            case "Panel Color" -> "\u0426\u0432\u0435\u0442 \u043f\u0430\u043d\u0435\u043b\u0435\u0439";
            case "Stripe Color 1" -> "\u0426\u0432\u0435\u0442 \u043f\u043e\u043b\u043e\u0441\u044b 1";
            case "Stripe Color 2" -> "\u0426\u0432\u0435\u0442 \u043f\u043e\u043b\u043e\u0441\u044b 2";
            case "Glow Color" -> "\u0426\u0432\u0435\u0442 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f";
            case "Snow Color" -> "\u0426\u0432\u0435\u0442 \u0441\u043d\u0435\u0433\u0430";
            case "Text Color" -> "\u0426\u0432\u0435\u0442 \u0442\u0435\u043a\u0441\u0442\u0430";
            case "Colors" -> "\u0426\u0432\u0435\u0442\u0430";
            case "Gradient Colors" -> "\u0426\u0432\u0435\u0442\u0430 \u0433\u0440\u0430\u0434\u0438\u0435\u043d\u0442\u0430";
            case "Theme Blur Color" -> "\u0426\u0432\u0435\u0442\u043d\u043e\u0439 \u0431\u043b\u044e\u0440";
            case "Repaired Armor" -> "\u0426\u0435\u043b\u0430\u044f \u0431\u0440\u043e\u043d\u044f";
            case "Targets" -> "\u0426\u0435\u043b\u0438";
            case "Sword Price" -> "\u0426\u0435\u043d\u0430 \u043c\u0435\u0447\u0435\u0439";
            case "Sell Price" -> "\u0426\u0435\u043d\u0430 \u043f\u0440\u043e\u0434\u0430\u0436\u0438";
            case "What To Buy" -> "\u0427\u0442\u043e \u043f\u043e\u043a\u0443\u043f\u0430\u0442\u044c";
            case "Chance" -> "\u0428\u0430\u043d\u0441";
            case "Miss Chance" -> "\u0428\u0430\u043d\u0441 \u043f\u0440\u043e\u043c\u0430\u0445\u0430";
            case "Shader 1" -> "\u0428\u0435\u0439\u0434\u0435\u0440 1";
            case "Shader 2" -> "\u0428\u0435\u0439\u0434\u0435\u0440 2";
            case "Fill Shader" -> "\u0428\u0435\u0439\u0434\u0435\u0440 \u0437\u0430\u043b\u0438\u0432\u043a\u0438";
            case "Sky Shader" -> "\u0428\u0435\u0439\u0434\u0435\u0440 \u043d\u0435\u0431\u0430";
            case "Width" -> "\u0428\u0438\u0440\u0438\u043d\u0430";
            case "Stripe Width" -> "\u0428\u0438\u0440\u0438\u043d\u0430 \u043f\u043e\u043b\u043e\u0441";
            case "Elements" -> "\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u044b";
            case "Interface Elements" -> "\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u044b \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430";
            case "Ender Pearl" -> "\u042d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433";
            case "Hand Effect" -> "\u042d\u0444\u0444\u0435\u043a\u0442 \u0440\u0443\u043a\u0438";
            case "Background Effect" -> "\u042d\u0444\u0444\u0435\u043a\u0442 \u0444\u043e\u043d\u0430";
            case "Language" -> "\u042f\u0437\u044b\u043a";
            case "Totem Pop Sound" -> "\u0417\u0432\u0443\u043a \u0441\u043d\u043e\u0441\u0430 \u0442\u043e\u0442\u0435\u043c\u0430";
            case "Hit Sound" -> "\u0417\u0432\u0443\u043a \u0443\u0434\u0430\u0440\u0430";
            case "Reset Totem Sound", "Clear Totem Sound" -> "\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c \u0437\u0432\u0443\u043a \u0441\u043d\u043e\u0441\u0430 \u0442\u043e\u0442\u0435\u043c\u0430";
            case "Clear Hit Sound" -> "\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c \u0437\u0432\u0443\u043a \u0443\u0434\u0430\u0440\u0430";
            case "Clear Screen", "Clear Text" -> "\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u044d\u043a\u0440\u0430\u043d";
            case "Bases File" -> "\u0424\u0430\u0439\u043b \u0441 \u0431\u0430\u0437\u0430\u043c\u0438";
            case "Min Height" -> "\u041c\u0438\u043d. \u0432\u044b\u0441\u043e\u0442\u0430";
            case "Max Height" -> "\u041c\u0430\u043a\u0441. \u0432\u044b\u0441\u043e\u0442\u0430";
            case "Turn Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043f\u043e\u0432\u043e\u0440\u043e\u0442\u0430";
            case "Brightness" -> "\u042f\u0440\u043a\u043e\u0441\u0442\u044c";
            case "Sky Brightness" -> "\u042f\u0440\u043a\u043e\u0441\u0442\u044c \u043d\u0435\u0431\u0430";
            case "Chroma Brightness" -> "\u042f\u0440\u043a\u043e\u0441\u0442\u044c \u0445\u0440\u043e\u043c\u044b";
            default -> DropdownMenuComponent.translateWords(name);
        };
    }

    public static String optionName(String value) {
        return DropdownMenuComponent.optionName(null, value);
    }

    public static String optionName(String settingName, String value) {
        String translated;
        if (MenuDescriptionLocalizer.isRussian() && (translated = AutoCrystalLocalization.optionName(settingName, value)) != null) {
            return translated;
        }
        if ("Smooth".equals(value) && ("Rotation Type".equals(settingName) || "Engine".equals(settingName) || "Pitch Follow".equals(settingName))) {
            return value;
        }
        String additional = ModuleSettingLocalizer.option(value);
        if (additional != null) {
            return additional;
        }
        if (!MenuDescriptionLocalizer.isRussian()) {
            return switch (value) {
                case "\u0421\u043e \u0432\u0441\u0435\u0445 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432" -> "All Containers";
                case "\u0411\u043b\u0443\u043c" -> "Bloom";
                case "\u0411\u0440\u043e\u043d\u044f" -> "Armor";
                case "\u0421\u0442\u0440\u0435\u043b\u044b" -> "Arrows";
                case "\u041f\u0440\u043e\u0431\u0438\u0442\u0438\u0435 3" -> "Breach 3";
                case "\u041f\u043e\u043b\u0435\u0437\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f" -> "Buff Potions";
                case "\u0412\u0440\u0435\u0434\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f" -> "Debuff Potions";
                case "\u041e\u0431\u044b\u0447\u043d\u044b\u0439" -> "Default";
                case "\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c 5" -> "Density 5";
                case "\u0414\u043e\u043d \u0437\u0435\u043b\u044c\u044f" -> "Don Potions";
                case "\u042f\u0439\u0446\u0430" -> "Eggs";
                case "\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435 \u0437\u043e\u043b\u043e\u0442\u044b\u0435 \u044f\u0431\u043b\u043e\u043a\u0438" -> "Enchanted Golden Apples";
                case "\u0417\u0430\u0433\u043e\u0432\u043e\u0440 \u043e\u0433\u043d\u044f 2" -> "Fire Aspect 2";
                case "\u041e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c" -> "Fire Resistance";
                case "\u0421 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f" -> "From Inventory";
                case "\u0417\u043e\u043b\u043e\u0442\u044b\u0435 \u044f\u0431\u043b\u043e\u043a\u0438" -> "Golden Apples";
                case "\u041b\u0435\u0433\u0438\u0442" -> "Legit";
                case "\u0417\u0430\u0440\u044f\u0434 \u0412\u0435\u0442\u0440\u0430" -> "Wind Charge";
                case "\u0412\u044b\u0441\u043e\u043a\u0438\u0439" -> "High";
                case "\u041f\u0440\u044b\u0436\u043e\u043a" -> "Jump";
                case "\u041a\u043b\u0430\u0432\u0438\u0448\u0430" -> "Keybind";
                case "\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 1" -> "Layout 1";
                case "\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 2" -> "Layout 2";
                case "\u0420\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0430 3" -> "Layout 3";
                case "\u041d\u0438\u0437\u043a\u0438\u0439" -> "Low";
                case "\u0421\u0440\u0435\u0434\u043d\u0438\u0439" -> "Medium";
                case "\u041f\u043e\u0447\u0438\u043d\u043a\u0430" -> "Mending";
                case "\u0414\u0432\u0438\u0436\u0435\u043d\u0438\u0435" -> "Move";
                case "\u042f\u0434" -> "Poison";
                case "\u0417\u0430\u0449\u0438\u0442\u0430 5" -> "Protection 5";
                case "\u041f\u043e\u0432\u043e\u0440\u043e\u0442" -> "Rotate";
                case "\u0417\u0430\u043a\u0440\u0443\u0433\u043b\u0435\u043d\u043d\u044b\u0439" -> "Rounded";
                case "\u0420\u0443\u0441\u0441\u043a\u0438\u0439" -> "Russian";
                case "\u041f\u043e\u0438\u0441\u043a \u0441\u043d\u0438\u0437\u0443" -> "Search Bottom";
                case "\u041f\u043e\u0438\u0441\u043a \u0441\u0432\u0435\u0440\u0445\u0443" -> "Search Top";
                case "\u041a\u043e\u043b\u0435\u0441\u043e \u0432\u044b\u0431\u043e\u0440\u0430" -> "Selection Wheel";
                case "\u041e\u0441\u0442\u0440\u043e\u0442\u0430 7" -> "Sharpness 7";
                case "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c" -> "Speed";
                case "Player Head" -> "Sphere";
                case "\u0421\u0444\u0435\u0440\u044b" -> "Spheres";
                case "\u041a\u0432\u0430\u0434\u0440\u0430\u0442\u043d\u044b\u0439" -> "Square";
                case "\u0421\u0438\u043b\u0430" -> "Strength";
                case "\u0412\u0438\u0434 1" -> "Style 1";
                case "\u0412\u0438\u0434 2" -> "Style 2";
                case "\u0412\u0438\u0434 3" -> "Style 3";
                case "\u0412\u0437\u043c\u0430\u0445" -> "Swing";
                case "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d\u044b" -> "Talismans";
                case "\u0428\u0438\u043f\u044b" -> "Thorns";
                case "Totem of Undying" -> "Totem/Talisman";
                case "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c 5" -> "Unbreaking 5";
                case "\u0426\u0435\u043d\u043d\u044b\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b" -> "Valuables";
                case "\u041e\u0440\u0443\u0436\u0438\u0435" -> "Weapons";
                case "\u041e\u043a\u0438\u0441\u043b\u0435\u043d\u0438\u0435" -> "Wind Burst";
                default -> value;
            };
        }
        if ("Smooth".equals(value)) {
            if ("Rotation Type".equals(settingName) || "Engine".equals(settingName) || "Pitch Follow".equals(settingName)) {
                return "Smooth";
            }
            return "\u041f\u043b\u0430\u0432\u043d\u0430\u044f";
        }
        return switch (value) {
            case "Select" -> "\u0412\u044b\u0431\u0440\u0430\u0442\u044c";
            case "Clear" -> "\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c";
            case "Reset" -> "\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c";
            case "Open" -> "\u041e\u0442\u043a\u0440\u044b\u0442\u044c";
            case "AI" -> "AI";
            case "BPS" -> "BPS";
            case "COORDS" -> "COORDS";
            case "English" -> "English";
            case "FPS" -> "FPS";
            case "FT Rod" -> "FT Rod";
            case "Focused" -> "Focused";
            case "Free" -> "Free";
            case "FunTime" -> "FunTime";
            case "FunTime Elytra" -> "FunTime Elytra";
            case "FunTime FOV" -> "FunTime FOV";
            case "Funtime Custom" -> "Funtime Custom";
            case "GUI" -> "GUI";
            case "Grim" -> "Grim";
            case "Grim Collision" -> "Grim Collision";
            case "Grim New" -> "Grim New";
            case "Grim Old" -> "Grim Old";
            case "Grim V2" -> "Grim V2";
            case "HUD" -> "HUD";
            case "HVH" -> "HVH";
            case "Holy" -> "Holy";
            case "HolyWorld" -> "HolyWorld";
            case "Jump Reset" -> "Jump Reset";
            case "Legit" -> "\u041b\u0435\u0433\u0438\u0442";
            case "Matrix" -> "Matrix";
            case "Oracle" -> "Oracle";
            case "PING" -> "PING";
            case "Polar" -> "Polar";
            case "ReallyWorld" -> "ReallyWorld";
            case "sloth" -> "sloth";
            case "Spooky" -> "Spooky";
            case "SpookyTime", "Spookytime" -> "SpookyTime";
            case "ApexTime", "Apextime" -> "ApexTime";
            case "Wind Charge" -> "\u0417\u0430\u0440\u044f\u0434 \u0412\u0435\u0442\u0440\u0430";
            case "TIME" -> "TIME";
            case "TNT" -> "TNT";
            case "TPS" -> "TPS";
            case "Target" -> "Target";
            case "Vanilla" -> "Vanilla";
            case "Auto" -> "\u0410\u0432\u0442\u043e";
            case "Auto Armor" -> "\u0410\u0432\u0442\u043e \u0431\u0440\u043e\u043d\u044f";
            case "Auto Sell" -> "\u0410\u0432\u0442\u043e \u043f\u0440\u043e\u0434\u0430\u0436\u0430";
            case "Nether Wart" -> "\u0410\u0434\u0441\u043a\u0438\u0439 \u043d\u0430\u0440\u043e\u0441\u0442";
            case "Diamond" -> "\u0410\u043b\u043c\u0430\u0437";
            case "Aura" -> "\u0410\u0443\u0440\u0430";
            case "Balatro" -> "\u0411\u0430\u043b\u0430\u0442\u0440\u043e";
            case "Unarmored" -> "\u0411\u0435\u0437 \u0431\u0440\u043e\u043d\u0438";
            case "White" -> "\u0411\u0435\u043b\u0430\u044f";
            case "ServerHelper Binds" -> "\u0411\u0438\u043d\u0434\u044b \u0441\u0435\u0440\u0432\u0435\u0440\u0430";
            case "Name Tag" -> "\u0411\u0438\u0440\u043a\u0430";
            case "Turquoise" -> "\u0411\u0438\u0440\u044e\u0437\u043e\u0432\u0430\u044f";
            case "Block" -> "\u0411\u043b\u043e\u043a";
            case "Bloom" -> "\u0411\u043b\u0443\u043c";
            case "Box", "Flat Box" -> "\u0411\u043e\u043a\u0441";
            case "Boss Bar" -> "\u0411\u043e\u0441\u0441\u0431\u0430\u0440";
            case "Barrels" -> "\u0411\u043e\u0447\u043a\u0438";
            case "Armor" -> "\u0411\u0440\u043e\u043d\u044f";
            case "Mace" -> "\u0411\u0443\u043b\u0430\u0432\u0430";
            case "Fast" -> "\u0411\u044b\u0441\u0442\u0440\u044b\u0439";
            case "Watermark" -> "\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a";
            case "Water Bucket" -> "\u0412\u0435\u0434\u0440\u043e \u0432\u043e\u0434\u044b";
            case "Vertex" -> "\u0412\u0435\u0440\u0442\u0435\u043a\u0441\u044b";
            case "Swipe" -> "\u0412\u0437\u043c\u0430\u0445";
            case "Vignette" -> "\u0412\u0438\u043d\u044c\u0435\u0442\u043a\u0430";
            case "Virus" -> "\u0412\u0438\u0440\u0443\u0441";
            case "On" -> "\u0412\u043a\u043b.";
            case "Down" -> "\u0412\u043d\u0438\u0437";
            case "Water" -> "\u0412\u043e\u0434\u0430";
            case "Wave" -> "\u0412\u043e\u043b\u043d\u0430";
            case "Time" -> "\u0412\u0440\u0435\u043c\u044f";
            case "All" -> "\u0412\u0441\u0435";
            case "Always" -> "\u0412\u0441\u0435\u0433\u0434\u0430";
            case "Staff Join" -> "\u0412\u0445\u043e\u0434 \u0441\u0442\u0430\u0444\u0444\u0430";
            case "Off" -> "\u0412\u044b\u043a\u043b.";
            case "High" -> "\u0412\u044b\u0441\u043e\u043a\u0438\u0439";
            case "Gamma" -> "\u0413\u0430\u043c\u043c\u0430";
            case "Main Menu" -> "\u0413\u043b\u0430\u0432\u043d\u043e\u0435 \u043c\u0435\u043d\u044e";
            case "Deep Space" -> "\u0413\u043b\u0443\u0431\u043e\u043a\u0438\u0439 \u043a\u043e\u0441\u043c\u043e\u0441";
            case "Naked Player", "Naked Players" -> "\u0413\u043e\u043b\u044b\u0435 \u0438\u0433\u0440\u043e\u043a\u0438";
            case "World Border" -> "\u0413\u0440\u0430\u043d\u0438\u0446\u0430 \u043c\u0438\u0440\u0430";
            case "Day" -> "\u0414\u0435\u043d\u044c";
            case "Dynamic Cooldown" -> "\u0414\u0438\u043d\u0430\u043c\u0438\u0447\u0435\u0441\u043a\u0430\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c";
            case "Dynamic" -> "\u0414\u0438\u043d\u0430\u043c\u0438\u0447\u043d\u044b\u0439";
            case "Distance" -> "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f";
            case "Dollar" -> "\u0414\u043e\u043b\u043b\u0430\u0440";
            case "Ancient Debris" -> "\u0414\u0440\u0435\u0432\u043d\u0438\u0435 \u043e\u0431\u043b\u043e\u043c\u043a\u0438";
            case "Friends" -> "\u0414\u0440\u0443\u0437\u044c\u044f";
            case "Duo" -> "\u0414\u0443\u043e";
            case "Smoke" -> "\u0414\u044b\u043c";
            case "Eat", "Food" -> "\u0415\u0434\u0430";
            case "Iron" -> "\u0416\u0435\u043b\u0435\u0437\u043e";
            case "Animal", "Animals" -> "\u0416\u0438\u0432\u043e\u0442\u043d\u044b\u0435";
            case "Liquid" -> "\u0416\u0438\u0434\u043a\u0438\u0439";
            case "Villager", "Villagers" -> "\u0416\u0438\u0442\u0435\u043b\u0438";
            case "Sunset" -> "\u0417\u0430\u043a\u0430\u0442";
            case "Faded" -> "\u0417\u0430\u0442\u0443\u0445\u0430\u044e\u0449\u0438\u0439";
            case "Stardust" -> "\u0417\u0432\u0451\u0437\u0434\u043d\u0430\u044f \u043f\u044b\u043b\u044c";
            case "Starfield" -> "\u0417\u0432\u0451\u0437\u0434\u043d\u043e\u0435 \u043f\u043e\u043b\u0435";
            case "Stars" -> "\u0417\u0432\u0451\u0437\u0434\u044b";
            case "Health" -> "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435";
            case "Potions" -> "\u0417\u0435\u043b\u044c\u044f";
            case "Green" -> "\u0417\u0435\u043b\u0451\u043d\u0430\u044f";
            case "Golden Carrot" -> "\u0417\u043e\u043b\u043e\u0442\u0430\u044f \u043c\u043e\u0440\u043a\u043e\u0432\u044c";
            case "Gold" -> "\u0417\u043e\u043b\u043e\u0442\u043e";
            case "Golden Apple" -> "\u0417\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e";
            case "Gold Ingot" -> "\u0417\u043e\u043b\u043e\u0442\u043e\u0439 \u0441\u043b\u0438\u0442\u043e\u043a";
            case "Ignore The Walls" -> "\u0418\u0433\u043d\u043e\u0440 \u0441\u0442\u0435\u043d";
            case "Player", "Players" -> "\u0418\u0433\u0440\u043e\u043a\u0438";
            case "Emerald" -> "\u0418\u0437\u0443\u043c\u0440\u0443\u0434";
            case "Inventory" -> "\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c";
            case "Invert" -> "\u0418\u043d\u0432\u0435\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0439";
            case "Use Item" -> "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442\u0441\u044f \u043f\u0440\u0435\u0434\u043c\u0435\u0442";
            case "Healing" -> "\u0418\u0441\u0446\u0435\u043b\u0435\u043d\u0438\u0435";
            case "Break CoolDown" -> "\u041a\u0414 \u043b\u043e\u043c\u0430\u043d\u0438\u044f";
            case "Item Cooldown" -> "\u041a\u0414 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430";
            case "Camouflage" -> "\u041a\u0430\u043c\u0443\u0444\u043b\u044f\u0436";
            case "Potatoes" -> "\u041a\u0430\u0440\u0442\u043e\u0444\u0435\u043b\u044c";
            case "Custom" -> "\u041a\u0430\u0441\u0442\u043e\u043c";
            case "Quartet" -> "\u041a\u0432\u0430\u0440\u0442\u0435\u0442";
            case "Quartz Block" -> "\u041a\u0432\u0430\u0440\u0446\u0435\u0432\u044b\u0439 \u0431\u043b\u043e\u043a";
            case "Buttons" -> "\u041a\u043d\u043e\u043f\u043a\u0438";
            case "Entity Collision" -> "\u041a\u043e\u043b\u043b\u0438\u0437\u0438\u044f \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439";
            case "Potion Combiner" -> "\u041a\u043e\u043c\u0431\u0430\u0439\u043d\u0435\u0440 \u0437\u0435\u043b\u0438\u0439";
            case "Potion Expire" -> "\u041a\u043e\u043d\u0435\u0446 \u0437\u0435\u043b\u044c\u044f";
            case "Coron" -> "\u041a\u043e\u0440\u043e\u043d\u0430";
            case "Red" -> "\u041a\u0440\u0430\u0441\u043d\u0430\u044f";
            case "Red-Black" -> "\u041a\u0440\u0430\u0441\u043d\u043e-\u0447\u0451\u0440\u043d\u0430\u044f";
            case "Creeper" -> "\u041a\u0440\u0438\u043f\u0435\u0440";
            case "Anchor" -> "\u042f\u043a\u043e\u0440\u044c";
            case "Crystal" -> "\u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b";
            case "Crystals" -> "\u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u044b";
            case "Crits Only Space" -> "\u041a\u0440\u0438\u0442\u044b \u0441 \u043f\u0440\u043e\u0431\u0435\u043b\u043e\u043c";
            case "Circle" -> "\u041a\u0440\u0443\u0433";
            case "Cube" -> "\u041a\u0443\u0431";
            case "Cooldowns" -> "\u041a\u0443\u043b\u0434\u0430\u0443\u043d\u044b";
            case "Cursor" -> "\u041a\u0443\u0440\u0441\u043e\u0440";
            case "Berry" -> "\u041a\u0443\u0441\u0442\u044b \u044f\u0433\u043e\u0434";
            case "Left" -> "\u041b\u041a\u041c";
            case "Lapis", "Lapis Lazuli" -> "\u041b\u0430\u0437\u0443\u0440\u0438\u0442";
            case "Ribbons" -> "\u041b\u0435\u043d\u0442\u044b";
            case "Break Shield" -> "\u041b\u043e\u043c\u0430\u0442\u044c \u0449\u0438\u0442";
            case "Media Player" -> "\u041c\u0435\u0434\u0438\u0430";
            case "Blink" -> "\u041c\u0435\u0440\u0446\u0430\u043d\u0438\u0435";
            case "Sword" -> "\u041c\u0435\u0447";
            case "Swords" -> "\u041c\u0435\u0447\u0438";
            case "Minimum" -> "\u041c\u0438\u043d\u0438\u043c\u0443\u043c";
            case "Mob", "Mobs" -> "\u041c\u043e\u0431\u044b";
            case "Carrots" -> "\u041c\u043e\u0440\u043a\u043e\u0432\u044c";
            case "Player Aim" -> "\u041d\u0430\u0432\u043e\u0434\u043a\u0430 \u0438\u0433\u0440\u043e\u043a\u0430";
            case "Anvils" -> "\u041d\u0430\u043a\u043e\u0432\u0430\u043b\u044c\u043d\u0438";
            case "Overlay" -> "\u041d\u0430\u043b\u043e\u0436\u0435\u043d\u0438\u0435";
            case "No Attack In Containers" -> "\u041d\u0435 \u0431\u0438\u0442\u044c \u0432 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u0430\u0445";
            case "No Attack When Eat" -> "\u041d\u0435 \u0431\u0438\u0442\u044c \u0432\u043e \u0432\u0440\u0435\u043c\u044f \u0435\u0434\u044b";
            case "Invisibility" -> "\u041d\u0435\u0432\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u044c";
            case "NameTag" -> "\u041d\u0435\u0439\u043c\u0442\u0435\u0433";
            case "Multiple" -> "\u041d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e";
            case "None" -> "\u041d\u0435\u0442";
            case "Low Durability" -> "\u041d\u0438\u0437\u043a\u0430\u044f \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c";
            case "Low" -> "\u041d\u0438\u0437\u043a\u0438\u0439";
            case "New" -> "\u041d\u043e\u0432\u044b\u0439";
            case "Night Vision" -> "\u041d\u043e\u0447\u043d\u043e\u0435 \u0437\u0440\u0435\u043d\u0438\u0435";
            case "Night" -> "\u041d\u043e\u0447\u044c";
            case "Block Update" -> "\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 \u0431\u043b\u043e\u043a\u043e\u0432";
            case "Updated" -> "\u041e\u0431\u043d\u043e\u0432\u043b\u0451\u043d\u043d\u044b\u0439";
            case "Default", "Normal" -> "\u041e\u0431\u044b\u0447\u043d\u044b\u0439";
            case "Block Overlay" -> "\u041e\u0432\u0435\u0440\u043b\u0435\u0439 \u0431\u043b\u043e\u043a\u043e\u0432";
            case "Fire Resistance" -> "\u041e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u044c";
            case "Fire" -> "\u041e\u0433\u043e\u043d\u044c";
            case "Single" -> "\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u044b\u0439";
            case "Orange" -> "\u041e\u0440\u0430\u043d\u0436\u0435\u0432\u0430\u044f";
            case "UnPress Shield" -> "\u041e\u0442\u0436\u0438\u043c\u0430\u0442\u044c \u0449\u0438\u0442";
            case "Open Container" -> "\u041e\u0442\u043a\u0440\u044b\u0442 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440";
            case "Right", "Right Click" -> "\u041f\u041a\u041c";
            case "Fall" -> "\u041f\u0430\u0434\u0435\u043d\u0438\u0435";
            case "Packet" -> "\u041f\u0430\u043a\u0435\u0442\u043d\u044b\u0439";
            case "Cobweb" -> "\u041f\u0430\u0443\u0442\u0438\u043d\u0430";
            case "Module Switch" -> "\u041f\u0435\u0440\u0435\u043a\u043b. \u043c\u043e\u0434\u0443\u043b\u0435\u0439";
            case "Feast" -> "\u041f\u0438\u0440";
            case "Pyramid" -> "\u041f\u0438\u0440\u0430\u043c\u0438\u0434\u0430";
            case "Plasma" -> "\u041f\u043b\u0430\u0437\u043c\u0430";
            case "Flame" -> "\u041f\u043b\u0430\u043c\u044f";
            case "Bad Effects" -> "\u041f\u043b\u043e\u0445\u0438\u0435 \u044d\u0444\u0444\u0435\u043a\u0442\u044b";
            case "Bind" -> "\u041f\u043e \u0431\u0438\u043d\u0434\u0443";
            case "Weather" -> "\u041f\u043e\u0433\u043e\u0434\u0430";
            case "Item Pick Up" -> "\u041f\u043e\u0434\u0431\u043e\u0440 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430";
            case "Private Search" -> "\u041f\u043e\u0438\u0441\u043a \u043f\u0440\u0438\u0432\u0430\u0442\u043e\u0432";
            case "Noon" -> "\u041f\u043e\u043b\u0434\u0435\u043d\u044c";
            case "FOV" -> "\u041f\u043e\u043b\u0435 \u0437\u0440\u0435\u043d\u0438\u044f";
            case "Sliders" -> "\u041f\u043e\u043b\u0437\u0443\u043d\u043a\u0438";
            case "Midnight" -> "\u041f\u043e\u043b\u043d\u043e\u0447\u044c";
            case "Fly" -> "\u041f\u043e\u043b\u0451\u0442";
            case "Crops" -> "\u041f\u043e\u0441\u0435\u0432\u044b";
            case "Emerge" -> "\u041f\u043e\u044f\u0432\u043b\u0435\u043d\u0438\u0435";
            case "Item", "Items" -> "\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b";
            case "Hand Items" -> "\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0432 \u0440\u0443\u043a\u0430\u0445";
            case "Prefix" -> "\u041f\u0440\u0435\u0444\u0438\u043a\u0441";
            case "Ghosts" -> "\u041f\u0440\u0438\u0437\u0440\u0430\u043a\u0438";
            case "3D Lines" -> "3D \u041b\u0438\u043d\u0438\u0438";
            case "Jump" -> "\u041f\u0440\u044b\u0436\u043e\u043a";
            case "Experience Bottle" -> "\u041f\u0443\u0437\u044b\u0440\u0451\u043a \u043e\u043f\u044b\u0442\u0430";
            case "Wheat" -> "\u041f\u0448\u0435\u043d\u0438\u0446\u0430";
            case "Difference" -> "\u0420\u0430\u0437\u043d\u0438\u0446\u0430";
            case "Dawn" -> "\u0420\u0430\u0441\u0441\u0432\u0435\u0442";
            case "Regeneration" -> "\u0420\u0435\u0433\u0435\u043d\u0435\u0440\u0430\u0446\u0438\u044f";
            case "Redstone" -> "\u0420\u0435\u0434\u0441\u0442\u043e\u0443\u043d";
            case "Pink" -> "\u0420\u043e\u0437\u043e\u0432\u0430\u044f";
            case "Powder Snow" -> "\u0420\u044b\u0445\u043b\u044b\u0439 \u0441\u043d\u0435\u0433";
            case "Sugar Cane" -> "\u0421\u0430\u0445\u0430\u0440\u043d\u044b\u0439 \u0442\u0440\u043e\u0441\u0442\u043d\u0438\u043a";
            case "Firefly" -> "\u0421\u0432\u0435\u0442\u043b\u044f\u0447\u043a\u0438";
            case "Glow" -> "\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435";
            case "Glowing Players" -> "\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435 \u0438\u0433\u0440\u043e\u043a\u043e\u0432";
            case "Beetroots" -> "\u0421\u0432\u0451\u043a\u043b\u0430";
            case "Self" -> "\u0421\u0435\u0431\u044f";
            case "Aurora" -> "\u0421\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u0441\u0438\u044f\u043d\u0438\u0435";
            case "Saddle" -> "\u0421\u0435\u0434\u043b\u043e";
            case "Hearts" -> "\u0421\u0435\u0440\u0434\u0435\u0447\u043a\u0438";
            case "Wither Hearts" -> "\u0421\u0435\u0440\u0434\u0435\u0447\u043a\u0438 \u0438\u0441\u0441\u0443\u0448\u0435\u043d\u0438\u044f";
            case "Network" -> "\u0421\u0435\u0442\u044c";
            case "Strength" -> "\u0421\u0438\u043b\u0430";
            case "Strength + Speed" -> "\u0421\u0438\u043b\u0430 + \u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c";
            case "Power" -> "\u0421\u0438\u043b\u044c\u043d\u0430\u044f";
            case "Strong Healing" -> "\u0421\u0438\u043b\u044c\u043d\u043e\u0435 \u0438\u0441\u0446\u0435\u043b\u0435\u043d\u0438\u0435";
            case "Sync" -> "\u0421\u0438\u043d\u0445\u0440\u043e\u043d";
            case "Blue" -> "\u0421\u0438\u043d\u044f\u044f";
            case "Scoreboard" -> "\u0421\u043a\u043e\u0440\u0431\u043e\u0440\u0434";
            case "Speed" -> "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c";
            case "Trail" -> "\u0421\u043b\u0435\u0434";
            case "Add" -> "\u0421\u043b\u043e\u0436\u0435\u043d\u0438\u0435";
            case "Mix" -> "\u0421\u043c\u0435\u0448\u0438\u0432\u0430\u043d\u0438\u0435";
            case "Snowflake" -> "\u0421\u043d\u0435\u0436\u0438\u043d\u043a\u0438";
            case "Solo" -> "\u0421\u043e\u043b\u043e";
            case "Solid" -> "\u0421\u043f\u043b\u043e\u0448\u043d\u043e\u0439";
            case "Medium" -> "\u0421\u0440\u0435\u0434\u043d\u0438\u0439";
            case "Old" -> "\u0421\u0442\u0430\u0440\u044b\u0439";
            case "Staff" -> "\u0421\u0442\u0430\u0444\u0444";
            case "Glass" -> "\u0421\u0442\u0435\u043a\u043b\u043e";
            case "Walls" -> "\u0421\u0442\u0435\u043d\u044b";
            case "Arrows" -> "\u0421\u0442\u0440\u0435\u043b\u044b";
            case "Chests" -> "\u0421\u0443\u043d\u0434\u0443\u043a\u0438";
            case "Player Head" -> "\u0421\u0444\u0435\u0440\u0430";
            case "Sphere" -> "\u0421\u0444\u0435\u0440\u044b";
            case "Tab", "TabList" -> "\u0422\u0430\u0431";
            case "Target Hud" -> "\u0422\u0430\u0440\u0433\u0435\u0442-\u0445\u0443\u0434";
            case "Current Rotation" -> "\u0422\u0435\u043a\u0443\u0449\u0430\u044f \u0440\u043e\u0442\u0430\u0446\u0438\u044f";
            case "Theme" -> "\u0422\u0435\u043c\u0430";
            case "Test" -> "\u0422\u0435\u0441\u0442";
            case "Type 1" -> "\u0422\u0438\u043f 1";
            case "Type 2" -> "\u0422\u0438\u043f 2";
            case "Only Critical" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u043a\u0440\u0438\u0442\u044b";
            case "Only Weapon" -> "\u0422\u043e\u043b\u044c\u043a\u043e \u043e\u0440\u0443\u0436\u0438\u0435";
            case "Totem Pop" -> "\u0422\u043e\u0442\u0435\u043c";
            case "Totem of Undying" -> "\u0422\u043e\u0442\u0435\u043c/\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d";
            case "Exact" -> "\u0422\u043e\u0447\u043d\u043e\u0435";
            case "Triple" -> "\u0422\u0440\u043e\u0439\u043d\u043e\u0439";
            case "Fog" -> "\u0422\u0443\u043c\u0430\u043d";
            case "Nebula" -> "\u0422\u0443\u043c\u0430\u043d\u043d\u043e\u0441\u0442\u044c";
            case "Tonnel" -> "\u0422\u0443\u043d\u043d\u0435\u043b\u044c";
            case "Warden Darkness" -> "\u0422\u044c\u043c\u0430 \u0412\u0430\u0440\u0434\u0435\u043d\u0430";
            case "Deep Blue" -> "\u0422\u0451\u043c\u043d\u043e-\u0441\u0438\u043d\u044f\u044f";
            case "Notifications" -> "\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f";
            case "Angle" -> "\u0423\u0433\u043e\u043b";
            case "Coal" -> "\u0423\u0433\u043e\u043b\u044c";
            case "Multiply" -> "\u0423\u043c\u043d\u043e\u0436\u0435\u043d\u0438\u0435";
            case "Universal" -> "\u0423\u043d\u0438\u0432\u0435\u0440\u0441\u0430\u043b\u044c\u043d\u044b\u0439";
            case "Firework", "Firework Rocket", "Fireworks" -> "\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a";
            case "Purple" -> "\u0424\u0438\u043e\u043b\u0435\u0442\u043e\u0432\u0430\u044f";
            case "Hub" -> "\u0425\u0430\u0431";
            case "Tail" -> "\u0425\u0432\u043e\u0441\u0442";
            case "Bread" -> "\u0425\u043b\u0435\u0431";
            case "HotBar" -> "\u0425\u043e\u0442\u0431\u0430\u0440";
            case "Hot Keys" -> "\u0425\u043e\u0442\u043a\u0435\u0438";
            case "Chroma" -> "\u0425\u0440\u043e\u043c\u0430";
            case "Aura Target" -> "\u0426\u0435\u043b\u044c \u043a\u0438\u043b\u043b\u0430\u0443\u0440\u044b";
            case "Particles" -> "\u0427\u0430\u0441\u0442\u0438\u0446\u044b";
            case "Block Breaking Particles" -> "\u0427\u0430\u0441\u0442\u0438\u0446\u044b \u043b\u043e\u043c\u0430\u043d\u0438\u044f \u0431\u043b\u043e\u043a\u043e\u0432";
            case "Sculk" -> "\u0421\u043a\u0430\u043b\u043a";
            case "Sculk Sensors" -> "\u0421\u043a\u0430\u043b\u043a-\u0441\u0435\u043d\u0441\u043e\u0440\u044b";
            case "Sculk Particles" -> "\u0427\u0430\u0441\u0442\u0438\u0446\u044b \u0441\u043a\u0430\u043b\u043a\u0430";
            case "Black Flash" -> "\u0427\u0451\u0440\u043d\u0430\u044f \u0432\u0441\u043f\u044b\u0448\u043a\u0430";
            case "Black Hole" -> "\u0427\u0451\u0440\u043d\u0430\u044f \u0434\u044b\u0440\u0430";
            case "Shulkers" -> "\u0428\u0430\u043b\u043a\u0435\u0440\u044b";
            case "FunTime Mine" -> "\u0428\u0430\u0445\u0442\u0430 FunTime";
            case "Sky Shader" -> "\u0428\u0435\u0439\u0434\u0435\u0440 \u043d\u0435\u0431\u0430";
            case "Shield" -> "\u0429\u0438\u0442";
            case "Screen" -> "\u042d\u043a\u0440\u0430\u043d";
            case "Elytra" -> "\u042d\u043b\u0438\u0442\u0440\u044b";
            case "Ender Pearl" -> "\u042d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433";
            case "Ender Chests" -> "\u042d\u043d\u0434\u0435\u0440-\u0441\u0443\u043d\u0434\u0443\u043a\u0438";
            case "Energy" -> "\u042d\u043d\u0435\u0440\u0433\u0438\u044f";
            default -> DropdownMenuComponent.translateWords(value);
        };
    }

    public static String translateWords(String value) {
        if (value == null) {
            return null;
        }
        return TRANSLATION_CACHE.computeIfAbsent(value, DropdownMenuComponent::translateWordsUncached);
    }

    private static String translateWordsUncached(String value) {
        if (value == null || value.isBlank() || value.chars().anyMatch(ch -> ch >= 1040 && ch <= 1103)) {
            return value;
        }
        return DropdownMenuComponent.translateKnownWords(value);
    }

    public static String translateMixedWords(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return DropdownMenuComponent.translateKnownWords(value);
    }

    private static String translateKnownWords(String value) {
        String result = value;
        String[][] words = new String[][]{{"Enabled", "\u0412\u043a\u043b\u044e\u0447\u0435\u043d\u043e"}, {"Disable", "\u041e\u0442\u043a\u043b\u044e\u0447\u0438\u0442\u044c"}, {"Show", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c"}, {"Use", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c"}, {"Only", "\u0422\u043e\u043b\u044c\u043a\u043e"}, {"When", "\u041a\u043e\u0433\u0434\u0430"}, {"Target", "\u0426\u0435\u043b\u044c"}, {"Targets", "\u0426\u0435\u043b\u0438"}, {"Player", "\u0418\u0433\u0440\u043e\u043a"}, {"Players", "\u0418\u0433\u0440\u043e\u043a\u0438"}, {"Movement", "\u0414\u0432\u0438\u0436\u0435\u043d\u0438\u0435"}, {"Rotation", "\u0420\u043e\u0442\u0430\u0446\u0438\u044f"}, {"Bow", "\u041b\u0443\u043a"}, {"Trident", "\u0422\u0440\u0435\u0437\u0443\u0431\u0435\u0446"}, {"Weapons", "\u041e\u0440\u0443\u0436\u0438\u0435"}, {"Camera", "\u041a\u0430\u043c\u0435\u0440\u0430"}, {"Attack", "\u0410\u0442\u0430\u043a\u0430"}, {"Distance", "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f"}, {"Range", "\u0414\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c"}, {"Height", "\u0412\u044b\u0441\u043e\u0442\u0430"}, {"Width", "\u0428\u0438\u0440\u0438\u043d\u0430"}, {"Radius", "\u0420\u0430\u0434\u0438\u0443\u0441"}, {"Speed", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c"}, {"Delay", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430"}, {"Duration", "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c"}, {"Chance", "\u0428\u0430\u043d\u0441"}, {"Color", "\u0426\u0432\u0435\u0442"}, {"Mode", "\u0420\u0435\u0436\u0438\u043c"}, {"Type", "\u0422\u0438\u043f"}, {"Item", "\u041f\u0440\u0435\u0434\u043c\u0435\u0442"}, {"Items", "\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b"}, {"Hand", "\u0420\u0443\u043a\u0430"}, {"Main", "\u041e\u0441\u043d\u043e\u0432\u043d\u0430\u044f"}, {"Off", "\u0412\u0442\u043e\u0440\u0430\u044f"}, {"Size", "\u0420\u0430\u0437\u043c\u0435\u0440"}, {"Scale", "\u041c\u0430\u0441\u0448\u0442\u0430\u0431"}, {"Glow", "\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435"}, {"Power", "\u0421\u0438\u043b\u0430"}, {"Alpha", "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c"}, {"Outline", "\u041e\u0431\u0432\u043e\u0434\u043a\u0430"}, {"Fill", "\u0417\u0430\u043b\u0438\u0432\u043a\u0430"}, {"Shader", "\u0428\u0435\u0439\u0434\u0435\u0440"}, {"Key", "\u041a\u043b\u0430\u0432\u0438\u0448\u0430"}, {"Auto", "\u0410\u0432\u0442\u043e"}, {"Custom", "\u041a\u0430\u0441\u0442\u043e\u043c"}, {"Minimum", "\u041c\u0438\u043d\u0438\u043c\u0443\u043c"}, {"Maximum", "\u041c\u0430\u043a\u0441\u0438\u043c\u0443\u043c"}, {"Min", "\u041c\u0438\u043d."}, {"Max", "\u041c\u0430\u043a\u0441."}, {"Smooth", "\u041f\u043b\u0430\u0432\u043d\u0430\u044f"}, {"Physics", "\u0424\u0438\u0437\u0438\u043a\u0430"}, {"World", "\u041c\u0438\u0440"}, {"Render", "\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435"}, {"Effects", "\u042d\u0444\u0444\u0435\u043a\u0442\u044b"}, {"Armor", "\u0411\u0440\u043e\u043d\u044f"}, {"Health", "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435"}, {"Damage", "\u0423\u0440\u043e\u043d"}, {"Check", "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430"}, {"Ignore", "\u0418\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c"}, {"Through", "\u0427\u0435\u0440\u0435\u0437"}, {"Walls", "\u0421\u0442\u0435\u043d\u044b"}, {"Inventory", "\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c"}, {"Background", "\u0424\u043e\u043d"}, {"Find", "\u041f\u043e\u0438\u0441\u043a"}, {"Lead", "\u0423\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0435"}, {"Forward", "\u0412\u043f\u0435\u0440\u0451\u0434"}, {"Delayed", "\u0417\u0430\u0434\u0435\u0440\u0436\u0430\u043d\u043d\u044b\u0439"}, {"Box", "\u0411\u043e\u043a\u0441"}, {"FOV", "\u041f\u043e\u043b\u0435 \u0437\u0440\u0435\u043d\u0438\u044f"}, {"Rocket", "\u0420\u0430\u043a\u0435\u0442\u0430"}, {"Firework", "\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a"}, {"Pitch", "\u041d\u0430\u043a\u043b\u043e\u043d"}, {"TNT", "\u0422\u0440\u043e\u0442\u0438\u043b"}, {"AI", "\u0418\u0418"}};
        String[][] extraWords = new String[][]{{"Aim Reset", "\u041e\u0442\u0432\u043e\u0434\u043a\u0430"}, {"Priority", "\u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442"}, {"Mace", "\u0411\u0443\u043b\u0430\u0432\u0430"}, {"Separate", "\u0420\u0430\u0437\u0434\u0435\u043b\u044f\u0442\u044c"}, {"Watermark", "\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a"}, {"Language", "\u042f\u0437\u044b\u043a"}, {"Line", "\u041b\u0438\u043d\u0438\u044f"}, {"Threshold", "\u041f\u043e\u0440\u043e\u0433"}, {"Triggers", "\u0423\u0441\u043b\u043e\u0432\u0438\u044f"}, {"Trigger", "\u0423\u0441\u043b\u043e\u0432\u0438\u0435"}, {"Keep", "\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0442\u044c"}, {"Enchanted", "\u0417\u0430\u0447\u0430\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435"}, {"Totems", "\u0422\u043e\u0442\u0435\u043c\u044b"}, {"Eating", "\u0435\u0434\u044b"}, {"While", "\u0432\u043e \u0432\u0440\u0435\u043c\u044f"}, {"Solid", "\u0421\u043f\u043b\u043e\u0448\u043d\u043e\u0439"}, {"Select", "\u0412\u044b\u0431\u0440\u0430\u0442\u044c"}, {"Selects", "\u0412\u044b\u0431\u0438\u0440\u0430\u0435\u0442"}, {"Allows", "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442"}, {"Enable", "\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c"}, {"Automatically", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438"}, {"Additional", "\u0414\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f"}, {"Scanner", "\u0421\u043a\u0430\u043d\u0435\u0440"}, {"Ores", "\u0420\u0443\u0434\u044b"}, {"Ore", "\u0420\u0443\u0434\u0430"}, {"Container", "\u041a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440"}, {"Containers", "\u041a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u044b"}, {"Interaction", "\u0412\u0437\u0430\u0438\u043c\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435"}, {"Volume", "\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c"}, {"Sounds", "\u0417\u0432\u0443\u043a\u0438"}, {"Sound", "\u0417\u0432\u0443\u043a"}, {"Animation", "\u0410\u043d\u0438\u043c\u0430\u0446\u0438\u044f"}, {"Animated", "\u0410\u043d\u0438\u043c\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0439"}, {"Appearance", "\u0412\u0438\u0434"}, {"Behavior", "\u041f\u043e\u0432\u0435\u0434\u0435\u043d\u0438\u0435"}, {"Amount", "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e"}, {"First", "\u041f\u0435\u0440\u0432\u044b\u0439"}, {"Second", "\u0412\u0442\u043e\u0440\u043e\u0439"}, {"Triple", "\u0422\u0440\u043e\u0439\u043d\u043e\u0439"}, {"Sphere", "\u0421\u0444\u0435\u0440\u0430"}, {"Count", "\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e"}, {"Strength", "\u0421\u0438\u043b\u0430"}, {"Intensity", "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c"}, {"Brightness", "\u042f\u0440\u043a\u043e\u0441\u0442\u044c"}, {"Shown", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u043c\u044b\u0435"}, {"Displayed", "\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0435\u043c\u044b\u0435"}, {"Around", "\u0412\u043e\u043a\u0440\u0443\u0433"}, {"Seconds", "\u0421\u0435\u043a\u0443\u043d\u0434\u044b"}, {"Anarchy", "\u0410\u043d\u0430\u0440\u0445\u0438\u044f"}, {"Command", "\u041a\u043e\u043c\u0430\u043d\u0434\u0430"}, {"Fix", "\u0418\u0441\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435"}, {"Slowdown", "\u0417\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u0435"}, {"Loot", "\u041b\u0443\u0442"}, {"Repair", "\u041f\u043e\u0447\u0438\u043d\u043a\u0430"}, {"Parse", "\u041f\u0430\u0440\u0441\u0438\u043d\u0433"}, {"Point", "\u041c\u0435\u0442\u043a\u0430"}, {"Shulker", "\u0428\u0430\u043b\u043a\u0435\u0440"}, {"Crossbow", "\u0410\u0440\u0431\u0430\u043b\u0435\u0442"}, {"Background", "\u0424\u043e\u043d"}, {"Blend", "\u0421\u043c\u0435\u0448\u0438\u0432\u0430\u043d\u0438\u0435"}, {"Saturation", "\u041d\u0430\u0441\u044b\u0449\u0435\u043d\u043d\u043e\u0441\u0442\u044c"}, {"Chromatic", "\u0425\u0440\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0430\u044f"}, {"Aberration", "\u0410\u0431\u0435\u0440\u0440\u0430\u0446\u0438\u044f"}, {"Chunk", "\u0427\u0430\u043d\u043a"}, {"Clear", "\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c"}, {"Cache", "\u041a\u044d\u0448"}, {"Click", "\u041a\u043b\u0438\u043a"}, {"Close", "\u0417\u0430\u043a\u0440\u044b\u0432\u0430\u0442\u044c"}, {"Screen", "\u042d\u043a\u0440\u0430\u043d"}, {"Correction", "\u041a\u043e\u0440\u0440\u0435\u043a\u0446\u0438\u044f"}, {"Creeper", "\u041a\u0440\u0438\u043f\u0435\u0440"}, {"Ratio", "\u0421\u043e\u043e\u0442\u043d\u043e\u0448\u0435\u043d\u0438\u0435"}, {"Debug", "\u041e\u0442\u043b\u0430\u0434\u043e\u0447\u043d\u044b\u0435"}, {"Detect", "\u041e\u043f\u0440\u0435\u0434\u0435\u043b\u044f\u0442\u044c"}, {"Eating", "\u0415\u0434\u0430"}, {"Elytra", "\u042d\u043b\u0438\u0442\u0440\u044b"}, {"Entity", "\u0421\u0443\u0449\u043d\u043e\u0441\u0442\u044c"}, {"Experience", "\u041e\u043f\u044b\u0442"}, {"Extra", "\u0414\u043e\u043f."}, {"Fall", "\u041f\u0430\u0434\u0435\u043d\u0438\u0435"}, {"Firefly", "\u0421\u0432\u0435\u0442\u043b\u044f\u0447\u043a\u0438"}, {"Forward", "\u0412\u043f\u0435\u0440\u0435\u0434"}, {"Ghost", "\u041f\u0440\u0438\u0437\u0440\u0430\u043a"}, {"Hunger", "\u0413\u043e\u043b\u043e\u0434"}, {"Naked", "\u0411\u0435\u0437 \u0431\u0440\u043e\u043d\u0438"}, {"Invisible", "\u041d\u0435\u0432\u0438\u0434\u0438\u043c\u044b\u0435"}, {"Links", "\u0421\u0432\u044f\u0437\u0438"}, {"Link", "\u0421\u0432\u044f\u0437\u044c"}, {"Health", "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435"}, {"Micro", "\u041c\u0438\u043a\u0440\u043e"}, {"Ground", "\u0417\u0435\u043c\u043b\u044f"}, {"Idle", "\u0411\u0435\u0437\u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435"}, {"Panel", "\u041f\u0430\u043d\u0435\u043b\u044c"}, {"Plast", "\u041f\u043b\u0430\u0441\u0442"}, {"Quick", "\u0411\u044b\u0441\u0442\u0440\u044b\u0439"}, {"Start", "\u0421\u0442\u0430\u0440\u0442"}, {"Randomize", "\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u043e"}, {"Refresh", "\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435"}, {"Regular", "\u041e\u0431\u044b\u0447\u043d\u044b\u0435"}, {"Restore", "\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435"}, {"Server", "\u0421\u0435\u0440\u0432\u0435\u0440"}, {"Slot", "\u0421\u043b\u043e\u0442"}, {"Slots", "\u0421\u043b\u043e\u0442\u044b"}, {"Smoke", "\u0414\u044b\u043c"}, {"Snap", "\u0420\u044b\u0432\u043e\u043a"}, {"Snow", "\u0421\u043d\u0435\u0433"}, {"Source", "\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a"}, {"Sources", "\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a\u0438"}, {"Spawn", "\u0421\u043f\u0430\u0432\u043d"}, {"Sprint", "\u0421\u043f\u0440\u0438\u043d\u0442"}, {"Stealer", "\u041b\u0443\u0442\u0435\u0440"}, {"Stripe", "\u041f\u043e\u043b\u043e\u0441\u0430"}, {"Throw", "\u0411\u0440\u043e\u0441\u043e\u043a"}, {"Time", "\u0412\u0440\u0435\u043c\u044f"}, {"Trap", "\u041b\u043e\u0432\u0443\u0448\u043a\u0430"}, {"Work", "\u0420\u0430\u0431\u043e\u0442\u0430"}, {"Zoom", "\u0417\u0443\u043c"}, {"Fold", "\u0421\u043b\u043e\u0436\u0438\u0442\u044c"}, {"Button", "\u041a\u043d\u043e\u043f\u043a\u0430"}, {"Durability", "\u041f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c"}, {"Indent", "\u041e\u0442\u0441\u0442\u0443\u043f"}, {"Rotate", "\u0412\u0440\u0430\u0449\u0430\u0442\u044c"}, {"Freeze", "\u0417\u0430\u043c\u043e\u0440\u043e\u0437\u043a\u0430"}, {"Blocks", "\u0411\u043b\u043e\u043a\u0438"}, {"Phrase", "\u0424\u0440\u0430\u0437\u0430"}, {"Confirm", "\u041f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u0435"}, {"Cooldown", "\u041a\u0443\u043b\u0434\u0430\u0443\u043d"}, {"Buy", "\u041f\u043e\u043a\u0443\u043f\u043a\u0430"}, {"Profit", "\u041f\u0440\u0438\u0431\u044b\u043b\u044c"}, {"Value", "\u0426\u0435\u043d\u0430"}, {"Settings", "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438"}, {"Setting", "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430"}, {"All", "\u0412\u0441\u0435"}, {"Always", "\u0412\u0441\u0435\u0433\u0434\u0430"}, {"Ancient", "\u0414\u0440\u0435\u0432\u043d\u0438\u0435"}, {"Debris", "\u041e\u0431\u043b\u043e\u043c\u043a\u0438"}, {"Animal", "\u0416\u0438\u0432\u043e\u0442\u043d\u043e\u0435"}, {"Animals", "\u0416\u0438\u0432\u043e\u0442\u043d\u044b\u0435"}, {"Anvils", "\u041d\u0430\u043a\u043e\u0432\u0430\u043b\u044c\u043d\u0438"}, {"Arrows", "\u0421\u0442\u0440\u0435\u043b\u044b"}, {"Bad", "\u0412\u0440\u0435\u0434\u043d\u044b\u0435"}, {"Barrels", "\u0411\u043e\u0447\u043a\u0438"}, {"Berry", "\u042f\u0433\u043e\u0434\u0430"}, {"Black", "\u0427\u0451\u0440\u043d\u0430\u044f"}, {"Hole", "\u0434\u044b\u0440\u0430"}, {"Block", "\u0411\u043b\u043e\u043a"}, {"Breaking", "\u0420\u0430\u0437\u0440\u0443\u0448\u0435\u043d\u0438\u0435"}, {"Update", "\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435"}, {"Boss", "\u0411\u043e\u0441\u0441"}, {"Bar", "\u043f\u0430\u043d\u0435\u043b\u044c"}, {"Bread", "\u0425\u043b\u0435\u0431"}, {"Buttons", "\u041a\u043d\u043e\u043f\u043a\u0438"}, {"ChestPlate", "\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a"}, {"Chests", "\u0421\u0443\u043d\u0434\u0443\u043a\u0438"}, {"Coal", "\u0423\u0433\u043e\u043b\u044c"}, {"Cobweb", "\u041f\u0430\u0443\u0442\u0438\u043d\u0430"}, {"Copper", "\u041c\u0435\u0434\u044c"}, {"Crystals", "\u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u044b"}, {"Crystal", "\u041a\u0440\u0438\u0441\u0442\u0430\u043b\u043b"}, {"Diamond", "\u0410\u043b\u043c\u0430\u0437"}, {"Difference", "\u0420\u0430\u0437\u043d\u0438\u0446\u0430"}, {"Donation", "\u0414\u043e\u043d\u0430\u0442\u043d\u044b\u0435"}, {"Potions", "\u0417\u0435\u043b\u044c\u044f"}, {"Easing", "\u0421\u0433\u043b\u0430\u0436\u0438\u0432\u0430\u043d\u0438\u0435"}, {"Effects", "\u042d\u0444\u0444\u0435\u043a\u0442\u044b"}, {"Eggs", "\u042f\u0439\u0446\u0430"}, {"Emerald", "\u0418\u0437\u0443\u043c\u0440\u0443\u0434"}, {"Ender", "\u042d\u043d\u0434\u0435\u0440"}, {"Bottle", "\u041f\u0443\u0437\u044b\u0440\u0451\u043a"}, {"Furnaces", "\u041f\u0435\u0447\u0438"}, {"Glowing", "\u0421\u0432\u0435\u0442\u044f\u0449\u0438\u0435\u0441\u044f"}, {"Gold", "\u0417\u043e\u043b\u043e\u0442\u043e"}, {"Golden", "\u0417\u043e\u043b\u043e\u0442\u043e\u0435"}, {"Apple", "\u042f\u0431\u043b\u043e\u043a\u043e"}, {"Carrot", "\u041c\u043e\u0440\u043a\u043e\u0432\u044c"}, {"Ingot", "\u0421\u043b\u0438\u0442\u043e\u043a"}, {"Hand", "\u0420\u0443\u043a\u0430"}, {"Hearts", "\u0421\u0435\u0440\u0434\u0446\u0430"}, {"Items", "\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b"}, {"Jump", "\u041f\u0440\u044b\u0436\u043e\u043a"}, {"Lapis", "\u041b\u0430\u0437\u0443\u0440\u0438\u0442"}, {"Left", "\u0412\u043b\u0435\u0432\u043e"}, {"Lightning", "\u041c\u043e\u043b\u043d\u0438\u044f"}, {"Low", "\u041d\u0438\u0437\u043a\u0430\u044f"}, {"Menu", "\u041c\u0435\u043d\u044e"}, {"Mob", "\u041c\u043e\u0431"}, {"Mobs", "\u041c\u043e\u0431\u044b"}, {"Naked", "\u0411\u0435\u0437 \u0431\u0440\u043e\u043d\u0438"}, {"Name", "\u0418\u043c\u044f"}, {"Tag", "\u0422\u0435\u0433"}, {"Nether", "\u041d\u0435\u0437\u0435\u0440"}, {"Netherite", "\u041d\u0435\u0437\u0435\u0440\u0438\u0442"}, {"Boots", "\u0411\u043e\u0442\u0438\u043d\u043a\u0438"}, {"Helmet", "\u0428\u043b\u0435\u043c"}, {"Leggings", "\u041f\u043e\u043d\u043e\u0436\u0438"}, {"Scrap", "\u041b\u043e\u043c"}, {"Sword", "\u041c\u0435\u0447"}, {"Network", "\u0421\u0435\u0442\u044c"}, {"Night", "\u041d\u043e\u0447\u044c"}, {"Vision", "\u0417\u0440\u0435\u043d\u0438\u0435"}, {"None", "\u041d\u0435\u0442"}, {"Normal", "\u041e\u0431\u044b\u0447\u043d\u044b\u0439"}, {"Overlay", "\u041e\u0432\u0435\u0440\u043b\u0435\u0439"}, {"Powder", "\u0420\u044b\u0445\u043b\u044b\u0439"}, {"Prefix", "\u041f\u0440\u0435\u0444\u0438\u043a\u0441"}, {"Private", "\u041f\u0440\u0438\u0432\u0430\u0442\u043d\u044b\u0439"}, {"Search", "\u041f\u043e\u0438\u0441\u043a"}, {"Quartz", "\u041a\u0432\u0430\u0440\u0446"}, {"Random", "\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u044b\u0439"}, {"Redstone", "\u0420\u0435\u0434\u0441\u0442\u043e\u0443\u043d"}, {"Right", "\u0412\u043f\u0440\u0430\u0432\u043e"}, {"Saddle", "\u0421\u0435\u0434\u043b\u043e"}, {"Scoreboard", "\u0421\u043a\u043e\u0440\u0431\u043e\u0440\u0434"}, {"Self", "\u0421\u0435\u0431\u044f"}, {"Shield", "\u0429\u0438\u0442"}, {"Shrink", "\u0421\u0436\u0430\u0442\u0438\u0435"}, {"Shulkers", "\u0428\u0430\u043b\u043a\u0435\u0440\u044b"}, {"Sparks", "\u0418\u0441\u043a\u0440\u044b"}, {"Spheres", "\u0421\u0444\u0435\u0440\u044b"}, {"Staff", "\u041c\u043e\u0434\u0435\u0440\u0430\u0446\u0438\u044f"}, {"Swing", "\u0412\u0437\u043c\u0430\u0445"}, {"Talismans", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d\u044b"}, {"Valuable", "\u0426\u0435\u043d\u043d\u044b\u0435"}, {"Vertex", "\u0412\u0435\u0440\u0448\u0438\u043d\u044b"}, {"Villager", "\u0416\u0438\u0442\u0435\u043b\u044c"}, {"Villagers", "\u0416\u0438\u0442\u0435\u043b\u0438"}, {"Walls", "\u0421\u0442\u0435\u043d\u044b"}, {"Warden", "\u0412\u0430\u0440\u0434\u0435\u043d"}, {"Darkness", "\u0422\u0435\u043c\u043d\u043e\u0442\u0430"}, {"Water", "\u0412\u043e\u0434\u0430"}, {"Bucket", "\u0412\u0435\u0434\u0440\u043e"}, {"Weapons", "\u041e\u0440\u0443\u0436\u0438\u0435"}, {"Weather", "\u041f\u043e\u0433\u043e\u0434\u0430"}};
        for (String[] pair : words) {
            result = result.replaceAll("(?i)\\b" + Pattern.quote(pair[0]) + "\\b", pair[1]);
        }
        for (String[] pair : extraWords) {
            result = result.replaceAll("(?i)\\b" + Pattern.quote(pair[0]) + "\\b", pair[1]);
        }
        return result;
    }

    private int accent() {
        return MenuStyle.accent();
    }

    private String categoryIcon(ModuleCategory category) {
        return switch (category) {
            default -> throw new MatchException(null, null);
            case ModuleCategory.COMBAT -> "\ue052";
            case ModuleCategory.MOVEMENT -> "\ue034";
            case ModuleCategory.PLAYER -> "\ue079";
            case ModuleCategory.RENDER -> "\ue009";
            case ModuleCategory.MISC -> "\ue017";
        };
    }

    private Bounds stateIconBounds(Bounds row) {
        return new Bounds(row.x + row.width - 17.0f, row.y + 2.0f, 17.0f, 18.0f);
    }

    private record Bounds(float x, float y, float width, float height) {
        boolean hover(double mouseX, double mouseY) {
            return MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
        }
    }

    private record ProfileChoiceBounds(Setting setting, String value, Bounds bounds) {
    }

    private record CosmeticItemBounds(SelectSetting setting, String value, Bounds bounds) {
    }

    private record ThemeBounds(String name, Bounds bounds) {
    }

    private static final class ModuleMotion {
        private float x;
        private float y;
        private float fromX;
        private float fromY;
        private float targetX;
        private float targetY;
        private long started;

        private ModuleMotion(float x, float y) {
            this.fromX = this.targetX = x;
            this.x = this.targetX;
            this.fromY = this.targetY = y;
            this.y = this.targetY;
        }

        private void moveTo(float nextX, float nextY, long now, boolean reorder) {
            this.update(now);
            if (reorder && (Math.abs(nextX - this.targetX) > 0.01f || Math.abs(nextY - this.targetY) > 0.01f)) {
                this.fromX = this.x;
                this.fromY = this.y;
                this.started = now;
            }
            this.targetX = nextX;
            this.targetY = nextY;
            if (this.started == 0L) {
                this.x = this.targetX;
                this.y = this.targetY;
            } else {
                this.update(now);
            }
        }

        private void update(long now) {
            if (this.started == 0L) {
                return;
            }
            float t = class_3532.method_15363((float)((float)(now - this.started) / 2.0E8f), (float)0.0f, (float)1.0f);
            float eased = t * t * (3.0f - 2.0f * t);
            this.x = class_3532.method_16439((float)eased, (float)this.fromX, (float)this.targetX);
            this.y = class_3532.method_16439((float)eased, (float)this.fromY, (float)this.targetY);
            if (t >= 1.0f) {
                this.started = 0L;
            }
        }

        private boolean isMoving() {
            return this.started != 0L;
        }
    }

    private record ChipBounds(String value, Bounds bounds, boolean multi) {
    }
}

