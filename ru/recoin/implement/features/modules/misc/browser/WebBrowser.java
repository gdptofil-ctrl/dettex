package ru.recoin.implement.features.modules.misc.browser;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10799;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import org.joml.Quaternionf;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.logger.implement.MinecraftLogger;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.events.keyboard.HotBarScrollEvent;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;
import ru.recoin.implement.events.render.WorldRenderEvent;

/**
 * Browser: screens hanging in the world, several at once. Each one is a web page (Chromium through
 * the Rinku mod) or the live window of a program from your desktop (Windows).
 *
 * Look at a screen and use the mouse right in the world: left / right click, hold to drag, wheel to
 * scroll. Type Key sends the keyboard into the screen you look at. Open Key opens the address bar
 * for a web screen. Works without anything, through a VPN, or through a proxy (Network).
 */
public final class WebBrowser
extends Module {
    public static final String SEARCH = "https://www.google.com/search?q=%s";
    private static final MinecraftLogger CHAT = new MinecraftLogger();
    private static final double REACH = 24.0;

    private final SelectSetting mode = new SelectSetting("Mode", "In World: screens hanging in the air; On Screen: one window on your HUD").value("In World", "On Screen").selected("In World");
    private final TextSetting homePage = new TextSetting("Home Page", "Page a new web screen opens").setText("https://www.google.com").setMax(256);
    private final BooleanSetting openOnEnable = new BooleanSetting("Open On Enable", "Turning the module on puts a Google screen in front of you").setValue(true);
    private final BooleanSetting worldMouse = new BooleanSetting("Mouse In World", "Look at a screen and click / scroll it right in the world").setValue(true);
    private final BooleanSetting autoType = new BooleanSetting("Auto Keyboard", "Clicking a search bar / text field on a page sends the keyboard into it (Esc to stop)").setValue(true);
    private final SelectSetting resolution = new SelectSetting("Resolution", "Sharpness of web pages (higher = more GPU)").value("720p", "1080p", "480p").selected("720p");
    private final ValueSetting maxScreens = new ValueSetting("Max Screens", "How many screens can be open at once").setValue(4.0f).range(1, 8);
    private final ValueSetting appFps = new ValueSetting("App FPS", "How often program windows are refreshed").setValue(20.0f).range(5, 60);
    // keys
    private final BindSetting openKey = new BindSetting("Address Key", "Address bar, back / forward for the web screen you look at");
    private final BindSetting typeKey = new BindSetting("Type Key", "Type into the screen you look at (Esc to stop)");
    private final BindSetting newKey = new BindSetting("New Screen Key", "Opens one more web screen in front of you");
    private final BindSetting appsKey = new BindSetting("Apps Key", "Put an open window or a program from the desktop on a screen");
    private final BindSetting closeKey = new BindSetting("Close Screen Key", "Closes the screen you look at");
    private final BindSetting placeKey = new BindSetting("Place Here Key", "Moves the screen you use in front of you");
    private final GroupSetting keys = new GroupSetting("Keys", "Keys of the screens", false).settings(this.openKey, this.typeKey, this.newKey, this.appsKey, this.closeKey, this.placeKey);
    // network
    private final SelectSetting proxyMode = new SelectSetting("Proxy", "Auto: Windows proxy / VPN; Manual: your proxy; Direct: none").value("Auto", "Manual", "Direct").selected("Auto");
    private final TextSetting proxyAddress = new TextSetting("Proxy Address", "e.g. socks5://127.0.0.1:1080 or http://user:pass@host:8080").setText("").setMax(256).visible(() -> this.proxyMode.isSelected("Manual"));
    private final GroupSetting network = new GroupSetting("Network", "Internet: direct, VPN or proxy", false).settings(this.proxyMode, this.proxyAddress);
    // placement
    private final ValueSetting worldWidth = new ValueSetting("Screen Width", "Width of a new screen in the world, in blocks").setValue(3.2f).range(1.0f, 10.0f).visible(() -> this.mode.isSelected("In World"));
    private final ValueSetting distance = new ValueSetting("Distance", "How far in front of you a screen appears").setValue(3.0f).range(1.5f, 10.0f).visible(() -> this.mode.isSelected("In World"));
    private final BooleanSetting followLook = new BooleanSetting("Follow Me", "The screen you use floats in front of you").setValue(false).visible(() -> this.mode.isSelected("In World"));
    private final SelectSetting corner = new SelectSetting("Corner", "Where the window sits on the HUD").value("Top Right", "Top Left", "Bottom Right", "Bottom Left", "Center").selected("Top Right").visible(() -> this.mode.isSelected("On Screen"));
    private final ValueSetting screenSize = new ValueSetting("Window Size %", "Width of the window, % of the screen").setValue(35.0f).range(15, 90).visible(() -> this.mode.isSelected("On Screen"));
    private final ValueSetting opacity = new ValueSetting("Opacity", "Transparency of the page").setValue(100.0f).range(20, 100);
    private final GroupSetting placement = new GroupSetting("Placement", "Where the screens are", false).settings(this.worldWidth, this.distance, this.followLook, this.corner, this.screenSize, this.opacity);
    // frame
    private final SelectSetting frameStyle = new SelectSetting("Frame", "Frame style").value("Theme", "Winter", "Halloween", "Rainbow", "Neon", "Custom").selected("Theme");
    private final ColorSetting colorA = new ColorSetting("Frame Color 1", "Top colour of the frame").value(0xFF4B8BFF).visible(() -> this.frameStyle.isSelected("Custom"));
    private final ColorSetting colorB = new ColorSetting("Frame Color 2", "Bottom colour of the frame").value(0xFFB15CFF).visible(() -> this.frameStyle.isSelected("Custom"));
    private final ValueSetting frameWidth = new ValueSetting("Frame Width", "Thickness of the frame in the world, in blocks").setValue(0.08f).range(0.0f, 0.4f);
    private final BooleanSetting glow = new BooleanSetting("Glow", "Glowing outline").setValue(true);
    private final BooleanSetting decorations = new BooleanSetting("Decorations", "Snowflakes in Winter, pumpkins and bats in Halloween").setValue(true);
    private final GroupSetting frameGroup = new GroupSetting("Frame", "Look of the frame", false).settings(this.frameStyle, this.colorA, this.colorB, this.frameWidth, this.glow, this.decorations);

    private final List<Panel> panels = new ArrayList<>();
    private Panel focused;
    // what the crosshair points at (render thread)
    private Panel hovered;
    private double hoverU;
    private double hoverV;
    private int lastMoveX = -1;
    private int lastMoveY = -1;
    // mouse button held on a screen
    private Panel pressed;
    private int pressedButton = -1;

    private long nextRetry;
    private int retries;
    private String lastError;
    private boolean warnedNoPanels;
    private long autoTypedClick;

    public WebBrowser() {
        super("Browser", "Browser", ModuleCategory.MISC);
        this.setup(this.mode, this.homePage, this.openOnEnable, this.worldMouse, this.autoType, this.resolution, this.maxScreens, this.appFps, this.keys, this.network, this.placement, this.frameGroup);
        BrowserNet.init();   // the saved proxy choice is needed before Chromium starts
    }

    private static WebBrowser instance;

    public static WebBrowser get() {
        return instance;
    }

    {
        instance = this;
    }

    void notice(String text) {
        try {
            CHAT.minecraftLog(new class_2561[]{class_2561.method_43470("§b[Browser] §f" + text)});
        } catch (Throwable ignored) {
        }
    }

    BrowserFrame.Style style() {
        return new BrowserFrame.Style(this.frameStyle.getSelected(), this.colorA.getColor(), this.colorB.getColor(), this.glow.isValue(), this.decorations.isValue());
    }

    int[] pageResolution() {
        return switch (this.resolution.getSelected()) {
            case "1080p" -> new int[]{1920, 1080};
            case "480p" -> new int[]{854, 480};
            default -> new int[]{1280, 720};
        };
    }

    String home() {
        String h = this.homePage.getText();
        return h == null || h.isBlank() ? "https://www.google.com" : BrowserBridge.toUrl(h, SEARCH);
    }

    private void syncNetwork() {
        BrowserNet.configure(BrowserNet.parseMode(this.proxyMode.getSelected()), this.proxyAddress.getText());
    }

    // ------------------------------------------------------------------ screens

    /** {anchor x, y, z, yaw} for a new screen in front of the player. */
    private Object[] front() {
        class_746 p = class_310.method_1551().field_1724;
        if (p == null) return null;
        float yaw = p.method_36454();
        double r = Math.toRadians(yaw);
        class_243 eye = p.method_33571();
        double d = this.distance.getValue();
        return new Object[]{eye.method_1031(-Math.sin(r) * d, 0.0, Math.cos(r) * d), yaw};
    }

    private boolean roomForOne() {
        if (this.panels.size() < Math.round(this.maxScreens.getValue())) return true;
        this.notice("Уже открыто " + this.panels.size() + " экранов (Max Screens). Закрой один (Close Screen Key) или увеличь лимит.");
        return false;
    }

    /** A new web screen in front of you (url null = home page). */
    public BrowserPanel addBrowserPanel(String url) {
        if (!BrowserBridge.installed()) {
            this.installRinku();
            return null;
        }
        Object[] f = this.front();
        if (f == null || !this.roomForOne()) return null;
        BrowserPanel p = new BrowserPanel(this, url == null ? this.home() : url, (class_243)f[0], (Float)f[1], this.worldWidth.getValue());
        this.panels.add(p);
        this.focused = p;
        return p;
    }

    /** Puts an open window, or a program it starts first, on a screen in front of you. */
    public void addApp(AppCatalog.Entry e) {
        if (!AppCatalog.supported()) {
            this.notice("Окна программ работают только на Windows.");
            return;
        }
        Object[] f = this.front();
        if (f == null || !this.roomForOne()) return;
        if (e.isWindow()) {
            this.addAppWindow(e.hwnd(), e.name(), (class_243)f[0], (Float)f[1]);
            return;
        }
        this.notice("Запускаю «" + e.name() + "», жду его окно...");
        class_310 mc = class_310.method_1551();
        AppCatalog.launch(e, w -> mc.execute(() -> {
            if (!this.isState()) return;
            if (w == null) {
                this.notice("Окно «" + e.name() + "» не появилось. Если программа открылась, добавь её во вкладке «Открытые окна».");
            } else if (this.roomForOne()) {
                this.addAppWindow(w.hwnd(), w.title(), (class_243)f[0], (Float)f[1]);
            }
        }));
    }

    private void addAppWindow(long hwnd, String name, class_243 anchor, float yaw) {
        for (Panel p : this.panels) {
            if (p instanceof AppPanel a && a.hwnd() == hwnd) {
                this.focused = p;
                this.notice("«" + name + "» уже на экране.");
                return;
            }
        }
        AppPanel p = new AppPanel(hwnd, name, Math.round(this.appFps.getValue()), anchor, yaw, this.worldWidth.getValue());
        this.panels.add(p);
        this.focused = p;
        this.notice("«" + p.title() + "» на экране. Смотри на него и кликай; " + this.keyName(this.typeKey, "Type Key") + " - печатать.");
    }

    public void closePanel(Panel p) {
        if (p == null) return;
        p.close();
        this.panels.remove(p);
        if (this.focused == p) this.focused = this.panels.isEmpty() ? null : this.panels.get(this.panels.size() - 1);
        if (this.hovered == p) this.hovered = null;
        if (this.pressed == p) this.pressed = null;
    }

    private void placeInFront(Panel p) {
        Object[] f = this.front();
        if (p == null || f == null) return;
        p.anchor = (class_243)f[0];
        p.yaw = (Float)f[1];
    }

    /** The screen you look at, else the last one you used. */
    private Panel target() {
        return this.hovered != null ? this.hovered : this.focused;
    }

    private String keyName(BindSetting b, String fallback) {
        return b.getKey() > 0 ? "клавиша " + fallback : fallback + " (назначь в настройках)";
    }

    private void installRinku() {
        if (RinkuInstaller.pendingRestart()) {
            this.notice("Rinku уже скачан, перезапусти игру.");
        } else {
            RinkuInstaller.install(this::notice);   // downloads Rinku into mods, works after a restart
        }
    }

    @Override
    public void activate() {
        this.syncNetwork();
        if (!BrowserBridge.installed()) {
            this.installRinku();
        } else if (BrowserBridge.downloadFailed()) {
            this.nextRetry = 0L;   // retry right away
        } else if (!BrowserBridge.ready()) {
            this.notice("Chromium загружается. В первый раз Rinku скачивает его (около 200 МБ), это займёт пару минут.");
        }
        this.warnedNoPanels = false;
        if (this.panels.isEmpty() && this.openOnEnable.isValue() && BrowserBridge.installed()) {
            this.addBrowserPanel(null);
        }
    }

    @Override
    public void deactivate() {
        for (Panel p : new ArrayList<>(this.panels)) p.close();
        this.panels.clear();
        this.focused = null;
        this.hovered = null;
        this.pressed = null;
    }

    // ------------------------------------------------------------------ input in the world

    /** Mouse button in the game (no menu open). True = it went into a screen, the game ignores it. */
    public boolean onWorldMouse(int button, int action) {
        if (!this.mode.isSelected("In World") || !this.worldMouse.isValue()) return false;
        if (action == 1) {
            Panel p = this.hovered;
            if (p == null || this.pressed != null) return this.pressed != null;
            int[] px = this.pixel(p, this.hoverU, this.hoverV);
            this.pressed = p;
            this.pressedButton = button;
            this.focused = p;
            p.mousePress(px[0], px[1], button);
            return true;
        }
        if (action == 0 && this.pressed != null && button == this.pressedButton) {
            Panel p = this.pressed;
            int[] px = p == this.hovered ? this.pixel(p, this.hoverU, this.hoverV) : new int[]{this.lastMoveX, this.lastMoveY};
            p.mouseRelease(px[0], px[1], button);
            this.pressed = null;
            this.pressedButton = -1;
            return true;
        }
        return false;
    }

    /** Finishes a click held on a screen (before a menu opens, its release would never reach the page). */
    private void releasePressed() {
        if (this.pressed != null) {
            this.pressed.mouseRelease(this.lastMoveX, this.lastMoveY, this.pressedButton);
            this.pressed = null;
            this.pressedButton = -1;
        }
    }

    private int[] pixel(Panel p, double u, double v) {
        int x = (int)Math.max(0, Math.min(p.pixelWidth() - 1, u * p.pixelWidth()));
        int y = (int)Math.max(0, Math.min(p.pixelHeight() - 1, v * p.pixelHeight()));
        return new int[]{x, y};
    }

    @EventHandler
    public void onScroll(HotBarScrollEvent event) {
        Panel p = this.hovered;
        if (p == null || !this.worldMouse.isValue() || class_310.method_1551().field_1755 != null) return;
        int[] px = this.pixel(p, this.hoverU, this.hoverV);
        p.wheel(px[0], px[1], event.getVertical());
        event.cancel();   // no hotbar switching while scrolling a page
    }

    /** Finds the screen under the crosshair (render thread, every frame). */
    private void updateHover(class_243 cam) {
        class_310 mc = class_310.method_1551();
        class_746 player = mc.field_1724;
        Panel best = null;
        double bestT = REACH;
        double bu = 0.0;
        double bv = 0.0;
        if (player != null && mc.field_1755 == null && this.worldMouse.isValue()) {
            class_243 dir = player.method_5828(1.0f);
            for (Panel p : this.panels) {
                double[] h = p.hit(cam, dir);
                if (h != null && h[0] < bestT) {
                    best = p;
                    bestT = h[0];
                    bu = h[1];
                    bv = h[2];
                }
            }
            // a block or a mob in front of the screen hides it
            class_239 t = mc.field_1765;
            if (best != null && t != null && t.method_17783() != class_239.class_240.field_1333 && t.method_17784().method_1022(cam) < bestT - 0.05) {
                best = null;
            }
        }
        this.hovered = best;
        this.hoverU = bu;
        this.hoverV = bv;
        if (best != null) {
            int[] px = this.pixel(best, bu, bv);
            if (px[0] != this.lastMoveX || px[1] != this.lastMoveY) {
                this.lastMoveX = px[0];
                this.lastMoveY = px[1];
                best.mouseMove(px[0], px[1]);   // hover + dragging while a button is held
            }
        }
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1755 != null) return;
        if (this.isDown(event, this.openKey)) {
            Panel t = this.target();
            BrowserPanel bp = t instanceof BrowserPanel b ? b : this.addBrowserPanel(null);
            if (bp != null) {
                this.focused = bp;
                mc.execute(() -> mc.method_1507(new BrowserScreen(this, bp)));
            }
        } else if (this.isDown(event, this.typeKey)) {
            Panel t = this.target();
            if (t == null) {
                this.notice("Посмотри на экран, в который хочешь печатать.");
            } else {
                this.focused = t;
                mc.execute(() -> mc.method_1507(new TypeScreen(t, false)));
            }
        } else if (this.isDown(event, this.newKey)) {
            this.addBrowserPanel(null);
        } else if (this.isDown(event, this.appsKey)) {
            mc.execute(() -> mc.method_1507(new AppPickerScreen(this)));
        } else if (this.isDown(event, this.closeKey)) {
            this.closePanel(this.target());
        } else if (this.isDown(event, this.placeKey)) {
            Panel t = this.focused != null ? this.focused : this.hovered;
            this.placeInFront(t);
        }
    }

    private boolean isDown(KeyEvent e, BindSetting b) {
        return b.getKey() > 0 && e.isKeyDown(b.getKey());
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (class_310.method_1551().field_1755 != null) this.releasePressed();
        this.syncNetwork();
        if (BrowserBridge.installed()) this.retryChromium();
        for (Panel p : new ArrayList<>(this.panels)) {
            if (!p.alive()) {
                this.notice("«" + p.title() + "» закрыто, убираю экран.");
                this.closePanel(p);
            } else if (p instanceof AppPanel a) {
                a.setFps(Math.round(this.appFps.getValue()));
            }
        }
        this.autoKeyboard();
        if (this.followLook.isValue() && this.focused != null && this.pressed == null) {
            this.placeInFront(this.focused);
        }
        int[] r = this.pageResolution();
        if (class_310.method_1551().field_1755 == null) {
            for (Panel p : this.panels) {
                if (p instanceof BrowserPanel b && (b.pixelWidth() != r[0] || b.pixelHeight() != r[1])) b.restoreSize();
            }
        }
    }

    /** A click into a text field on a web screen sends the keyboard there (TypeScreen) until Esc. */
    private void autoKeyboard() {
        class_310 mc = class_310.method_1551();
        long now = System.currentTimeMillis();
        for (Panel p : this.panels) {
            if (!(p instanceof BrowserPanel b)) continue;
            b.tickFocusScript();
            if (!this.autoType.isValue() || mc.field_1755 != null || mc.field_1724 == null) continue;
            long click = b.lastClick();
            if (b.editing() && click != this.autoTypedClick && now - click < 2500L) {
                this.autoTypedClick = click;
                this.focused = b;
                this.releasePressed();
                mc.method_1507(new TypeScreen(b, true));
            }
        }
    }

    /** Rinku downloads Chromium only once at game start; when that failed, try again by itself. */
    private void retryChromium() {
        if (BrowserBridge.ready() || !BrowserBridge.downloadFailed() || BrowserBridge.retrying() || System.currentTimeMillis() < this.nextRetry) {
            return;
        }
        if (this.retries == 0) {
            this.notice("Chromium не скачался при запуске игры (не было интернета). Качаю ещё раз... Сеть: " + BrowserNet.describe() + ".");
        }
        ++this.retries;
        BrowserBridge.retryDownload(error -> {
            if (error == null) {
                this.retries = 0;
                this.lastError = null;
                this.notice("Chromium скачан, запускаю браузер!");
            } else {
                this.lastError = error;
                long wait = Math.min(120_000L, 15_000L * this.retries);   // 15 s, 30 s, ... up to 2 min
                this.nextRetry = System.currentTimeMillis() + wait;
                if (this.retries == 1 || this.retries % 5 == 0) {
                    this.notice("Не получилось скачать Chromium: " + error + ". Попробую снова через " + wait / 1000 + " с. Включи VPN или укажи прокси в Network -> Proxy.");
                }
            }
        });
    }

    /** What to show instead of a web page while it is not there yet. */
    String statusText() {
        if (!BrowserBridge.installed()) return "Нужен мод Rinku";
        if (BrowserBridge.retrying() || !BrowserBridge.downloadFailed() && !BrowserBridge.downloadDone()) {
            int pct = Math.round(BrowserBridge.progress() * 100);
            return pct > 0 && pct < 100 ? "Скачиваю Chromium: " + pct + "%" : "Готовлю Chromium...";
        }
        if (BrowserBridge.downloadFailed()) {
            long s = Math.max(0L, (this.nextRetry - System.currentTimeMillis()) / 1000L);
            return "Chromium не скачался" + (this.lastError != null ? " (" + this.lastError + ")" : "") + ", повтор через " + s + " с";
        }
        return BrowserBridge.ready() ? "Загружаю страницу..." : "Запускаю Chromium...";
    }

    // ------------------------------------------------------------------ drawing in the world

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        class_310 mc = class_310.method_1551();
        for (Panel p : this.panels) p.renderTick();   // new frames of program windows
        if (!this.mode.isSelected("In World") || mc.field_1755 instanceof BrowserScreen) {
            this.hovered = null;
            return;
        }
        class_4184 camera = mc.field_1773.method_19418();
        class_243 cam = camera.method_71156();
        this.updateHover(cam);
        BrowserFrame.Style style = this.style();
        int color = Math.round(this.opacity.getValue() / 100.0f * 255) << 24 | 0xFFFFFF;
        class_4587 m = event.getStack();
        for (Panel p : this.panels) {
            double hw = p.halfWidth();
            double hh = p.halfHeight();
            class_243 right = p.right();
            class_243 up = Panel.up();
            BrowserFrame.draw3D(p.anchor, right, up, hw, hh, this.frameWidth.getValue(), style);
            class_2960 tex = p.texture();
            if (tex != null) {
                m.method_22903();
                m.method_22904(p.anchor.field_1352 - cam.field_1352, p.anchor.field_1351 - cam.field_1351, p.anchor.field_1350 - cam.field_1350);
                m.method_22907(new Quaternionf().rotationY((float)(Math.PI - Math.toRadians(p.yaw))));
                Render3DUtil.drawPage(m.method_23760().method_56822(), tex, (float)-hw, (float)-hh, (float)(hw * 2), (float)(hh * 2), color);
                m.method_22909();
            } else {
                // dark placeholder while it loads
                class_243 a = p.point(0.0, 1.0);
                class_243 b = p.point(1.0, 1.0);
                class_243 c = p.point(1.0, 0.0);
                class_243 d = p.point(0.0, 0.0);
                Render3DUtil.drawGradientQuad(a, b, c, d, 0xE0101018, 0xE0101018, 0xE0202430, 0xE0202430, true);
            }
        }
        // a small cross where you point on the screen
        if (this.hovered != null) {
            Panel p = this.hovered;
            class_243 at = p.point(this.hoverU, this.hoverV).method_1019(p.normal().method_1021(0.01));
            double s = Math.max(0.025, p.width * 0.008);
            int col = this.pressed != null ? 0xFFFFD040 : 0xFFFFFFFF;
            Render3DUtil.drawLine(at.method_1019(p.right().method_1021(-s)), at.method_1019(p.right().method_1021(s)), col, 2.0f, false);
            Render3DUtil.drawLine(at.method_1019(Panel.up().method_1021(-s)), at.method_1019(Panel.up().method_1021(s)), col, 2.0f, false);
        }
    }

    // ------------------------------------------------------------------ HUD

    @EventHandler
    public void onDraw(DrawEvent event) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1755 instanceof BrowserScreen) return;
        class_332 c = event.getDrawContext();
        int sw = mc.method_22683().method_4486();
        int sh = mc.method_22683().method_4502();
        if (!this.mode.isSelected("On Screen")) {
            if (mc.field_1755 != null) return;
            Panel h = this.hovered;
            if (h != null) {
                String line = h.texture() == null ? h.status() : h.title() + "   ЛКМ/ПКМ - клик, колесо - прокрутка" + (this.typeKey.getKey() > 0 ? ", Type Key - печатать" : "");
                int w = mc.field_1772.method_1727(line);
                c.method_51433(mc.field_1772, line, (sw - w) / 2, sh / 2 + 14, 0xFFD0E4FF, true);
            } else if (this.panels.isEmpty() && BrowserBridge.installed() && !BrowserBridge.ready()) {
                c.method_51433(mc.field_1772, "Browser: " + this.statusText(), 6, 6, 0xFFB0C4FF, true);
            }
            return;
        }
        Panel p = this.focused != null ? this.focused : this.panels.isEmpty() ? null : this.panels.get(0);
        if (p == null) return;
        int tw = Math.max(1, p.pixelWidth());
        int th = Math.max(1, p.pixelHeight());
        int w = Math.round(sw * this.screenSize.getValue() / 100.0f);
        int h = w * th / tw;
        int margin = 10;
        String cr = this.corner.getSelected();
        int x = cr.contains("Left") ? margin + 4 : cr.equals("Center") ? (sw - w) / 2 : sw - w - margin - 4;
        int y = cr.contains("Top") ? margin + 4 : cr.equals("Center") ? (sh - h) / 2 : sh - h - margin - 4;
        BrowserFrame.draw2D(c, x, y, w, h, 0, this.style());
        class_2960 tex = p.texture();
        if (tex != null) {
            int argb = Math.round(this.opacity.getValue() / 100.0f * 255) << 24 | 0xFFFFFF;
            c.method_25293(class_10799.field_56883, tex, x, y, 0.0f, 0.0f, w, h, tw, th, tw, th, argb);
        } else {
            c.method_25294(x, y, x + w, y + h, 0xE0101018);
            String msg = p.status();
            c.method_51433(mc.field_1772, msg, x + (w - mc.field_1772.method_1727(msg)) / 2, y + h / 2 - 4, 0xFFB0C4FF, true);
        }
    }
}
