package ru.recoin.implement.features.modules.misc.bob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Bob's voice. Main engine: Piper, a neural text-to-speech that runs offline on your PC and sounds
 * like a real person (Russian voices Irina, Dmitri, Denis, Ruslan; downloaded once on first use).
 * Fallback: the old Windows voice. The mouth animation follows the real loudness of the speech.
 */
public final class BobVoice {
    public static final class Settings {
        public String engine = "Neural";     // Neural / Windows
        public String voice = "dmitri";      // irina / dmitri / denis / ruslan
        public String windowsGender = "Auto";
        public float speed = 1.0f;           // 0.5 .. 2.0
        public float pitch = 1.0f;           // 0.7 .. 1.4 (playback rate)
        public int volume = 80;              // 0 .. 100
        public boolean on = true;
    }

    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Dettex-Bob-Speech");
        t.setDaemon(true);
        return t;
    });
    private static volatile Clip clip;
    private static volatile float[] envelope = new float[0];   // loudness per 20 ms of the current phrase
    private static volatile long clipStartedAt;
    private static volatile float clipRate = 1.0f;
    private static volatile long estimatedEnd;
    private static volatile int generation;
    private static volatile boolean neuralFailed;
    private static volatile long neuralRetryAt;

    private BobVoice() {
    }

    public static boolean isWindows() {
        return WindowsVoice.isWindows();
    }

    /** Says the text in the background (the previous phrase is cut off). */
    public static void say(String text, Settings s, Consumer<String> status) {
        int gen = ++generation;
        estimatedEnd = System.currentTimeMillis() + 700L + (long)(text.length() * 62L / Math.max(0.5f, s.speed));
        BobVoice.stopClip();
        if (!s.on || s.volume <= 0) {
            return;
        }
        if (neuralFailed && System.currentTimeMillis() > neuralRetryAt) {
            neuralFailed = false;   // try the neural voice again from time to time
        }
        if (!"Neural".equals(s.engine) || !BobVoice.isWindows() || neuralFailed) {
            WindowsVoice.say(text, s.windowsGender, Math.round((s.speed - 1.0f) * 6.0f), s.volume, s.pitch < 0.9f ? "low" : s.pitch > 1.1f ? "high" : "medium", true);
            return;
        }
        WORKER.execute(() -> {
            if (gen != generation) return;   // a newer phrase is already waiting
            try {
                byte[] wav = BobVoice.synthesize(text, s, status);
                if (gen != generation) return;
                BobVoice.play(wav, s);
            } catch (Exception e) {
                LoggerUtil.info("[Bob] neural voice failed, using the Windows voice: " + e);
                boolean net = e instanceof java.net.ConnectException || e instanceof java.net.http.HttpTimeoutException || String.valueOf(e.getMessage()).startsWith("HTTP");
                status.accept(net
                        ? "Не могу скачать нейроголос (" + e.getClass().getSimpleName() + "): GitHub или Hugging Face недоступны из твоей сети. Пока говорю голосом Windows и попробую снова через 10 минут."
                        : "Нейроголос не запустился (" + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()) + "), говорю голосом Windows.");
                neuralFailed = true;
                neuralRetryAt = System.currentTimeMillis() + 600_000L;
                WindowsVoice.say(text, s.windowsGender, 0, s.volume, "medium", true);
            }
        });
    }

    /** Runs piper.exe: text on stdin, WAV in a temp file. */
    private static byte[] synthesize(String text, Settings s, Consumer<String> status) throws IOException, InterruptedException {
        Path exe = BobFiles.piper(status);
        Path model = BobFiles.voice(s.voice, status);
        Path out = Files.createTempFile("bob", ".wav");
        try {
            float lengthScale = 1.0f / Math.max(0.5f, Math.min(2.0f, s.speed));
            Process p = new ProcessBuilder(exe.toString(), "--model", model.toString(), "--output_file", out.toString(),
                    "--length_scale", String.valueOf(lengthScale), "--sentence_silence", "0.15")
                    .directory(exe.getParent().toFile()).redirectErrorStream(true).start();
            try (OutputStream in = p.getOutputStream()) {
                in.write(text.replace('\n', ' ').getBytes(StandardCharsets.UTF_8));
                in.write('\n');
            }
            p.getInputStream().transferTo(OutputStream.nullOutputStream());
            if (!p.waitFor(30, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new IOException("piper timed out");
            }
            byte[] wav = Files.readAllBytes(out);
            if (wav.length < 100) {
                throw new IOException("piper produced no audio (exit " + p.exitValue() + ")");
            }
            return wav;
        } finally {
            Files.deleteIfExists(out);
        }
    }

    private static void play(byte[] wav, Settings s) throws Exception {
        AudioInputStream original = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav));
        AudioFormat f = original.getFormat();
        byte[] pcm = original.readAllBytes();
        envelope = BobVoice.envelope(pcm, f);
        // pitch: play the same samples a bit faster or slower
        float rate = Math.max(0.7f, Math.min(1.4f, s.pitch));
        AudioFormat shifted = new AudioFormat(f.getEncoding(), f.getSampleRate() * rate, f.getSampleSizeInBits(), f.getChannels(), f.getFrameSize(), f.getFrameRate() * rate, f.isBigEndian());
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(pcm), shifted, pcm.length / Math.max(1, f.getFrameSize()));
        Clip c = AudioSystem.getClip();
        c.open(stream);
        try {
            FloatControl gain = (FloatControl)c.getControl(FloatControl.Type.MASTER_GAIN);
            float linear = Math.max(0.0001f, s.volume / 100.0f);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), (float)(20.0 * Math.log10(linear)))));
        } catch (Exception ignored) {
            // no gain control on this device: plays at full volume
        }
        clipRate = rate;
        clipStartedAt = System.currentTimeMillis();
        clip = c;
        c.addLineListener(ev -> {
            if (ev.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                c.close();
            }
        });
        c.start();
    }

    /** Loudness (0..1) for every 20 ms of 16-bit PCM. */
    static float[] envelope(byte[] pcm, AudioFormat f) {
        if (f.getSampleSizeInBits() != 16) {
            return new float[0];
        }
        int frame = f.getFrameSize();
        int perSlot = Math.max(1, (int)(f.getSampleRate() * 0.02f));
        int frames = pcm.length / frame;
        float[] out = new float[frames / perSlot + 1];
        ByteBuffer b = ByteBuffer.wrap(pcm).order(f.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        float max = 1e-6f;
        for (int i = 0; i < out.length; ++i) {
            double sum = 0.0;
            int n = 0;
            for (int k = i * perSlot; k < Math.min(frames, (i + 1) * perSlot); ++k) {
                short v = b.getShort(k * frame);
                sum += (double)v * v;
                ++n;
            }
            out[i] = n == 0 ? 0.0f : (float)Math.sqrt(sum / n);
            max = Math.max(max, out[i]);
        }
        for (int i = 0; i < out.length; ++i) {
            out[i] = Math.min(1.0f, out[i] / max * 1.3f);
        }
        return out;
    }

    /** How open Bob's mouth is right now (0..1), or -1 when he is silent. */
    public static float mouth() {
        Clip c = clip;
        if (c != null && c.isOpen() && c.isRunning()) {
            float[] env = envelope;
            if (env.length == 0) return 0.5f;
            long ms = (long)((System.currentTimeMillis() - clipStartedAt) * clipRate);
            int i = (int)(ms / 20L);
            return i >= 0 && i < env.length ? env[i] : 0.0f;
        }
        if (WindowsVoice.isTalking()) {
            double t = System.currentTimeMillis() / 1000.0;
            return (float)Math.abs(Math.sin(t * 17.0) * 0.7 + Math.sin(t * 29.0) * 0.3);
        }
        return -1.0f;
    }

    public static boolean isTalking() {
        Clip c = clip;
        return c != null && c.isOpen() && c.isRunning() || WindowsVoice.isTalking();
    }

    /** True while a phrase is being prepared (first run downloads and loads the voice). */
    public static boolean isPreparing() {
        return !BobVoice.isTalking() && System.currentTimeMillis() < estimatedEnd && generation > 0 && clip == null;
    }

    private static void stopClip() {
        Clip c = clip;
        clip = null;
        if (c != null) {
            try {
                c.stop();
                c.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void stop() {
        ++generation;
        BobVoice.stopClip();
        WindowsVoice.stop();
        estimatedEnd = 0L;
    }

    public static void shutdown() {
        BobVoice.stop();
        WindowsVoice.shutdown();
    }

    public static String windowsVoices() {
        return WindowsVoice.voices();
    }

    /** Lets the neural voice be tried again (after the settings changed). */
    public static void resetFailure() {
        neuralFailed = false;
    }
}
