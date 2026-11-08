import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Telegram bot of DettexClient. One file, no libraries: run with "java DettexBot.java" (Java 21).
 * The token is read from bot-token.txt next to this file (or the BOT_TOKEN variable).
 *
 * Menu: Buy, My purchases, HWID, Configs, Buy configs, Support, Media.
 */
public final class DettexBot {
    static final String SUPPORT = "@dizzelvibecode";

    // Licence keys: the bot signs them, the launcher checks the signature offline.
    static final String LICENSE_PREFIX = "DettexLicense/v1|";
    static final String KEY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int PLAN_LIFETIME = 255;
    static final SecureRandom RANDOM = new SecureRandom();
    static byte[] licenseSecret;
    static long ownerId;

    static String api;   // https://api.telegram.org/bot<token>/
    // follows the Windows proxy / VPN (java.net.useSystemProxies is set in start-bot.bat)
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).proxy(java.net.ProxySelector.getDefault()).build();

    // ------------------------------------------------------------------ texts

    static final String LINE = "━━━━━━━━━━━━━━━━━━";

    static String header(String title) {
        return "<b>" + title + "</b>\n" + LINE + "\n";
    }

    static String main(String name) {
        return "⚡ <b>DETTEX CLIENT</b> ⚡\n" + LINE + "\n"
                + "Привет, <b>" + esc(name) + "</b>! 👋\n\n"
                + "Добро пожаловать в официального бота <b>DettexClient</b> — клиента для Minecraft 1.21.11.\n\n"
                + "Меню с кнопками открыто внизу 👇";
    }

    static Map<String, String> pages() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("buy", header("🛒 Купить Dettex")
                + "⏳ Клиент <b>ещё не вышел</b>.\n\n"
                + "Продажи откроются сразу после релиза — следи за новостями в разделе 📺 <b>Медиа</b>.\n\n"
                + "<i>Есть вопросы? Пиши в 🆘 Поддержку.</i>");
        p.put("purchases", header("📦 Мои покупки")
                + "🫙 У тебя пока <b>нет покупок</b>.\n\n"
                + "Когда купишь Dettex или конфиг, всё появится здесь.");
        p.put("hwid", header("🔑 HWID")
                + "🚫 <b>HWID нет</b>.\n\n"
                + "HWID привязывается после покупки клиента. Пока покупок нет — привязывать нечего.");
        p.put("cfg", header("⚙️ Конфиги")
                + "📂 Конфигов <b>ещё нет</b>.\n\n"
                + "Купленные конфиги будут лежать здесь.");
        p.put("buycfg", header("🛍 Купить КФГ")
                + "⏳ Конфигов в продаже <b>ещё нет</b>.\n\n"
                + "Первые конфиги появятся вместе с релизом клиента.");
        p.put("support", header("🆘 Поддержка")
                + "Нужна помощь? Пиши напрямую:\n\n"
                + "👤 " + SUPPORT + "\n\n"
                + "<i>Опиши проблему одним сообщением и приложи скриншот — так ответ будет быстрее.</i>");
        p.put("media", header("📺 Медиа")
                + "Ютуберы, стримеры, сотрудничество и новости проекта:\n\n"
                + "👤 " + SUPPORT + "\n\n"
                + "<i>Снимаешь видео по Minecraft? Напиши — обсудим.</i>");
        return p;
    }

    static String mainKeyboard() {
        return keyboard(List.of(
                List.of(btn("🛒 Купить", "buy"), btn("📦 Мои покупки", "purchases")),
                List.of(btn("⚙️ КФГ", "cfg"), btn("🛍 Купить КФГ", "buycfg")),
                List.of(btn("🔑 HWID", "hwid")),
                List.of(btn("🆘 Поддержка", "support"), btn("📺 Медиа", "media"))));
    }

    /** Labels of the menu under the input field -> page. */
    static final Map<String, String> MENU = new LinkedHashMap<>();

    static {
        MENU.put("🛒 Купить", "buy");
        MENU.put("📦 Мои покупки", "purchases");
        MENU.put("⚙️ КФГ", "cfg");
        MENU.put("🛍 Купить КФГ", "buycfg");
        MENU.put("🔑 HWID", "hwid");
        MENU.put("🆘 Поддержка", "support");
        MENU.put("📺 Медиа", "media");
    }

    /** The menu that stays at the bottom of the chat instead of the keyboard. */
    static String replyMenu() {
        String[][] rows = {{"🛒 Купить", "📦 Мои покупки"}, {"⚙️ КФГ", "🛍 Купить КФГ"}, {"🔑 HWID"}, {"🆘 Поддержка", "📺 Медиа"}};
        List<String> r = new ArrayList<>();
        for (String[] row : rows) {
            List<String> b = new ArrayList<>();
            for (String t : row) b.add("{\"text\":" + str(t) + "}");
            r.add("[" + String.join(",", b) + "]");
        }
        return "{\"keyboard\":[" + String.join(",", r) + "],\"resize_keyboard\":true,\"is_persistent\":true,\"input_field_placeholder\":" + str("Выбери раздел 👇") + "}";
    }

    /** Buttons under a page opened from the bottom menu (only a "write" link where it makes sense). */
    static String linkKeyboard(String page) {
        if (page.equals("support") || page.equals("media")) {
            return keyboard(List.of(List.of(urlBtn("✉️ Написать " + SUPPORT, "https://t.me/" + SUPPORT.substring(1)))));
        }
        return null;
    }

    static String pageKeyboard(String page) {
        List<List<String>> rows = new ArrayList<>();
        if (page.equals("support") || page.equals("media")) {
            rows.add(List.of(urlBtn("✉️ Написать " + SUPPORT, "https://t.me/" + SUPPORT.substring(1))));
        }
        rows.add(List.of(btn("⬅️ Назад", "home")));
        return keyboard(rows);
    }

    // ------------------------------------------------------------------ bot loop

    public static void main(String[] args) throws Exception {
        String token = System.getenv("BOT_TOKEN");
        Path file = Path.of("bot-token.txt");
        if ((token == null || token.isBlank()) && Files.exists(file)) token = Files.readString(file);
        if (token != null) token = token.replace("\uFEFF", "").replaceAll("\\s+", "");   // Notepad BOM, spaces, line breaks
        if (token == null || token.isBlank()) {
            System.out.println("Положи токен бота в файл bot-token.txt рядом с DettexBot.java");
            return;
        }
        String base = System.getenv().getOrDefault("BOT_API", "https://api.telegram.org");
        api = base + "/bot" + token + "/";
        licenseSecret = MessageDigest.getInstance("SHA-256").digest((LICENSE_PREFIX + token).getBytes(StandardCharsets.UTF_8));
        ownerId = loadOwnerId();

        log("Подключаюсь к Telegram...");
        Map<String, Object> me = null;
        for (int attempt = 0; me == null && attempt < 1000; ++attempt) {
            me = call("getMe", "{}");
            if (me == null) log("Telegram недоступен. Проверь интернет; если Telegram у тебя открывается только через VPN - включи VPN.");
        }
        if (!Boolean.TRUE.equals(me.get("ok"))) {
            log("Telegram не принял токен: " + me.get("description") + ". Проверь bot-token.txt (токен берётся у @BotFather).");
            return;
        }
        Map<?, ?> info = (Map<?, ?>)me.get("result");
        // a webhook blocks getUpdates (error 409): remove it, keep waiting messages
        Map<String, Object> wh = call("deleteWebhook", "{\"drop_pending_updates\":false}");
        if (wh != null && !Boolean.TRUE.equals(wh.get("ok"))) log("Не удалось снять вебхук: " + wh.get("description"));
        log("Бот @" + info.get("username") + " запущен! Открой https://t.me/" + info.get("username") + " и напиши /start. Не закрывай это окно.");
        call("setMyCommands", "{\"commands\":[{\"command\":\"start\",\"description\":\"Главное меню\"},{\"command\":\"support\",\"description\":\"Поддержка\"}]}");
        call("setMyDescription", "{\"description\":" + str("⚡ Официальный бот DettexClient — клиента для Minecraft 1.21.11.\n\nПокупка, конфиги, HWID и поддержка. Нажми «Старт»!") + "}");
        call("setMyShortDescription", "{\"short_description\":" + str("DettexClient: покупка, конфиги, HWID, поддержка") + "}");

        long offset = 0;
        while (true) {
            Map<String, Object> res = call("getUpdates", "{\"offset\":" + offset + ",\"timeout\":50,\"allowed_updates\":[\"message\",\"callback_query\"]}");
            if (res == null) continue;   // no connection, call() already waited
            if (!Boolean.TRUE.equals(res.get("ok"))) {
                long code = num(res.get("error_code"));
                if (code == 409) {
                    log("Ошибка 409: этот бот уже запущен в другом окне или на другом компьютере/хостинге (или стоит вебхук). Закрой лишние копии бота. Снимаю вебхук и пробую снова...");
                    call("deleteWebhook", "{\"drop_pending_updates\":false}");
                } else {
                    log("Telegram вернул ошибку " + code + ": " + res.get("description"));
                }
                Thread.sleep(5000);
                continue;
            }
            for (Object u : (List<?>)res.get("result")) {
                Map<?, ?> upd = (Map<?, ?>)u;
                offset = Math.max(offset, num(upd.get("update_id")) + 1);
                try {
                    handle(upd);
                } catch (Exception e) {
                    log("Ошибка: " + e);
                }
            }
        }
    }

    static void handle(Map<?, ?> upd) {
        if (upd.get("message") instanceof Map<?, ?> msg) {
            long chat = num(((Map<?, ?>)msg.get("chat")).get("id"));
            long fromId = msg.get("from") instanceof Map<?, ?> f ? num(f.get("id")) : 0L;
            String text = msg.get("text") instanceof String s ? s.trim() : "";
            String name = firstName(msg.get("from"));
            log(name + ": " + text);
            String menuPage = MENU.get(text);
            if (text.startsWith("/genkey")) {
                generateKeys(chat, fromId, text);   // owner only
            } else if (text.startsWith("/myid") || text.equals("/id")) {
                send(chat, "🪪 Твой Telegram ID: <code>" + fromId + "</code>", null);
            } else if (menuPage != null) {
                send(chat, pages().get(menuPage), linkKeyboard(menuPage));   // a button of the bottom menu
            } else if (text.startsWith("/support")) {
                send(chat, pages().get("support"), linkKeyboard("support"));
            } else {
                // /start (or anything else): welcome + the menu with all the buttons at the bottom
                send(chat, main(name), replyMenu());
            }
        } else if (upd.get("callback_query") instanceof Map<?, ?> cb) {
            String data = String.valueOf(cb.get("data"));
            Map<?, ?> msg = (Map<?, ?>)cb.get("message");
            call("answerCallbackQuery", "{\"callback_query_id\":" + str(String.valueOf(cb.get("id"))) + "}");
            if (msg == null) return;
            long chat = num(((Map<?, ?>)msg.get("chat")).get("id"));
            long id = num(msg.get("message_id"));
            String name = firstName(cb.get("from"));
            log(name + " -> " + data);
            if (data.equals("home")) {
                edit(chat, id, main(name), mainKeyboard());
            } else if (pages().containsKey(data)) {
                edit(chat, id, pages().get(data), pageKeyboard(data));
            }
        }
    }

    static String firstName(Object from) {
        if (from instanceof Map<?, ?> f && f.get("first_name") instanceof String n) return n;
        return "друг";
    }

    static void send(long chat, String text, String keyboard) {
        Map<String, Object> r = call("sendMessage", "{\"chat_id\":" + chat + ",\"text\":" + str(text) + ",\"parse_mode\":\"HTML\",\"disable_web_page_preview\":true" + (keyboard == null ? "" : ",\"reply_markup\":" + keyboard) + "}");
        if (r != null && !Boolean.TRUE.equals(r.get("ok"))) log("Не отправилось сообщение: " + r.get("description"));
    }

    static void edit(long chat, long id, String text, String keyboard) {
        Map<String, Object> r = call("editMessageText", "{\"chat_id\":" + chat + ",\"message_id\":" + id + ",\"text\":" + str(text) + ",\"parse_mode\":\"HTML\",\"disable_web_page_preview\":true,\"reply_markup\":" + keyboard + "}");
        // a very old message cannot be edited: send a new one instead
        if (r != null && !Boolean.TRUE.equals(r.get("ok")) && !String.valueOf(r.get("description")).contains("not modified")) {
            send(chat, text, keyboard);
        }
    }

    // ------------------------------------------------------------------ Telegram API

    @SuppressWarnings("unchecked")
    static Map<String, Object> call(String method, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(api + method))
                    .timeout(Duration.ofSeconds(70))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Object o = new Json(res.body()).value();
            return o instanceof Map ? (Map<String, Object>)o : null;
        } catch (Exception e) {
            log("Нет связи с Telegram (" + e.getClass().getSimpleName() + "), пробую снова...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            return null;
        }
    }

    static String btn(String text, String data) {
        return "{\"text\":" + str(text) + ",\"callback_data\":" + str(data) + "}";
    }

    static String urlBtn(String text, String url) {
        return "{\"text\":" + str(text) + ",\"url\":" + str(url) + "}";
    }

    static String keyboard(List<List<String>> rows) {
        List<String> r = new ArrayList<>();
        for (List<String> row : rows) r.add("[" + String.join(",", row) + "]");
        return "{\"inline_keyboard\":[" + String.join(",", r) + "]}";
    }

    static long num(Object o) {
        return o instanceof Number n ? n.longValue() : 0L;
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** JSON string literal. */
    static String str(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) b.append(String.format("\\u%04x", (int)c));
                    else b.append(c);
                }
            }
        }
        return b.append('"').toString();
    }

    static void log(String s) {
        String line = "[" + LocalTime.now().withNano(0) + "] " + s;
        System.out.println(line);
        try {
            Files.writeString(Path.of("bot.log"), line + System.lineSeparator(), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ licence keys

    static long loadOwnerId() {
        String env = System.getenv("OWNER_ID");
        if (env != null && env.trim().matches("\\d+")) return Long.parseLong(env.trim());
        try {
            Path f = Path.of("owner-id.txt");
            if (Files.exists(f)) {
                String s = Files.readString(f).replaceAll("[^0-9]", "");
                if (!s.isEmpty()) return Long.parseLong(s);
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    static void generateKeys(long chat, long fromId, String text) {
        if (ownerId == 0) {
            send(chat, "🪪 Твой Telegram ID: <code>" + fromId + "</code>\n\n"
                    + "Сохрани его в файл <code>owner-id.txt</code> рядом с ботом (или задай переменную OWNER_ID) "
                    + "и перезапусти бота — тогда /genkey заработает.", null);
            return;
        }
        if (fromId != ownerId) {
            send(chat, "⛔ Ключи может создавать только владелец бота.", null);
            return;
        }
        int plan = 30;
        int count = 1;
        for (String arg : text.trim().split("\\s+")) {
            String v = arg.toLowerCase();
            if (v.isEmpty() || v.startsWith("/genkey")) continue;
            if (v.startsWith("x")) {
                try {
                    count = Math.max(1, Math.min(20, Integer.parseInt(v.substring(1))));
                } catch (Exception ignored) {
                }
            } else if (v.equals("life") || v.equals("forever") || v.equals("inf") || v.equals("навсегда")) {
                plan = PLAN_LIFETIME;
            } else {
                String digits = v.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    int n = Integer.parseInt(digits);
                    if (n == 1 || n == 7 || n == 30) plan = n;
                }
            }
        }
        try {
            StringBuilder sb = new StringBuilder("🔑 <b>Ключи Dettex</b> — " + planLabel(plan) + "\n" + LINE + "\n");
            for (int i = 0; i < count; i++) {
                String[] k = mintKey(plan);
                sb.append("<code>").append(k[0]).append("</code>\nUID: <code>").append(k[1]).append("</code>\n");
            }
            sb.append("\nВведи ключ в лаунчере. Срок считается с первой активации.");
            send(chat, sb.toString(), null);
        } catch (Exception e) {
            send(chat, "❌ Не удалось создать ключ: " + esc(String.valueOf(e)), null);
        }
    }

    static String planLabel(int plan) {
        switch (plan) {
            case 1: return "на 1 день";
            case 7: return "на 7 дней";
            case 30: return "на 30 дней";
            case PLAN_LIFETIME: return "навсегда";
            default: return plan + " дн.";
        }
    }

    /** Mints one key: payload (plan + UID) and a 5-byte HMAC signature, base32. */
    static String[] mintKey(int plan) throws Exception {
        byte[] payload = new byte[5];
        payload[0] = (byte) plan;
        byte[] uid = new byte[4];
        RANDOM.nextBytes(uid);
        System.arraycopy(uid, 0, payload, 1, 4);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(licenseSecret, "HmacSHA256"));
        byte[] sig = mac.doFinal(payload);
        byte[] raw = new byte[10];
        System.arraycopy(payload, 0, raw, 0, 5);
        System.arraycopy(sig, 0, raw, 5, 5);
        String s = base32(raw);
        String key = "DTX-" + s.substring(0, 4) + "-" + s.substring(4, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16);
        return new String[]{key, hex(uid)};
    }

    static String base32(byte[] data) {
        StringBuilder b = new StringBuilder();
        int buf = 0, bits = 0;
        for (byte x : data) {
            buf = (buf << 8) | (x & 0xff);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                b.append(KEY_ALPHABET.charAt((buf >> bits) & 31));
            }
        }
        if (bits > 0) b.append(KEY_ALPHABET.charAt((buf << (5 - bits)) & 31));
        return b.toString();
    }

    static String hex(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (byte x : b) s.append(String.format("%02X", x));
        return s.toString();
    }

    /** Small JSON reader: objects -> Map, arrays -> List, numbers -> Long / Double. */
    static final class Json {
        private final String s;
        private int i;

        Json(String s) {
            this.s = s;
        }

        Object value() {
            ws();
            char c = s.charAt(i);
            if (c == '{') {
                Map<String, Object> m = new LinkedHashMap<>();
                i++;
                ws();
                if (s.charAt(i) == '}') {
                    i++;
                    return m;
                }
                while (true) {
                    ws();
                    String k = string();
                    ws();
                    i++;   // :
                    m.put(k, value());
                    ws();
                    if (s.charAt(i++) == '}') return m;
                }
            }
            if (c == '[') {
                List<Object> l = new ArrayList<>();
                i++;
                ws();
                if (s.charAt(i) == ']') {
                    i++;
                    return l;
                }
                while (true) {
                    l.add(value());
                    ws();
                    if (s.charAt(i++) == ']') return l;
                }
            }
            if (c == '"') return string();
            if (s.startsWith("true", i)) {
                i += 4;
                return true;
            }
            if (s.startsWith("false", i)) {
                i += 5;
                return false;
            }
            if (s.startsWith("null", i)) {
                i += 4;
                return null;
            }
            int st = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            String n = s.substring(st, i);
            return n.contains(".") || n.contains("e") || n.contains("E") ? (Object)Double.parseDouble(n) : (Object)Long.parseLong(n);
        }

        private String string() {
            StringBuilder b = new StringBuilder();
            i++;
            while (true) {
                char c = s.charAt(i++);
                if (c == '"') return b.toString();
                if (c == '\\') {
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n' -> b.append('\n');
                        case 't' -> b.append('\t');
                        case 'r' -> b.append('\r');
                        case 'b' -> b.append('\b');
                        case 'f' -> b.append('\f');
                        case 'u' -> {
                            b.append((char)Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                        }
                        default -> b.append(e);
                    }
                } else {
                    b.append(c);
                }
            }
        }

        private void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }
    }
}
