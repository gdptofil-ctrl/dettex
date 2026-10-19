/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_5250
 */
package ru.recoin.implement.features.modules.player;

import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.render.TextFactoryEvent;

public class NameProtect
extends Module {
    private final TextSetting nameSetting = new TextSetting("Name", "Nickname that will replace yours").setText("t.me/DettexClient").setMax(16);
    private final BooleanSetting friendsSetting = new BooleanSetting("Friends", "Hide friends' nicknames").setValue(true);
    private final BooleanSetting hideAnarchy = new BooleanSetting("Hide Anarchy", "Replace the anarchy number").setValue(false);
    private boolean awaitingAnarchyNumber;

    public NameProtect() {
        super("StreamerMode", "Streamer Mode", ModuleCategory.PLAYER);
        this.setup(this.nameSetting, this.friendsSetting, this.hideAnarchy);
    }

    @EventHandler
    public void onTextFactory(TextFactoryEvent event) {
        event.replaceText(mc.method_1548().method_1676(), this.nameSetting.getText());
        if (this.friendsSetting.isValue()) {
            FriendUtils.getFriends().forEach(friend -> event.replaceText(friend.getName(), this.nameSetting.getText()));
        }
        if (!this.hideAnarchy.isValue() || event.getText() == null) {
            this.awaitingAnarchyNumber = false;
            return;
        }
        String text = event.getText();
        String replaced = text.replaceAll("(?iu)\u0410\u043d\u0430\u0440\u0445\u0438\u044f[-\\s]*(?:#|\u2116)?\\s*\\d+", "\u0410\u043d\u0430\u0440\u0445\u0438\u044f-67");
        if (!replaced.equals(text)) {
            this.awaitingAnarchyNumber = false;
            event.setText(replaced);
            return;
        }
        if (text.matches("(?iu).*\u0410\u043d\u0430\u0440\u0445\u0438\u044f[-\\s]*(?:#|\u2116)?\\s*$")) {
            this.awaitingAnarchyNumber = true;
        } else if (this.awaitingAnarchyNumber) {
            if (text.matches("\\s*\\d+\\s*")) {
                event.setText(text.replaceFirst("\\d+", "67"));
            }
            this.awaitingAnarchyNumber = false;
        }
    }

    public static class_5250 protectAnarchy(class_2561 decorated) {
        String replaced;
        NameProtect module = Instance.get(NameProtect.class);
        if (module == null || !module.isState() || !module.hideAnarchy.isValue() || decorated == null) {
            return decorated == null ? class_2561.method_43473() : decorated.method_27661();
        }
        String plain = decorated.getString();
        if (plain.equals(replaced = plain.replaceAll("(?iu)\u0410\u043d\u0430\u0440\u0445\u0438\u044f[-\\s]*(?:#|\u2116)?\\s*\\d+", "\u0410\u043d\u0430\u0440\u0445\u0438\u044f-67"))) {
            return decorated.method_27661();
        }
        class_2583 style = decorated.method_10866();
        return class_2561.method_43470((String)replaced).method_10862(style);
    }
}

