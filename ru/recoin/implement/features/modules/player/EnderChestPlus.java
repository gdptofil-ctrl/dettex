/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_2588
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_490
 *  net.minecraft.class_7417
 */
package ru.recoin.implement.features.modules.player;

import java.util.Locale;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_2588;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_7417;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.container.CloseScreenEvent;
import ru.recoin.implement.events.container.SetScreenEvent;

public final class EnderChestPlus
extends Module {
    private transient class_465<?> retainedScreen;
    private transient class_1703 retainedHandler;

    public EnderChestPlus() {
        super("EnderChestPlus", "Ender-Chest Plus", ModuleCategory.PLAYER);
    }

    @Override
    public void activate() {
        this.clearRetainedScreen();
        if (EnderChestPlus.mc.field_1724 != null) {
            this.logDirect("\u0417\u0430\u043a\u0440\u043e\u0439\u0442\u0435 \u044d\u043d\u0434\u0435\u0440-\u0441\u0443\u043d\u0434\u0443\u043a, \u0437\u0430\u0442\u0435\u043c \u043d\u0430\u0436\u043c\u0438\u0442\u0435 E, \u0447\u0442\u043e\u0431\u044b \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0435\u0433\u043e \u0441\u043d\u043e\u0432\u0430");
        }
    }

    @Override
    public void deactivate() {
        class_1703 handler = this.retainedHandler;
        this.clearRetainedScreen();
        if (EnderChestPlus.mc.field_1724 != null && handler != null && EnderChestPlus.mc.field_1724.field_7512 == handler) {
            EnderChestPlus.mc.field_1724.method_7346();
        }
    }

    @EventHandler
    public void onCloseScreen(CloseScreenEvent event) {
        class_465 screen;
        class_437 class_4372 = event.getScreen();
        if (!(class_4372 instanceof class_465) || !this.isEnderChest(screen = (class_465)class_4372)) {
            return;
        }
        this.retainedScreen = screen;
        this.retainedHandler = screen.method_17577();
        mc.method_1507(null);
        event.cancel();
    }

    @EventHandler
    public void onSetScreen(SetScreenEvent event) {
        if (!(event.getScreen() instanceof class_490) || this.retainedScreen == null || this.retainedHandler == null) {
            return;
        }
        if (EnderChestPlus.mc.field_1724 != null && EnderChestPlus.mc.field_1724.field_7512 == this.retainedHandler) {
            event.setScreen((class_437)this.retainedScreen);
        } else {
            this.clearRetainedScreen();
        }
    }

    private boolean isEnderChest(class_465<?> screen) {
        Object title;
        class_1707 handler;
        class_1703 class_17032 = screen.method_17577();
        if (!(class_17032 instanceof class_1707) || (handler = (class_1707)class_17032).method_17388() != 3) {
            return false;
        }
        class_7417 class_74172 = screen.method_25440().method_10851();
        if (class_74172 instanceof class_2588 && ((class_2588)class_74172).method_11022().equals("container.enderchest")) {
            return true;
        }
        title = screen.method_25440().getString().toLowerCase(Locale.ROOT);
        return ((String)title).contains("ender") || ((String)title).contains("\u044d\u043d\u0434\u0435\u0440");
    }

    private void clearRetainedScreen() {
        this.retainedScreen = null;
        this.retainedHandler = null;
    }
}

