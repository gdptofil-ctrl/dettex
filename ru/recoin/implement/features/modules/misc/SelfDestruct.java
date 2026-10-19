/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_437
 */
package ru.recoin.implement.features.modules.misc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_437;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.events.chat.ChatEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.render.Hud;

public class SelfDestruct
extends Module {
    private static boolean locked;
    private final TextSetting restorePhrase = new TextSetting("Restore Word", "Word used to restore the client").setText("rake").setMin(1).setMax(32);
    private final List<Module> cachedModules = new ArrayList<Module>();
    private boolean hudWasEnabled;
    private boolean selfDestructActive;

    public SelfDestruct() {
        super("SelfDestruct", "Self Destruct", ModuleCategory.MISC);
        this.setup(this.restorePhrase);
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
        class_437 current = SelfDestruct.mc.field_1755;
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
        Notifications.getInstance().clear();
        Render3DUtil.TEXTURE.clear();
        Render3DUtil.TEXTURE_DEPTH.clear();
        Render3DUtil.LINE.clear();
        Render3DUtil.LINE_DEPTH.clear();
        Render3DUtil.QUAD.clear();
        Render3DUtil.QUAD_DEPTH.clear();
        Main.getInstance().getBoxESPRepository().entities.clear();
        Main.getInstance().getBoxESPRepository().blocks.clear();
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

