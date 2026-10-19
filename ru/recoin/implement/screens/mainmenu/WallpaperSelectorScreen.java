/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.mainmenu;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.common.util.render.ShapeShaderRenderer;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.mainmenu.MenuMotion;
import ru.recoin.implement.screens.mainmenu.ParallaxMenuBackground;
import ru.recoin.implement.screens.mainmenu.RecoinPostGuiScreen;
import ru.recoin.implement.screens.mainmenu.WallpaperStore;

final class WallpaperSelectorScreen
extends class_437
implements QuickImports,
RecoinPostGuiScreen {
    private static final float PANEL_WIDTH = 326.0f;
    private static final float PANEL_HEIGHT = 296.0f;
    private static final float PADDING = 14.0f;
    private static final float HEADER_HEIGHT = 38.0f;
    private static final float TILE_GAP = 8.0f;
    private static final int COLUMNS = 3;
    private static final int VISIBLE_ROWS = 4;
    private static final long OPEN_ANIMATION_MS = 160L;
    private static final long CLOSE_ANIMATION_MS = 130L;
    private static final int PANEL_FILL = -1710220734;
    private static final int TILE_FILL = -1710220734;
    private static final int GLASS_OUTLINE = 0;
    private static final String DELETE_ICON = "\ue05c";
    private static class_2960 defaultThumbId;
    private static class_2960 loadingThumbId;
    private final class_437 parent;
    private final ParallaxMenuBackground background = new ParallaxMenuBackground();
    private final MenuMotion[] tileHover = new MenuMotion[48];
    private final MenuMotion[] tileAppear = new MenuMotion[48];
    private float scroll;
    private float animatedScroll;
    private long openedAt;
    private long closingAt;
    private long lastFrameNanos;
    private boolean initialized;
    private boolean closing;
    private boolean animationStarted;

    WallpaperSelectorScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Wallpapers"));
        int i;
        this.parent = parent;
        for (i = 0; i < this.tileHover.length; ++i) {
            this.tileHover[i] = new MenuMotion();
        }
        for (i = 0; i < this.tileAppear.length; ++i) {
            this.tileAppear[i] = new MenuMotion();
        }
    }

    private static class_2960 getOrCreateDefaultThumb() {
        if (defaultThumbId != null) {
            return defaultThumbId;
        }
        int w = 256;
        int h = 144;
        class_1011 img = new class_1011(w, h, false);
        for (int y = 0; y < h; ++y) {
            float ny = (float)y / (float)h;
            for (int x = 0; x < w; ++x) {
                float nx = (float)x / (float)w;
                float dx = (nx - 0.5f) * 1.7777778f;
                float dy = ny - 0.5f;
                float d = (float)Math.sqrt(dx * dx + dy * dy);
                float nebula = (float)Math.exp(-d * 2.4f);
                float wave = (float)Math.sin(nx * 5.0f + ny * 2.8f) * 0.18f;
                int r = Math.clamp((long)((int)((12.0f + nebula * 110.0f + wave * 25.0f) * 0.88f)), 8, 255);
                int g = Math.clamp((long)((int)((16.0f + nebula * 100.0f + wave * 30.0f) * 0.88f)), 10, 255);
                int b = Math.clamp((long)((int)((38.0f + nebula * 225.0f + wave * 40.0f) * 0.88f)), 24, 255);
                int hash = x * 374761393 + y * 668265263;
                float star = (float)((hash = (hash ^ hash >> 13) * 1274126177) & Integer.MAX_VALUE) / 2.1474836E9f;
                if (star > 0.983f) {
                    float starB = (star - 0.983f) / 0.017f;
                    r = Math.clamp((long)((int)((float)r + starB * 210.0f)), 0, 255);
                    g = Math.clamp((long)((int)((float)g + starB * 220.0f)), 0, 255);
                    b = 255;
                }
                img.method_61941(x, y, 0xFF000000 | r << 16 | g << 8 | b);
            }
        }
        class_1043 tex = new class_1043(() -> "recoin_default_thumb", img);
        defaultThumbId = class_2960.method_60655((String)"recoin", (String)"wallpaper/default_thumb");
        class_310.method_1551().method_1531().method_4616(defaultThumbId, (class_1044)tex);
        return defaultThumbId;
    }

    private static class_2960 getOrCreateLoadingThumb() {
        if (loadingThumbId != null) {
            return loadingThumbId;
        }
        int w = 256;
        int h = 144;
        class_1011 img = new class_1011(w, h, false);
        for (int y = 0; y < h; ++y) {
            float ny = (float)y / (float)h;
            for (int x = 0; x < w; ++x) {
                float nx = (float)x / (float)w;
                float px = (nx - 0.5f) * 1.7777778f;
                float py = ny - 0.5f;
                float dist = (float)Math.sqrt(px * px + py * py);
                float ribbon = (float)Math.exp(-Math.abs((double)py + Math.sin(px * 2.4f) * (double)0.18f) * 7.0);
                float cloud = (float)(Math.sin(px * 3.0f) * Math.cos(py * 3.0f) * 0.5 + 0.5);
                float r = 0.018f * (1.0f - cloud) + 0.07f * cloud + 0.24f * ribbon * 0.8f + 0.19f * (float)Math.exp(-dist * dist * 5.0f);
                float g = 0.024f * (1.0f - cloud) + 0.038f * cloud + 0.08f * ribbon * 0.8f + 0.12f * (float)Math.exp(-dist * dist * 5.0f);
                float b = 0.055f * (1.0f - cloud) + 0.14f * cloud + 0.34f * ribbon * 0.8f + 0.31f * (float)Math.exp(-dist * dist * 5.0f);
                int hash = x * 374761393 + y * 668265263;
                float star = (float)((hash = (hash ^ hash >> 13) * 1274126177) & Integer.MAX_VALUE) / 2.1474836E9f;
                if (star > 0.985f) {
                    r += 0.32f;
                    g += 0.35f;
                    b += 0.5f;
                }
                int ir = Math.clamp((long)Math.round(r * 255.0f), 0, 255);
                int ig = Math.clamp((long)Math.round(g * 255.0f), 0, 255);
                int ib = Math.clamp((long)Math.round(b * 255.0f), 0, 255);
                img.method_61941(x, y, 0xFF000000 | ir << 16 | ig << 8 | ib);
            }
        }
        class_1043 tex = new class_1043(() -> "recoin_loading_thumb", img);
        loadingThumbId = class_2960.method_60655((String)"recoin", (String)"wallpaper/loading_thumb");
        class_310.method_1551().method_1531().method_4616(loadingThumbId, (class_1044)tex);
        return loadingThumbId;
    }

    protected void method_25426() {
        if (!this.initialized) {
            WallpaperStore store = WallpaperStore.getInstance();
            store.refreshFromDirectory();
            for (WallpaperStore.Entry entry : store.entries()) {
                store.loadThumb(entry.id);
            }
            this.initialized = true;
        }
        this.clampScroll();
        this.animatedScroll = this.scroll;
        this.openedAt = 0L;
        this.lastFrameNanos = System.nanoTime();
        this.animationStarted = false;
        for (MenuMotion motion : this.tileHover) {
            motion.reset(0.0f);
        }
        for (MenuMotion motion : this.tileAppear) {
            motion.reset(0.0f);
        }
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        this.background.render(context, this.field_22789, this.field_22790, mouseX, mouseY);
    }

    @Override
    public void renderRecoinOverlay(class_332 context, int mouseX, int mouseY, float delta) {
        this.renderContent(context, mouseX, mouseY, delta);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderContent(class_332 context, int mouseX, int mouseY, float delta) {
        Render2DUtil.setActiveContext(context);
        Render2DUtil.setQueuedShapeActive(true);
        try {
            blur.beginFrame();
            this.beginAnimationFrame();
            float seconds = this.frameSeconds();
            float alpha = this.transitionAnimation();
            float s = this.scale();
            float x = this.panelX();
            float y = this.panelY();
            float oldAlpha = Render2DUtil.getRenderAlpha();
            Render2DUtil.setRenderAlpha(oldAlpha * alpha);
            context.method_51448().pushMatrix();
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            try {
                this.drawGlass(matrix, x, y, 326.0f * s, 296.0f * s, 11.0f * s, -1710220734, 0);
                this.drawTitle(matrix, x, y, s);
                this.updateAnimations(mouseX, mouseY, seconds, s, x, y);
                this.drawTiles(context, matrix, x, y, mouseX, mouseY, s);
                FontRenderer.flushQueuedText(context);
            }
            finally {
                context.method_51448().popMatrix();
                Render2DUtil.setRenderAlpha(oldAlpha);
            }
        }
        finally {
            blur.setLiveMode(false);
            Render2DUtil.setActiveContext(null);
            Render2DUtil.setQueuedShapeActive(false);
        }
    }

    private void drawTitle(class_4587 matrix, float x, float y, float s) {
        FontRenderer font = Fonts.getMenuSize(WallpaperSelectorScreen.scaled(14, s), Fonts.Type.INTER_BOLD);
        float fontH = font.getStringHeight("\u041e\u0431\u043e\u0438");
        float titleCenterY = y + 38.0f * s / 2.0f;
        font.drawCenteredString(matrix, "\u041e\u0431\u043e\u0438", x + 326.0f * s / 2.0f, titleCenterY - fontH / 2.0f, -986119);
    }

    private void drawTiles(class_332 context, class_4587 matrix, float panelX, float panelY, int mouseX, int mouseY, float s) {
        WallpaperStore store = WallpaperStore.getInstance();
        List<TileData> tiles = this.buildTiles(store);
        float tileW = this.tileWidth(s);
        float tileH = this.tileHeight(s);
        float gridX = panelX + 14.0f * s;
        float gridY = panelY + 38.0f * s;
        float viewH = this.viewportHeight(s);
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), gridX, gridY, 298.0f * s, viewH);
        for (int i = 0; i < tiles.size(); ++i) {
            int outlineColor;
            TileData tile = tiles.get(i);
            int col = i % 3;
            int row = i / 3;
            float tx = gridX + (float)col * (tileW + 8.0f * s);
            float ty = gridY + ((float)row - this.animatedScroll) * (tileH + 8.0f * s);
            if (ty + tileH < gridY - tileH || ty > gridY + viewH + tileH) continue;
            float hover = i < this.tileHover.length ? this.tileHover[i].value() : 0.0f;
            float appear = i < this.tileAppear.length ? this.tileAppear[i].value() : 1.0f;
            float prevAlpha = Render2DUtil.getRenderAlpha();
            Render2DUtil.setRenderAlpha(prevAlpha * appear);
            context.method_51448().pushMatrix();
            boolean selected = tile.id.equals(store.selectedId());
            int n = outlineColor = selected ? -713789709 : 0;
            if (tile.type == TileType.DEFAULT) {
                this.drawDefaultTile(matrix, tx, ty, tileW, tileH, hover, s);
                this.drawImmediateGlassOutline(matrix, tx, ty, tileW, tileH, 6.0f * s, outlineColor, 1.0f * s);
            } else if (tile.type == TileType.LOADING) {
                this.drawLoadingTile(matrix, tx, ty, tileW, tileH, hover, s);
                this.drawImmediateGlassOutline(matrix, tx, ty, tileW, tileH, 6.0f * s, outlineColor, 1.0f * s);
            } else if (tile.type == TileType.CUSTOM) {
                this.drawCustomTile(matrix, tx, ty, tileW, tileH, tile, hover, s);
                this.drawImmediateGlassOutline(matrix, tx, ty, tileW, tileH, 6.0f * s, outlineColor, 1.0f * s);
                this.drawDeleteButton(matrix, tx + tileW - 14.0f * s, ty + 3.0f * s, mouseX, mouseY, s);
            } else {
                this.drawGlass(matrix, tx, ty, tileW, tileH, 6.0f * s, -1710220734, outlineColor, hover);
                this.drawAddTile(matrix, tx, ty, tileW, tileH, hover, s);
            }
            context.method_51448().popMatrix();
            Render2DUtil.setRenderAlpha(prevAlpha);
        }
        FontRenderer.flushQueuedText(context);
        scissor.pop();
    }

    private void drawDefaultTile(class_4587 matrix, float x, float y, float w, float h, float hover, float s) {
        class_2960 thumb = WallpaperSelectorScreen.getOrCreateDefaultThumb();
        ShapeProperties shape = ShapeProperties.create(matrix, x, y, w, h).round(6.0f * s).build();
        ShapeShaderRenderer.renderRoundTexture(shape, thumb, -1, 0.0f, 0.0f, 1.0f, 1.0f);
        this.drawImmediateGlassLayer(matrix, x, y, w, h, 6.0f * s, 0.5f, WallpaperSelectorScreen.mix(0x14000000, 0, hover));
    }

    private void drawLoadingTile(class_4587 matrix, float x, float y, float w, float h, float hover, float s) {
        class_2960 thumb = WallpaperSelectorScreen.getOrCreateLoadingThumb();
        ShapeProperties shape = ShapeProperties.create(matrix, x, y, w, h).round(6.0f * s).build();
        ShapeShaderRenderer.renderRoundTexture(shape, thumb, -1, 0.0f, 0.0f, 1.0f, 1.0f);
        this.drawImmediateGlassLayer(matrix, x, y, w, h, 6.0f * s, 0.5f, WallpaperSelectorScreen.mix(0x14000000, 0, hover));
    }

    private void drawCustomTile(class_4587 matrix, float x, float y, float w, float h, TileData tile, float hover, float s) {
        class_2960 thumb = WallpaperStore.getInstance().loadThumb(tile.id);
        if (thumb != null) {
            int[] dim = WallpaperStore.getInstance().thumbSize(tile.id);
            float imgAspect = (float)dim[0] / (float)Math.max(1, dim[1]);
            float tileAspect = w / h;
            float u1 = 0.0f;
            float u2 = 1.0f;
            float v1 = 0.0f;
            float v2 = 1.0f;
            float visible;
            if (imgAspect > tileAspect) {
                visible = tileAspect / imgAspect;
                u1 = (1.0f - visible) / 2.0f;
                u2 = 1.0f - u1;
            } else {
                visible = imgAspect / tileAspect;
                v1 = (1.0f - visible) / 2.0f;
                v2 = 1.0f - v1;
            }
            ShapeProperties shape = ShapeProperties.create(matrix, x, y, w, h).round(6.0f * s).build();
            ShapeShaderRenderer.renderRoundTexture(shape, thumb, -1, u1, v1, u2, v2);
            this.drawImmediateGlassLayer(matrix, x, y, w, h, 6.0f * s, 0.5f, WallpaperSelectorScreen.mix(0x18000000, 0, hover));
        } else {
            this.drawGlass(matrix, x, y, w, h, 6.0f * s, -1710220734, 0, hover);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawAddTile(class_4587 matrix, float x, float y, float w, float h, float hover, float s) {
        float cx = x + w / 2.0f;
        float cy = y + h / 2.0f;
        int color = WallpaperSelectorScreen.mix(-8747366, -1, hover);
        float len = 14.0f * s;
        float thick = 2.0f * s;
        float halfLen = len / 2.0f;
        float halfThick = thick / 2.0f;
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, cx - halfLen, cy - halfThick, len, thick).round(halfThick).color(color).build());
            rectangle.render(ShapeProperties.create(matrix, cx - halfThick, cy - halfLen, thick, len).round(halfThick).color(color).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    private void drawDeleteButton(class_4587 matrix, float x, float y, int mouseX, int mouseY, float s) {
        float size = 11.0f * s;
        boolean hovered = WallpaperSelectorScreen.inside(mouseX, mouseY, x - 3.0f * s, y - 3.0f * s, size + 6.0f * s, size + 6.0f * s);
        int color = hovered ? -56781 : -285258918;
        FontRenderer icon = Fonts.getMenuSize(WallpaperSelectorScreen.scaled(13, s), Fonts.Type.COMBINED_ICONS);
        float iconH = icon.getStringHeight(DELETE_ICON);
        icon.drawCenteredString(matrix, DELETE_ICON, x + size / 2.0f, y + size / 2.0f - iconH / 2.0f, color);
    }

    private void updateAnimations(int mouseX, int mouseY, float seconds, float s, float panelX, float panelY) {
        this.animatedScroll = MenuMotion.interpolate(this.animatedScroll, this.scroll, 2.0f, seconds);
        if (Math.abs(this.animatedScroll - this.scroll) < 0.001f) {
            this.animatedScroll = this.scroll;
        }
        List<TileData> tiles = this.buildTiles(WallpaperStore.getInstance());
        float tileW = this.tileWidth(s);
        float tileH = this.tileHeight(s);
        float gridX = panelX + 14.0f * s;
        float gridY = panelY + 38.0f * s;
        float viewH = this.viewportHeight(s);
        for (int i = 0; i < tiles.size() && i < this.tileHover.length; ++i) {
            int col = i % 3;
            int row = i / 3;
            float tx = gridX + (float)col * (tileW + 8.0f * s);
            float ty = gridY + ((float)row - this.animatedScroll) * (tileH + 8.0f * s);
            boolean hovered = WallpaperSelectorScreen.inside(mouseX, mouseY, gridX, gridY, 298.0f * s, viewH) && WallpaperSelectorScreen.inside(mouseX, mouseY, tx, ty, tileW, tileH);
            this.tileHover[i].update(hovered ? 1.0f : 0.0f, 4.0f, seconds);
            this.tileAppear[i].update(1.0f, 0.75f, seconds);
        }
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        double my;
        if (this.closing) {
            return true;
        }
        int button = click.comp_4800().comp_4801();
        if (button != 0 && button != 1) {
            return true;
        }
        float s = this.scale();
        float gridX = this.panelX() + 14.0f * s;
        float gridY = this.panelY() + 38.0f * s;
        float tileW = this.tileWidth(s);
        float tileH = this.tileHeight(s);
        double mx = click.comp_4798();
        if (!WallpaperSelectorScreen.inside(mx, my = click.comp_4799(), gridX, gridY, 298.0f * s, this.viewportHeight(s))) {
            return true;
        }
        WallpaperStore store = WallpaperStore.getInstance();
        List<TileData> tiles = this.buildTiles(store);
        for (int i = 0; i < tiles.size(); ++i) {
            int col = i % 3;
            float tx = gridX + (float)col * (tileW + 8.0f * s);
            int row = i / 3;
            float ty = gridY + ((float)row - this.animatedScroll) * (tileH + 8.0f * s);
            if (!WallpaperSelectorScreen.inside(mx, my, tx, ty, tileW, tileH)) continue;
            TileData tile = tiles.get(i);
            if (tile.type == TileType.ADD) {
                if (button == 1) {
                    class_156.method_668().method_672(store.directory());
                } else {
                    this.openFileDialog();
                }
            } else if (tile.type == TileType.CUSTOM && WallpaperSelectorScreen.inside(mx, my, tx + tileW - 17.0f * s, ty + 1.0f * s, 16.0f * s, 16.0f * s)) {
                store.remove(tile.id);
            } else {
                store.select(tile.id);
            }
            return true;
        }
        return true;
    }

    public boolean method_25401(double mouseX, double mouseY, double hAmount, double vAmount) {
        if (this.closing) {
            return true;
        }
        float s = this.scale();
        if (!WallpaperSelectorScreen.inside(mouseX, mouseY, this.panelX() + 14.0f * s, this.panelY() + 38.0f * s, 298.0f * s, this.viewportHeight(s))) {
            return false;
        }
        int totalTiles = 2 + WallpaperStore.getInstance().entries().size() + 1;
        int totalRows = (totalTiles + 3 - 1) / 3;
        float maxScroll = Math.max(0.0f, (float)(totalRows - 4));
        this.scroll = Math.max(0.0f, Math.min(maxScroll, this.scroll - (float)vAmount * 0.68f));
        return true;
    }

    public boolean method_25404(class_11908 key) {
        if (this.closing) {
            return true;
        }
        if (key.comp_4795() == 256) {
            this.method_25419();
            return true;
        }
        return true;
    }

    private void openFileDialog() {
        new Thread(() -> {
            boolean success = false;
            try {
                String cmd = "Add-Type -AssemblyName System.Windows.Forms; $f = New-Object System.Windows.Forms.OpenFileDialog; $f.Title = '\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435'; $f.Filter = '\u0418\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u044f (*.png;*.jpg;*.jpeg;*.bmp)|*.png;*.jpg;*.jpeg;*.bmp|\u0412\u0441\u0435 \u0444\u0430\u0439\u043b\u044b (*.*)|*.*'; $form = New-Object System.Windows.Forms.Form; $form.TopMost = $true; $form.Show(); $form.Activate(); $form.Visible = $false; if ($f.ShowDialog($form) -eq [System.Windows.Forms.DialogResult]::OK) { [Console]::WriteLine($f.FileName) }; $form.Dispose()";
                Process process = new ProcessBuilder("powershell.exe", "-Sta", "-NoProfile", "-Command", cmd).redirectErrorStream(true).start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                    File file;
                    String line = reader.readLine();
                    process.waitFor();
                    if (line != null && !line.trim().isEmpty() && (file = new File(line.trim())).exists()) {
                        this.onFileChosen(file);
                        success = true;
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (!success) {
                try {
                    EventQueue.invokeLater(() -> {
                        try {
                            File selected;
                            Frame frame = new Frame();
                            frame.setUndecorated(true);
                            frame.setAlwaysOnTop(true);
                            frame.setLocationRelativeTo(null);
                            frame.setVisible(true);
                            frame.toFront();
                            FileDialog dialog = new FileDialog(frame, "\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435", 0);
                            dialog.setFilenameFilter((dir, name) -> {
                                String n = name.toLowerCase(Locale.ROOT);
                                return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".bmp");
                            });
                            dialog.setVisible(true);
                            String file = dialog.getFile();
                            String dir2 = dialog.getDirectory();
                            dialog.dispose();
                            frame.dispose();
                            if (file != null && dir2 != null && (selected = new File(dir2, file)).exists()) {
                                this.onFileChosen(selected);
                            }
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    });
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }, "Recoin wallpaper picker").start();
    }

    private void onFileChosen(File file) {
        mc.execute(() -> {
            WallpaperStore store = WallpaperStore.getInstance();
            String id = store.add(file);
            if (id != null) {
                store.loadThumb(id);
                store.select(id);
            }
        });
    }

    private List<TileData> buildTiles(WallpaperStore store) {
        ArrayList<TileData> tiles = new ArrayList<TileData>();
        tiles.add(new TileData("default", TileType.DEFAULT));
        tiles.add(new TileData("loading", TileType.LOADING));
        for (WallpaperStore.Entry entry : store.entries()) {
            tiles.add(new TileData(entry.id, TileType.CUSTOM));
        }
        tiles.add(new TileData("add", TileType.ADD));
        return tiles;
    }

    private void drawGlass(class_4587 matrix, float x, float y, float width, float height, float round, int fill, int outline) {
        this.drawGlass(matrix, x, y, width, height, round, fill, outline, 0.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawGlass(class_4587 matrix, float x, float y, float width, float height, float round, int fill, int outline, float hover) {
        boolean oldHud = Render2DUtil.isHudDecorationActive();
        boolean oldGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.setShadowEnabled(false);
            blur.setColorlessMode(true);
            blur.setStyledBlurRadius(28.0f);
            blur.render(ShapeProperties.create(matrix, x, y, width, height).round(round).softness(5.0f).quality(50.0f).thickness(0.65f).outlineColor(outline).color(ColorUtil.multAlpha(fill, 1.0f - hover * 0.18f)).build());
        }
        finally {
            blur.resetStyledBlurRadius();
            blur.setColorlessMode(false);
            blur.setShadowEnabled(true);
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(oldHud);
            Render2DUtil.setGuiDecorationActive(oldGui);
        }
        this.drawImmediateGlassLayer(matrix, x, y, width, height, round, 0.5f, ColorUtil.multAlpha(1375797768, 1.0f - hover * 0.34f));
        this.drawImmediateGlassLayer(matrix, x, y, width, height, round, -0.1f, ColorUtil.getOutline(0.8f - hover * 0.16f));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawImmediateGlassLayer(class_4587 matrix, float x, float y, float width, float height, float round, float softness, int color) {
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(round).softness(softness).thickness(0.0f).quality(50.0f).outlineColor(0).color(color).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawImmediateGlassOutline(class_4587 matrix, float x, float y, float width, float height, float round, int outlineColor, float thickness) {
        boolean queued = Render2DUtil.isQueuedShapeActive();
        Render2DUtil.setQueuedShapeActive(false);
        try {
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(round).softness(0.5f).thickness(thickness).outlineColor(outlineColor).color(0).build());
        }
        finally {
            Render2DUtil.setQueuedShapeActive(queued);
        }
    }

    private float tileWidth(float s) {
        return 94.0f * s;
    }

    private float tileHeight(float s) {
        return this.tileWidth(s) * 9.0f / 16.0f;
    }

    private float viewportHeight(float s) {
        float tileH = this.tileHeight(s);
        return 4.0f * (tileH + 8.0f * s) - 8.0f * s;
    }

    private void clampScroll() {
        int totalTiles = 2 + WallpaperStore.getInstance().entries().size() + 1;
        int totalRows = (totalTiles + 3 - 1) / 3;
        float maxScroll = Math.max(0.0f, (float)(totalRows - 4));
        this.scroll = Math.max(0.0f, Math.min(this.scroll, maxScroll));
        this.animatedScroll = Math.max(0.0f, Math.min(this.animatedScroll, maxScroll));
    }

    private float frameSeconds() {
        long now = System.nanoTime();
        float seconds = Math.min(0.05f, Math.max(0.0f, (float)(now - this.lastFrameNanos) / 1.0E9f));
        this.lastFrameNanos = now;
        return seconds;
    }

    private void beginAnimationFrame() {
        if (this.animationStarted) {
            return;
        }
        this.lastFrameNanos = this.openedAt = System.nanoTime();
        this.animationStarted = true;
    }

    private float transitionAnimation() {
        if (!this.animationStarted) {
            return 0.0f;
        }
        long elapsed = System.nanoTime() - (this.closing ? this.closingAt : this.openedAt);
        float duration = (float)(this.closing ? 130L : 160L) * 1000000.0f;
        float t = Math.max(0.0f, Math.min(1.0f, (float)elapsed / duration));
        float fade = t * t * (3.0f - 2.0f * t);
        return this.closing ? 1.0f - fade : fade;
    }

    private float scale() {
        return Math.max(0.72f, Math.min(1.0f, Math.min((float)this.field_22789 / 520.0f, (float)this.field_22790 / 390.0f)));
    }

    private float panelX() {
        return ((float)this.field_22789 - 326.0f * this.scale()) / 2.0f;
    }

    private float panelY() {
        return ((float)this.field_22790 - 296.0f * this.scale()) / 2.0f;
    }

    private static int scaled(int size, float scale) {
        return Math.max(6, Math.round((float)size * scale));
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
    }

    private static int mix(int from, int to, float value) {
        value = Math.max(0.0f, Math.min(1.0f, value));
        int a = Math.round((float)(from >>> 24 & 0xFF) + (float)((to >>> 24 & 0xFF) - (from >>> 24 & 0xFF)) * value);
        int r = Math.round((float)(from >>> 16 & 0xFF) + (float)((to >>> 16 & 0xFF) - (from >>> 16 & 0xFF)) * value);
        int g = Math.round((float)(from >>> 8 & 0xFF) + (float)((to >>> 8 & 0xFF) - (from >>> 8 & 0xFF)) * value);
        int b = Math.round((float)(from & 0xFF) + (float)((to & 0xFF) - (from & 0xFF)) * value);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public void method_25419() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        this.closingAt = System.nanoTime();
    }

    public void method_25393() {
        if (this.closing && System.nanoTime() - this.closingAt >= 130000000L) {
            WallpaperStore.getInstance().unloadThumbs();
            mc.method_1507(this.parent);
            return;
        }
        super.method_25393();
    }

    public boolean method_25422() {
        return false;
    }

    private record TileData(String id, TileType type) {
    }

    private static enum TileType {
        DEFAULT,
        LOADING,
        CUSTOM,
        ADD;

    }
}

