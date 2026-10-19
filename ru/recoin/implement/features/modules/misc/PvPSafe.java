/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 */
package ru.recoin.implement.features.modules.misc;

import java.util.regex.Pattern;
import net.minecraft.class_2561;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.other.Instance;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.chat.ChatCommandEvent;
import ru.recoin.implement.events.chat.ChatEvent;
import ru.recoin.implement.features.commands.CommandDispatcher;

public class PvPSafe
extends Module {
    private static final Pattern HUB_COMMAND_PATTERN = Pattern.compile("^(hub|lobby|quit|exit)(\\s.*)?$", 2);
    private static final Pattern ANARCHY_COMMAND_PATTERN = Pattern.compile("^an(archy)?(\\s*\\d+)(\\s.*)?$", 2);
    private long lastWarning;

    public static PvPSafe getInstance() {
        return Instance.get(PvPSafe.class);
    }

    public PvPSafe() {
        super("PvPSafe", "PvP Safe", ModuleCategory.MISC);
    }

    @EventHandler(value=1)
    public void onChat(ChatEvent event) {
        String sub;
        String prefix;
        if (PvPSafe.mc.field_1724 == null || !ServerUtil.inPvp()) {
            return;
        }
        String msg = event.getMessage() == null ? "" : event.getMessage().trim();
        if (msg.startsWith(prefix = CommandDispatcher.prefix) && ((sub = msg.substring(prefix.length()).trim().toLowerCase()).equals("rct") || sub.startsWith("rct "))) {
            event.cancel();
            this.warnBlocked();
        }
    }

    @EventHandler
    public void onChatCommand(ChatCommandEvent event) {
        String command;
        if (PvPSafe.mc.field_1724 == null || !ServerUtil.inPvp()) {
            return;
        }
        String string = command = event.getCommand() == null ? "" : event.getCommand().trim();
        if (this.isBlockedCommand(command)) {
            event.cancel();
            this.warnBlocked();
        }
    }

    private boolean isBlockedCommand(String command) {
        return HUB_COMMAND_PATTERN.matcher(command).matches() || ANARCHY_COMMAND_PATTERN.matcher(command).matches();
    }

    public boolean shouldDisableDisconnectButton() {
        return this.isState() && ServerUtil.inPvp();
    }

    public void warnBlocked() {
        long now = System.currentTimeMillis();
        if (now - this.lastWarning < 1000L) {
            return;
        }
        this.lastWarning = now;
        this.logDirect(new class_2561[]{class_2561.method_43470((String)"\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d\u0430 \u0434\u043e \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f PvP.")});
    }
}

