/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 */
package ru.recoin.api.feature.module;

import java.util.List;
import net.minecraft.class_124;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.exception.ModuleException;
import ru.recoin.common.QuickImports;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.implement.events.keyboard.KeyEvent;
import ru.recoin.implement.features.modules.combat.Aura;

public class ModuleSwitcher
implements QuickLogger,
QuickImports {
    private final List<Module> modules;

    public ModuleSwitcher(List<Module> modules, EventManager eventManager) {
        this.modules = modules;
        eventManager.register(this);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        for (Module module : this.modules) {
            if (module instanceof Aura) {
                Aura aura = (Aura)module;
                aura.onVortexKey(event);
            }
            if (!event.isKeyDown(module.getKey()) && !event.isKeyReleased(module.getKey())) continue;
            try {
                this.handleModuleState(module, event.action());
            }
            catch (Exception e) {
                this.handleException(module.getName(), e);
            }
        }
    }

    private void handleModuleState(Module module, int action) {
        if (module.getType() == 1 && action == 1) {
            module.switchState();
            return;
        }
        if (module.getType() == 0) {
            if (action == 1) {
                module.setState(true);
            } else if (action == 0) {
                module.setState(false);
            }
        }
    }

    private void handleException(String moduleName, Exception e) {
        if (e instanceof ModuleException) {
            this.logDirect("[" + moduleName + "] " + String.valueOf(class_124.field_1061) + e.getMessage());
        } else {
            LoggerUtil.error("Error in module " + moduleName + ": " + e.getMessage(), e);
        }
    }
}

