package ru.recoin.implement.features.modules.misc.minigames;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MineQuiz questions. Built-in ones plus the player's own from config/dettex/quiz.json
 * (the file is created with an example on first launch, edit it to add more).
 */
public final class QuizBank {
    public static final String MINECRAFT = "Minecraft";
    public static final String SERVERS = "Servers";

    /** answers[0] is always the right one in the source; they are shuffled when a lesson starts. */
    public record Question(String category, String text, String[] answers, String explain) {
    }

    private static final List<Question> BUILT_IN = new ArrayList<>();

    private static void q(String cat, String text, String explain, String right, String... wrong) {
        String[] a = new String[wrong.length + 1];
        a[0] = right;
        System.arraycopy(wrong, 0, a, 1, wrong.length);
        BUILT_IN.add(new Question(cat, text, a, explain));
    }

    static {
        // ---------------------------------------------------------------- Minecraft
        q(MINECRAFT, "В каком году вышла первая публичная версия Minecraft?", "Первую версию выложили в мае 2009 года.", "2009", "2011", "2007", "2012");
        q(MINECRAFT, "Кто придумал Minecraft?", "Маркус Перссон, ник Notch.", "Маркус Перссон (Notch)", "Йенс Бергенстен (Jeb)", "Билл Гейтс", "Гейб Ньюэлл");
        q(MINECRAFT, "Когда вышла полная версия Minecraft 1.0?", "Релиз 1.0 показали на MineCon в ноябре 2011.", "2011", "2009", "2013", "2010");
        q(MINECRAFT, "Какая компания купила Mojang в 2014 году?", "Microsoft купила Mojang за 2,5 млрд долларов.", "Microsoft", "Sony", "Google", "Valve");
        q(MINECRAFT, "Как появился крипер?", "Notch ошибся в размерах модели свиньи, и получился крипер.", "Из ошибки в модели свиньи", "Его нарисовал фанат", "Его взяли из другой игры", "Из модели зомби");
        q(MINECRAFT, "Сколько блоков обсидиана нужно минимум для портала в Незер?", "Рамка 4x5 без углов: 10 блоков.", "10", "14", "8", "12");
        q(MINECRAFT, "Из чего крафтится незеритовый слиток?", "4 незеритовых обломка + 4 золотых слитка.", "4 обломка + 4 золотых слитка", "4 обломка + 4 алмаза", "9 обломков", "1 обломок + 8 железа");
        q(MINECRAFT, "В каком обновлении появился незерит?", "1.16 — Nether Update (2020).", "1.16", "1.14", "1.18", "1.12");
        q(MINECRAFT, "Какой слепой моб появился в обновлении 1.19?", "Хранитель (Warden) ищет игрока по звуку и вибрациям.", "Хранитель (Warden)", "Иссушитель", "Пиглин-зверь", "Страж");
        q(MINECRAFT, "В какой версии добавили булаву (mace)?", "1.21 — Tricky Trials.", "1.21", "1.20", "1.19", "1.17");
        q(MINECRAFT, "Из чего крафтится булава?", "Тяжёлое ядро + стержень вихря.", "Тяжёлое ядро + стержень вихря", "Железный блок + палка", "Алмаз + обсидиан", "Звезда Незера + палка");
        q(MINECRAFT, "Какой максимальный уровень зачарования «Острота»?", "Острота бывает до V.", "V", "III", "X", "IV");
        q(MINECRAFT, "Кто роняет тотем бессмертия?", "Тотем выпадает с заклинателя (Evoker).", "Заклинатель", "Ведьма", "Разбойник", "Эндермен");
        q(MINECRAFT, "Какую высоту мира сделали в 1.18?", "Мир идёт от Y=-64 до Y=320.", "От -64 до 320", "От 0 до 256", "От -128 до 256", "От 0 до 512");
        q(MINECRAFT, "Какой ширины чанк?", "Чанк — 16 на 16 блоков.", "16 блоков", "32 блока", "8 блоков", "64 блока");
        q(MINECRAFT, "Сколько тиков в одной секунде игры?", "Игра считает 20 тиков в секунду.", "20", "60", "10", "30");
        q(MINECRAFT, "Сколько длятся игровые сутки?", "Полный день и ночь — 20 минут.", "20 минут", "10 минут", "1 час", "24 минуты");
        q(MINECRAFT, "Чего боятся эндермены?", "Вода наносит им урон.", "Воды", "Огня", "Света", "Кошек");
        q(MINECRAFT, "Где найти элитры?", "Элитры висят в кораблях в городах Энда.", "В корабле в городе Энда", "В крепости Незера", "В подводной руине", "У торговца");
        q(MINECRAFT, "Какое зачарование нельзя совместить с «Починкой» на луке?", "Бесконечность и Починка несовместимы.", "Бесконечность", "Сила", "Откидывание", "Горящая стрела");
        q(MINECRAFT, "Кто написал музыку оригинального Minecraft?", "Композитор C418 (Даниэль Розенфельд).", "C418", "Marshmello", "Hans Zimmer", "Lena Raine");
        q(MINECRAFT, "Во что превращается житель, если в него ударит молния?", "Житель становится ведьмой.", "В ведьму", "В зомби-жителя", "В разбойника", "Ни во что");
        q(MINECRAFT, "Во что превращается свинья от удара молнии?", "Свинья становится зомбифицированным пиглином.", "В зомбифицированного пиглина", "В крипера", "В хоглина", "В зомби");
        q(MINECRAFT, "Чем приручают волка?", "Волка приручают костями.", "Костью", "Мясом", "Рыбой", "Пшеницей");
        q(MINECRAFT, "Сколько сердец здоровья у игрока?", "10 сердец = 20 единиц здоровья.", "10", "20", "8", "12");
        q(MINECRAFT, "Какой блок нельзя сломать в выживании?", "Бедрок неразрушим в выживании.", "Бедрок", "Обсидиан", "Незеритовый блок", "Древние обломки");
        q(MINECRAFT, "Что даёт «Поглощение IV» и «Сопротивление» при съедании?", "Это эффекты зачарованного золотого яблока.", "Зачарованное золотое яблоко", "Золотое яблоко", "Золотая морковь", "Подозрительное рагу");
        q(MINECRAFT, "Какой самый старый известный сервер-анархия?", "2b2t работает с 2010 года.", "2b2t", "Hypixel", "Mineplex", "FunTime");
        // ---------------------------------------------------------------- servers
        q(SERVERS, "Какой режим самый популярный на FunTime, HolyWorld, ReallyWorld и SpookyTime?", "Все они известны прежде всего анархией.", "Анархия", "Скайблок", "Бедварс", "Креатив");
        q(SERVERS, "На каком языке в основном общаются на FunTime, HolyWorld, ReallyWorld и SpookyTime?", "Это русскоязычные сервера.", "На русском", "На английском", "На немецком", "На китайском");
        q(SERVERS, "В какой доменной зоне сайт и адрес ReallyWorld?", "Адрес сервера заканчивается на reallyworld.ru.", ".ru", ".com", ".net", ".org");
        q(SERVERS, "Что такое «анархия» на сервере майнкрафт?", "Можно грабить, ломать и убивать — почти без запретов.", "Выживание почти без правил: гриф и PvP разрешены", "Мини-игра на время", "Режим без мобов", "Креатив для строителей");
        q(SERVERS, "Что обычно будет, если выйти с сервера в режиме PvP на анархии?", "Персонаж погибает, а вещи выпадают — так не убежишь от боя.", "Персонаж умрёт и потеряет вещи", "Ничего", "Получишь бан навсегда", "Тебя телепортирует на спавн");
        q(SERVERS, "Что значит «вайп»?", "Вайп — полный сброс карты и прогресса игроков.", "Сброс мира и прогресса", "Новый донат", "Ивент с боссом", "Обновление лаунчера");
        q(SERVERS, "Какой командой на многих серверах открывают аукцион?", "Обычно /ah (Auction House).", "/ah", "/spawn", "/home", "/kit");
        q(SERVERS, "Что делает команда /rtp?", "Random TP — телепорт в случайное место мира.", "Телепорт в случайное место", "Возврат на спавн", "Телепорт к другу", "Открывает магазин");
        q(SERVERS, "Зачем на анархии держат тотем во второй руке?", "Тотем спасает от смерти, если он в руке.", "Чтобы не умереть от смертельного удара", "Для скорости", "Чтобы светиться", "Для крафта");
        q(SERVERS, "Что значит «кд» в разговоре игроков?", "КД — кулдаун, время перезарядки предмета или команды.", "Перезарядка (кулдаун)", "Клановый дом", "Код доступа", "Кейс доната");
    }

    private QuizBank() {
    }

    private static Path userFile() {
        Path dir;
        try {
            dir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("dettex");
        } catch (Throwable ignored) {
            dir = Paths.get("config", "dettex");
        }
        return dir.resolve("quiz.json");
    }

    /** Player's own questions; a template with an example is written on first launch. */
    public static List<Question> userQuestions() {
        List<Question> out = new ArrayList<>();
        Path f = QuizBank.userFile();
        try {
            if (!Files.exists(f)) {
                Files.createDirectories(f.getParent());
                Files.writeString(f, "[\n"
                        + "  {\n"
                        + "    \"category\": \"Servers\",\n"
                        + "    \"question\": \"Your own question (category Minecraft or Servers)\",\n"
                        + "    \"answers\": [\"Right answer first\", \"Wrong 1\", \"Wrong 2\", \"Wrong 3\"],\n"
                        + "    \"explain\": \"Shown after the answer\",\n"
                        + "    \"example\": true\n"
                        + "  }\n"
                        + "]\n", StandardCharsets.UTF_8);
                return out;
            }
            JsonElement root = JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8));
            if (!root.isJsonArray()) {
                return out;
            }
            for (JsonElement e : root.getAsJsonArray()) {
                if (!e.isJsonObject()) continue;
                JsonObject o = e.getAsJsonObject();
                if (o.has("example") && o.get("example").getAsBoolean()) continue;
                if (!o.has("question") || !o.has("answers") || !o.get("answers").isJsonArray()) continue;
                JsonArray arr = o.getAsJsonArray("answers");
                if (arr.size() < 2) continue;
                String[] answers = new String[Math.min(4, arr.size())];
                for (int i = 0; i < answers.length; ++i) {
                    answers[i] = arr.get(i).getAsString();
                }
                String cat = o.has("category") ? o.get("category").getAsString() : SERVERS;
                out.add(new Question(MINECRAFT.equalsIgnoreCase(cat) ? MINECRAFT : SERVERS, o.get("question").getAsString(), answers,
                        o.has("explain") ? o.get("explain").getAsString() : ""));
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    /** A lesson: up to `size` random questions of the category (null = mixed). */
    public static List<Question> lesson(String category, int size) {
        List<Question> pool = new ArrayList<>();
        for (Question q : BUILT_IN) {
            if (category == null || q.category().equals(category)) pool.add(q);
        }
        for (Question q : QuizBank.userQuestions()) {
            if (category == null || q.category().equals(category)) pool.add(q);
        }
        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0, Math.min(size, pool.size())));
    }

    public static int count(String category) {
        int n = 0;
        for (Question q : BUILT_IN) {
            if (category == null || q.category().equals(category)) ++n;
        }
        for (Question q : QuizBank.userQuestions()) {
            if (category == null || q.category().equals(category)) ++n;
        }
        return n;
    }
}
