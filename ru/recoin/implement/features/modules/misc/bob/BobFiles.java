package ru.recoin.implement.features.modules.misc.bob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads what Bob needs on first use into config/dettex/bob (nothing of it is inside the mod):
 * Piper (neural text-to-speech, MIT) with a Russian voice, Vosk (offline speech recognition,
 * Apache 2.0) and its small Russian model. Each file is downloaded once.
 */
public final class BobFiles {
    public static final String PIPER_ZIP = "https://github.com/rhasspy/piper/releases/download/2023.11.14-2/piper_windows_amd64.zip";
    public static final String VOICE_BASE = "https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/ru/ru_RU/";
    public static final String VOSK_JAR = "https://repo1.maven.org/maven2/com/alphacephei/vosk/0.3.45/vosk-0.3.45.jar";
    public static final String VOSK_MODEL_ZIP = "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip";
    public static final String VOSK_MODEL_DIR = "vosk-model-small-ru-0.22";

    private static final Map<String, Object> LOCKS = new ConcurrentHashMap<>();
    private static HttpClient http;

    private BobFiles() {
    }

    public static Path dir() {
        Path d;
        try {
            d = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("dettex").resolve("bob");
        } catch (Throwable ignored) {
            d = Paths.get("config", "dettex", "bob");
        }
        try {
            Files.createDirectories(d);
        } catch (IOException ignored) {
        }
        return d;
    }

    /** piper.exe, downloading and unpacking Piper if needed. */
    public static Path piper(Consumer<String> status) throws IOException {
        Path exe = BobFiles.dir().resolve("piper").resolve("piper.exe");
        if (Files.exists(exe)) {
            return exe;
        }
        synchronized (LOCKS.computeIfAbsent("piper", k -> new Object())) {
            if (!Files.exists(exe)) {
                Path zip = BobFiles.download(PIPER_ZIP, BobFiles.dir().resolve("piper.zip"), "голос Piper", status);
                BobFiles.unzip(zip, BobFiles.dir());   // the zip contains a "piper" folder
                Files.deleteIfExists(zip);
            }
        }
        if (!Files.exists(exe)) {
            throw new IOException("piper.exe not found after unpacking");
        }
        return exe;
    }

    /** voice: irina, dmitri, denis, ruslan. Returns the .onnx path (the .onnx.json is next to it). */
    public static Path voice(String voice, Consumer<String> status) throws IOException {
        String name = "ru_RU-" + voice + "-medium.onnx";
        Path onnx = BobFiles.dir().resolve("voices").resolve(name);
        Path json = BobFiles.dir().resolve("voices").resolve(name + ".json");
        synchronized (LOCKS.computeIfAbsent("voice-" + voice, k -> new Object())) {
            if (!Files.exists(json)) {
                BobFiles.download(VOICE_BASE + voice + "/medium/" + name + ".json?download=true", json, "настройки голоса", status);
            }
            if (!Files.exists(onnx)) {
                BobFiles.download(VOICE_BASE + voice + "/medium/" + name + "?download=true", onnx, "голос " + voice, status);
            }
        }
        return onnx;
    }

    public static Path voskJar(Consumer<String> status) throws IOException {
        Path jar = BobFiles.dir().resolve("vosk-0.3.45.jar");
        synchronized (LOCKS.computeIfAbsent("vosk-jar", k -> new Object())) {
            if (!Files.exists(jar)) {
                BobFiles.download(VOSK_JAR, jar, "распознавание речи", status);
            }
        }
        return jar;
    }

    public static Path voskModel(Consumer<String> status) throws IOException {
        Path model = BobFiles.dir().resolve(VOSK_MODEL_DIR);
        if (Files.isDirectory(model.resolve("am"))) {
            return model;
        }
        synchronized (LOCKS.computeIfAbsent("vosk-model", k -> new Object())) {
            if (!Files.isDirectory(model.resolve("am"))) {
                Path zip = BobFiles.download(VOSK_MODEL_ZIP, BobFiles.dir().resolve("vosk-model.zip"), "русскую модель речи (45 МБ)", status);
                BobFiles.unzip(zip, BobFiles.dir());
                Files.deleteIfExists(zip);
            }
        }
        return model;
    }

    /** Downloads to a temp file first, so a broken download never looks finished. */
    static Path download(String url, Path target, String what, Consumer<String> status) throws IOException {
        if (http == null) {
            http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.ALWAYS).build();
        }
        Files.createDirectories(target.getParent());
        Path tmp = target.resolveSibling(target.getFileName() + ".part");
        status.accept("Скачиваю " + what + "...");
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "DettexClient-Bob/1.0").timeout(Duration.ofMinutes(15)).GET().build();
            HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() != 200) {
                throw new IOException("HTTP " + resp.statusCode() + " for " + url);
            }
            long total = resp.headers().firstValueAsLong("Content-Length").orElse(-1L);
            long done = 0L;
            int lastPercent = -1;
            try (InputStream in = resp.body(); OutputStream out = Files.newOutputStream(tmp)) {
                byte[] buf = new byte[1 << 16];
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    done += n;
                    if (total > 0) {
                        int percent = (int)(done * 100 / total);
                        if (percent / 25 != lastPercent / 25) {
                            lastPercent = percent;
                            status.accept("Скачиваю " + what + ": " + percent + "%");
                        }
                    }
                }
            }
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted");
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    static void unzip(Path zip, Path into) throws IOException {
        Path root = into.toAbsolutePath().normalize();
        try (ZipInputStream z = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry e;
            while ((e = z.getNextEntry()) != null) {
                Path out = root.resolve(e.getName()).normalize();
                if (!out.startsWith(root)) {
                    continue;   // never write outside the folder
                }
                if (e.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    Files.copy(z, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
