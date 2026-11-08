package ru.recoin.implement.features.modules.misc.minigames;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;

/**
 * Upgrader (like on CS case sites, but only with virtual items and coins of this client):
 * pick one of your items, pick a more expensive target. The chance is your price / target price
 * (x2 = 50%, x8 = 12.5%). The arrow spins over the wheel: stopping in the green sector upgrades
 * the item, otherwise it burns. Nothing is real: no server items, no money.
 */
public final class UpgraderScreen extends GameScreen {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long SPIN_MS = 4200L;
    private static final double[] QUICK = {1.5, 2.0, 3.0, 5.0, 8.0, 10.0};
    /** Luck bonus: chances are 35% higher than the fair price ratio (x8 = 16.9% instead of 12.5%), max 95%. */
    public static final double LUCK = 1.35;

    public static double chanceFor(double multiplier) {
        return multiplier <= 1.0 ? 0.0 : Math.min(0.95, LUCK / multiplier);
    }

    private record Confetti(float x, float y, float vx, float vy, int color, float spin) {
    }

    private final java.util.List<Confetti> confetti = new java.util.ArrayList<>();
    private long confettiAt;
    private long shakeAt;

    private String stake = "";
    private boolean shopMode;
    private String target = "";
    private int targetScroll;
    private boolean spinning;
    private long spinStart;
    private double spinFrom;
    private double spinTo;
    private boolean pendingWin;
    private double angle;          // pointer angle in degrees, 0 = up, clockwise
    private String resultText = "";
    private int resultColor = TEXT;
    private long resultAt;
    private int lastTick = -1;

    public UpgraderScreen() {
        super("Upgrader");
        MiniGameStore.Data d = MiniGameStore.get();
        for (UpgradeItems.Item it : UpgradeItems.ALL) {
            if (d.items.getOrDefault(it.id(), 0) > 0) {
                this.stake = it.id();   // the most expensive owned item ends up selected
            }
        }
    }

    private UpgradeItems.Item stakeItem() {
        return UpgradeItems.get(this.stake);
    }

    private UpgradeItems.Item targetItem() {
        return UpgradeItems.get(this.target);
    }

    private double chance() {
        UpgradeItems.Item s = this.stakeItem();
        UpgradeItems.Item t = this.targetItem();
        if (s == null || t == null || t.price() <= s.price()) {
            return 0.0;
        }
        return UpgraderScreen.chanceFor((double)t.price() / s.price());
    }

    private void quick(double mult) {
        UpgradeItems.Item s = this.stakeItem();
        if (s == null) return;
        UpgradeItems.Item best = null;
        for (UpgradeItems.Item it : UpgradeItems.ALL) {
            double m = (double)it.price() / s.price();
            if (m >= mult - 1e-6 && (best == null || it.price() < best.price())) {
                best = it;
            }
        }
        if (best != null) this.target = best.id();
    }

    private void upgrade() {
        double chance = this.chance();
        if (this.spinning || chance <= 0.0 || MiniGameStore.count(this.stake) <= 0) {
            return;
        }
        double roll = RANDOM.nextDouble();
        this.pendingWin = roll < chance;
        // green sector is [0, chance*360) from the top; the arrow stops where the roll fell
        double stop = roll * 360.0;
        double base = this.angle % 360.0;
        this.spinFrom = this.angle;
        this.spinTo = this.angle - base + 360.0 * 6 + stop;
        this.spinStart = System.currentTimeMillis();
        this.spinning = true;
        this.resultText = "";
        MiniGameStore.addItem(this.stake, -1);   // the stake is used up either way
        MiniGameStore.save();
    }

    private void finishSpin() {
        this.spinning = false;
        this.angle = this.spinTo;
        UpgradeItems.Item s = this.stakeItem();
        UpgradeItems.Item t = this.targetItem();
        MiniGameStore.Data d = MiniGameStore.get();
        if (this.pendingWin && t != null) {
            MiniGameStore.addItem(t.id(), 1);
            int x10 = s == null ? 0 : (int)Math.round(t.price() * 10.0 / s.price());
            d.bestUpgradeX10 = Math.max(d.bestUpgradeX10, x10);
            this.resultText = "УСПЕХ!  " + (s == null ? "" : s.name() + " → ") + t.name() + String.format("  (x%.1f)", x10 / 10.0);
            this.resultColor = GREEN;
            GameScreen.sound("minecraft:ui.toast.challenge_complete", 1.0f);
            this.burst(this.field_22789 / 2, this.field_22790 / 2);
            this.stake = t.id();
            this.target = "";
        } else {
            this.resultText = "Не повезло: " + (s == null ? "предмет" : s.name()) + " сгорел";
            this.resultColor = RED;
            this.shakeAt = System.currentTimeMillis();
            GameScreen.sound("minecraft:entity.item.break", 0.8f);
            if (MiniGameStore.count(this.stake) <= 0) {
                this.stake = "";
            }
        }
        this.resultAt = System.currentTimeMillis();
        MiniGameStore.save();
    }

    private void sell() {
        UpgradeItems.Item s = this.stakeItem();
        if (this.spinning || s == null || MiniGameStore.count(s.id()) <= 0) return;
        MiniGameStore.addItem(s.id(), -1);
        MiniGameStore.get().coins += Math.max(1L, s.price() * 8L / 10L);
        if (MiniGameStore.count(s.id()) <= 0) this.stake = "";
        MiniGameStore.save();
        GameScreen.sound("minecraft:entity.experience_orb.pickup", 0.8f);
    }

    private void buy(UpgradeItems.Item item) {
        MiniGameStore.Data d = MiniGameStore.get();
        if (this.spinning || item == null || d.coins < item.price()) return;
        d.coins -= item.price();
        MiniGameStore.addItem(item.id(), 1);
        this.stake = item.id();
        this.shopMode = false;
        MiniGameStore.save();
        GameScreen.sound("minecraft:entity.experience_orb.pickup", 1.2f);
    }

    @Override
    protected void draw(class_332 c, float delta) {
        int w = Math.min(470, this.field_22789 - 16);
        int h = Math.min(270, this.field_22790 - 16);
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
        this.panel(c, x, y, w, h, BG);
        MiniGameStore.Data d = MiniGameStore.get();
        this.size(GameScreen.TITLE);
        this.text(c, "Upgrader", x + 10, y + 6, GOLD);
        this.size(GameScreen.BODY);
        this.text(c, "виртуальные предметы · удача +35%", x + 80, y + 9, MUTED);
        String coins = "⛁ " + d.coins;
        this.text(c, coins, x + w - 10 - this.width(coins), y + 9, GOLD);
        c.method_25294(x + 8, y + 22, x + w - 8, y + 23, 0x30FFFFFF);
        if (this.spinning) {
            long t = System.currentTimeMillis() - this.spinStart;
            float p = Math.min(1.0f, t / (float)SPIN_MS);
            float ease = 1.0f - (float)Math.pow(1.0f - p, 4.0);
            this.angle = this.spinFrom + (this.spinTo - this.spinFrom) * ease;
            int tick = (int)(this.angle / 12.0);
            if (tick != this.lastTick) {
                this.lastTick = tick;
                GameScreen.sound("minecraft:block.note_block.hat", 1.6f);
            }
            if (p >= 1.0f) {
                this.finishSpin();
            }
        }
        int colW = 130;
        this.drawInventory(c, x + 8, y + 28, colW, h - 36);
        this.drawTargets(c, x + w - colW - 8, y + 28, colW, h - 36);
        this.drawWheel(c, x + 8 + colW + 6, y + 28, w - 2 * colW - 28, h - 36);
    }

    private void drawInventory(class_332 c, int x, int y, int w, int h) {
        this.panel(c, x, y, w, h, CARD);
        this.text(c, "Твои предметы", x + 6, y + 5, TEXT);
        List<UpgradeItems.Item> owned = new ArrayList<>();
        for (UpgradeItems.Item it : UpgradeItems.ALL) {
            if (MiniGameStore.count(it.id()) > 0) owned.add(it);
        }
        int cell = 22;
        int cols = Math.max(1, (w - 8) / cell);
        for (int i = 0; i < owned.size(); ++i) {
            UpgradeItems.Item it = owned.get(i);
            int cx = x + 4 + (i % cols) * cell;
            int cy = y + 18 + (i / cols) * cell;
            if (cy + cell > y + h - 46) break;
            boolean sel = it.id().equals(this.stake);
            boolean hover = this.hovered(cx, cy, cell - 2, cell - 2);
            c.method_25294(cx, cy, cx + cell - 2, cy + cell - 2, sel ? 0xFF3A4660 : hover ? 0xFF2C3446 : 0xFF232A38);
            c.method_25294(cx, cy + cell - 4, cx + cell - 2, cy + cell - 2, it.color());
            c.method_51427(UpgradeItems.stack(it), cx + 2, cy + 1);
            int n = MiniGameStore.count(it.id());
            if (n > 1) this.text(c, String.valueOf(n), cx + cell - 4 - this.width(String.valueOf(n)), cy + 10, TEXT);
            if (sel) this.frame(c, cx, cy, cell - 2, cell - 2, GOLD);
            if (!this.spinning) this.hitArea(cx, cy, cell - 2, cell - 2, () -> this.stake = it.id());
        }
        if (owned.isEmpty()) {
            this.text(c, "Пусто. Купи справа", x + 6, y + 22, MUTED);
            this.text(c, "или пройди MineQuiz", x + 6, y + 33, MUTED);
        }
        UpgradeItems.Item s = this.stakeItem();
        int by = y + h - 44;
        this.text(c, s == null ? "Ставка не выбрана" : this.fit(s.name(), w - 10), x + 6, by, s == null ? MUTED : s.color());
        this.text(c, s == null ? "" : "⛁ " + s.price(), x + 6, by + 10, GOLD);
        this.button(c, x + 4, y + h - 22, w / 2 - 6, 18, "Продать", 0xFF5A3A1E, TEXT, s != null && !this.spinning, this::sell);
        boolean daily = MiniGameStore.dailyBonusReady();
        this.button(c, x + w / 2 + 2, y + h - 22, w / 2 - 6, 18, daily ? "+300 ⛁" : "Завтра", 0xFF2E5E2E, TEXT, daily && !this.spinning, () -> {
            MiniGameStore.takeDailyBonus(300L);
            GameScreen.sound("minecraft:entity.player.levelup", 1.2f);
        });
    }

    private void drawTargets(class_332 c, int x, int y, int w, int h) {
        this.panel(c, x, y, w, h, CARD);
        UpgradeItems.Item stakeNow = this.stakeItem();
        boolean shop = this.shopMode || stakeNow == null;
        UpgradeItems.Item s = shop ? null : stakeNow;
        this.text(c, shop ? "Магазин" : "Улучшить до", x + 6, y + 5, TEXT);
        this.button(c, x + w - 52, y + 2, 48, 13, shop ? "Апгрейд" : "Магазин", 0xFF2A3140, TEXT, !this.spinning && (stakeNow != null || !shop), () -> {
            this.shopMode = !this.shopMode;
            this.targetScroll = 0;
        });
        List<UpgradeItems.Item> list = new ArrayList<>();
        for (UpgradeItems.Item it : UpgradeItems.ALL) {
            if (shop ? it.price() <= 3000L : it.price() > s.price()) list.add(it);
        }
        int rowH = 20;
        int visible = Math.max(1, (h - 22) / rowH);
        this.targetScroll = Math.max(0, Math.min(this.targetScroll, Math.max(0, list.size() - visible)));
        for (int i = 0; i < visible && i + this.targetScroll < list.size(); ++i) {
            UpgradeItems.Item it = list.get(i + this.targetScroll);
            int ry = y + 18 + i * rowH;
            boolean sel = it.id().equals(this.target);
            boolean hover = this.hovered(x + 3, ry, w - 6, rowH - 2);
            c.method_25294(x + 3, ry, x + w - 3, ry + rowH - 2, sel ? 0xFF3A4660 : hover ? 0xFF2C3446 : 0xFF232A38);
            c.method_25294(x + 3, ry, x + 5, ry + rowH - 2, it.color());
            c.method_51427(UpgradeItems.stack(it), x + 7, ry + 1);
            this.text(c, this.fit(it.name(), w - 70), x + 26, ry + 2, TEXT);
            if (s != null) {
                double m = (double)it.price() / s.price();
                String info = String.format("x%.1f  %.1f%%", m, UpgraderScreen.chanceFor(m) * 100.0);
                this.text(c, info, x + 26, ry + 11, m >= 8 ? RED : m >= 3 ? GOLD : GREEN);
            } else {
                this.text(c, "Купить ⛁ " + it.price(), x + 26, ry + 11, MiniGameStore.get().coins >= it.price() ? GOLD : RED);
            }
            if (sel) this.frame(c, x + 3, ry, w - 6, rowH - 2, GOLD);
            if (!this.spinning) {
                this.hitArea(x + 3, ry, w - 6, rowH - 2, s == null ? () -> this.buy(it) : () -> this.target = it.id());
            }
        }
        if (list.isEmpty()) {
            this.text(c, "Это самый дорогой", x + 6, y + 22, MUTED);
            this.text(c, "предмет!", x + 6, y + 33, MUTED);
        }
    }

    private void drawWheel(class_332 c, int x, int y, int w, int h) {
        this.panel(c, x, y, w, h, CARD);
        int cx = x + w / 2;
        int cy = y + 16 + Math.min(w, h - 80) / 2;
        int r = Math.max(30, Math.min(w, h - 80) / 2 - 8);
        double chance = this.chance();
        // shake the wheel a little after a lost spin
        long sinceShake = System.currentTimeMillis() - this.shakeAt;
        if (sinceShake < 450L) {
            cx += (int)Math.round(Math.sin(sinceShake / 18.0) * 3.0 * (1.0 - sinceShake / 450.0));
        }
        // ring: bright green win sector from the top (clockwise), dark lose sector, 6 px thick
        int dots = 240;
        double pointerA = ((this.angle % 360.0) + 360.0) % 360.0;
        for (int i = 0; i < dots; ++i) {
            double a = i * 360.0 / dots;
            boolean win = a < chance * 360.0;
            double rad = Math.toRadians(a);
            double nearPointer = Math.abs(((a - pointerA) + 540.0) % 360.0 - 180.0);
            float glow = (float)Math.exp(-nearPointer / 10.0);
            int col = win ? GameScreen.lighten(win && glow > 0.3f ? 0xFF6CFF8A : GREEN) : (glow > 0.3f ? 0xFF5A6478 : 0xFF2E3444);
            for (int k = 0; k < 6; ++k) {
                int px = cx + (int)Math.round(Math.sin(rad) * (r - k));
                int py = cy - (int)Math.round(Math.cos(rad) * (r - k));
                c.method_25294(px - 1, py - 1, px + 2, py + 2, col);
            }
        }
        // tick marks every 10 %
        for (int i = 0; i < 10; ++i) {
            double rad = Math.toRadians(i * 36.0);
            for (int k = 8; k < 12; ++k) {
                int px = cx + (int)Math.round(Math.sin(rad) * (r - k));
                int py = cy - (int)Math.round(Math.cos(rad) * (r - k));
                c.method_25294(px, py, px + 1, py + 1, 0x90FFFFFF);
            }
        }
        // pointer with a soft glow
        double rad = Math.toRadians(this.angle);
        int pointerColor = this.spinning ? GOLD : !this.resultText.isEmpty() && System.currentTimeMillis() - this.resultAt < 6000L ? this.resultColor : TEXT;
        for (int k = 0; k < r - 8; k += 2) {
            int px = cx + (int)Math.round(Math.sin(rad) * k);
            int py = cy - (int)Math.round(Math.cos(rad) * k);
            int size = k > r - 20 ? 4 : 2;
            c.method_25294(px - size, py - size, px + size + 1, py + size + 1, (pointerColor & 0x00FFFFFF) | 0x30000000);
            c.method_25294(px - size / 2, py - size / 2, px + size - size / 2, py + size - size / 2, pointerColor);
        }
        c.method_25294(cx - 3, cy - 3, cx + 4, cy + 4, GOLD);
        UpgradeItems.Item s = this.stakeItem();
        UpgradeItems.Item t = this.targetItem();
        String pct = chance > 0 ? String.format("%.2f%%", chance * 100.0) : "—";
        this.centered(c, pct, cx, cy + 10, chance > 0 ? GREEN : MUTED);
        // stake -> target
        int iy = y + h - 64;
        if (s != null) c.method_51427(UpgradeItems.stack(s), cx - 40, iy);
        this.centered(c, "→", cx, iy + 4, MUTED);
        if (t != null) c.method_51427(UpgradeItems.stack(t), cx + 24, iy);
        if (s != null && t != null) {
            this.centered(c, String.format("x%.1f", (double)t.price() / s.price()), cx, iy - 10, GOLD);
        }
        // quick multipliers
        int qw = (w - 8) / QUICK.length;
        for (int i = 0; i < QUICK.length; ++i) {
            double m = QUICK[i];
            String label = m == Math.floor(m) ? "x" + (int)m : "x" + m;
            this.button(c, x + 4 + i * qw, y + h - 44, qw - 2, 14, label, 0xFF2A3140, TEXT, s != null && !this.spinning, () -> this.quick(m));
        }
        boolean can = s != null && t != null && chance > 0 && !this.spinning && MiniGameStore.count(s.id()) > 0;
        this.button(c, x + 4, y + h - 26, w - 8, 22, this.spinning ? "Крутится..." : "УЛУЧШИТЬ", can ? 0xFFB8860B : 0xFF3A3A3A, TEXT, can, this::upgrade);
        this.drawConfetti(c);
        if (!this.resultText.isEmpty() && System.currentTimeMillis() - this.resultAt < 6000L) {
            int tw = this.width(this.resultText) + 16;
            int rx = cx - tw / 2;
            int ry = y + 4;
            this.panel(c, rx, ry, tw, 14, 0xF0101010);
            this.centered(c, this.resultText, cx, ry + 3, this.resultColor);
        }
    }

    private void burst(int cx, int cy) {
        this.confetti.clear();
        int[] colors = {GREEN, GOLD, BLUE, 0xFFFF5EA8, 0xFFB15CFF, 0xFFFFFFFF};
        for (int i = 0; i < 70; ++i) {
            double a = RANDOM.nextDouble() * Math.PI * 2.0;
            float sp = 60.0f + RANDOM.nextFloat() * 160.0f;
            this.confetti.add(new Confetti(cx, cy, (float)Math.cos(a) * sp, (float)Math.sin(a) * sp - 80.0f, colors[RANDOM.nextInt(colors.length)], RANDOM.nextFloat() * 6.0f));
        }
        this.confettiAt = System.currentTimeMillis();
    }

    private void drawConfetti(class_332 c) {
        if (this.confetti.isEmpty()) return;
        float t = (System.currentTimeMillis() - this.confettiAt) / 1000.0f;
        if (t > 1.8f) {
            this.confetti.clear();
            return;
        }
        float alpha = Math.min(1.0f, (1.8f - t) / 0.6f);
        for (Confetti p : this.confetti) {
            float x = p.x() + p.vx() * t;
            float y = p.y() + p.vy() * t + 220.0f * t * t;   // gravity
            int size = 2 + (int)(Math.abs(Math.sin(p.spin() + t * 8.0)) * 2.0);
            int col = ((int)((p.color() >>> 24) * alpha) << 24) | (p.color() & 0xFFFFFF);
            c.method_25294((int)x, (int)y, (int)x + size, (int)y + 2, col);
        }
    }

    private String fit(String s, int maxWidth) {
        if (this.width(s) <= maxWidth) return s;
        String out = s;
        while (out.length() > 1 && this.width(out + "…") > maxWidth) out = out.substring(0, out.length() - 1);
        return out + "…";
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double h, double v) {
        this.targetScroll -= (int)Math.signum(v);
        return true;
    }

    @Override
    protected boolean keyPressed(int key) {
        if (key == 257 || key == 335 || key == 32) {
            this.upgrade();
            return true;
        }
        return false;
    }
}
