/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 */
package ru.recoin.implement.features.commands.defaults;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import ru.recoin.api.feature.command.Command;
import ru.recoin.api.feature.command.argument.IArgConsumer;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.ai.NeuroRotation;

public final class AiRotationCommand
extends Command {
    public AiRotationCommand() {
        super("ai", "airot", "airotation", "neuro");
    }

    @Override
    public void execute(String label, IArgConsumer args2) {
        Object result;
        String raw = args2.rawRest().trim();
        if (raw.isEmpty()) {
            this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .ai <train|learn|run|stop|status|log|profile|list> [\u043f\u0440\u043e\u0444\u0438\u043b\u044c]", class_124.field_1080);
            return;
        }
        String[] split = raw.split("\\s+", 2);
        String action = split[0].toLowerCase(Locale.ROOT);
        String requestedProfile = split.length > 1 ? split[1].trim() : NeuroRotation.getProfile();
        switch (action) {
            case "train": {
                result = NeuroRotation.startRecording(requestedProfile);
                break;
            }
            case "learn": {
                result = NeuroRotation.learn(requestedProfile);
                break;
            }
            case "run": {
                Aura aura;
                result = NeuroRotation.run(requestedProfile);
                if (!NeuroRotation.isRunning() || (aura = Aura.getInstance()) == null) break;
                aura.selectAiRotation();
                if (aura.isState()) break;
                aura.setState(true);
                break;
            }
            case "stop": {
                result = NeuroRotation.stop();
                break;
            }
            case "status": {
                result = NeuroRotation.getStatus();
                break;
            }
            case "log": {
                result = "AI log: " + String.valueOf(NeuroRotation.logPath().toAbsolutePath());
                break;
            }
            case "profile": {
                result = split.length > 1 ? NeuroRotation.setProfile(requestedProfile) : "\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0439 AI-\u043f\u0440\u043e\u0444\u0438\u043b\u044c: " + NeuroRotation.getProfile();
                break;
            }
            case "list": {
                List<String> profiles = NeuroRotation.listProfiles();
                result = profiles.isEmpty() ? "AI-\u043f\u0440\u043e\u0444\u0438\u043b\u0438 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b." : "AI-\u043f\u0440\u043e\u0444\u0438\u043b\u0438: " + String.join((CharSequence)", ", profiles);
                break;
            }
            default: {
                result = "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .ai <train|learn|run|stop|status|log|profile|list> [\u043f\u0440\u043e\u0444\u0438\u043b\u044c]";
            }
        }
        this.logDirect((String)result, class_124.field_1076);
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args2) {
        String raw = args2.rawRest();
        String trimmed = raw.stripLeading();
        if (!trimmed.contains(" ")) {
            String prefix = trimmed.toLowerCase(Locale.ROOT);
            return Stream.of("train", "learn", "run", "stop", "status", "log", "profile", "list").filter(value -> value.startsWith(prefix));
        }
        String[] split = trimmed.split("\\s+", 2);
        if (split[0].equalsIgnoreCase("train") || split[0].equalsIgnoreCase("learn") || split[0].equalsIgnoreCase("run") || split[0].equalsIgnoreCase("profile")) {
            String prefix = split.length > 1 ? split[1].toLowerCase(Locale.ROOT) : "";
            return NeuroRotation.listProfiles().stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix));
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0417\u0430\u043f\u0438\u0441\u044c, \u043e\u0431\u0443\u0447\u0435\u043d\u0438\u0435 \u0438 \u0437\u0430\u043f\u0443\u0441\u043a AI-\u0440\u043e\u0442\u0430\u0446\u0438\u0438";
    }

    @Override
    public List<String> getLongDesc() {
        return List.of("> ai train <\u043f\u0440\u043e\u0444\u0438\u043b\u044c> \u2014 \u0437\u0430\u043f\u0438\u0441\u0430\u0442\u044c \u0440\u0443\u0447\u043d\u043e\u0439 \u0430\u0438\u043c", "> ai stop \u2014 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c \u0438\u043b\u0438 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c AI", "> ai learn <\u043f\u0440\u043e\u0444\u0438\u043b\u044c> \u2014 \u043e\u0431\u0443\u0447\u0438\u0442\u044c GRU-\u043c\u043e\u0434\u0435\u043b\u044c 17\u219232\u219219", "> ai run <\u043f\u0440\u043e\u0444\u0438\u043b\u044c> \u2014 \u0437\u0430\u043f\u0443\u0441\u0442\u0438\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c", "> ai status \u2014 \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435");
    }
}

