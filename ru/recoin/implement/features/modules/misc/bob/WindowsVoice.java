package ru.recoin.implement.features.modules.misc.bob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import ru.recoin.common.util.logger.LoggerUtil;

/**
 * Fallback voice: the speech synthesizer built into Windows (System.Speech), run in one background
 * PowerShell process so every phrase starts at once. Voices installed in Windows are used (a Russian
 * one is preferred). On other systems, or if speech is unavailable, Bob just shows his text.
 */
final class WindowsVoice {
    private static final String SCRIPT = String.join("\r\n",
            "$ErrorActionPreference = 'SilentlyContinue'",
            "Add-Type -AssemblyName System.Speech",
            "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer",
            "$s.SetOutputToDefaultAudioDevice()",
            "$all = $s.GetInstalledVoices() | Where-Object { $_.Enabled } | ForEach-Object { $_.VoiceInfo }",
            "[Console]::Out.WriteLine('VOICES|' + (($all | ForEach-Object { $_.Name + ' (' + $_.Culture.Name + ', ' + $_.Gender + ')' }) -join '; '))",
            "[Console]::Out.Flush()",
            "while ($true) {",
            "  $line = [Console]::In.ReadLine()",
            "  if ($line -eq $null) { break }",
            "  $p = $line.Split('|')",
            "  if ($p[0] -ne 'SAY') { continue }",
            "  try {",
            "    $ru = $all | Where-Object { $_.Culture.Name -like 'ru*' }",
            "    $pool = if ($ru) { $ru } else { $all }",
            "    $g = $p[1]",
            "    $pick = $pool | Where-Object { $g -eq 'Auto' -or $_.Gender.ToString() -eq $g } | Select-Object -First 1",
            "    if (-not $pick) { $pick = $pool | Select-Object -First 1 }",
            "    if ($pick) { $s.SelectVoice($pick.Name) }",
            "    $s.Rate = [int]$p[2]",
            "    $s.Volume = [int]$p[3]",
            "    $text = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($p[5]))",
            "    $lang = if ($pick) { $pick.Culture.Name } else { 'ru-RU' }",
            "    $ssml = \"<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='$lang'><prosody pitch='\" + $p[4] + \"'>\" + [Security.SecurityElement]::Escape($text) + '</prosody></speak>'",
            "    $s.SpeakSsml($ssml)",
            "  } catch { }",
            "  [Console]::Out.WriteLine('DONE')",
            "  [Console]::Out.Flush()",
            "}");

    private static Process process;
    private static BufferedWriter input;
    private static volatile boolean speaking;
    private static volatile long speakingSince;
    private static volatile long estimatedEnd;
    private static volatile String voices = "";
    private static volatile boolean unavailable;

    private WindowsVoice() {
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static synchronized boolean ensure() {
        if (unavailable) {
            return false;
        }
        if (process != null && process.isAlive()) {
            return true;
        }
        if (!WindowsVoice.isWindows()) {
            unavailable = true;
            return false;
        }
        try {
            Path dir;
            try {
                dir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("dettex");
            } catch (Throwable ignored) {
                dir = Paths.get("config", "dettex");
            }
            Files.createDirectories(dir);
            Path script = dir.resolve("bob_voice.ps1");
            Files.writeString(script, SCRIPT, StandardCharsets.UTF_8);
            process = new ProcessBuilder("powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", script.toString())
                    .redirectErrorStream(true).start();
            input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.US_ASCII));
            Process p = process;
            Thread reader = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.startsWith("VOICES|")) {
                            voices = line.substring(7);
                            LoggerUtil.info("[Bob] voices: " + voices);
                        } else if (line.equals("DONE")) {
                            speaking = false;
                        }
                    }
                } catch (Exception ignored) {
                }
                speaking = false;
            }, "Dettex-Bob-Voice");
            reader.setDaemon(true);
            reader.start();
            return true;
        } catch (Exception e) {
            unavailable = true;
            LoggerUtil.info("[Bob] speech is not available: " + e);
            return false;
        }
    }

    /**
     * Says the text. gender: Auto / Male / Female; rate -10..10; volume 0..100; pitch x-low/low/medium/high/x-high.
     * Returns how long the animation should run if there is no voice.
     */
    public static synchronized void say(String text, String gender, int rate, int volume, String pitch, boolean voiceOn) {
        long now = System.currentTimeMillis();
        speakingSince = now;
        estimatedEnd = now + 600L + text.length() * 62L;   // used when there is no voice
        if (!voiceOn || volume <= 0 || !WindowsVoice.ensure()) {
            speaking = false;
            return;
        }
        try {
            String b64 = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
            input.write("SAY|" + gender + "|" + rate + "|" + volume + "|" + pitch + "|" + b64);
            input.newLine();
            input.flush();
            speaking = true;
        } catch (Exception e) {
            speaking = false;
            WindowsVoice.shutdown();
        }
    }

    /** True while Bob is talking (the voice is playing, or the estimated time without a voice). */
    public static boolean isTalking() {
        long now = System.currentTimeMillis();
        if (speaking) {
            return now - speakingSince < 30000L;
        }
        return now < estimatedEnd && (unavailable || process == null);
    }

    public static synchronized void stop() {
        WindowsVoice.shutdown();   // the simplest way to cut a phrase: restart the speech process next time
        estimatedEnd = 0L;
    }

    public static synchronized void shutdown() {
        speaking = false;
        if (process != null) {
            process.destroy();
            process = null;
        }
        input = null;
    }

    public static String voices() {
        return voices;
    }

    public static boolean available() {
        return !unavailable;
    }
}
