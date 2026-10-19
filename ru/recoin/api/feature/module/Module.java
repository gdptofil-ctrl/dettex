/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package ru.recoin.api.feature.module;

import net.minecraft.class_310;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.SettingRepository;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.common.QuickImports;
import ru.recoin.core.Main;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.misc.Sounds;

public class Module
extends SettingRepository
implements QuickImports {
    private final String name;
    private final String visibleName;
    private final ModuleCategory category;
    private final Animation animation = new DecelerateAnimation().setMs(150).setValue(1.0);
    private int key = -1;
    private int defaultKey = -1;
    private int type = 1;
    public boolean state;

    public Module(String name, ModuleCategory category) {
        this(name, name, category);
    }

    public Module(String name, String visibleName, ModuleCategory category) {
        this.name = name;
        this.visibleName = visibleName;
        this.category = category;
        this.initializeAnimationState();
    }

    private void initializeAnimationState() {
        this.animation.setDirection(Direction.BACKWARDS);
        this.animation.complete();
    }

    public void switchState() {
        this.setState(!this.state);
    }

    public void setState(boolean state) {
        this.animation.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
        if (state != this.state) {
            this.state = state;
            this.handleStateChange();
        }
    }

    private void handleStateChange() {
        class_310 client = class_310.method_1551();
        if (client.field_1724 != null && client.field_1687 != null) {
            Notifications.getInstance().queueModuleSwitch(this.visibleName, this.state, this instanceof Sounds || SoundManager.moduleSoundsEnabled() ? SoundManager.moduleToggle(this.state) : null);
            if (this.state) {
                this.activate();
            } else {
                this.deactivate();
            }
        }
        this.toggleSilent(this.state);
    }

    private void toggleSilent(boolean activate) {
        EventManager eventManager = Main.getInstance().getEventManager();
        if (activate) {
            eventManager.register(this);
        } else {
            eventManager.unregister(this);
        }
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public String getName() {
        return this.name;
    }

    public String getVisibleName() {
        return this.visibleName;
    }

    public ModuleCategory getCategory() {
        return this.category;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public int getKey() {
        return this.key;
    }

    public int getDefaultKey() {
        return this.defaultKey;
    }

    public int getType() {
        return this.type;
    }

    public boolean isState() {
        return this.state;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setDefaultKey(int defaultKey) {
        this.defaultKey = defaultKey;
    }

    public void setType(int type) {
        this.type = type;
    }
}

