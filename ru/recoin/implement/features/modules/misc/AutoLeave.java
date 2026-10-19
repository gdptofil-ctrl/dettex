/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_2561
 *  net.minecraft.class_5250
 */
package ru.recoin.implement.features.modules.misc;

import java.util.Locale;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.draggables.StaffList;

public class AutoLeave
extends Module {
    private final SelectSetting leaveType = new SelectSetting("Leave Type", "Allows you to select the leave type").value("Hub", "Main Menu");
    private final MultiSelectSetting triggerSetting = new MultiSelectSetting("Triggers", "Select in which case you will exit").value("Players", "Staff");
    private final ValueSetting distanceSetting = new ValueSetting("Max Distance", "Maximum distance for triggering auto leave").setValue(10.0f).range(5, 40).visible(() -> this.triggerSetting.isSelected("Players"));

    public AutoLeave() {
        super("AutoLeave", "Auto Leave", ModuleCategory.MISC);
        this.setup(this.leaveType, this.triggerSetting, this.distanceSetting);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (ServerUtil.isPvp()) {
            return;
        }
        if (this.triggerSetting.isSelected("Players")) {
            AutoLeave.mc.field_1687.method_18456().stream().filter(p -> AutoLeave.mc.field_1724.method_5739((class_1297)p) < this.distanceSetting.getValue() && AutoLeave.mc.field_1724 != p && !FriendUtils.isFriend((class_1297)p)).findFirst().ifPresent(p -> {
                String name = p.method_5477().getString();
                float dist = AutoLeave.mc.field_1724.method_5739((class_1297)p);
                this.leave((class_2561)class_2561.method_43470((String)(name + String.format(Locale.ROOT, " - \u043f\u043e\u044f\u0432\u0438\u043b\u0441\u044f \u0440\u044f\u0434\u043e\u043c (%.1f\u043c)", Float.valueOf(dist)))));
            });
        }
        if (this.triggerSetting.isSelected("Staff") && !StaffList.getInstance().list.isEmpty()) {
            this.leave((class_2561)class_2561.method_43470((String)"\u0421\u0442\u0430\u0444\u0444 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435"));
        }
    }

    public void leave(class_2561 text) {
        class_5250 notification = class_2561.method_43470((String)"[AutoLeave] ").method_10852(text);
        switch (this.leaveType.getSelected()) {
            case "Hub": {
                Notifications.getInstance().addList((class_2561)notification, 10000L);
                mc.method_1562().method_45730("hub");
                break;
            }
            case "Main Menu": {
                mc.method_1562().method_48296().method_10747((class_2561)class_2561.method_43470((String)"[AutoLeave]\n").method_10852(text));
            }
        }
        this.setState(false);
    }
}

