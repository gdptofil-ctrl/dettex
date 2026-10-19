/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_2338
 *  net.minecraft.class_2378
 *  net.minecraft.class_2960
 *  net.minecraft.class_3414
 *  net.minecraft.class_3419
 *  net.minecraft.class_5819
 *  net.minecraft.class_7923
 */
package ru.recoin.api.system.sound;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2338;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_5819;
import net.minecraft.class_7923;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.implement.features.modules.misc.Sounds;

public final class SoundManager
implements QuickImports {
    static ExecutorService CUSTOM_SOUND_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread2 = new Thread(r, "recoin-custom-sounds");
        thread2.setDaemon(true);
        return thread2;
    });
    public static class_3414 ENABLE_MODULE_1 = class_3414.method_47908((class_2960)class_2960.method_60654((String)"minecraft:function_on1"));
    public static class_3414 DISABLE_MODULE_1 = class_3414.method_47908((class_2960)class_2960.method_60654((String)"minecraft:function_off1"));
    public static class_3414 ENABLE_MODULE_2 = class_3414.method_47908((class_2960)class_2960.method_60654((String)"minecraft:function_on2"));
    public static class_3414 DISABLE_MODULE_2 = class_3414.method_47908((class_2960)class_2960.method_60654((String)"minecraft:function_off2"));
    public static class_3414 APPLE_PAY = class_3414.method_47908((class_2960)class_2960.method_60654((String)"minecraft:applepay"));
    public static class_3414 EVENT = class_3414.method_47908((class_2960)class_2960.method_60654((String)"minecraft:event"));
    public static class_3414 UI_CLICK = class_3414.method_47908((class_2960)class_2960.method_60654((String)"minecraft:ui.button.click"));

    public static void init() {
        try {
            Files.createDirectories(SoundManager.customSoundDirectory(), new FileAttribute[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
        class_2378.method_10230((class_2378)class_7923.field_41172, (class_2960)ENABLE_MODULE_1.comp_3319(), (Object)ENABLE_MODULE_1);
        class_2378.method_10230((class_2378)class_7923.field_41172, (class_2960)DISABLE_MODULE_1.comp_3319(), (Object)DISABLE_MODULE_1);
        class_2378.method_10230((class_2378)class_7923.field_41172, (class_2960)ENABLE_MODULE_2.comp_3319(), (Object)ENABLE_MODULE_2);
        class_2378.method_10230((class_2378)class_7923.field_41172, (class_2960)DISABLE_MODULE_2.comp_3319(), (Object)DISABLE_MODULE_2);
        class_2378.method_10230((class_2378)class_7923.field_41172, (class_2960)APPLE_PAY.comp_3319(), (Object)APPLE_PAY);
        class_2378.method_10230((class_2378)class_7923.field_41172, (class_2960)EVENT.comp_3319(), (Object)EVENT);
    }

    public static class_3414 moduleToggle(boolean enabled) {
        boolean type2;
        Sounds sounds = Sounds.getInstance();
        boolean bl = type2 = sounds != null && sounds.type2();
        return enabled ? (type2 ? ENABLE_MODULE_2 : ENABLE_MODULE_1) : (type2 ? DISABLE_MODULE_2 : DISABLE_MODULE_1);
    }

    public static boolean moduleSoundsEnabled() {
        Sounds sounds = Sounds.getInstance();
        return sounds == null || sounds.isState();
    }

    public static void playSound(class_3414 sound) {
        SoundManager.playSound(sound, 1.0f, 1.0f);
    }

    public static void playSound(class_3414 sound, float volume, float pitch) {
        if (!PlayerIntersectionUtil.nullCheck()) {
            Sounds sounds = Sounds.getInstance();
            float clientVolume = sounds == null ? 1.0f : sounds.volume();
            mc.method_1483().method_4873((class_1113)new class_1109(sound, class_3419.field_15245, volume * clientVolume, pitch, class_5819.method_43047(), SoundManager.mc.field_1724.method_24515()));
        }
    }

    public static void playCustomTotemSound() {
        Sounds sounds = Sounds.getInstance();
        if (sounds != null && !SoundManager.isDefaultTotemSound(sounds.totemSound.getText())) {
            SoundManager.playCustomSound(sounds.totemSound.getText());
        }
    }

    public static void playCustomHitSound() {
        SoundManager.playCustomSound(Sounds.getInstance() == null ? null : Sounds.getInstance().hitSound.getText());
    }

    public static boolean shouldReplaceCustomTotemSound() {
        Sounds sounds = Sounds.getInstance();
        if (sounds == null || !sounds.isState() || SoundManager.isDefaultTotemSound(sounds.totemSound.getText())) {
            return false;
        }
        Path file = SoundManager.resolveCustomSound(sounds.totemSound.getText());
        return file != null && Files.isRegularFile(file, new LinkOption[0]);
    }

    public static boolean playInterfaceClickSound() {
        Sounds sounds = Sounds.getInstance();
        if (sounds == null) {
            return false;
        }
        if (!sounds.interfaceEnabled.isValue()) {
            return true;
        }
        if (mc == null || mc.method_1483() == null) {
            return true;
        }
        float pitch = sounds.interfacePitch.isSelected("Low") ? 0.8f : (sounds.interfacePitch.isSelected("High") ? 1.2f : 1.0f);
        class_2338 pos = SoundManager.mc.field_1724 == null ? class_2338.field_10980 : SoundManager.mc.field_1724.method_24515();
        mc.method_1483().method_4873((class_1113)new class_1109(UI_CLICK, class_3419.field_15250, sounds.interfaceVolume.getValue() / 100.0f, pitch, class_5819.method_43047(), pos));
        return true;
    }

    private static boolean isDefaultTotemSound(String fileName) {
        return fileName == null || fileName.isBlank() || "totem_pop.wav".equalsIgnoreCase(fileName.trim());
    }

    public static boolean shouldReplaceCustomHitSound() {
        Sounds sounds = Sounds.getInstance();
        return sounds != null && sounds.isState() && SoundManager.resolveCustomSound(sounds.hitSound.getText()) != null;
    }

    public static void chooseCustomSound(TextSetting setting) {
        if (setting == null) {
            return;
        }
        new Thread(() -> {
            try {
                String command = "Add-Type -AssemblyName System.Windows.Forms; $f = New-Object System.Windows.Forms.OpenFileDialog; $f.Title = 'Select custom sound'; $f.Filter = 'WAV audio (*.wav)|*.wav'; $form = New-Object System.Windows.Forms.Form; $form.TopMost = $true; $form.Show(); $form.Activate(); $form.Visible = $false; if ($f.ShowDialog($form) -eq [System.Windows.Forms.DialogResult]::OK) { [Console]::WriteLine($f.FileName) }; $form.Dispose()";
                Process process = new ProcessBuilder("powershell.exe", "-Sta", "-NoProfile", "-Command", command).redirectErrorStream(true).start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                    Path file;
                    String selected = reader.readLine();
                    process.waitFor();
                    if (selected != null && !selected.isBlank() && Files.isRegularFile(file = Path.of(selected.trim(), new String[0]).toAbsolutePath().normalize(), new LinkOption[0])) {
                        mc.execute(() -> setting.setText(file.toString()));
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }, "Recoin sound picker").start();
    }

    private static void playCustomSound(String fileName) {
        float volume;
        if (!SoundManager.moduleSoundsEnabled()) {
            return;
        }
        Path file = SoundManager.resolveCustomSound(fileName);
        Sounds sounds = Sounds.getInstance();
        float f = volume = sounds == null ? 1.0f : sounds.volume();
        if (file == null || !Files.isRegularFile(file, new LinkOption[0])) {
            return;
        }
        CUSTOM_SOUND_EXECUTOR.execute(() -> SoundManager.playWave(file, volume));
    }

    private static Path resolveCustomSound(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        Path root = SoundManager.customSoundDirectory().toAbsolutePath().normalize();
        Path configured = Path.of(fileName.trim(), new String[0]);
        Path file = (configured.isAbsolute() ? configured : root.resolve(configured)).normalize();
        String lower = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return lower.endsWith(".wav") ? file : null;
    }

    private static Path customSoundDirectory() {
        return SoundManager.mc.field_1697.toPath().resolve("recoin").resolve("sounds");
    }

    private static void playWave(Path file, float volume) {
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file.toFile());){
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                float normalized = Math.max(1.0E-4f, Math.min(1.0f, volume));
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), 20.0f * (float)Math.log10(normalized))));
            }
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        }
        catch (Exception ignored) {
            SoundManager.playWaveWithWindows(file);
        }
    }

    private static void playWaveWithWindows(Path file) {
        String path = file.toAbsolutePath().toString().replace("'", "''");
        String command = "(New-Object System.Media.SoundPlayer '" + path + "').PlaySync()";
        try {
            new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", command).redirectErrorStream(true).start();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private SoundManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

