package ru.recoin.implement.features.modules.misc.bob;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Bob's ears: offline speech recognition (Vosk, Russian model) from the microphone.
 * Push To Talk: listens only while the key is held, the phrase is sent when the key is released.
 * Wake Word: always listens, phrases that start with «Боб» are commands.
 * The Vosk library and model are downloaded once on first use (see BobFiles).
 */
public final class BobEars {
    private static final float RATE = 16000.0f;

    private final Consumer<String> onPhrase;   // final recognised phrase
    private final Consumer<String> status;     // messages for the player
    private java.util.function.BooleanSupplier awaitingAnswer = () -> false;
    private volatile boolean running;
    private volatile boolean active;           // push to talk: key held
    private volatile boolean wakeMode;
    private volatile String partial = "";
    private volatile float level;
    private Thread thread;

    public BobEars(Consumer<String> onPhrase, Consumer<String> status) {
        this.onPhrase = onPhrase;
        this.status = status;
    }

    /** While this is true, a bare «да / нет / номер» is delivered even in Wake Word mode. */
    public void setAwaitingAnswer(java.util.function.BooleanSupplier awaiting) {
        this.awaitingAnswer = awaiting;
    }

    /** A short reply to Bob's own question (да / нет / a number / an answer option). */
    public static boolean isAnswer(String text) {
        String t = text.toLowerCase().trim();
        return t.length() <= 28 && t.matches("(?U)(да|ага|угу|давай|конечно|го|ес|yes|ok|ок|окей|хочу|согласен|нет|не|неа|no|нисколь|потом|не надо|отмена|[1-4]|перв\\w*|втор\\w*|трет\\w*|четверт\\w*|один|одна|два|две|три|четыре)( .*)?");
    }

    public void setWakeMode(boolean wake) {
        this.wakeMode = wake;
    }

    /** Push to talk: true while the key is held. */
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isListening() {
        return this.running && (this.active || this.wakeMode);
    }

    public String partial() {
        return this.partial;
    }

    /** Microphone loudness 0..1 (for the animation). */
    public float level() {
        return this.level;
    }

    private volatile long retryAt;
    private volatile String lastError = "";

    public synchronized void start() {
        if (this.running || System.currentTimeMillis() < this.retryAt) {
            return;
        }
        this.running = true;
        this.thread = new Thread(this::run, "Dettex-Bob-Ears");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public synchronized void stop() {
        this.running = false;
        this.active = false;
        this.partial = "";
    }

    private void run() {
        Object model = null;
        URLClassLoader loader = null;
        TargetDataLine line = null;
        try {
            Path jar = BobFiles.voskJar(this.status);
            Path modelDir = BobFiles.voskModel(this.status);
            loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, BobEars.class.getClassLoader());
            Class<?> modelClass = Class.forName("org.vosk.Model", true, loader);
            Class<?> recClass = Class.forName("org.vosk.Recognizer", true, loader);
            this.status.accept("Загружаю распознавание речи...");
            model = modelClass.getConstructor(String.class).newInstance(modelDir.toString());
            Constructor<?> recCtor = recClass.getConstructor(modelClass, float.class);
            Method accept = recClass.getMethod("acceptWaveForm", byte[].class, int.class);
            Method result = recClass.getMethod("getResult");
            Method partialResult = recClass.getMethod("getPartialResult");
            Method finalResult = recClass.getMethod("getFinalResult");
            Method close = recClass.getMethod("close");

            AudioFormat format = new AudioFormat(RATE, 16, 1, true, false);
            line = (TargetDataLine)AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
            line.open(format, 4096);
            line.start();
            this.status.accept("Микрофон готов! " + (this.wakeMode ? "Скажи «Боб» и команду." : "Держи клавишу разговора и говори."));
            byte[] buf = new byte[2048];
            Object rec = null;
            boolean wasActive = false;
            long lastPartial = 0L;
            int frameCount = 0;
            while (this.running) {
                int n = line.read(buf, 0, buf.length);
                if (n <= 0) continue;
                frameCount++;
                if ((frameCount % 2) == 0) {
                    this.level = BobEars.rms(buf, n);
                }
                boolean listening = this.wakeMode || this.active;
                if (!listening) {
                    if (wasActive && rec != null) {
                        // key released: take everything that was said
                        try {
                            String text = BobEars.text(String.valueOf(finalResult.invoke(rec)));
                            close.invoke(rec);
                            rec = null;
                            this.partial = "";
                            this.deliver(text, false);
                        } catch (Throwable t) {
                            if (rec != null) { try { close.invoke(rec); } catch (Throwable ignored) {} rec = null; }
                            this.partial = "";
                        }
                    }
                    wasActive = false;
                    continue;
                }
                if (rec == null) {
                    rec = recCtor.newInstance(model, RATE);
                }
                wasActive = true;
                if ((Boolean)accept.invoke(rec, buf, n)) {
                    String text = BobEars.text(String.valueOf(result.invoke(rec)));
                    this.partial = "";
                    if (this.wakeMode) {
                        this.deliver(text, true);
                    } else if (!text.isEmpty()) {
                        // a pause inside push to talk: keep the words, send them on release
                        this.partial = text;
                        this.pending = this.pending.isEmpty() ? text : this.pending + " " + text;
                    }
                    lastPartial = 0L;
                } else {
                    long now = System.currentTimeMillis();
                    if ((frameCount % 6) == 0 && now - lastPartial > 250L) {
                        lastPartial = now;
                        JsonObject o = JsonParser.parseString(BobEars.fixEncoding(String.valueOf(partialResult.invoke(rec)))).getAsJsonObject();
                        String p = o.has("partial") ? o.get("partial").getAsString() : "";
                        this.partial = this.pending.isEmpty() ? p : (this.pending + " " + p).trim();
                    }
                }
            }
            if (rec != null) close.invoke(rec);
        } catch (Throwable e) {
            LoggerUtil.info("[Bob] microphone / speech recognition failed: " + e);
            String msg = e instanceof IllegalArgumentException || e instanceof javax.sound.sampled.LineUnavailableException
                    ? "Не могу открыть микрофон. Проверь, что он подключён и что в Windows разрешён доступ к микрофону для приложений."
                    : e instanceof java.net.ConnectException || e instanceof java.net.http.HttpTimeoutException || e instanceof java.io.IOException && String.valueOf(e.getMessage()).contains("HTTP")
                    ? "Не могу скачать распознавание речи (" + e.getClass().getSimpleName() + "). Возможно, сайт недоступен из твоей сети. Можно скачать вручную " + BobFiles.VOSK_MODEL_ZIP + " и распаковать в папку " + BobFiles.dir() + "."
                    : "Распознавание речи не запустилось: " + e.getClass().getSimpleName() + (e.getMessage() == null ? "" : " " + e.getMessage());
            this.retryAt = System.currentTimeMillis() + 120_000L;   // try again in 2 minutes, not every tick
            if (!msg.equals(this.lastError)) {
                this.lastError = msg;
                this.status.accept(msg + " Попробую ещё раз через 2 минуты.");
            }
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
            if (model != null) {
                try {
                    model.getClass().getMethod("close").invoke(model);
                } catch (Throwable ignored) {
                }
            }
            this.running = false;
            this.level = 0.0f;
        }
    }

    private volatile String pending = "";

    private void deliver(String last, boolean wake) {
        String text = wake ? last : (this.pending.isEmpty() ? last : (this.pending + " " + last).trim());
        this.pending = "";
        if (text == null || text.isBlank()) return;
        if (wake) {
            if (!BobEars.startsWithName(text)) {
                if (this.awaitingAnswer.getAsBoolean() && BobEars.isAnswer(text)) {
                    this.onPhrase.accept(text.trim());   // answering Bob's question by voice
                }
                return;   // otherwise not for Bob
            }
        }
        this.onPhrase.accept(text.trim());
    }

    /** Ways the recogniser writes «Боб». */
    public static final String[] HEARD_NAMES = {"бобби", "бобик", "боб", "баб", "боп", "поп", "бог", "bob"};

    static boolean startsWithName(String text) {
        String lower = text.toLowerCase().trim();
        for (String n : HEARD_NAMES) {
            if (lower.equals(n) || lower.startsWith(n + " ")) return true;
        }
        return false;
    }

    private static String text(String json) {
        try {
            JsonObject o = JsonParser.parseString(BobEars.fixEncoding(json)).getAsJsonObject();
            return o.has("text") ? o.get("text").getAsString().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Vosk returns UTF-8, but on Windows JNA decodes it with the system code page (cp1251), so
     * «привет» arrives as «РїСЂРёРІРµС‚». Re-encode it back when that happened.
     */
    static String fixEncoding(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        for (String cp : new String[]{"windows-1251", "Cp1252"}) {
            try {
                java.nio.charset.Charset charset = java.nio.charset.Charset.forName(cp);
                if (!charset.newEncoder().canEncode(s)) continue;
                byte[] raw = s.getBytes(charset);
                String fixed = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                        .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                        .decode(java.nio.ByteBuffer.wrap(raw)).toString();
                if (!fixed.equals(s)) return fixed;
            } catch (Exception ignored) {
                // not mojibake for this code page
            }
        }
        return s;
    }

    private static float rms(byte[] b, int n) {
        double sum = 0.0;
        int count = n / 2;
        for (int i = 0; i + 1 < n; i += 2) {
            short v = (short)(b[i] & 0xFF | b[i + 1] << 8);
            sum += (double)v * v;
        }
        return count == 0 ? 0.0f : (float)Math.min(1.0, Math.sqrt(sum / count) / 6000.0);
    }
}
