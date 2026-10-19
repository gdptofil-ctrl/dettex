/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.misc;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.player.TickEvent;

public class ChatHelper
extends Module {
    private final BooleanSetting antiSpam = new BooleanSetting("\u0410\u043d\u0442\u0438 \u0441\u043f\u0430\u043c", "\u041e\u0431\u044a\u0435\u0434\u0438\u043d\u044f\u0435\u0442 \u043e\u0434\u0438\u043d\u0430\u043a\u043e\u0432\u044b\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f \u0438 \u043f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0438\u0445 \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e").setValue(true);
    private final BooleanSetting infiniteChat = new BooleanSetting("\u0411\u0435\u0441\u043a\u043e\u043d\u0435\u0447\u043d\u044b\u0439 \u0447\u0430\u0442", "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u043f\u0440\u043e\u043a\u0440\u0443\u0447\u0438\u0432\u0430\u0442\u044c \u0447\u0430\u0442 \u0434\u043e \u043f\u0435\u0440\u0432\u043e\u0433\u043e \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f \u0442\u0435\u043a\u0443\u0449\u0435\u0439 \u0441\u0435\u0441\u0441\u0438\u0438").setValue(true);
    private final BooleanSetting autoEventDelay = new BooleanSetting("\u0410\u0432\u0442\u043e /event delay", "\u041f\u0440\u0438 \u0441\u043c\u0435\u043d\u0435 \u0430\u043d\u0430\u0440\u0445\u0438\u0438 \u0438 \u043a\u0430\u0436\u0434\u044b\u0435 5 \u043c\u0438\u043d\u0443\u0442 \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0435\u0442 /event delay").setValue(false);
    private final StopWatch eventDelayWatch = new StopWatch();
    private int lastAnarchy = -1;

    public ChatHelper() {
        super("ChatHelper", "Chat Helper", ModuleCategory.MISC);
        this.setup(this.antiSpam, this.infiniteChat, this.autoEventDelay);
    }

    public static ChatHelper getInstance() {
        return Instance.get(ChatHelper.class);
    }

    @Override
    public void activate() {
        this.lastAnarchy = ServerUtil.getAnarchy();
        this.eventDelayWatch.reset();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        boolean changed;
        if (ChatHelper.mc.field_1724 == null || ChatHelper.mc.field_1724.field_3944 == null) {
            return;
        }
        int current = ServerUtil.getAnarchy();
        boolean bl = changed = current != this.lastAnarchy;
        if (changed) {
            this.lastAnarchy = current;
        }
        if (!this.autoEventDelay.isValue() || current <= 0) {
            return;
        }
        if (changed) {
            this.sendEventDelay();
            return;
        }
        if (this.eventDelayWatch.every(300000.0)) {
            this.sendEventDelay();
        }
    }

    private void sendEventDelay() {
        ChatHelper.mc.field_1724.field_3944.method_45730("event delay");
        this.eventDelayWatch.reset();
    }

    public boolean isAntiSpamEnabled() {
        return this.isState() && this.antiSpam.isValue();
    }

    public boolean isInfiniteChatEnabled() {
        return this.isState() && this.infiniteChat.isValue();
    }
}

