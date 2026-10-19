/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_156
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.class_156;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.MenuSearch;
import ru.recoin.implement.screens.menu.MenuStyle;

public final class ConfigPanel {
    private final List<String> files = new ArrayList<String>();
    private final Map<String, Rect> rows = new LinkedHashMap<String, Rect>();
    private final Map<String, Rect> buttons = new LinkedHashMap<String, Rect>();
    private final Map<String, String> dates = new LinkedHashMap<String, String>();
    private final Map<String, Float> hover = new LinkedHashMap<String, Float>();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());
    private String pendingDelete = "";
    private long pendingDeleteTime;
    private long lastFrame;
    private float hoverBlend;
    private String selected = "";
    private String name = "new-config";
    private String status = "";
    private String pendingSave = "";
    private long pendingSaveTime;
    private boolean editing;
    private boolean selectName;
    private boolean initialized;
    private float scroll;
    private float smoothScroll;
    private Rect panel;
    private Rect list;
    private Rect input;

    public void open() {
        this.editing = false;
        this.pendingDelete = "";
        this.pendingSave = "";
        this.hover.clear();
        this.lastFrame = 0L;
        this.refresh();
    }

    private Path directory() {
        return Main.getInstance().getClientInfoProvider().configsDir().toPath().toAbsolutePath().normalize();
    }

    private void refresh() {
        this.initialized = true;
        this.files.clear();
        this.dates.clear();
        Path root = this.directory();
        if (!Files.exists(root, new LinkOption[0])) {
            return;
        }
        try (Stream<Path> paths = Files.list(root);){
            paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)).map(path -> path.getFileName().toString()).filter(file -> file.toLowerCase(Locale.ROOT).endsWith(".json")).sorted(String.CASE_INSENSITIVE_ORDER).forEach(this.files::add);
            if (!this.files.contains(this.selected)) {
                this.selected = "";
            }
            for (String file2 : this.files) {
                BasicFileAttributes attributes = Files.readAttributes(this.resolve(file2), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                this.dates.put(file2, DATE.format((attributes.creationTime().toMillis() > 0L ? attributes.creationTime() : attributes.lastModifiedTime()).toInstant()));
            }
            this.hover.keySet().removeIf(key -> key.contains(":") && !this.files.contains(key.substring(key.indexOf(58) + 1)));
        }
        catch (IOException error) {
            this.status = "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432";
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(class_332 context, class_4587 matrix, float x, float y, float width, float height, int mouseX, int mouseY, String query) {
        if (!this.initialized) {
            this.refresh();
        }
        this.panel = new Rect(x, y, width, height);
        long now = System.nanoTime();
        float elapsed = this.lastFrame == 0L ? 0.016666668f : Math.min(0.1f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        this.hoverBlend = 1.0f - (float)Math.exp(-elapsed / 0.04f);
        this.list = new Rect(x + 8.0f, y + 32.0f, width - 16.0f, Math.max(32.0f, height - 148.0f));
        this.input = new Rect(x + 10.0f, y + height - 100.0f, width - 20.0f, 24.0f);
        this.rows.clear();
        this.buttons.clear();
        MenuStyle.medium(12.0f).drawString(matrix, this.ru() ? "\u041a\u043e\u043d\u0444\u0438\u0433\u0438" : "Configurations", x + 10.0f, y + 8.0f, -986118);
        List<String> shown = this.files.stream().filter(file -> MenuSearch.matches(file, query)).toList();
        float contentHeight = Math.max(0, shown.size() * 44 - 6);
        float maximum = Math.max(0.0f, contentHeight - this.list.height);
        this.scroll = Math.max(-maximum, Math.min(0.0f, this.scroll));
        this.smoothScroll = MathUtil.interpolateSmooth(2.0, this.smoothScroll, this.scroll);
        this.smoothScroll = Math.max(-maximum, Math.min(0.0f, this.smoothScroll));
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), this.list.x, this.list.y, this.list.width, this.list.height);
        try {
            for (int i = 0; i < shown.size(); ++i) {
                String file2 = shown.get(i);
                Rect row = new Rect(this.list.x + 2.0f, this.list.y + (float)(i * 44) + this.smoothScroll, this.list.width - 10.0f, 38.0f);
                if (row.y + row.height < this.list.y || row.y > this.list.y + this.list.height) continue;
                this.rows.put(file2, row);
                MenuStyle.fill(matrix, row.x, row.y, row.width, row.height, 5.0f, MenuStyle.moduleBackground());
                float strength = this.progress("Row:" + file2, this.list.hover(mouseX, mouseY) && row.hover(mouseX, mouseY)) * 0.07f;
                MenuStyle.fill(matrix, row.x, row.y, row.width, row.height, 5.0f, ColorUtil.multAlpha(-7301470, strength));
                String display = file2.substring(0, file2.length() - 5);
                MenuSearch.draw(matrix, MenuStyle.medium(10.0f), MenuStyle.fitMedium(display, row.width - 140.0f, 10.0f), row.x + 9.0f, row.y + 7.0f, -986118, query);
                MenuStyle.font(7.5f).drawString(matrix, (this.ru() ? "\u0421\u043e\u0437\u0434\u0430\u043d: " : "Created: ") + this.dates.getOrDefault(file2, "\u2014"), row.x + 9.0f, row.y + 23.0f, -7301470);
                float right = row.x + row.width;
                this.actionIcon(matrix, "Delete:" + file2, "Delete", right - 119.0f, row.y + 7.0f, mouseX, mouseY);
                this.actionIcon(matrix, "Save:" + file2, "Save", right - 93.0f, row.y + 7.0f, mouseX, mouseY);
                this.button(matrix, "Load:" + file2, this.ru() ? "\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c" : "Load", right - 67.0f, row.y + 7.0f, 60.0f, mouseX, mouseY);
            }
            if (shown.isEmpty()) {
                this.centered(matrix, MenuStyle.medium(10.0f), this.ru() ? "\u041a\u043e\u043d\u0444\u0438\u0433\u0438 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b" : "No configurations found", this.list.x + this.list.width / 2.0f, this.list.y + this.list.height / 2.0f, -7301470);
            }
        }
        finally {
            scissor.pop();
        }
        if (maximum > 0.0f) {
            float thumb = Math.max(18.0f, this.list.height * this.list.height / contentHeight);
            float thumbY = this.list.y + -this.smoothScroll / maximum * (this.list.height - thumb);
            MenuStyle.fill(matrix, this.list.x + this.list.width - 2.0f, this.list.y, 1.5f, this.list.height, 0.75f, MenuStyle.border());
            MenuStyle.fill(matrix, this.list.x + this.list.width - 2.0f, thumbY, 1.5f, thumb, 0.75f, -7301470);
        }
        MenuStyle.font(8.0f).drawString(matrix, this.ru() ? "\u0418\u043c\u044f \u043a\u043e\u043d\u0444\u0438\u0433\u0430" : "Configuration name", this.input.x, this.input.y - 12.0f, -7301470);
        MenuStyle.fill(matrix, this.input.x, this.input.y, this.input.width, this.input.height, 5.0f, MenuStyle.moduleBackground());
        MenuStyle.outline(matrix, this.input.x, this.input.y, this.input.width, this.input.height, 5.0f, 0.5f, this.editing ? MenuStyle.accent() : MenuStyle.border());
        String text = this.name + (this.editing && System.currentTimeMillis() % 1000L < 500L ? "|" : "");
        MenuStyle.font(9.0f).drawString(matrix, MenuStyle.fit(text, this.input.width - 12.0f, 9.0f), this.input.x + 6.0f, this.input.y + 7.0f, this.selectName ? MenuStyle.accent() : -986118);
        this.button(matrix, "Save", this.ru() ? "\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c" : "Save", x + 10.0f, y + height - 69.0f, width - 20.0f, mouseX, mouseY);
        Rect folder = new Rect(x + 10.0f, y + height - 40.0f, width - 20.0f, 24.0f);
        this.buttons.put("Folder", folder);
        this.drawHover(matrix, "Folder", folder, mouseX, mouseY);
        this.icon(matrix, "Folder", folder.x + 7.0f, folder.y + 7.0f, -7301470);
        this.centeredLeft(matrix, MenuStyle.medium(10.0f), "Folder", folder.x + 25.0f, folder.y + 12.0f, -7301470);
        MenuStyle.font(7.5f).drawString(matrix, MenuStyle.fit(this.status, width - 20.0f, 7.5f), x + 10.0f, y + height - 14.0f, -7301470);
    }

    private boolean ru() {
        return MenuDescriptionLocalizer.isRussian();
    }

    private void button(class_4587 matrix, String key, String label, float x, float y, float width, int mx, int my) {
        Rect rect = new Rect(x, y, width, 23.0f);
        this.buttons.put(key, rect);
        MenuStyle.fill(matrix, x, y, width, 23.0f, 5.0f, key.startsWith("Load:") ? ColorUtil.multAlpha(MenuStyle.accent(), 0.18f) : MenuStyle.moduleBackground());
        this.drawHover(matrix, key, rect, mx, my);
        this.centered(matrix, MenuStyle.medium(10.0f), label, x + width / 2.0f, y + 11.5f, -986118);
    }

    private float progress(String key, boolean active) {
        float value = this.hover.getOrDefault(key, Float.valueOf(0.0f)).floatValue();
        value += ((active ? 1.0f : 0.0f) - value) * this.hoverBlend;
        this.hover.put(key, Float.valueOf(value));
        return value;
    }

    private void drawHover(class_4587 matrix, String key, Rect rect, int mx, int my) {
        boolean active = rect.hover(mx, my) && (!key.contains(":") || this.list.hover(mx, my));
        MenuStyle.fill(matrix, rect.x, rect.y, rect.width, rect.height, 5.0f, ColorUtil.multAlpha(-7301470, this.progress(key, active) * 0.07f));
    }

    private void actionIcon(class_4587 matrix, String key, String symbol, float x, float y, int mx, int my) {
        Rect rect = new Rect(x, y, 22.0f, 23.0f);
        this.buttons.put(key, rect);
        float highlight = this.progress(key, rect.hover(mx, my) && this.list.hover(mx, my));
        this.icon(matrix, symbol, x + 6.0f, y + 6.5f, key.equals("Delete:" + this.pendingDelete) && System.currentTimeMillis() - this.pendingDeleteTime <= 5000L ? -33918 : ColorUtil.overCol(-7301470, -986118, highlight));
    }

    private void icon(class_4587 matrix, String symbol, float x, float y, int color) {
        String glyph = switch (symbol) {
            case "Delete" -> "\ue070";
            case "Folder" -> "\ue0aa";
            default -> "\ue030";
        };
        this.centered(matrix, Fonts.getSize(26, Fonts.Type.COMBINED_ICONS), glyph, x + 5.0f, y + 5.0f, color);
    }

    private void centered(class_4587 matrix, FontRenderer font, String text, float cx, float cy, int color) {
        FontRenderer.TextBounds bounds = font.getStringBounds(text);
        font.drawString(matrix, text, cx - bounds.x() - bounds.width() / 2.0f, cy - bounds.y() - bounds.height() / 2.0f - Render2DUtil.getTextYOffset(), color);
    }

    private void centeredLeft(class_4587 matrix, FontRenderer font, String text, float x, float cy, int color) {
        FontRenderer.TextBounds bounds = font.getStringBounds(text);
        FontRenderer.TextBounds cap = font.getStringBounds("H");
        font.drawString(matrix, text, x - bounds.x(), cy - cap.y() - cap.height() / 2.0f - Render2DUtil.getTextYOffset(), color);
    }

    public boolean mouseClicked(double x, double y, int button) {
        if (this.panel == null || !this.panel.hover(x, y) || button != 0) {
            return false;
        }
        this.editing = this.input.hover(x, y);
        if (this.editing) {
            this.selectName = true;
            return true;
        }
        this.selectName = false;
        for (Map.Entry<String, Rect> entry : this.buttons.entrySet()) {
            if (!entry.getValue().hover(x, y) || entry.getKey().contains(":") && !this.list.hover(x, y)) continue;
            String key = entry.getKey();
            int split = key.indexOf(58);
            if (split >= 0) {
                this.selected = key.substring(split + 1);
                this.name = this.selected.substring(0, this.selected.length() - 5);
                key = key.substring(0, split);
            }
            this.perform(key);
            return true;
        }
        if (this.list.hover(x, y)) {
            for (Map.Entry<String, Rect> row : this.rows.entrySet()) {
                if (!row.getValue().hover(x, y)) continue;
                this.selected = row.getKey();
                this.name = this.selected.substring(0, this.selected.length() - 5);
                this.pendingSave = "";
                this.pendingDelete = "";
                return true;
            }
        }
        return true;
    }

    private Path resolve(String file) throws IOException {
        Path root = this.directory();
        Path target = root.resolve(file).normalize();
        if (!target.getParent().equals(root) || Files.isSymbolicLink(root) || Files.isSymbolicLink(target)) {
            throw new IOException("Invalid configuration path");
        }
        return target;
    }

    private String destination() {
        String value = this.name.strip();
        if (!value.matches("[\\p{L}\\p{N}_ -]{1,48}") || value.endsWith(" ") || value.matches("(?i)(con|prn|aux|nul|com[1-9]|lpt[1-9])")) {
            throw new IllegalArgumentException("\u0418\u043c\u044f: 1\u201348 \u0431\u0443\u043a\u0432, \u0446\u0438\u0444\u0440, \u043f\u0440\u043e\u0431\u0435\u043b\u043e\u0432, \u0434\u0435\u0444\u0438\u0441\u043e\u0432 \u0438\u043b\u0438 _");
        }
        return value + ".json";
    }

    private void perform(String action) {
        try {
            if (!action.equals("Delete")) {
                this.pendingDelete = "";
            }
            if (action.equals("Folder")) {
                if (Files.isSymbolicLink(this.directory())) {
                    throw new IOException("Invalid configuration directory");
                }
                Files.createDirectories(this.directory(), new FileAttribute[0]);
                class_156.method_668().method_60932(this.directory());
                return;
            }
            if (action.equals("Delete")) {
                if (this.selected.isEmpty()) {
                    throw new IllegalArgumentException("\u0412\u044b\u0431\u0435\u0440\u0438 \u043a\u043e\u043d\u0444\u0438\u0433 \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430");
                }
                if (!this.pendingDelete.equals(this.selected) || System.currentTimeMillis() - this.pendingDeleteTime > 5000L) {
                    this.pendingDelete = this.selected;
                    this.pendingDeleteTime = System.currentTimeMillis();
                    this.status = this.ru() ? "\u041d\u0430\u0436\u043c\u0438 \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0435\u0449\u0451 \u0440\u0430\u0437. \u041a\u043e\u043d\u0444\u0438\u0433 \u0431\u0443\u0434\u0435\u0442 \u043f\u0435\u0440\u0435\u043c\u0435\u0449\u0451\u043d \u0432 .trash" : "Click delete again to move this config to .trash";
                    return;
                }
                Path source = this.resolve(this.selected);
                if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("Configuration not found");
                }
                Path trash = this.directory().resolve(".trash");
                if (Files.isSymbolicLink(trash)) {
                    throw new IOException("Invalid trash directory");
                }
                Files.createDirectories(trash, new FileAttribute[0]);
                Files.move(source, trash.resolve(System.currentTimeMillis() + "-" + this.selected), new CopyOption[0]);
                this.pendingDelete = "";
                this.pendingSave = "";
                this.refresh();
                this.status = this.ru() ? "\u041a\u043e\u043d\u0444\u0438\u0433 \u043f\u0435\u0440\u0435\u043c\u0435\u0449\u0451\u043d \u0432 .trash" : "Configuration moved to .trash";
                return;
            }
            if (action.equals("Load")) {
                if (this.selected.isEmpty()) {
                    throw new IllegalArgumentException("\u0412\u044b\u0431\u0435\u0440\u0438 \u043a\u043e\u043d\u0444\u0438\u0433 \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430");
                }
                if (!Files.isRegularFile(this.resolve(this.selected), LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("Configuration not found");
                }
                Main.getInstance().getFileController().loadFile(this.selected);
                this.status = "\u0417\u0430\u0433\u0440\u0443\u0436\u0435\u043d: " + this.selected;
                return;
            }
            if (!action.equals("Save")) {
                return;
            }
            String target = this.destination();
            Path path = this.resolve(target);
            if (Files.exists(path, new LinkOption[0]) && (!this.pendingSave.equals(target) || System.currentTimeMillis() - this.pendingSaveTime > 5000L)) {
                this.pendingSave = target;
                this.pendingSaveTime = System.currentTimeMillis();
                this.status = "\u041a\u043e\u043d\u0444\u0438\u0433 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442. \u041d\u0430\u0436\u043c\u0438 \u00ab\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c\u00bb \u0435\u0449\u0451 \u0440\u0430\u0437 \u0434\u043b\u044f \u043f\u0435\u0440\u0435\u0437\u0430\u043f\u0438\u0441\u0438";
                return;
            }
            Files.createDirectories(this.directory(), new FileAttribute[0]);
            Main.getInstance().getFileController().saveFile(target);
            this.pendingSave = "";
            this.selected = target;
            this.refresh();
            this.status = "\u0413\u043e\u0442\u043e\u0432\u043e: " + target;
        }
        catch (IllegalArgumentException error) {
            this.status = error.getMessage();
        }
        catch (Exception error) {
            this.status = "\u041e\u0448\u0438\u0431\u043a\u0430: " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
        }
    }

    public boolean keyPressed(int key, int modifiers) {
        if (!this.editing) {
            return false;
        }
        if ((modifiers & 2) != 0 && key == 65) {
            this.selectName = true;
        } else if (key == 259) {
            this.name = this.selectName ? "" : (this.name.isEmpty() ? "" : this.name.substring(0, this.name.length() - 1));
            this.selectName = false;
            this.pendingSave = "";
        } else if (key == 257 || key == 335) {
            this.editing = false;
            this.selectName = false;
        } else if (key == 256) {
            this.editing = false;
            this.selectName = false;
        }
        return true;
    }

    public boolean charTyped(char character) {
        if (!this.editing || Character.isISOControl(character)) {
            return false;
        }
        if (this.selectName) {
            this.name = "";
            this.selectName = false;
        }
        if (this.name.length() < 48) {
            this.name = this.name + character;
        }
        this.pendingSave = "";
        return true;
    }

    public boolean mouseScrolled(double x, double y, double amount) {
        if (this.list == null || !this.list.hover(x, y)) {
            return false;
        }
        this.scroll += (float)amount * 24.0f;
        return true;
    }

    private record Rect(float x, float y, float width, float height) {
        private boolean hover(double mx, double my) {
            return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.width) && my < (double)(this.y + this.height);
        }
    }
}

