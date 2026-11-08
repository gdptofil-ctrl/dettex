package ru.recoin.implement.features.modules.misc.minigames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_332;

/**
 * MineQuiz: short lessons of questions about Minecraft and Russian anarchy servers.
 * 10 questions, 3 hearts, XP and coins for right answers (coins go to the Upgrader), a day streak.
 * Keys: 1-4 choose an answer, Enter / Space = next.
 */
public final class QuizScreen extends GameScreen {
    private static final int LESSON_SIZE = 10;
    private static final int XP_PER_RIGHT = 10;
    private static final int COINS_PER_RIGHT = 40;

    private enum Phase { MENU, QUESTION, RESULT }

    private Phase phase = Phase.MENU;
    private List<QuizBank.Question> lesson = List.of();
    private int index;
    private int hearts;
    private int right;
    private int combo;
    private int bestCombo;
    private String[] shown = new String[0];
    private int rightIndex;
    private int picked = -1;
    private long pickedAt;
    private boolean failed;
    private long phaseAt = System.currentTimeMillis();

    public QuizScreen() {
        super("MineQuiz");
    }

    private void start(String category) {
        this.lesson = QuizBank.lesson(category, LESSON_SIZE);
        if (this.lesson.isEmpty()) {
            return;
        }
        this.index = 0;
        this.hearts = 3;
        this.right = 0;
        this.combo = 0;
        this.bestCombo = 0;
        this.failed = false;
        this.prepare();
        this.phase = Phase.QUESTION;
        this.phaseAt = System.currentTimeMillis();
    }

    private void prepare() {
        QuizBank.Question q = this.lesson.get(this.index);
        List<String> answers = new ArrayList<>(List.of(q.answers()));
        String correct = answers.get(0);
        Collections.shuffle(answers);
        this.shown = answers.toArray(new String[0]);
        this.rightIndex = answers.indexOf(correct);
        this.picked = -1;
    }

    private void pick(int i) {
        if (this.picked >= 0 || i >= this.shown.length) {
            return;
        }
        this.picked = i;
        this.pickedAt = System.currentTimeMillis();
        if (i == this.rightIndex) {
            ++this.right;
            ++this.combo;
            this.bestCombo = Math.max(this.bestCombo, this.combo);
            GameScreen.sound("minecraft:entity.experience_orb.pickup", 1.0f + Math.min(0.6f, this.combo * 0.08f));
        } else {
            --this.hearts;
            this.combo = 0;
            GameScreen.sound("minecraft:entity.villager.no", 1.0f);
        }
    }

    private void next() {
        if (this.picked < 0) {
            return;
        }
        if (this.hearts <= 0) {
            this.finish(true);
            return;
        }
        if (++this.index >= this.lesson.size()) {
            this.finish(false);
            return;
        }
        this.prepare();
    }

    private void finish(boolean outOfHearts) {
        this.failed = outOfHearts;
        int xp = this.right * XP_PER_RIGHT + (outOfHearts ? 0 : 20) + this.bestCombo * 2;
        int coins = this.right * COINS_PER_RIGHT + (outOfHearts ? 0 : 100);
        MiniGameStore.lessonFinished(xp, coins);
        this.lastXp = xp;
        this.lastCoins = coins;
        this.phase = Phase.RESULT;
        this.phaseAt = System.currentTimeMillis();
        GameScreen.sound(outOfHearts ? "minecraft:block.anvil.land" : "minecraft:ui.toast.challenge_complete", 1.0f);
    }

    private int lastXp;
    private int lastCoins;

    @Override
    protected void draw(class_332 c, float delta) {
        int w = Math.min(360, this.field_22789 - 20);
        int h = Math.min(250, this.field_22790 - 20);
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
        this.panel(c, x, y, w, h, BG);
        this.header(c, x, y, w);
        switch (this.phase) {
            case MENU -> this.drawMenu(c, x, y + 30, w, h - 30);
            case QUESTION -> this.drawQuestion(c, x, y + 30, w, h - 30);
            case RESULT -> this.drawResult(c, x, y + 30, w, h - 30);
        }
    }

    private void header(class_332 c, int x, int y, int w) {
        MiniGameStore.Data d = MiniGameStore.get();
        this.size(GameScreen.TITLE);
        this.text(c, "MineQuiz", x + 10, y + 7, GREEN);
        this.size(GameScreen.BODY);
        String stats = "★ " + d.xp + " XP   ☀ " + MiniGameStore.currentStreak() + " дн.   ⛁ " + d.coins;
        this.text(c, stats, x + w - 10 - this.width(stats), y + 10, GOLD);
        c.method_25294(x + 8, y + 24, x + w - 8, y + 25, 0x30FFFFFF);
    }

    private void drawMenu(class_332 c, int x, int y, int w, int h) {
        this.size(GameScreen.TITLE);
        this.centered(c, "Выбери урок", x + w / 2, y + 4, TEXT);
        this.size(GameScreen.BODY);
        int bw = w - 60;
        int bx = x + 30;
        int by = y + 24;
        this.button(c, bx, by, bw, 32, "Minecraft  (" + QuizBank.count(QuizBank.MINECRAFT) + " вопросов)", 0xFF2E7D32, TEXT, true, () -> this.start(QuizBank.MINECRAFT));
        this.button(c, bx, by + 40, bw, 32, "Сервера: FunTime, HolyWorld, ReallyWorld, SpookyTime  (" + QuizBank.count(QuizBank.SERVERS) + ")", 0xFF1E5AA8, TEXT, true, () -> this.start(QuizBank.SERVERS));
        this.button(c, bx, by + 80, bw, 32, "Микс  (все вопросы)", 0xFF6A3FB5, TEXT, true, () -> this.start(null));
        this.centered(c, "10 вопросов, 3 сердца. За верные ответы — XP и монеты для Upgrader.", x + w / 2, y + h - 34, MUTED);
        this.centered(c, "Свои вопросы: config/dettex/quiz.json", x + w / 2, y + h - 20, MUTED);
    }

    private void drawQuestion(class_332 c, int x, int y, int w, int h) {
        // progress bar and hearts
        int barW = w - 90;
        c.method_25294(x + 12, y + 4, x + 12 + barW, y + 10, 0xFF2A3140);
        float done = (this.index + (this.picked >= 0 ? 1 : 0)) / (float)this.lesson.size();
        c.method_25294(x + 12, y + 4, x + 12 + Math.round(barW * done), y + 10, GREEN);
        StringBuilder hs = new StringBuilder();
        for (int i = 0; i < 3; ++i) {
            hs.append(i < this.hearts ? "❤" : "♡");
        }
        this.text(c, hs.toString(), x + w - 60, y + 3, RED);
        if (this.combo >= 2) {
            this.text(c, "x" + this.combo, x + w - 26, y + 3, GOLD);
        }
        QuizBank.Question q = this.lesson.get(this.index);
        this.text(c, (this.index + 1) + " / " + this.lesson.size() + "   " + (q.category().equals(QuizBank.SERVERS) ? "Сервера" : "Minecraft"), x + 12, y + 16, MUTED);
        this.size(GameScreen.TITLE - 2);
        int ty = y + 28;
        for (String line : this.wrap(q.text(), w - 24)) {
            this.text(c, line, x + 12, ty, TEXT);
            ty += this.lineHeight();
        }
        this.size(GameScreen.BODY);
        int ay = Math.max(ty + 8, y + 62);
        int aw = (w - 34) / 2;
        for (int i = 0; i < this.shown.length; ++i) {
            int ax = x + 12 + (i % 2) * (aw + 10);
            int by = ay + (i / 2) * 34;
            int color = CARD;
            if (this.picked >= 0) {
                if (i == this.rightIndex) color = 0xFF256B34;
                else if (i == this.picked) color = 0xFF8A2430;
            }
            final int n = i;
            String label = (i + 1) + ". " + this.fit(this.shown[i], aw - 10);
            this.button(c, ax, by, aw, 28, label, color, TEXT, this.picked < 0, () -> this.pick(n));
            if (this.picked >= 0 && (i == this.rightIndex || i == this.picked)) {
                this.frame(c, ax, by, aw, 28, i == this.rightIndex ? GREEN : RED);
            }
        }
        if (this.picked >= 0) {
            boolean ok = this.picked == this.rightIndex;
            int fy = ay + 74;
            this.text(c, ok ? "Верно! +" + XP_PER_RIGHT + " XP" : "Неверно. Ответ: " + this.fit(this.shown[this.rightIndex], w - 140), x + 12, fy, ok ? GREEN : RED);
            if (!q.explain().isEmpty()) {
                List<String> ex = this.wrap(q.explain(), w - 24);
                int ey = fy + this.lineHeight();
                for (int i = 0; i < Math.min(2, ex.size()); ++i) {
                    this.text(c, ex.get(i), x + 12, ey, MUTED);
                    ey += this.lineHeight();
                }
            }
            this.button(c, x + w - 102, y + h - 30, 90, 22, this.hearts <= 0 || this.index + 1 >= this.lesson.size() ? "Итоги →" : "Дальше →", ok ? 0xFF2E7D32 : 0xFF8A2430, TEXT, true, this::next);
        }
    }

    private void drawResult(class_332 c, int x, int y, int w, int h) {
        this.size(GameScreen.TITLE + 4);
        this.centered(c, this.failed ? "Сердца закончились" : "Урок пройден!", x + w / 2, y + 10, this.failed ? RED : GREEN);
        this.size(GameScreen.BODY);
        this.centered(c, "Верных ответов: " + this.right + " из " + this.lesson.size(), x + w / 2, y + 36, TEXT);
        this.centered(c, "Лучшая серия ответов: " + this.bestCombo, x + w / 2, y + 50, TEXT);
        this.centered(c, "+" + this.lastXp + " XP    +" + this.lastCoins + " монет", x + w / 2, y + 70, GOLD);
        this.centered(c, "Серия дней: " + MiniGameStore.currentStreak() + "   Уроков всего: " + MiniGameStore.get().lessonsDone, x + w / 2, y + 86, MUTED);
        this.button(c, x + w / 2 - 105, y + h - 44, 100, 24, "Ещё урок", 0xFF2E7D32, TEXT, true, () -> {
            this.phase = Phase.MENU;
        });
        this.button(c, x + w / 2 + 5, y + h - 44, 100, 24, "Upgrader →", 0xFF1E5AA8, TEXT, true, () -> this.field_22787.method_1507(new UpgraderScreen()));
    }

    private String fit(String s, int maxWidth) {
        if (this.width(s) <= maxWidth) {
            return s;
        }
        String out = s;
        while (out.length() > 1 && this.width(out + "…") > maxWidth) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "…";
    }

    @Override
    protected boolean keyPressed(int key) {
        if (this.phase == Phase.QUESTION) {
            if (key >= 49 && key <= 52) {   // 1..4
                this.pick(key - 49);
                return true;
            }
            if (key == 257 || key == 335 || key == 32) {   // Enter / keypad Enter / Space
                this.next();
                return true;
            }
        }
        return false;
    }
}
