package ru.recoin.implement.features.modules.misc.browser;

import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/**
 * Controls of the Browser: the page fills the screen inside the themed frame, with an address bar
 * (type an address or just words to search), back / forward / reload / home. Mouse and keyboard go
 * into the page. Esc closes the controls; the page keeps playing in the world or on the HUD.
 */
public final class BrowserScreen extends class_437 {
    private static final int MARGIN = 18;
    private static final int BAR = 22;

    private final WebBrowser module;
    private final BrowserPanel panel;
    private class_342 address;
    private class_4185 back;
    private class_4185 forward;

    public BrowserScreen(WebBrowser module, BrowserPanel panel) {
        super(class_2561.method_43470("Browser"));
        this.module = module;
        this.panel = panel;
    }

    private BrowserBridge b() {
        return this.panel.bridge();
    }

    private int px() {
        return MARGIN;
    }

    private int py() {
        return MARGIN + BAR;
    }

    private int pw() {
        return Math.max(1, this.field_22789 - MARGIN * 2);
    }

    private int ph() {
        return Math.max(1, this.field_22790 - this.py() - MARGIN);
    }

    private double scale() {
        return class_310.method_1551().method_22683().method_4495();
    }

    private void resizePage() {
        this.panel.resizeTemporarily((int)(this.pw() * this.scale()), (int)(this.ph() * this.scale()));
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        int x = MARGIN + 2;
        int y = MARGIN + 2;
        int bw = 20;
        this.back = this.method_37063(class_4185.method_46430(class_2561.method_43470("←"), btn -> {
            if (this.b() != null) this.b().back();
        }).method_46434(x, y, bw, 18).method_46431());
        x += bw + 2;
        this.forward = this.method_37063(class_4185.method_46430(class_2561.method_43470("→"), btn -> {
            if (this.b() != null) this.b().forward();
        }).method_46434(x, y, bw, 18).method_46431());
        x += bw + 2;
        this.method_37063(class_4185.method_46430(class_2561.method_43470("⟳"), btn -> {
            if (this.b() != null) this.b().reload();
        }).method_46434(x, y, bw, 18).method_46431());
        x += bw + 2;
        this.method_37063(class_4185.method_46430(class_2561.method_43470("⌂"), btn -> {
            this.panel.load(this.module.home());
        }).method_46434(x, y, bw, 18).method_46431());
        x += bw + 2;
        this.method_37063(class_4185.method_46430(class_2561.method_43470("+"), btn -> {
            BrowserPanel np = this.module.addBrowserPanel(null);   // one more screen in the world
            if (np != null) this.field_22787.method_1507(new BrowserScreen(this.module, np));
        }).method_46434(x, y, bw, 18).method_46431());
        x += bw + 2;
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Программы"), btn -> this.field_22787.method_1507(new AppPickerScreen(this.module))).method_46434(this.field_22789 - MARGIN - 2 - 70, y, 70, 18).method_46431());
        x += 4;
        int aw = Math.max(80, this.field_22789 - MARGIN - 2 - 74 - x);
        this.address = new class_342(this.field_22793, x, y, aw, 18, class_2561.method_43470("Address"));
        this.address.method_1880(4096);
        BrowserBridge b = this.b();
        this.address.method_1852(b != null ? b.url() : this.module.home());
        this.method_37063(this.address);
        this.resizePage();
    }

    @Override
    public void method_25410(int w, int h) {
        super.method_25410(w, h);
        this.resizePage();
    }

    @Override
    public void method_25393() {
        super.method_25393();
        BrowserBridge b = this.b();
        if (b == null) return;
        if (this.back != null) this.back.field_22763 = b.canBack();
        if (this.forward != null) this.forward.field_22763 = b.canForward();
        if (this.address != null && !this.address.method_25370()) {
            String url = b.url();
            if (!url.isBlank() && !url.equals(this.address.method_1882())) this.address.method_1852(url);
        }
    }

    @Override
    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        BrowserFrame.draw2D(c, this.px(), this.py(), this.pw(), this.ph(), BAR, this.module.style());
        BrowserBridge b = this.b();
        class_2960 tex = b != null && b.textureReady() ? b.texture() : null;
        if (tex != null) {
            int w = this.pw();
            int h = this.ph();
            c.method_25290(class_10799.field_56883, tex, this.px(), this.py(), 0.0f, 0.0f, w, h, w, h);
        } else {
            c.method_25294(this.px(), this.py(), this.px() + this.pw(), this.py() + this.ph(), 0xF0101018);
            String msg = this.module.statusText();
            c.method_51433(this.field_22793, msg, this.px() + (this.pw() - this.field_22793.method_1727(msg)) / 2, this.py() + this.ph() / 2 - 4, 0xFFB0C4FF, true);
        }
        // loading strip under the address bar
        if (b != null && b.loading() && this.address != null) {
            int x0 = this.address.method_46426();
            int width = this.address.method_25368();
            int seg = Math.max(20, width / 4);
            int off = (int)(System.currentTimeMillis() / 5L % (width + seg)) - seg;
            int s = Math.max(x0, x0 + off);
            int e = Math.min(x0 + width, x0 + off + seg);
            int yy = this.address.method_46427() + 17;
            if (e > s) c.method_25294(s, yy, e, yy + 2, BrowserFrame.colors(this.module.style())[0]);
        }
        super.method_25394(c, mouseX, mouseY, delta);
    }

    private boolean inPage(double x, double y) {
        return x >= this.px() && y >= this.py() && x < this.px() + this.pw() && y < this.py() + this.ph();
    }

    private int mx(double x) {
        return (int)((x - this.px()) * this.scale());
    }

    private int my(double y) {
        return (int)((y - this.py()) * this.scale());
    }

    private void go() {
        BrowserBridge b = this.b();
        if (b == null || this.address == null) return;
        String url = BrowserBridge.toUrl(this.address.method_1882(), WebBrowser.SEARCH);
        this.address.method_1852(url);
        this.panel.load(url);
        this.method_25395(null);
        b.focus(true);
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (super.method_25402(click, doubled)) return true;
        BrowserBridge b = this.b();
        if (b == null || !this.inPage(click.comp_4798(), click.comp_4799())) return false;
        this.method_25395(null);   // the page takes the keyboard, not the address bar
        b.mousePress(this.mx(click.comp_4798()), this.my(click.comp_4799()), click.comp_4800().comp_4801());
        b.focus(true);
        return true;
    }

    @Override
    public boolean method_25406(class_11909 click) {
        if (super.method_25406(click)) return true;
        BrowserBridge b = this.b();
        if (b == null) return false;
        b.mouseRelease(this.mx(click.comp_4798()), this.my(click.comp_4799()), click.comp_4800().comp_4801());
        return true;
    }

    @Override
    public void method_16014(double x, double y) {
        BrowserBridge b = this.b();
        if (b != null && this.inPage(x, y)) b.mouseMove(this.mx(x), this.my(y));
        super.method_16014(x, y);
    }

    @Override
    public boolean method_25403(class_11909 click, double dx, double dy) {
        BrowserBridge b = this.b();
        if (b != null && this.inPage(click.comp_4798(), click.comp_4799())) {
            b.mouseMove(this.mx(click.comp_4798()), this.my(click.comp_4799()));   // drag = move with the button held
            return true;
        }
        return super.method_25403(click, dx, dy);
    }

    @Override
    public boolean method_25401(double x, double y, double h, double v) {
        BrowserBridge b = this.b();
        if (b != null && this.inPage(x, y)) {
            b.wheel(this.mx(x), this.my(y), v);
            return true;
        }
        return super.method_25401(x, y, h, v);
    }

    @Override
    public boolean method_25404(class_11908 key) {
        int k = key.comp_4795();
        if (this.address != null && this.address.method_25370() && (k == 257 || k == 335)) {   // Enter in the address bar
            this.go();
            return true;
        }
        if (k == 256) {   // Esc closes the controls
            this.method_25419();
            return true;
        }
        if (super.method_25404(key)) return true;
        if (this.address != null && this.address.method_25370()) return true;
        BrowserBridge b = this.b();
        if (b != null) {
            b.keyPress(k, key.comp_4796(), key.comp_4797());
            b.focus(true);
        }
        return true;
    }

    @Override
    public boolean method_16803(class_11908 key) {
        if (super.method_16803(key)) return true;
        if (this.address != null && this.address.method_25370()) return true;
        BrowserBridge b = this.b();
        if (b != null) b.keyRelease(key.comp_4795(), key.comp_4796(), key.comp_4797());
        return true;
    }

    @Override
    public boolean method_25400(class_11905 ch) {
        if (super.method_25400(ch)) return true;
        if (this.address != null && this.address.method_25370()) return true;
        BrowserBridge b = this.b();
        int cp = ch.comp_4793();
        if (b != null && cp != 0) {
            for (char c : Character.toChars(cp)) b.keyTyped(c, ch.comp_4794());
        }
        return true;
    }

    @Override
    public void method_25432() {
        super.method_25432();
        this.panel.restoreSize();   // back to the world / HUD size
    }

    @Override
    public boolean method_25421() {
        return false;
    }
}
