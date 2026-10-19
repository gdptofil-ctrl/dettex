/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.misc;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.keyboard.KeyEvent;

public final class AutoMessage
extends Module {
    private final BindSetting messageBind = new BindSetting("Message Bind", "Send the saved AutoMessage text to chat");
    private final TextSetting message = new TextSetting("AutoMessage Text", "Hidden AutoMessage text").setText("").setMax(256).visible(() -> false);

    public AutoMessage() {
        super("AutoMessage", ModuleCategory.MISC);
        this.setup(this.messageBind, this.message);
    }

    public static AutoMessage getInstance() {
        return Instance.get(AutoMessage.class);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (AutoMessage.mc.field_1755 != null || !event.isKeyDown(this.messageBind.getKey())) {
            return;
        }
        String value = this.message.getText();
        if (value == null || value.isBlank() || AutoMessage.mc.field_1724 == null || AutoMessage.mc.field_1724.field_3944 == null) {
            return;
        }
        AutoMessage.mc.field_1724.field_3944.method_45729(value);
    }

    public void setMessage(String value) {
        this.message.setText(value == null ? "" : value.trim());
    }

    public String getMessage() {
        String value = this.message.getText();
        return value == null ? "" : value;
    }
}

