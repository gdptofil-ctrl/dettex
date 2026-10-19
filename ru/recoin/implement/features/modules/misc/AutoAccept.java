/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 *  net.minecraft.class_7439
 */
package ru.recoin.implement.features.modules.misc;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_2596;
import net.minecraft.class_7439;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.TickEvent;

public class AutoAccept
extends Module {
    static final Pattern CLAN_INVITE_PATTERN = Pattern.compile("(?:\\[[^\\]]*]\\s*)*([A-Za-z0-9_]{3,16})\\s+\u043f\u0440\u0438\u0433\u043b\u0430\u0448\u0430\u0435\u0442\\s+\u0412\u0430\u0441\\s+\u0432\\s+\u043a\u043b\u0430\u043d", 66);
    private final String[] teleportMessages = new String[]{"has requested teleport", "\u043f\u0440\u043e\u0441\u0438\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f", "\u0445\u043e\u0447\u0435\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u043a \u0432\u0430\u043c", "\u043f\u0440\u043e\u0441\u0438\u0442 \u043a \u0432\u0430\u043c \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f", "\u0437\u0430\u043f\u0440\u043e\u0441\u0438\u043b \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044e", "\u0437\u0430\u043f\u0440\u043e\u0441\u0438\u043b \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442", "\u0445\u043e\u0447\u0435\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f", "\u043e\u0442\u043f\u0440\u0430\u0432\u0438\u043b \u0437\u0430\u043f\u0440\u043e\u0441 \u043d\u0430 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044e", "\u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u043a \u0432\u0430\u043c"};
    private final BooleanSetting autoTpAccept = new BooleanSetting("Auto Tp Accept", "Automatically accepts teleport requests").setValue(true);
    private final BooleanSetting autoClanAccept = new BooleanSetting("Auto Clan Accept", "Automatically accepts clan invites").setValue(true);
    private final BooleanSetting friendSetting = new BooleanSetting("Only Friends", "Will only accept requests from friends").setValue(false);
    private boolean canAcceptTeleport;
    private String pendingClanInvite;

    public AutoAccept() {
        super("AutoAccept", "Auto Accept", ModuleCategory.MISC);
        this.setup(this.autoTpAccept, this.autoClanAccept, this.friendSetting);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        String clanNick;
        class_2596<?> class_25962 = e.getPacket();
        if (!(class_25962 instanceof class_7439)) {
            return;
        }
        class_7439 packet = (class_7439)class_25962;
        String message = packet.comp_763().getString();
        if (this.autoTpAccept.isValue() && this.isTeleportMessage(message)) {
            boolean validPlayer;
            this.canAcceptTeleport = validPlayer = !this.friendSetting.isValue() || FriendUtils.getFriends().stream().anyMatch(friend -> message.contains(friend.getName()));
        }
        String string = clanNick = this.autoClanAccept.isValue() ? this.extractClanInviteNick(message) : null;
        if (clanNick != null) {
            boolean validPlayer;
            boolean bl = validPlayer = !this.friendSetting.isValue() || FriendUtils.isFriend(clanNick);
            if (validPlayer) {
                this.pendingClanInvite = clanNick;
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoAccept.mc.field_1724 == null || AutoAccept.mc.field_1724.field_3944 == null || ServerUtil.isPvp()) {
            return;
        }
        if (this.autoTpAccept.isValue() && this.canAcceptTeleport) {
            AutoAccept.mc.field_1724.field_3944.method_45730("tpaccept");
            this.canAcceptTeleport = false;
        }
        if (this.autoClanAccept.isValue() && this.pendingClanInvite != null && !this.pendingClanInvite.isEmpty()) {
            AutoAccept.mc.field_1724.field_3944.method_45729("/clan accept " + this.pendingClanInvite);
            this.pendingClanInvite = null;
        }
    }

    private boolean isTeleportMessage(String message) {
        String lowered = message.toLowerCase();
        return Arrays.stream(this.teleportMessages).map(String::toLowerCase).anyMatch(lowered::contains);
    }

    private String extractClanInviteNick(String message) {
        String stripped = message.replace('\u270b', ' ').trim();
        Matcher matcher = CLAN_INVITE_PATTERN.matcher(stripped);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}

