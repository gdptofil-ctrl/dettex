package ru.recoin.implement.features.modules.misc.bob;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.class_310;
import net.minecraft.class_640;
import ru.recoin.api.feature.module.Module;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.misc.minigames.MiniGameStore;
import ru.recoin.implement.features.modules.misc.minigames.QuizBank;
import ru.recoin.implement.features.modules.misc.minigames.QuizScreen;
import ru.recoin.implement.features.modules.misc.minigames.UpgraderScreen;

/**
 * What Bob understands. Works offline with simple phrase matching (Russian and English):
 * toggling modules, FPS / coordinates / health / ping / time, a quick quiz question, jokes, opening
 * MineQuiz or Upgrader, and answers ("да" / "нет") to his own random questions.
 */
public final class BobBrain {
    private static final Random RANDOM = new Random();

    /** Something Bob asked and is waiting for a yes / no (or a quiz answer) for. */
    private Runnable pendingYes;
    private String pendingNo = "";
    private long pendingUntil;
    private QuizBank.Question quiz;
    private String[] quizAnswers;
    private int quizRight;

    public boolean waitingForAnswer() {
        return System.currentTimeMillis() < this.pendingUntil;
    }

    private static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).replace('ё', 'е').replaceAll("[^\\p{L}\\p{N} ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static boolean has(String text, String... words) {
        for (String w : words) {
            if (text.contains(w)) return true;
        }
        String[] tokens = text.split(" ");
        for (String w : words) {
            if (w.length() < 5 || w.contains(" ")) continue;
            for (String tok : tokens) {
                if (tok.length() < 4) continue;
                String head = tok.length() > w.length() + 2 ? tok.substring(0, w.length() + 1) : tok;
                if (BobBrain.osa(head, w) <= 1 || tok.length() >= w.length() && BobBrain.osa(tok.substring(0, w.length()), w) <= 1) return true;
            }
        }
        return false;
    }

    /** Edit distance with swaps of neighbour letters (typos like «апргейдер»). */
    static int osa(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); ++i) d[i][0] = i;
        for (int j = 0; j <= b.length(); ++j) d[0][j] = j;
        for (int i = 1; i <= a.length(); ++i) {
            for (int j = 1; j <= b.length(); ++j) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
            }
        }
        return d[a.length()][b.length()];
    }

    /** Russian letters to Latin, so «киллаура» can match «KillAura» and «аттак аура» «Attack Aura». */
    static String translit(String s) {
        String[][] map = {{"щ", "sch"}, {"ш", "sh"}, {"ч", "ch"}, {"ж", "zh"}, {"ю", "yu"}, {"я", "ya"}, {"ё", "e"}, {"э", "e"}, {"х", "h"}, {"ц", "c"},
                {"а", "a"}, {"б", "b"}, {"в", "v"}, {"г", "g"}, {"д", "d"}, {"е", "e"}, {"з", "z"}, {"и", "i"}, {"й", "i"}, {"к", "k"}, {"л", "l"}, {"м", "m"},
                {"н", "n"}, {"о", "o"}, {"п", "p"}, {"р", "r"}, {"с", "s"}, {"т", "t"}, {"у", "u"}, {"ф", "f"}, {"ы", "y"}, {"ь", ""}, {"ъ", ""}};
        String out = s.toLowerCase(Locale.ROOT);
        for (String[] m : map) out = out.replace(m[0], m[1]);
        return out;
    }

    private static String pick(String... options) {
        return options[RANDOM.nextInt(options.length)];
    }

    /** Answer to a message (already without the "боб" prefix). */
    public String reply(String message) {
        String t = BobBrain.norm(message);
        t = BobBrain.numbersToDigits(t);   // voice says numbers and ordinals as words
        class_310 mc = class_310.method_1551();
        // answers to Bob's own question
        if (this.waitingForAnswer()) {
            if (this.quiz != null) {
                String a = this.answerQuiz(t);
                if (a != null) return a;
            }
            if (t.matches("(да|ага|угу|давай|конечно|го|ес|yes|yeah|ok|ок|окей|хочу|согласен|хорошо|ладно)( .*)?")) {
                Runnable r = this.pendingYes;
                this.clearPending();
                if (r != null) r.run();
                return BobBrain.pick("Сделано!", "Готово!", "Уже!");
            }
            if (t.matches("(?U)(нет|не|неа|нисколько|никак|no|nope|не надо|не хочу|потом|отмена|отстань)( .*)?")) {
                String n = this.pendingNo;
                this.clearPending();
                return n.isEmpty() ? BobBrain.pick("Ладно.", "Как скажешь.", "Хорошо, не буду.") : n;
            }
        }
        if (t.isEmpty()) return BobBrain.pick("Да-да? Я слушаю.", "Слушаю тебя!", "Что такое?");
        if (has(t, "помощ", "что умеешь", "команды", "help")) {
            return "Скажи: включи или выключи модуль, сколько фпс, где я, сколько здоровья, пинг, время, вопрос, шутка, викторина, апгрейдер, громче, тише, замолчи.";
        }
        if (has(t, "привет", "здравств", "хай", "хелло", "hello", "hi ")) return BobBrain.pick("Привет! Я Боб.", "Здорово! Чем помочь?", "Привет-привет!");
        if (has(t, "кто ты", "ты кто", "who are you")) return "Я Боб, твой помощник в Dettex. Могу включать модули и отвечать на вопросы.";
        if (has(t, "как дела", "как ты")) return BobBrain.pick("Отлично! Батарейка полная.", "Хорошо, спасибо! А у тебя?", "Работаю в полную силу!");
        if (has(t, "спасибо", "спс", "thanks")) return BobBrain.pick("Пожалуйста!", "Обращайся!", "Всегда рад помочь.");
        if (has(t, "кто тебя", "кем ты", "создател", "кто сделал", "кто создал", "откуда ты")) return "Меня собрали для клиента Dettex. Я робот-помощник: слушаю команды, включаю модули и болтаю с тобой.";
        if (has(t, "парол", "password")) return "Я не знаю твой пароль и никогда его не спрошу. И никому его не говори, даже мне!";
        if (has(t, "поиграй за меня", "поиграйся", "попехайся", "постой за меня", "мама позвала", "кушать позвал", "отойду", "я афк", "ушел есть", "ушёл есть")) {
            Module afk = BobBrain.findExact("AntiAFK");
            if (afk == null) return "Приятного аппетита! Я посторожу экран, но играть за тебя не умею.";
            if (!afk.isState()) class_310.method_1551().execute(() -> afk.setState(true));
            return "Приятного аппетита! Включил AntiAFK, чтобы тебя не кикнули. Скажи «боб выключи антиафк», когда вернёшься.";
        }
        if (has(t, "водичк", "попей", "покушай", "поешь")) return BobBrain.pick("Я робот, я пью электричество. А вот тебе водички не помешает!", "Спасибо, я на солнечных батарейках.");
        if (has(t, "нахуй", "нахер", "пошел ты", "пошёл ты", "дурак", "тупой", "идиот", "свин", "лох", "отстань", "заткнись")) {
            return BobBrain.pick("Обидно, но я всё равно тебя люблю.", "Я не обижаюсь, у меня нет чувств. Ну, почти.", "Ладно-ладно, просто скажи, чем помочь.", "Эй, я же стараюсь!");
        }
        if (has(t, "любишь", "друг", "скучно")) return BobBrain.pick("Я твой друг! Хочешь, задам вопрос по майнкрафту?", "Скучно? Давай викторину: скажи «боб вопрос».");
        if (has(t, "умеешь", "можешь")) return "Я включаю и выключаю модули, говорю фпс, координаты, пинг и время, задаю вопросы, шучу и открываю Upgrader. Подключи мне ИИ в настройках, и я смогу отвечать на что угодно.";
        // modules
        java.util.regex.Matcher on = java.util.regex.Pattern.compile("(включи|включай|вкл|вруби|запусти|активируй|поставь|enable|turn on)\\s+(.+)").matcher(t);
        java.util.regex.Matcher off = java.util.regex.Pattern.compile("(выключи|выключай|выкл|выруби|отруби|отключи|убери|деактивируй|disable|turn off)\\s+(.+)").matcher(t);
        if (off.find()) return BobBrain.toggle(off.group(2), false);
        if (on.find()) return BobBrain.toggle(on.group(2), true);
        if (has(t, "что включено", "какие модули", "включенные")) {
            List<String> names = new ArrayList<>();
            for (Module m : Main.getInstance().getModuleProvider().getModules()) {
                if (m.isState()) names.add(m.getVisibleName());
            }
            return names.isEmpty() ? "Сейчас ничего не включено." : "Включено " + names.size() + ": " + String.join(", ", names.subList(0, Math.min(8, names.size()))) + (names.size() > 8 ? " и другие." : ".");
        }
        // info
        if (has(t, "фпс", "fps", "кадр")) {
            int fps = mc.method_47599();
            return "У тебя " + fps + " кадров в секунду. " + (fps >= 240 ? "Летает!" : fps >= 90 ? "Неплохо." : "Можно включить Optimization.");
        }
        if (has(t, "где я", "координат", "coords")) {
            if (mc.field_1724 == null) return "Ты сейчас не в мире.";
            return String.format("Ты на X %d, Y %d, Z %d.", (int)Math.floor(mc.field_1724.method_23317()), (int)Math.floor(mc.field_1724.method_23318()), (int)Math.floor(mc.field_1724.method_23321()));
        }
        if (has(t, "здоров", "хп", "сердц", "health")) {
            if (mc.field_1724 == null) return "Ты сейчас не в мире.";
            float hp = mc.field_1724.method_6032();
            return "У тебя " + Math.round(hp / 2.0f * 10.0f) / 10.0f + " сердец." + (hp < 8.0f ? " Осторожно, мало!" : "");
        }
        if (has(t, "пинг", "ping")) {
            if (mc.field_1724 == null || mc.method_1562() == null) return "Пинг есть только на сервере.";
            class_640 entry = mc.method_1562().method_2871(mc.field_1724.method_5667());
            return entry == null ? "Не вижу свой пинг." : "Твой пинг " + entry.method_2959() + " мс.";
        }
        if (has(t, "время", "который час", "time")) return "Сейчас " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ".";
        if (has(t, "монет", "баланс", "coins")) return "У тебя " + MiniGameStore.get().coins + " монет в Upgrader.";
        // fun
        if (has(t, "шутк", "анекдот", "рассмеши", "joke")) {
            return BobBrain.pick(
                    "Крипер заходит в бар. Бар больше не заходит никуда.",
                    "Почему скелет не пошёл на вечеринку? Ему не с кем было потусить, одни кости.",
                    "Я хотел скопать алмазы, но докопался только до лавы. Вот такой у меня характер.",
                    "Эндермен спросил, можно ли посмотреть мне в глаза. Я сказал: лучше не надо.",
                    "Зомби не опаздывают. Они просто приходят медленно и с достоинством.");
        }
        if (has(t, "вопрос", "викторин", "quiz")) return this.askQuiz();
        if (has(t, "апгрейд", "upgrader")) {
            mc.execute(() -> mc.method_1507(new UpgraderScreen()));
            return "Открываю Upgrader. Удачи!";
        }
        if (has(t, "minequiz", "майнквиз", "урок")) {
            mc.execute(() -> mc.method_1507(new QuizScreen()));
            return "Открываю MineQuiz!";
        }
        return null;   // not a simple command: the language model (if set up) answers
    }

    public static String notUnderstood() {
        return BobBrain.pick("Не совсем понял. Скажи «боб помощь», и я расскажу, что умею. А чтобы я отвечал на любые вопросы, подключи мне ИИ в настройках.",
                "Хм, этого я пока не знаю. Включи мне ИИ в настройках Bob, и я стану умнее.",
                "Не понял тебя. Можешь сказать по-другому?");
    }

    private static final String[][] ALIASES = {
            {"оптимизац", "Optimization"}, {"шейдер", "Shader"}, {"атмосфер", "Atmosphere"}, {"дожд", "Rain"},
            {"косметик", "Cosmetics"}, {"музык", "MusicHud"}, {"плавн", "SmoothCamera"}, {"камер", "SmoothCamera"},
            {"шрифт", "Shrift"}, {"праздн", "Holiday"}, {"хэллоуин", "Holiday"}, {"хеллоуин", "Holiday"}, {"зим", "Winter"},
            {"фулбрайт", "FullBright"}, {"яркост", "FullBright"}, {"подсветк удар", "HitColor"}, {"цвет удар", "HitColor"},
            {"хитбокс", "HitColorHitbox"}, {"таргет", "TargetESP"}, {"звук", "EventSounds"}, {"интерфейс", "Hud"},
            {"худ", "Hud"}, {"викторин", "MineQuiz"}, {"апгрейд", "Upgrader"}, {"апргейд", "Upgrader"},
            {"киллаур", "Aura"}, {"килаур", "Aura"}, {"кил аур", "Aura"}, {"аттак аур", "Aura"}, {"атак аур", "Aura"}, {"аура", "Aura"},
            {"антиафк", "AntiAFK"}, {"анти афк", "AntiAFK"}, {"афк", "AntiAFK"}, {"спринт", "AutoSprint"}, {"бег", "AutoSprint"},
            {"тотем", "AutoTotem"}, {"есп", "EntityESP"}, {"вх", "EntityESP"}, {"хрен", "XRay"}, {"иксрей", "XRay"}, {"рентген", "XRay"},
            {"фрикам", "FreeCam"}, {"свободн камер", "FreeCam"}, {"прицел", "CrossHair"}, {"хотбар", "CooldownHotbar"}};

    /** Finds a module by (part of) its name, ignoring case, spaces and punctuation (Russian nicknames too). */
    public static Module findModule(String query) {
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        for (String[] alias : ALIASES) {
            boolean all = true;
            for (String part : alias[0].split(" ")) {
                if (!lowerQuery.contains(part)) all = false;
            }
            if (all) {
                Module m = BobBrain.findExact(alias[1]);
                if (m != null) return m;
            }
        }
        lowerQuery = lowerQuery.replaceAll("\\b(модуль|модули|функцию|функция|чит|мне|пожалуйста|пж|плиз|плз|please|быстро|сейчас|обратно|снова)\\b", " ").trim();
        String q = lowerQuery.replaceAll("[^\\p{L}\\p{N}]", "");
        if (q.isEmpty()) return null;
        Module best = null;
        for (Module m : Main.getInstance().getModuleProvider().getModules()) {
            String a = m.getName().toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
            String b = m.getVisibleName().toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
            if (a.equals(q) || b.equals(q)) return m;
            if ((a.startsWith(q) || b.startsWith(q) || q.startsWith(a)) && (best == null || a.length() < best.getName().length())) best = m;
        }
        if (best != null) return best;
        // Russian spelling of an English name, or a typo: closest name by edit distance
        String tq = BobBrain.translit(q);
        double bestScore = 0.0;
        for (Module m : Main.getInstance().getModuleProvider().getModules()) {
            for (String name : new String[]{m.getName(), m.getVisibleName()}) {
                String n = name.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
                if (n.isEmpty()) continue;
                int dist = BobBrain.osa(tq, n);
                double score = 1.0 - dist / (double)Math.max(tq.length(), n.length());
                if (n.contains(tq) && tq.length() >= 4) score = Math.max(score, 0.8);
                if (tq.contains(n) && n.length() >= 4) score = Math.max(score, 0.75);
                if (score > bestScore) {
                    bestScore = score;
                    best = m;
                }
            }
        }
        return bestScore >= 0.62 ? best : null;
    }

    private static Module findExact(String name) {
        for (Module m : Main.getInstance().getModuleProvider().getModules()) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    /** Toggles a module named by the language model; returns false if there is no such module. */
    public static boolean apply(String name, boolean enable) {
        Module m = BobBrain.findModule(name);
        if (m == null || m instanceof Bob) return false;
        if (m.isState() != enable) class_310.method_1551().execute(() -> m.setState(enable));
        return true;
    }

    /** Short description of the game state for the language model. */
    public static String context() {
        class_310 mc = class_310.method_1551();
        StringBuilder sb = new StringBuilder("Модули клиента: ");
        List<String> on = new ArrayList<>();
        int i = 0;
        for (Module m : Main.getInstance().getModuleProvider().getModules()) {
            if (i++ > 0) sb.append(", ");
            sb.append(m.getName());
            if (m.isState()) on.add(m.getName());
        }
        sb.append(". Сейчас включены: ").append(on.isEmpty() ? "ничего" : String.join(", ", on)).append('.');
        sb.append(" FPS: ").append(mc.method_47599()).append('.');
        if (mc.field_1724 != null) {
            sb.append(String.format(" Игрок на X %d Y %d Z %d, здоровье %.0f из 20.", (int)mc.field_1724.method_23317(), (int)mc.field_1724.method_23318(), (int)mc.field_1724.method_23321(), mc.field_1724.method_6032()));
        }
        sb.append(" Время: ").append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))).append('.');
        return sb.toString();
    }

    private static String toggle(String query, boolean enable) {
        Module m = BobBrain.findModule(query);
        if (m == null) return "Не нашёл модуль «" + query + "».";
        if (m instanceof Bob) return enable ? "Я и так здесь!" : "Чтобы выключить меня, выключи модуль Bob.";
        if (m.isState() == enable) return m.getVisibleName() + (enable ? " уже включён." : " уже выключен.");
        class_310.method_1551().execute(() -> m.setState(enable));
        return m.getVisibleName() + (enable ? " включён." : " выключен.");
    }

    private String askQuiz() {
        List<QuizBank.Question> q = QuizBank.lesson(null, 1);
        if (q.isEmpty()) return "Вопросы закончились.";
        this.quiz = q.get(0);
        List<String> answers = new ArrayList<>(List.of(this.quiz.answers()));
        String right = answers.get(0);
        java.util.Collections.shuffle(answers);
        this.quizAnswers = answers.toArray(new String[0]);
        this.quizRight = answers.indexOf(right);
        this.pendingYes = null;
        this.pendingNo = "";
        this.pendingUntil = System.currentTimeMillis() + 60000L;
        StringBuilder sb = new StringBuilder(this.quiz.text());
        for (int i = 0; i < this.quizAnswers.length; ++i) {
            sb.append(' ').append(i + 1).append(") ").append(this.quizAnswers[i]).append('.');
        }
        return sb.append(" Ответь номером.").toString();
    }

    private static String numbersToDigits(String t) {
        String[][] map = {{"первый", "1"}, {"первая", "1"}, {"первое", "1"}, {"один", "1"}, {"одна", "1"}, {"раз", "1"},
                {"второй", "2"}, {"вторая", "2"}, {"второе", "2"}, {"два", "2"}, {"две", "2"},
                {"третий", "3"}, {"третья", "3"}, {"третье", "3"}, {"три", "3"},
                {"четвертый", "4"}, {"четвёртый", "4"}, {"четвертая", "4"}, {"четыре", "4"}};
        for (String[] m : map) {
            if (t.equals(m[0]) || t.startsWith(m[0] + " ")) return m[1] + t.substring(m[0].length());
        }
        return t;
    }

    private String answerQuiz(String t) {
        int n = -1;
        if (t.matches("[1-4].*")) n = t.charAt(0) - '1';
        else for (int i = 0; i < this.quizAnswers.length; ++i) {
            if (BobBrain.norm(this.quizAnswers[i]).equals(t)) n = i;
        }
        if (n < 0 || n >= this.quizAnswers.length) return null;
        boolean ok = n == this.quizRight;
        String explain = this.quiz.explain();
        String right = this.quizAnswers[this.quizRight];
        this.clearPending();
        if (ok) {
            MiniGameStore.get().coins += 20;
            MiniGameStore.save();
            return "Правильно! Плюс 20 монет. " + explain;
        }
        return "Неа, правильно: " + right + ". " + explain;
    }

    private void clearPending() {
        this.pendingYes = null;
        this.pendingNo = "";
        this.pendingUntil = 0L;
        this.quiz = null;
    }

    /** A random question from Bob (null = nothing to say right now). */
    public String randomQuestion() {
        class_310 mc = class_310.method_1551();
        int k = RANDOM.nextInt(6);
        switch (k) {
            case 0: {
                Module opt = BobBrain.findModule("Optimization");
                if (opt != null && !opt.isState() && mc.method_47599() < 120) {
                    this.ask(() -> opt.setState(true), "Ладно, но если будет лагать — зови.");
                    return "Что-то кадров маловато. Включить Optimization?";
                }
                return this.randomQuestion();
            }
            case 1:
                return this.askQuiz();
            case 2:
                this.ask(() -> mc.execute(() -> mc.method_1507(new UpgraderScreen())), "Ну и правильно, монеты целее будут.");
                return "Может, попробуем удачу в Upgrader?";
            case 3:
                this.ask(null, "");
                return BobBrain.pick("Как у тебя дела?", "Ты не устал? Может, перерыв?", "Нравится сегодняшняя катка?");
            case 4:
                return BobBrain.pick("Не забудь попить воды!", "Кстати, я умею включать модули. Скажи «боб включи» и название.", "Сделай перерыв, глазам тоже нужен отдых.");
            default:
                this.ask(() -> mc.execute(() -> mc.method_1507(new QuizScreen())), "Хорошо, в другой раз.");
                return "Пройдём урок MineQuiz? Там дают монеты.";
        }
    }

    private void ask(Runnable yes, String no) {
        this.quiz = null;
        this.pendingYes = yes;
        this.pendingNo = no;
        this.pendingUntil = System.currentTimeMillis() + 45000L;
    }
}
