package ru.recoin.implement.features.modules.misc.browser;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/**
 * Pick what to put on a new screen: an open window, a program from the desktop / Start menu /
 * Downloads, or a new web page.
 */
public final class AppPickerScreen extends class_437 {
    private static final int ROW = 14;

    private final WebBrowser module;
    private class_342 search;
    private boolean windowsTab = true;
    private List<AppCatalog.Entry> windows = new ArrayList<>();
    private List<AppCatalog.Entry> programs = null;
    private List<AppCatalog.Entry> shown = new ArrayList<>();
    private int scroll;
    private String message = "";

    public AppPickerScreen(WebBrowser module) {
        super(class_2561.method_43470("Apps"));
        this.module = module;
    }

    private int top() {
        return 62;
    }

    private int left() {
        return Math.max(10, this.field_22789 / 2 - 170);
    }

    private int listWidth() {
        return Math.min(340, this.field_22789 - 20);
    }

    private int rows() {
        return Math.max(1, (this.field_22790 - this.top() - 24) / ROW);
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        int x = this.left();
        int w = this.listWidth();
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Открытые окна"), b -> this.tab(true)).method_46434(x, 12, w / 3 - 2, 18).method_46431());
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Программы"), b -> this.tab(false)).method_46434(x + w / 3, 12, w / 3 - 2, 18).method_46431());
        this.method_37063(class_4185.method_46430(class_2561.method_43470("+ Веб-страница"), b -> {
            this.module.addBrowserPanel(null);
            this.method_25419();
        }).method_46434(x + 2 * w / 3, 12, w - 2 * w / 3, 18).method_46431());
        this.search = new class_342(this.field_22793, x, 36, w, 18, class_2561.method_43470("Search"));
        this.search.method_47404(class_2561.method_43470("Поиск..."));
        this.search.method_1863(s -> this.refilter());
        this.method_37063(this.search);
        this.method_25395(this.search);
        if (!AppCatalog.supported()) {
            this.message = "Окна программ работают только на Windows.";
            return;
        }
        this.windows = AppCatalog.openWindows();
        this.refilter();
    }

    private void tab(boolean windows) {
        this.windowsTab = windows;
        if (windows) {
            this.windows = AppCatalog.openWindows();
        } else if (this.programs == null) {
            this.programs = AppCatalog.programs();
        }
        this.scroll = 0;
        this.refilter();
    }

    private void refilter() {
        List<AppCatalog.Entry> src = this.windowsTab ? this.windows : this.programs == null ? List.of() : this.programs;
        this.shown = AppCatalog.filter(src, this.search == null ? "" : this.search.method_1882());
        this.scroll = Math.max(0, Math.min(this.scroll, this.shown.size() - this.rows()));
    }

    @Override
    public void method_25394(class_332 c, int mouseX, int mouseY, float delta) {
        c.method_25294(0, 0, this.field_22789, this.field_22790, 0xC0080810);
        super.method_25394(c, mouseX, mouseY, delta);
        int x = this.left();
        int w = this.listWidth();
        int y0 = this.top();
        int rows = this.rows();
        c.method_25294(x, y0 - 2, x + w, y0 + rows * ROW + 2, 0x90000000);
        if (!this.message.isEmpty()) {
            c.method_51433(this.field_22793, this.message, x + 4, y0 + 4, 0xFFFFB060, true);
        } else if (this.shown.isEmpty()) {
            c.method_51433(this.field_22793, this.windowsTab ? "Нет открытых окон" : "Ничего не найдено", x + 4, y0 + 4, 0xFF9090A0, true);
        }
        for (int i = 0; i < rows && this.scroll + i < this.shown.size(); ++i) {
            AppCatalog.Entry e = this.shown.get(this.scroll + i);
            int ry = y0 + i * ROW;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= ry && mouseY < ry + ROW;
            if (hover) c.method_25294(x, ry, x + w, ry + ROW, 0x60FFFFFF);
            String name = e.name();
            String tag = e.where();
            int tagW = this.field_22793.method_1727(tag);
            while (name.length() > 3 && this.field_22793.method_1727(name) > w - tagW - 16) name = name.substring(0, name.length() - 2);
            if (!name.equals(e.name())) name = name + "…";
            c.method_51433(this.field_22793, name, x + 4, ry + 3, 0xFFFFFFFF, true);
            c.method_51433(this.field_22793, tag, x + w - tagW - 4, ry + 3, 0xFF8090B0, true);
        }
        String hint = "Клик - открыть на экране в мире.  Esc - закрыть.";
        c.method_51433(this.field_22793, hint, (this.field_22789 - this.field_22793.method_1727(hint)) / 2, this.field_22790 - 14, 0xFF9090A0, true);
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (super.method_25402(click, doubled)) return true;
        double mx = click.comp_4798();
        double my = click.comp_4799();
        int x = this.left();
        int w = this.listWidth();
        if (mx < x || mx >= x + w || my < this.top()) return false;
        int i = (int)((my - this.top()) / ROW);
        if (i >= this.rows() || this.scroll + i >= this.shown.size()) return false;
        AppCatalog.Entry e = this.shown.get(this.scroll + i);
        this.module.addApp(e);
        this.method_25419();
        return true;
    }

    @Override
    public boolean method_25401(double x, double y, double h, double v) {
        this.scroll = Math.max(0, Math.min(this.scroll - (int)Math.signum(v) * 3, this.shown.size() - this.rows()));
        return true;
    }

    @Override
    public boolean method_25421() {
        return false;
    }
}
