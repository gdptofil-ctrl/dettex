package ru.recoin.implement.features.modules.misc.bob;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.logger.implement.MinecraftLogger;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.events.chat.ChatEvent;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.events.render.DrawEvent;

/**
 * Bob: a little voice assistant robot in the corner of the screen. Talk to him in chat with
 * "боб ..." / "bob ..." (or press the Talk Key): he toggles modules, tells FPS, coordinates, health,
 * ping, time, asks quiz questions and jokes. He speaks with a Windows voice (choice of voice, pitch,
 * speed, volume), moves his mouth while talking and sometimes asks you something himself.
 * Messages to Bob are never sent to the server.
 */
public final class Bob
extends Module {
    private static final Random RANDOM = new Random();
    private static final MinecraftLogger CHAT = new MinecraftLogger();

    // voice
    private final SelectSetting engine = new SelectSetting("Voice Engine", "Neural: realistic offline voice (downloads once ~80 MB); Windows: the old system voice").value("Neural", "Windows").selected("Neural");
    private final SelectSetting neuralVoice = new SelectSetting("Neural Voice", "Irina — female, Dmitri / Denis / Ruslan — male").value("Dmitri", "Irina", "Denis", "Ruslan").selected("Dmitri").visible(() -> this.engine.isSelected("Neural"));
    private final SelectSetting windowsVoice = new SelectSetting("Windows Voice", "Which Windows voice").value("Auto", "Female", "Male").selected("Auto").visible(() -> this.engine.isSelected("Windows"));
    private final ValueSetting volume = new ValueSetting("Volume", "Voice volume").setValue(80.0f).range(0, 100);
    private final ValueSetting speed = new ValueSetting("Speed", "How fast Bob talks").setValue(1.0f).range(0.5f, 2.0f);
    private final ValueSetting pitch = new ValueSetting("Pitch", "Lower or higher voice").setValue(1.0f).range(0.7f, 1.4f);
    private final BooleanSetting voiceOn = new BooleanSetting("Voice", "Speak out loud. Off: only text").setValue(true);
    private final GroupSetting voiceGroup = new GroupSetting("Voice", "How Bob sounds", false).settings(this.voiceOn, this.engine, this.neuralVoice, this.windowsVoice, this.volume, this.speed, this.pitch);
    // microphone
    private final BooleanSetting microphone = new BooleanSetting("Microphone", "Talk to Bob with your voice (offline recognition, downloads once ~50 MB)").setValue(false);
    private final SelectSetting micMode = new SelectSetting("Mic Mode", "Push To Talk: hold the Talk Key while speaking; Wake Word: start the phrase with «Боб»").value("Push To Talk", "Wake Word").selected("Push To Talk").visible(this.microphone::isValue);
    private final BindSetting talkKey = new BindSetting("Talk Key", "Hold to speak (microphone on) or opens the chat with «боб » (microphone off)");
    private final GroupSetting micGroup = new GroupSetting("Microphone", "Voice commands", false).settings(this.microphone, this.micMode, this.talkKey);
    // brain
    private final SelectSetting brainMode = new SelectSetting("Brain", "Simple: built-in commands only. Ollama: free AI on your PC. OpenAI-compatible / Anthropic: AI with your API key").value("Simple", "Ollama", "OpenAI-compatible", "Anthropic").selected("Simple");
    private final TextSetting apiUrl = new TextSetting("API URL", "Empty = default address of the chosen service").visible(() -> !this.brainMode.isSelected("Simple"));
    private final TextSetting apiKey = new TextSetting("API Key", "Your key (not needed for Ollama). Stored in the client config on your PC").visible(() -> this.brainMode.isSelected("OpenAI-compatible") || this.brainMode.isSelected("Anthropic"));
    private final TextSetting aiModel = new TextSetting("Model", "Empty = qwen2.5:7b (Ollama), gpt-4o-mini, claude-haiku-5-5").visible(() -> !this.brainMode.isSelected("Simple"));
    private final GroupSetting brainGroup = new GroupSetting("Brain", "How smart Bob is", false).settings(this.brainMode, this.apiUrl, this.apiKey, this.aiModel);
    // behaviour and look
    private final BooleanSetting randomQuestions = new BooleanSetting("Random Questions", "Bob asks you something from time to time").setValue(true);
    private final ValueSetting interval = new ValueSetting("Question Every (min)", "Average time between Bob's questions").setValue(5.0f).range(1, 30).visible(this.randomQuestions::isValue);
    private final SelectSetting corner = new SelectSetting("Position", "Where Bob sits").value("Bottom Right", "Bottom Left", "Top Right", "Top Left").selected("Bottom Right");
    private final ValueSetting size = new ValueSetting("Size", "Size of Bob").setValue(1.0f).range(0.5f, 2.0f);
    private final BooleanSetting chatLog = new BooleanSetting("Chat Log", "Also write the conversation into the chat (only you see it)").setValue(true);
    private final BooleanSetting noPrefixAnswers = new BooleanSetting("Answer Without Name", "Reply to Bob's question with just «да» / «нет» / a number").setValue(true);

    private static boolean greeted;
    private final BobBrain brain = new BobBrain();
    private final BobAI ai = new BobAI();
    private final BobEars ears = new BobEars(this::onHeard, this::notice);
    private volatile boolean thinking;
    private String lastVoiceKey = "";
    private String bubble = "";
    private long bubbleAt;
    private long nextQuestionAt;
    private long blinkAt;
    private final List<Runnable> queue = new ArrayList<>();

    public Bob() {
        super("Bob", "Bob", ModuleCategory.MISC);
        this.setup(this.voiceGroup, this.micGroup, this.brainGroup, this.randomQuestions, this.interval, this.corner, this.size, this.chatLog, this.noPrefixAnswers);
    }

    @Override
    public void activate() {
        this.scheduleQuestion();
        BobVoice.resetFailure();
        if (greeted) {
            return;
        }
        greeted = true;
        this.say(BobVoice.isWindows() ? "Привет! Я Боб. Напиши в чат «боб помощь», и я расскажу, что умею." : "Привет! Я Боб. Голос работает только на Windows, но писать я умею.");
    }

    @Override
    public void deactivate() {
        BobVoice.shutdown();
        this.ears.stop();
        this.ai.forget();
        this.bubble = "";
    }

    private void scheduleQuestion() {
        double minutes = this.interval.getValue() * (0.6 + RANDOM.nextDouble() * 0.8);
        this.nextQuestionAt = System.currentTimeMillis() + (long)(minutes * 60000.0);
    }

    private BobVoice.Settings voiceSettings() {
        BobVoice.Settings v = new BobVoice.Settings();
        v.engine = this.engine.getSelected();
        v.voice = this.neuralVoice.getSelected().toLowerCase();
        v.windowsGender = this.windowsVoice.getSelected();
        v.speed = this.speed.getValue();
        v.pitch = this.pitch.getValue();
        v.volume = this.volume.getInt();
        v.on = this.voiceOn.isValue();
        return v;
    }

    /** Status messages (downloads, microphone) go to the chat only. */
    private void notice(String text) {
        try {
            class_310.method_1551().execute(() -> CHAT.minecraftLog(new class_2561[]{class_2561.method_43470("\u00a7b[Боб] \u00a77" + text)}));
        } catch (Throwable ignored) {
        }
    }

    /** Bob says something: bubble on screen, voice, and a chat line. */
    public void say(String text) {
        this.bubble = text;
        this.bubbleAt = System.currentTimeMillis();
        BobVoice.say(text, this.voiceSettings(), this::notice);
        if (this.chatLog.isValue()) {
            try {
                CHAT.minecraftLog(new class_2561[]{class_2561.method_43470("§b[Боб] §f" + text)});
            } catch (Throwable ignored) {
            }
        }
    }

    private static String stripName(String message) {
        String m = message.trim();
        String lower = m.toLowerCase();
        for (String name : new String[]{"боб", "bob", "бобик"}) {
            if (lower.equals(name)) return "";
            if (lower.startsWith(name) && lower.length() > name.length() && !Character.isLetterOrDigit(lower.charAt(name.length()))) {
                return m.substring(name.length()).replaceFirst("^[\\s,.:!?-]+", "");
            }
        }
        return null;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        String rest = Bob.stripName(event.getMessage());
        if (rest == null && this.noPrefixAnswers.isValue() && this.brain.waitingForAnswer()) {
            String m = event.getMessage().trim().toLowerCase();
            if (BobEars.isAnswer(m) || m.matches("[1-4][).].*")) {
                rest = event.getMessage().trim();
            }
        }
        if (rest == null) {
            return;
        }
        event.cancel();   // never send this to the server
        this.handle(event.getMessage().trim(), rest);
    }

    /** A phrase heard from the microphone. */
    private void onHeard(String phrase) {
        String rest = phrase;
        String lower = phrase.toLowerCase().trim();
        for (String n : BobEars.HEARD_NAMES) {
            if (lower.equals(n)) { rest = ""; break; }
            if (lower.startsWith(n + " ")) { rest = phrase.trim().substring(n.length()).trim(); break; }
        }
        final String said = rest;
        class_310.method_1551().execute(() -> this.handle("\u266a " + phrase, said));
    }

    private void handle(String original, String rest) {
        if (this.chatLog.isValue()) {
            try {
                CHAT.minecraftLog(new class_2561[]{class_2561.method_43470("\u00a77Ты: " + original)});
            } catch (Throwable ignored) {
            }
        }
        String lower = rest.toLowerCase();
        if (lower.matches("(замолчи|тихо|стоп|молчи|хватит|stop|shut up).*")) {
            BobVoice.stop();
            this.bubble = "";
            return;
        }
        if (lower.startsWith("громче") || lower.startsWith("louder")) {
            this.volume.setValue(Math.min(100.0f, this.volume.getValue() + 20.0f));
            this.say("Так лучше слышно?");
            return;
        }
        if (lower.startsWith("тише") || lower.startsWith("quieter")) {
            this.volume.setValue(Math.max(10.0f, this.volume.getValue() - 20.0f));
            this.say("Говорю тише.");
            return;
        }
        if (lower.startsWith("забудь") || lower.startsWith("новый разговор")) {
            this.ai.forget();
            this.say("Начинаем разговор с чистого листа.");
            return;
        }
        this.scheduleQuestion();   // just talked: no random question right away
        String answer = this.brain.reply(rest);
        if (answer != null) {
            this.say(answer);
            return;
        }
        if (this.brainMode.isSelected("Simple")) {
            this.say(BobBrain.notUnderstood());
            return;
        }
        this.askAI(rest);
    }

    private void askAI(String question) {
        if (this.thinking) {
            this.say("Секунду, я ещё думаю над прошлым вопросом.");
            return;
        }
        this.thinking = true;
        this.bubble = "Думаю...";
        this.bubbleAt = System.currentTimeMillis();
        BobAI.Config cfg = new BobAI.Config(this.brainMode.getSelected(), this.apiUrl.getText(), this.apiKey.getText(), this.aiModel.getText(), BobBrain.context());
        Thread t = new Thread(() -> {
            try {
                BobAI.Answer a = this.ai.ask(question, cfg);
                class_310.method_1551().execute(() -> {
                    for (String m : a.enable()) BobBrain.apply(m, true);
                    for (String m : a.disable()) BobBrain.apply(m, false);
                    this.say(a.text().isEmpty() ? "Готово." : a.text());
                });
            } catch (Exception e) {
                String why = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                if (e instanceof java.net.http.HttpTimeoutException) {
                    why = "слишком долго думал. Первый ответ Ollama бывает медленным, пока модель загружается, спроси ещё раз.";
                }
                String msg = this.brainMode.isSelected("Ollama") && (e instanceof java.net.ConnectException || why.contains("Connect"))
                        ? "Не вижу Ollama на твоём компьютере. Установи её с сайта ollama.com и скачай модель командой «ollama pull qwen2.5:7b»."
                        : "ИИ не ответил: " + (why.length() > 120 ? why.substring(0, 120) : why);
                class_310.method_1551().execute(() -> this.say(msg));
            } finally {
                this.thinking = false;
            }
        }, "Dettex-Bob-AI");
        t.setDaemon(true);
        t.start();
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        int key = this.talkKey.getKey();
        if (key <= 0 || event.key() != key) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (this.microphone.isValue() && this.micMode.isSelected("Push To Talk")) {
            if (event.isKeyDown(key) && mc.field_1755 == null) {
                BobVoice.stop();   // you talk: Bob stops talking
                this.ears.setActive(true);
            } else if (event.isKeyReleased(key)) {
                this.ears.setActive(false);
            }
            return;
        }
        if (!this.microphone.isValue() && mc.field_1755 == null && event.isKeyDown(key)) {
            mc.execute(() -> mc.method_1507(new class_408("боб ", false)));
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_310 mc = class_310.method_1551();
        if (this.microphone.isValue()) {
            this.ears.setWakeMode(this.micMode.isSelected("Wake Word"));
            this.ears.setAwaitingAnswer(this.brain::waitingForAnswer);
            this.ears.start();
        } else {
            this.ears.stop();
        }
        String voiceKey = this.engine.getSelected() + this.neuralVoice.getSelected();
        if (!voiceKey.equals(this.lastVoiceKey)) {
            this.lastVoiceKey = voiceKey;
            BobVoice.resetFailure();   // a new voice choice: try the neural voice again
        }
        if (mc.field_1724 == null || !this.randomQuestions.isValue()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now >= this.nextQuestionAt) {
            this.scheduleQuestion();
            if (mc.field_1755 == null && !BobVoice.isTalking()) {
                String q = this.brain.randomQuestion();
                if (q != null) this.say(q);
            }
        }
    }

    // ------------------------------------------------------------------ drawing

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (event.getDrawContext() == null) {
            return;
        }
        class_332 c = event.getDrawContext();
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_22683() == null) {
            return;
        }
        float s = this.size.getValue() * 1.6f;
        int bw = Math.round(26 * s);    // head size
        int sw = mc.method_22683().method_4486();
        int sh = mc.method_22683().method_4502();
        boolean right = this.corner.getSelected().contains("Right");
        boolean bottom = this.corner.getSelected().contains("Bottom");
        long now = System.currentTimeMillis();
        double t = now / 1000.0;
        boolean talking = BobVoice.isTalking();
        boolean listening = this.ears.isListening() && (this.micMode.isSelected("Push To Talk") || !this.ears.partial().isEmpty());
        // gentle floating
        int bob = (int)Math.round(Math.sin(t * 2.0) * 2.0 * s);
        int x = right ? sw - bw - 12 : 12;
        int y = (bottom ? sh - bw - Math.round(46 * s) : 26) + bob;
        this.drawRobot(c, x, y, s, talking, now, listening);
        // speech bubble
        long shown = now - this.bubbleAt;
        if (listening && this.micMode.isSelected("Push To Talk") || !this.ears.partial().isEmpty() && this.ears.isListening()) {
            String heard = this.ears.partial();
            String saved = this.bubble;
            long savedAt = this.bubbleAt;
            this.bubble = "\u25cf Слушаю" + (heard.isEmpty() ? "..." : ": " + heard);
            this.bubbleAt = now - 100000L;
            this.drawBubble(c, mc, x, y, bw, right, bottom, s, false, 0L);
            this.bubble = saved;
            this.bubbleAt = savedAt;
            return;
        }
        if (this.thinking) {
            String dots = ".".repeat((int)(now / 400L % 3L) + 1);
            String saved = this.bubble;
            this.bubble = "Думаю" + dots;
            this.drawBubble(c, mc, x, y, bw, right, bottom, s, false, 0L);
            this.bubble = saved;
            return;
        }
        if (!this.bubble.isEmpty() && (talking || shown < 4000L + this.bubble.length() * 40L)) {
            this.drawBubble(c, mc, x, y, bw, right, bottom, s, talking, shown);
        }
    }

    private void drawRobot(class_332 c, int x, int y, float s, boolean talking, long now, boolean listening) {
        int bw = Math.round(26 * s);
        int bh = Math.round(22 * s);
        int body = 0xFF3A7BFF;
        int bodyDark = 0xFF2A5BC4;
        // antenna with a glowing tip (pulses while talking)
        int ax = x + bw / 2;
        c.method_25294(ax - 1, y - Math.round(7 * s), ax + 1, y, 0xFF9DB4D8);
        float pulse = talking ? (float)(0.5 + 0.5 * Math.sin(now / 90.0)) : listening ? 0.3f + 0.7f * Math.min(1.0f, this.ears.level() * 2.0f) : 0.3f;
        int tip = Math.round(3 * s);
        int glow = ((int)(80 + 175 * pulse) << 24) | (listening ? 0xFF4D6A : 0xFFD23C);
        c.method_25294(ax - tip - 2, y - Math.round(7 * s) - tip - 2, ax + tip + 2, y - Math.round(7 * s) + tip - 2 + 2, (glow & 0x00FFFFFF) | 0x40000000);
        c.method_25294(ax - tip, y - Math.round(7 * s) - tip, ax + tip, y - Math.round(7 * s) + tip - 2, glow | 0xFF000000);
        // ears
        c.method_25294(x - Math.round(3 * s), y + bh / 3, x, y + bh * 2 / 3, bodyDark);
        c.method_25294(x + bw, y + bh / 3, x + bw + Math.round(3 * s), y + bh * 2 / 3, bodyDark);
        // head with rounded corners
        int r = Math.round(3 * s);
        c.method_25294(x + r, y, x + bw - r, y + bh, body);
        c.method_25294(x, y + r, x + bw, y + bh - r, body);
        c.method_25294(x + 1, y + bh - r, x + bw - 1, y + bh, bodyDark);
        // face screen
        int fx = x + Math.round(3 * s);
        int fy = y + Math.round(3 * s);
        int fw = bw - Math.round(6 * s);
        int fh = bh - Math.round(7 * s);
        c.method_25294(fx, fy, fx + fw, fy + fh, 0xFF0E1726);
        // eyes: blink every few seconds, look around a little
        if (now > this.blinkAt + 4200L) this.blinkAt = now + RANDOM.nextInt(2500);
        boolean blink = now > this.blinkAt && now < this.blinkAt + 140L;
        int look = (int)Math.round(Math.sin(now / 1700.0) * 1.5 * s);
        int ew = Math.round(4 * s);
        int eh = blink ? Math.max(1, Math.round(s)) : Math.round(5 * s);
        int ey = fy + Math.round(3 * s) + (blink ? Math.round(2 * s) : 0);
        int eyeColor = 0xFF7CF5FF;
        c.method_25294(fx + Math.round(3 * s) + look, ey, fx + Math.round(3 * s) + look + ew, ey + eh, eyeColor);
        c.method_25294(fx + fw - Math.round(3 * s) - ew + look, ey, fx + fw - Math.round(3 * s) + look, ey + eh, eyeColor);
        // mouth: opens and closes with the speech
        int mw = Math.round(8 * s);
        int mx = fx + fw / 2 - mw / 2;
        int my = fy + fh - Math.round(4 * s);
        float open = Math.max(0.0f, BobVoice.mouth());
        int mh = Math.max(1, Math.round((1 + open * 4) * s));
        c.method_25294(mx, my - mh / 2, mx + mw, my + (mh + 1) / 2, talking ? 0xFFFF7AA8 : eyeColor);
        // little body
        int tx = x + Math.round(6 * s);
        c.method_25294(tx, y + bh + 1, x + bw - Math.round(6 * s), y + bh + Math.round(10 * s), bodyDark);
        c.method_25294(tx + Math.round(3 * s), y + bh + Math.round(3 * s), tx + Math.round(6 * s), y + bh + Math.round(6 * s), talking ? 0xFF6CFF8A : 0xFF4A5A70);
    }

    private void drawBubble(class_332 c, class_310 mc, int x, int y, int bw, boolean right, boolean bottom, float s, boolean talking, long shown) {
        FontRenderer font = Fonts.getSize(16, Fonts.Type.INTER_BOLD);
        class_4587 matrix = MatrixStackCompat.from(c.method_51448());
        // typing effect while Bob talks
        int chars = talking ? Math.min(this.bubble.length(), (int)(shown / 45L) + 1) : this.bubble.length();
        String text = this.bubble.substring(0, chars);
        int maxW = 200;
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (line.length() > 0 && font.getStringWidth(test) > maxW) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        if (lines.size() > 7) lines = lines.subList(lines.size() - 7, lines.size());   // keep the newest lines
        float lh = font.getStringHeight("Ag") + 3.0f;
        float w = 0.0f;
        for (String l : lines) w = Math.max(w, font.getStringWidth(l));
        int pad = 6;
        int bwid = (int)w + pad * 2;
        int bhei = (int)(lines.size() * lh) + pad * 2 - 2;
        int bx = right ? x + bw - bwid : x;
        int by = bottom ? y - bhei - Math.round(12 * s) : y + Math.round(36 * s);
        float fade = Math.min(1.0f, Math.max(0.0f, (4000L + this.bubble.length() * 40L - shown) / 600.0f));
        if (talking) fade = 1.0f;
        int a = Math.round(0xE0 * fade);
        c.method_25294(bx + 2, by, bx + bwid - 2, by + bhei, a << 24 | 0x121A2A);
        c.method_25294(bx, by + 2, bx + bwid, by + bhei - 2, a << 24 | 0x121A2A);
        c.method_25294(bx, by + bhei - 2, bx + 3, by + bhei, a << 24 | 0x3A7BFF);
        // tail pointing to Bob
        int tx = right ? x + bw / 2 : x + bw / 2;
        for (int i = 0; i < 5; ++i) {
            int ty = bottom ? by + bhei + i : by - i - 1;
            c.method_25294(tx - (5 - i), ty, tx + (5 - i), ty + 1, a << 24 | 0x121A2A);
        }
        int textColor = Math.round(255 * fade) << 24 | 0xFFFFFF;
        for (int i = 0; i < lines.size(); ++i) {
            font.drawString(matrix, lines.get(i), bx + pad, by + pad - 1 + i * lh, textColor);
        }
    }
}
