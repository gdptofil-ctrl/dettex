/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.recoin.implement.features.modules.misc;

import com.google.gson.JsonObject;
import java.util.Arrays;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.QuickLogger;
import ru.recoin.implement.features.modules.misc.anydesk.AnyDeskError;
import ru.recoin.implement.features.modules.misc.anydesk.AnyDeskRelayClient;

public final class AnyDesk
extends Module
implements QuickLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"recoin-anydesk");
    private volatile transient long clientId;
    private volatile transient String connectionState = "\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d";
    private transient long generation;
    private transient AnyDeskRelayClient relay;
    private final SelectSetting connectionError = new SelectSetting("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f", "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f"){

        @Override
        public void setSelected(String selected) {
            super.setSelected(selected);
            AnyDesk.this.synchronizeError();
        }

        @Override
        public SelectSetting selected(String selected) {
            super.selected(selected);
            AnyDesk.this.synchronizeError();
            return this;
        }
    }.value((String[])Arrays.stream(AnyDeskError.values()).map(AnyDeskError::getDisplayName).toArray(String[]::new));
    private final Setting idHeader = new Setting("AnyDesk ID", "AnyDesk ID"){

        @Override
        public String getName() {
            return "ID: " + String.valueOf(AnyDesk.this.clientId == 0L ? "\u2014" : Long.valueOf(AnyDesk.this.clientId));
        }
    };
    private final Setting stateHeader = new Setting("\u0421\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435", "\u0421\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435"){

        @Override
        public String getName() {
            return AnyDesk.this.connectionState;
        }
    };
    private final ButtonSetting copyId = new ButtonSetting("\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c ID", "\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c ID").setButtonName("\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c ID").setRunnable(this::copyId);

    public AnyDesk() {
        super("AnyDesk", "AnyDesk", ModuleCategory.MISC);
        this.setup(this.idHeader, this.stateHeader, this.copyId, this.connectionError);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> this.stopRelay());
    }

    @Override
    public void setState(boolean enabled) {
        if (enabled == this.isState()) {
            return;
        }
        super.setState(enabled);
        if (enabled) {
            if (this.relay == null) {
                this.activate();
            }
        } else if (this.relay != null || this.clientId != 0L || !"\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d".equals(this.connectionState)) {
            this.deactivate();
        }
    }

    @Override
    public void activate() {
        this.stopRelay();
        long activeGeneration = this.generation;
        this.connectionState = "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044f\u2026";
        this.relay = new AnyDeskRelayClient(status -> mc.execute(() -> {
            if (activeGeneration == this.generation && this.isState()) {
                this.updateStatus((AnyDeskRelayClient.Status)status);
            }
        }));
        this.synchronizeError();
        this.relay.start();
    }

    @Override
    public void deactivate() {
        this.stopRelay();
    }

    public long getClientId() {
        return this.clientId;
    }

    public String getConnectionState() {
        return this.connectionState;
    }

    public void loadConfig(JsonObject config) {
        JsonObject settings;
        if (config == null) {
            return;
        }
        JsonObject jsonObject = settings = config.has("Settings") && config.get("Settings").isJsonObject() ? config.getAsJsonObject("Settings") : config;
        if (settings.has(this.connectionError.getName()) && settings.get(this.connectionError.getName()).isJsonPrimitive()) {
            this.connectionError.setSelected(AnyDeskError.fromDisplayName(settings.get(this.connectionError.getName()).getAsString()).getDisplayName());
        } else if (settings.has("\u041e\u0448\u0438\u0431\u043a\u0430 Wayland") && settings.get("\u041e\u0448\u0438\u0431\u043a\u0430 Wayland").isJsonPrimitive() && settings.getAsJsonPrimitive("\u041e\u0448\u0438\u0431\u043a\u0430 Wayland").isBoolean()) {
            this.connectionError.setSelected((settings.get("\u041e\u0448\u0438\u0431\u043a\u0430 Wayland").getAsBoolean() ? AnyDeskError.WAYLAND : AnyDeskError.NONE).getDisplayName());
        }
        this.synchronizeError();
    }

    private void copyId() {
        if (this.clientId != 0L) {
            AnyDesk.mc.field_1774.method_1455(Long.toString(this.clientId));
            this.logDirect("AnyDesk ID \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d: " + this.clientId);
        } else {
            this.logDirect("AnyDesk: \u0434\u043e\u0436\u0434\u0438\u0441\u044c \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438");
        }
    }

    private AnyDeskError selectedError() {
        String selected = this.connectionError.getSelected();
        return selected == null ? AnyDeskError.NONE : AnyDeskError.fromDisplayName(selected);
    }

    private void synchronizeError() {
        if (this.relay != null) {
            this.relay.setSessionError(this.selectedError());
        }
    }

    private void stopRelay() {
        ++this.generation;
        if (this.relay != null) {
            this.relay.close();
            this.relay = null;
        }
        this.clientId = 0L;
        this.connectionState = "\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d";
    }

    private void updateStatus(AnyDeskRelayClient.Status status) {
        boolean idChanged = this.clientId != status.clientId();
        this.clientId = status.clientId();
        String peer = "ID " + status.peerId() + (String)(status.peerIp() != null && !status.peerIp().isBlank() ? " (IP \u0438\u0437 \u0437\u0430\u043f\u0440\u043e\u0441\u0430: " + status.peerIp() + ")" : " (\u043f\u0443\u0431\u043b\u0438\u0447\u043d\u044b\u0439 IP \u043d\u0435 \u043f\u0435\u0440\u0435\u0434\u0430\u043d)");
        switch (status.state()) {
            case STARTING: {
                this.connectionState = "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044f\u2026";
                break;
            }
            case ONLINE: {
                this.connectionState = "\u041e\u0436\u0438\u0434\u0430\u043d\u0438\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f";
                if (!idChanged) break;
                AnyDeskError error = this.selectedError();
                this.logDirect("AnyDesk ID: " + this.clientId + (String)(error == AnyDeskError.NONE ? " \u00b7 \u0410\u0432\u0442\u043e\u043f\u0440\u0438\u043d\u044f\u0442\u0438\u0435 \u0432\u043a\u043b\u044e\u0447\u0435\u043d\u043e" : (error == AnyDeskError.FREEZE ? " \u00b7 Freeze: \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u0435 \u0441\u043e\u0435\u0434\u0438\u043d\u0435\u043d\u0438\u044f" : " \u00b7 \u041e\u0448\u0438\u0431\u043a\u0430: " + error.getDisplayName())));
                LOGGER.info("AnyDesk registered with ID {}", (Object)this.clientId);
                break;
            }
            case APPROVED: {
                this.connectionState = "\u041f\u0440\u0438\u043d\u044f\u0442\u0438\u0435 \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e";
                this.logDirect("AnyDesk: \u0437\u0430\u043f\u0440\u043e\u0441 \u043e\u0442 " + peer + " \u043f\u0440\u0438\u043d\u044f\u0442.");
                break;
            }
            case CONNECTING: {
                this.connectionState = "\u0418\u0434\u0451\u0442 \u0441\u043e\u0435\u0434\u0438\u043d\u0435\u043d\u0438\u0435\u2026";
                break;
            }
            case SESSION_ERROR: {
                this.connectionState = "\u041e\u0448\u0438\u0431\u043a\u0430 \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0430";
                this.logDirect("AnyDesk: \u043d\u0430 \u0437\u0430\u043f\u0440\u043e\u0441 \u043e\u0442 " + peer + " \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0430 \u043e\u0448\u0438\u0431\u043a\u0430: " + AnyDeskError.fromProtocolKey(status.detail()).getDisplayName());
                break;
            }
            case DISCONNECTED: {
                this.connectionState = "\u041f\u0435\u0440\u0435\u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435\u2026";
                LOGGER.info("AnyDesk reconnecting: {}", (Object)status.detail());
                break;
            }
            case ERROR: {
                this.connectionState = "\u041e\u0448\u0438\u0431\u043a\u0430 \u0437\u0430\u043f\u0443\u0441\u043a\u0430";
                this.logDirect("AnyDesk: \u043e\u0448\u0438\u0431\u043a\u0430 \u0437\u0430\u043f\u0443\u0441\u043a\u0430, \u043f\u043e\u0434\u0440\u043e\u0431\u043d\u043e\u0441\u0442\u0438 \u0432 \u0436\u0443\u0440\u043d\u0430\u043b\u0435");
                LOGGER.warn("AnyDesk failed: {}", (Object)status.detail());
                this.setState(false);
                break;
            }
            case STOPPED: {
                this.connectionState = "\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d";
                this.setState(false);
            }
        }
    }
}

