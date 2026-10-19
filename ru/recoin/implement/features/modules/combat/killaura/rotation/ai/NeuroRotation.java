/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.ai;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.ai.NeuroController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.ai.NeuroModel;
import ru.recoin.implement.features.modules.combat.killaura.target.BacktrackPositionResolver;

public final class NeuroRotation {
    private static final class_310 MC = class_310.method_1551();
    private static final NeuroController CONTROLLER = new NeuroController();
    private static final NeuroController RETURN_CONTROLLER = new NeuroController();
    private static final String HEADER = "t,gcd,clean,yaw,pitch,dyaw,dpitch,has,tid,rx,ry,rz,bw,bh,dist,vis,on,atk,hp,ground,sprint\n";
    private static String profile = "default";
    private static NeuroModel model;
    private static BufferedWriter writer;
    private static boolean running;
    private static volatile boolean learning;
    private static volatile String status;
    private static int targetId;
    private static int attackTargetId;
    private static int attackTick;
    private static int tick;
    private static int recorded;
    private static int recentTargetTicks;
    private static float previousYaw;
    private static float previousPitch;
    private static boolean previousValid;
    private static boolean attacked;
    private static int returnTicks;

    private NeuroRotation() {
    }

    public static synchronized String startRecording(String requested) {
        if (writer != null) {
            return "Запись уже идёт";
        }
        if (learning) {
            return "Дождись окончания обучения";
        }
        String selected = NeuroRotation.sanitize(requested);
        try {
            Files.createDirectories(NeuroRotation.dataDirectory(), new FileAttribute[0]);
            Path path = NeuroRotation.dataDirectory().resolve(selected + ".csv");
            boolean empty = !Files.exists(path, new LinkOption[0]) || Files.size(path) == 0L;
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (empty) {
                writer.write(HEADER);
            }
            profile = selected;
            running = false;
            model = null;
            CONTROLLER.reset();
            RETURN_CONTROLLER.reset();
            previousValid = false;
            recorded = 0;
            tick = 0;
            recentTargetTicks = 0;
            status = "Запись: " + selected;
            return "Запись ручного аима: " + String.valueOf(path.toAbsolutePath());
        }
        catch (Exception error) {
            writer = null;
            status = "Ошибка записи: " + error.getMessage();
            return status;
        }
    }

    public static synchronized String stop() {
        if (writer != null) {
            try {
                writer.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            writer = null;
            status = "Сохранено " + recorded + " тиков: " + profile;
            return status;
        }
        running = false;
        model = null;
        targetId = Integer.MIN_VALUE;
        CONTROLLER.reset();
        RETURN_CONTROLLER.reset();
        status = learning ? "Обучение продолжается" : "Нейро-ротация остановлена";
        return status;
    }

    public static synchronized String run(String requested) {
        if (writer != null) {
            return "Сначала останови запись";
        }
        String selected = NeuroRotation.sanitize(requested);
        try {
            NeuroModel loaded = NeuroModel.load(NeuroRotation.rootDirectory().resolve(selected + ".json"));
            if (loaded == null) {
                return "GRU-модель " + selected + " не найдена. Старые .ai-модели несовместимы; обучи новую через .ai train и .ai learn";
            }
            profile = selected;
            model = loaded;
            running = true;
            targetId = Integer.MIN_VALUE;
            CONTROLLER.reset();
            RETURN_CONTROLLER.reset();
            status = "GRU-модель запущена: " + selected;
            return status;
        }
        catch (Exception error) {
            status = "Ошибка модели: " + error.getMessage();
            return status;
        }
    }

    public static synchronized boolean ensureRunning() {
        if (running && model != null) {
            return true;
        }
        if (writer != null) {
            return false;
        }
        return NeuroRotation.run(profile).startsWith("GRU-модель запущена");
    }

    public static synchronized Angle predict(Angle current, class_1309 target) {
        if (model == null || NeuroRotation.MC.field_1724 == null) {
            return current;
        }
        class_238 box = BacktrackPositionResolver.boundingBox((class_1297)target);
        class_243 eyes = NeuroRotation.MC.field_1724.method_33571();
        class_243 delta = box.method_1005().method_1020(eyes);
        double flat = Math.max(Math.hypot(delta.field_1352, delta.field_1350), 0.05);
        float desiredYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0f;
        float desiredPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, flat)));
        float widthYaw = Math.max((float)Math.toDegrees(Math.atan2(box.method_17939() / 2.0, flat)), 0.5f);
        float widthPitch = Math.max((float)Math.toDegrees(Math.atan2(box.method_17940() / 2.0, flat)), 0.5f);
        double distance = NeuroRotation.distance(eyes, box);
        if (targetId != target.method_5628()) {
            CONTROLLER.reset();
            targetId = target.method_5628();
        }
        RETURN_CONTROLLER.reset();
        returnTicks = 0;
        float error = model.error(ThreadLocalRandom.current().nextFloat());
        float[] step = CONTROLLER.step(model, current.getYaw(), current.getPitch(), desiredYaw, desiredPitch, widthYaw, widthPitch, distance, error, NeuroRotation.gcd(), 0.7f, 4, 1.0f);
        return new Angle(current.getYaw() + step[0], class_3532.method_15363((float)(current.getPitch() + step[1]), (float)-90.0f, (float)90.0f));
    }

    public static synchronized Angle returnTo(Angle current, Angle desired) {
        if (model == null) {
            return null;
        }
        if (targetId != Integer.MIN_VALUE) {
            CONTROLLER.reset();
            targetId = Integer.MIN_VALUE;
            RETURN_CONTROLLER.reset();
            returnTicks = 0;
        }
        float differenceYaw = class_3532.method_15393((float)(desired.getYaw() - current.getYaw()));
        float differencePitch = desired.getPitch() - current.getPitch();
        if (Math.abs(differenceYaw) <= Math.max(3.0f, NeuroRotation.gcd()) && Math.abs(differencePitch) <= Math.max(3.0f, NeuroRotation.gcd())) {
            RETURN_CONTROLLER.reset();
            returnTicks = 0;
            return null;
        }
        if (++returnTicks > 30) {
            RETURN_CONTROLLER.reset();
            return null;
        }
        float[] step = RETURN_CONTROLLER.step(model, current.getYaw(), current.getPitch(), desired.getYaw(), desired.getPitch(), 5.0f, 15.0f, 3.0, 0.6f * model.error(ThreadLocalRandom.current().nextFloat()), NeuroRotation.gcd(), 0.7f, 4, 1.0f);
        return new Angle(current.getYaw() + step[0], class_3532.method_15363((float)(current.getPitch() + step[1]), (float)-90.0f, (float)90.0f));
    }

    public static synchronized void attacked() {
        CONTROLLER.attacked();
    }

    public static synchronized void onAttack(AttackEvent event) {
        class_1297 class_12972;
        if (writer == null || !event.isPre() || !((class_12972 = event.getEntity()) instanceof class_1309)) {
            return;
        }
        class_1309 target = (class_1309)class_12972;
        attackTargetId = target.method_5628();
        attackTick = tick;
        attacked = true;
    }

    public static synchronized void tickRecording() {
        if (writer == null || NeuroRotation.MC.field_1724 == null || NeuroRotation.MC.field_1687 == null) {
            return;
        }
        ++tick;
        float yaw = NeuroRotation.MC.field_1724.method_36454();
        float pitch = NeuroRotation.MC.field_1724.method_36455();
        float dyaw = previousValid ? class_3532.method_15393((float)(yaw - previousYaw)) : 0.0f;
        float dpitch = previousValid ? pitch - previousPitch : 0.0f;
        previousYaw = yaw;
        previousPitch = pitch;
        previousValid = true;
        class_1309 target = NeuroRotation.recordingTarget();
        if (target != null) {
            recentTargetTicks = 40;
        } else if (recentTargetTicks-- <= 0) {
            attacked = false;
            return;
        }
        class_238 box = target == null ? null : target.method_5829();
        class_243 eyes = NeuroRotation.MC.field_1724.method_33571();
        class_243 relative = box == null ? class_243.field_1353 : box.method_1005().method_1020(eyes);
        Aura aura = Aura.getInstance();
        int clean = aura == null || !aura.isState() ? 1 : 0;
        int on = box != null && NeuroRotation.rayHits(eyes, yaw, pitch, box) ? 1 : 0;
        int attack = attacked ? 1 : 0;
        attacked = false;
        try {
            writer.write(String.format(Locale.ROOT, "%d,%.6f,%d,%.4f,%.4f,%.4f,%.4f,%d,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%d,%d,%d,%.1f,%d,%d%n", tick, Float.valueOf(NeuroRotation.gcd()), clean, Float.valueOf(yaw), Float.valueOf(pitch), Float.valueOf(dyaw), Float.valueOf(dpitch), target == null ? 0 : 1, target == null ? -1 : target.method_5628(), relative.field_1352, relative.field_1351, relative.field_1350, box == null ? 0.0 : box.method_17939(), box == null ? 0.0 : box.method_17940(), box == null ? -1.0 : NeuroRotation.distance(eyes, box), target == null ? 0 : 1, on, attack, Float.valueOf(target == null ? -1.0f : target.method_6032()), NeuroRotation.MC.field_1724.method_24828() ? 1 : 0, NeuroRotation.MC.field_1724.method_5624() ? 1 : 0));
            if (++recorded % 256 == 0) {
                writer.flush();
            }
        }
        catch (Exception error) {
            try {
                writer.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            writer = null;
            status = "Ошибка записи: " + error.getMessage();
        }
    }

    private static class_1309 recordingTarget() {
        class_1309 target;
        class_1297 class_12972;
        class_243 eyes = NeuroRotation.MC.field_1724.method_33571();
        if (tick - attackTick <= 60 && (class_12972 = NeuroRotation.MC.field_1687.method_8469(attackTargetId)) instanceof class_1309 && (target = (class_1309)class_12972).method_5805() && NeuroRotation.distance(eyes, target.method_5829()) <= 5.0) {
            return target;
        }
        class_1309 nearest = null;
        double best = 5.0;
        for (class_1297 entity : NeuroRotation.MC.field_1687.method_18112()) {
            double candidate;
            class_1309 target2;
            if (!(entity instanceof class_1309) || (target2 = (class_1309)entity) == NeuroRotation.MC.field_1724 || !target2.method_5805() || (candidate = NeuroRotation.distance(eyes, target2.method_5829())) >= best) continue;
            best = candidate;
            nearest = target2;
        }
        return nearest;
    }

    private static boolean rayHits(class_243 eyes, float yaw, float pitch, class_238 box) {
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        class_243 direction = new class_243(-Math.sin(y) * Math.cos(p), -Math.sin(p), Math.cos(y) * Math.cos(p));
        return box.method_1006(eyes) || box.method_992(eyes, eyes.method_1019(direction.method_1021(5.0))).isPresent();
    }

    private static double distance(class_243 point, class_238 box) {
        double x = Math.max(Math.max(box.field_1323 - point.field_1352, 0.0), point.field_1352 - box.field_1320);
        double y = Math.max(Math.max(box.field_1322 - point.field_1351, 0.0), point.field_1351 - box.field_1325);
        double z = Math.max(Math.max(box.field_1321 - point.field_1350, 0.0), point.field_1350 - box.field_1324);
        return Math.sqrt(x * x + y * y + z * z);
    }

    private static float gcd() {
        double sensitivity = (Double)NeuroRotation.MC.field_1690.method_42495().method_41753();
        double multiplier = sensitivity * 0.6 + 0.2;
        return (float)(multiplier * multiplier * multiplier * 1.2);
    }

    public static synchronized String learn(String requested) {
        if (learning) {
            return "Обучение уже идёт";
        }
        if (writer != null) {
            return "Сначала останови запись";
        }
        String selected = NeuroRotation.sanitize(requested);
        if (!Files.isRegularFile(NeuroRotation.dataDirectory().resolve(selected + ".csv"), new LinkOption[0])) {
            return "Нет записи " + selected + ".csv";
        }
        profile = selected;
        learning = true;
        status = "Обучение " + selected + " запущено; подробности: .ai log";
        Thread thread2 = new Thread(() -> NeuroRotation.train(selected), "recoin-neuro-train");
        thread2.setDaemon(true);
        thread2.start();
        return status;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static void train(String selected) {
        try {
            Path scripts = NeuroRotation.rootDirectory().resolve("trainer");
            Files.createDirectories(scripts, new FileAttribute[0]);
            for (String name : List.of("common.py", "train_aura.py")) {
                InputStream input = NeuroRotation.class.getClassLoader().getResourceAsStream("assets/recoin/neuro/trainer/" + name);
                try {
                    if (input == null) {
                        throw new IllegalStateException("Отсутствует " + name);
                    }
                    Files.copy(input, scripts.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                }
                finally {
                    if (input == null) continue;
                    input.close();
                }
            }
            ArrayList<String> tail = new ArrayList<String>();
            tail.add(scripts.resolve("train_aura.py").toString());
            tail.add("--data");
            tail.add(NeuroRotation.dataDirectory().toString());
            tail.add("--out");
            tail.add(NeuroRotation.rootDirectory().resolve(selected + ".json").toString());
            int exit = NeuroRotation.runTrainer(tail);
            Class<NeuroRotation> clazz = NeuroRotation.class;
            synchronized (NeuroRotation.class) {
                status = exit == 0 ? "Обучение завершено: " + selected + ". Запусти .ai run " + selected : "Обучение не удалось (код " + exit + "). Проверь .ai log и Python с numpy/torch";
                // ** MonitorExit[var5_9] (shouldn't be in output)
                return;
            }
        }
        catch (Exception error) {
            Class<NeuroRotation> clazz = NeuroRotation.class;
            synchronized (NeuroRotation.class) {
                status = "Ошибка обучения: " + error.getMessage();
                // ** MonitorExit[var2_4] (shouldn't be in output)
                return;
            }
        }
        finally {
            learning = false;
        }
    }

    private static int runTrainer(List<String> tail) throws Exception {
        IOException last = null;
        for (List<String> launcher : List.of(List.of("py", "-3.13"), List.of("py", "-3.14"), List.of("python"), List.of("py"))) {
            ArrayList<String> command = new ArrayList<String>(launcher);
            command.addAll(tail);
            try {
                return new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(NeuroRotation.logPath().toFile()).start().waitFor();
            }
            catch (IOException error) {
                last = error;
            }
        }
        throw last != null ? last : new IllegalStateException("Python \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
    }

    public static boolean isRunning() {
        return running;
    }

    public static boolean isRecording() {
        return writer != null;
    }

    public static boolean isTraining() {
        return learning;
    }

    public static String getStatus() {
        return status + (String)(writer == null ? "" : " (" + recorded + " тиков)");
    }

    public static String getProfile() {
        return profile;
    }

    public static String setProfile(String requested) {
        profile = NeuroRotation.sanitize(requested);
        return "Активный нейро-профиль: " + profile;
    }

    public static Path logPath() {
        return NeuroRotation.rootDirectory().resolve("training.log");
    }

    public static List<String> listProfiles() {
        LinkedHashSet<String> profiles = new LinkedHashSet<String>();
        profiles.add("default");
        for (Path directory : List.of(NeuroRotation.dataDirectory(), NeuroRotation.rootDirectory())) {
            try {
                Stream<Path> stream = Files.list(directory);
                try {
                    stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).map(path -> path.getFileName().toString()).filter(name -> name.endsWith(directory.equals(NeuroRotation.dataDirectory()) ? ".csv" : ".json")).map(name -> name.substring(0, name.lastIndexOf(46))).sorted().forEach(profiles::add);
                }
                finally {
                    if (stream == null) continue;
                    stream.close();
                }
            }
            catch (Exception exception) {}
        }
        return List.copyOf(profiles);
    }

    private static Path rootDirectory() {
        return NeuroRotation.MC.field_1697.toPath().resolve("Recoin").resolve("Neuro");
    }

    private static Path dataDirectory() {
        return NeuroRotation.rootDirectory().resolve("data");
    }

    private static String sanitize(String value) {
        String name = value == null ? "" : value.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
        return name.isEmpty() ? "default" : name.substring(0, Math.min(name.length(), 48));
    }

    static {
        status = "Нейро-модель не запущена";
        targetId = Integer.MIN_VALUE;
        attackTargetId = -1;
        attackTick = -100;
    }
}
