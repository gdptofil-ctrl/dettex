package ru.recoin.implement.features.modules.misc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_437;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.events.chat.ChatEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.render.Hud;

public class Unhook
extends Module {
    private static boolean locked;
    private final TextSetting restorePhrase = new TextSetting("Restore Word", "Word used to restore the client").setText("rake").setMin(1).setMax(32);
    private final BooleanSetting hardHide = new BooleanSetting("Hard Hide", "Hide client files, remove baritone and traces so they don't appear in logs/folder").setValue(true);
    private final List<Module> cachedModules = new ArrayList<Module>();
    private boolean hudWasEnabled;
    private boolean selfDestructActive;

    public Unhook() {
        super("Unhook", "Unhook", ModuleCategory.MISC);
        this.setup(this.restorePhrase, this.hardHide);
    }

    public static boolean isLocked() {
        return locked;
    }

    @Override
    public void activate() {
        if (this.selfDestructActive) {
            return;
        }
        this.cachedModules.clear();
        this.selfDestructActive = true;
        locked = true;
        class_437 current = Unhook.mc.field_1755;
        if (current != null) {
            mc.method_1507(null);
        }
        RotationController.INSTANCE.clear();
        RotationController.INSTANCE.setRotation(null);
        Hud hud = Main.getInstance().getModuleProvider().get(Hud.class);
        boolean bl = this.hudWasEnabled = hud != null && hud.isState();
        if (hud != null && this.hudWasEnabled) {
            hud.setState(false);
        }
        ArrayList<Module> modules = new ArrayList<Module>(Main.getInstance().getModuleRepository().modules());
        for (Module module : modules) {
            if (module == this || !module.isState()) continue;
            this.cachedModules.add(module);
            module.setState(false);
        }
        try {
            Notifications.getInstance().clear();
        } catch (Throwable ignored) {
        }
        try {
            Render3DUtil.TEXTURE.clear();
            Render3DUtil.TEXTURE_DEPTH.clear();
            Render3DUtil.LINE.clear();
            Render3DUtil.LINE_DEPTH.clear();
            Render3DUtil.QUAD.clear();
            Render3DUtil.QUAD_DEPTH.clear();
        } catch (Throwable ignored) {
        }
        try {
            Main.getInstance().getBoxESPRepository().entities.clear();
            Main.getInstance().getBoxESPRepository().blocks.clear();
        } catch (Throwable ignored) {
        }
        if (this.hardHide.isValue()) {
            this.hardHide();
        }
    }

    private void hardHide() {
        try {
            Object mcObj = this.mc;
            if (mcObj == null) return;
            Object runDir = mcObj.getClass().getField("runDirectory").get(mcObj);
            if (runDir == null) return;
            Path gameDir = ((java.io.File)runDir).toPath().toAbsolutePath().normalize();
            this.hidePath(gameDir.resolve("RecoinDLC"));
            this.hidePath(gameDir.resolve("recoin"));
            this.hidePath(gameDir.resolve("Dettex"));
            this.hidePath(gameDir.resolve("dettex"));
            this.hidePath(gameDir.resolve("mods").resolve("RecoinDLC"));
            this.hidePath(gameDir.resolve("mods").resolve("recoin"));
            this.hidePath(gameDir.resolve("mods").resolve("Dettex"));
            this.hidePath(gameDir.resolve("mods").resolve("dettex"));
            this.hidePath(gameDir.resolve("mods").resolve("baritone"));
            this.hidePath(gameDir.resolve("baritone"));
            this.hidePath(gameDir.resolve("logs").resolve("latest.log"));
            this.hidePath(gameDir.resolve("crash-reports"));
        } catch (Throwable ignored) {
        }
        try {
            String[] sys = {"RecoinDLC", "recoindlc", "DettexClient", "dettexclient", "baritone"};
            String[] roots = {System.getProperty("user.home"), System.getenv("APPDATA"), System.getenv("LOCALAPPDATA"), System.getProperty("java.io.tmpdir")};
            for (String r : roots) {
                if (r == null) continue;
                Path p = Path.of(r);
                for (String s : sys) {
                    this.hidePath(p.resolve(".dettex").resolve(s));
                    this.hidePath(p.resolve("Dettex").resolve(s));
                    this.hidePath(p.resolve(".Dettex").resolve(s));
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void hidePath(Path path) {
        if (path == null) return;
        try {
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    try (Stream<Path> walk = Files.walk(path)) {
                        walk.sorted((a,b) -> b.getNameCount() - a.getNameCount()).forEach(p -> {
                            try {
                                if (Files.isRegularFile(p)) {
                                    try { Files.setAttribute(p, "dos:hidden", Boolean.TRUE); } catch (Throwable ignored) {}
                                    try { Files.setAttribute(p, "dos:readonly", Boolean.TRUE); } catch (Throwable ignored) {}
                                    try { p.toFile().delete(); } catch (Throwable ignored) {}
                                }
                            } catch (Throwable ignored) {}
                        });
                    }
                }
                try { Files.setAttribute(path, "dos:hidden", Boolean.TRUE); } catch (Throwable ignored) {}
                try { path.toFile().setWritable(false); } catch (Throwable ignored) {}
                try { path.toFile().delete(); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {
        }
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (!this.selfDestructActive) {
            return;
        }
        if (!e.getMessage().trim().equalsIgnoreCase(this.restorePhrase.getText().trim())) {
            return;
        }
        e.cancel();
        this.restoreState();
    }

    private void restoreState() {
        RotationController.INSTANCE.clear();
        RotationController.INSTANCE.setRotation(null);
        for (Module module : new ArrayList<Module>(this.cachedModules)) {
            if (module.isState()) continue;
            module.setState(true);
        }
        this.cachedModules.clear();
        Hud hud = Main.getInstance().getModuleProvider().get(Hud.class);
        if (hud != null && this.hudWasEnabled && !hud.isState()) {
            hud.setState(true);
        }
        this.selfDestructActive = false;
        this.hudWasEnabled = false;
        locked = false;
        this.setState(false);
    }
}

