package ru.recoin.implement.features.modules.misc.browser;

import net.minecraft.class_243;
import net.minecraft.class_2960;

/** A web page screen (Chromium through Rinku). Created right away, the page starts once Chromium is ready. */
public final class BrowserPanel extends Panel {
    private final WebBrowser module;
    private String startUrl;
    private BrowserBridge bridge;
    private int pw;
    private int ph;
    // a text field on the page is selected (reported by FocusWatch)
    private volatile boolean editing;
    private volatile long editingChanged;
    private volatile long lastClick;
    private int injectTimer;

    public BrowserPanel(WebBrowser module, String url, class_243 anchor, float yaw, double width) {
        super(anchor, yaw, width);
        this.module = module;
        this.startUrl = url;
        int[] r = module.pageResolution();
        this.pw = r[0];
        this.ph = r[1];
    }

    /** The browser, created on first use; null while Chromium is still loading. */
    public BrowserBridge bridge() {
        if (this.bridge == null && BrowserBridge.installed() && BrowserBridge.ready()) {
            this.bridge = BrowserBridge.create(this.startUrl, this.pw, this.ph);
            if (this.bridge != null) FocusWatch.register(this);
        }
        return this.bridge;
    }

    /** Page size back to the normal one (the controls screen uses the window size). */
    public void restoreSize() {
        int[] r = this.module.pageResolution();
        this.pw = r[0];
        this.ph = r[1];
        if (this.bridge != null) this.bridge.resize(this.pw, this.ph);
    }

    public void resizeTemporarily(int w, int h) {
        if (this.bridge != null) this.bridge.resize(w, h);
    }

    public void load(String url) {
        this.startUrl = url;
        if (this.bridge() != null) this.bridge.load(url);
    }

    // ------------------------------------------------------------------ text fields

    boolean isBrowser(Object o) {
        BrowserBridge b = this.bridge;
        return b != null && o != null && b.handle() == o;
    }

    void setEditing(boolean e) {
        if (e != this.editing) this.editingChanged = System.currentTimeMillis();
        this.editing = e;
    }

    /** A text field on the page is selected right now. */
    public boolean editing() {
        return this.editing;
    }

    /** How long the field state has not changed, in ms. */
    public long editingStableFor() {
        return System.currentTimeMillis() - this.editingChanged;
    }

    long lastClick() {
        return this.lastClick;
    }

    /** Called every tick: keeps the focus script in the page (pages change on navigation). */
    void tickFocusScript() {
        if (this.bridge == null) return;
        FocusWatch.install();
        if (--this.injectTimer <= 0) {
            this.injectTimer = 20;
            this.bridge.runJs(FocusWatch.SCRIPT);
        }
    }

    /** Leaves the text field (Esc), so the keyboard goes back to the game. */
    public void blur() {
        if (this.bridge != null) this.bridge.runJs(FocusWatch.BLUR);
        this.editing = false;
        this.editingChanged = System.currentTimeMillis();
    }

    @Override
    public String title() {
        String u = this.bridge != null ? this.bridge.url() : this.startUrl;
        if (u == null || u.isBlank()) u = this.startUrl;
        String s = u.replaceFirst("^https?://(www\\.)?", "");
        return s.length() > 40 ? s.substring(0, 40) + "…" : s;
    }

    @Override
    public class_2960 texture() {
        BrowserBridge b = this.bridge();
        return b != null && b.textureReady() ? b.texture() : null;
    }

    @Override
    public int pixelWidth() {
        return this.pw;
    }

    @Override
    public int pixelHeight() {
        return this.ph;
    }

    @Override
    public String status() {
        return this.module.statusText();
    }

    @Override
    public void mouseMove(int x, int y) {
        if (this.bridge() != null) this.bridge.mouseMove(x, y);
    }

    @Override
    public void mousePress(int x, int y, int button) {
        this.lastClick = System.currentTimeMillis();
        if (this.bridge() != null) {
            this.bridge.runJs(FocusWatch.SCRIPT);   // make sure the page reports its text fields
            this.bridge.mouseMove(x, y);
            this.bridge.mousePress(x, y, button);
            this.bridge.focus(true);
        }
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        if (this.bridge() != null) this.bridge.mouseRelease(x, y, button);
    }

    @Override
    public void wheel(int x, int y, double amount) {
        if (this.bridge() != null) this.bridge.wheel(x, y, amount);
    }

    @Override
    public void keyPress(int key, int scancode, int mods) {
        if (this.bridge() != null) {
            this.bridge.keyPress(key, scancode, mods);
            this.bridge.focus(true);
        }
    }

    @Override
    public void keyRelease(int key, int scancode, int mods) {
        if (this.bridge() != null) this.bridge.keyRelease(key, scancode, mods);
    }

    @Override
    public void charTyped(char c, int mods) {
        if (this.bridge() != null) this.bridge.keyTyped(c, mods);
    }

    @Override
    public void close() {
        FocusWatch.unregister(this);
        if (this.bridge != null) {
            this.bridge.close();
            this.bridge = null;
        }
    }
}
