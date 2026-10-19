/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.misc.anydesk;

public enum AnyDeskError {
    NONE("\u0411\u0435\u0437 \u043e\u0448\u0438\u0431\u043a\u0438", 0, ""),
    WAYLAND("Wayland", 18, "display_server_not_supported"),
    REJECTED("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u043e\u0442\u043a\u043b\u043e\u043d\u0435\u043d\u043e", 6, "desk_rt_rejected"),
    AUTH_FAILED("\u041e\u0448\u0438\u0431\u043a\u0430 \u0430\u0432\u0442\u043e\u0440\u0438\u0437\u0430\u0446\u0438\u0438", 3, "desk_rt_auth_failed"),
    ACL_DENIED("\u0414\u043e\u0441\u0442\u0443\u043f \u0437\u0430\u043f\u0440\u0435\u0449\u0451\u043d", 14, "desk_rt_acl_denied"),
    NO_CAPTURE_PERMISSIONS("\u041d\u0435\u0442 \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u0438\u044f \u043d\u0430 \u0437\u0430\u0445\u0432\u0430\u0442 \u044d\u043a\u0440\u0430\u043d\u0430", 21, "desk_rt_no_capture_permissions"),
    INVALID_VERSION("\u041d\u0435\u0441\u043e\u0432\u043c\u0435\u0441\u0442\u0438\u043c\u0430\u044f \u0432\u0435\u0440\u0441\u0438\u044f", 5, "desk_rt_invalid_ver"),
    LOGON_DISABLED("\u0423\u0434\u0430\u043b\u0451\u043d\u043d\u044b\u0439 \u0432\u0445\u043e\u0434 \u043e\u0442\u043a\u043b\u044e\u0447\u0451\u043d", 7, "desk_rt_logon_disabled"),
    TWO_FACTOR_FAILED("\u041e\u0448\u0438\u0431\u043a\u0430 \u0434\u0432\u0443\u0445\u0444\u0430\u043a\u0442\u043e\u0440\u043d\u043e\u0439 \u0430\u0432\u0442\u043e\u0440\u0438\u0437\u0430\u0446\u0438\u0438", 23, "desk_rt_two_factor_auth_failed"),
    TWO_FACTOR_UNSUPPORTED("\u0414\u0432\u0443\u0445\u0444\u0430\u043a\u0442\u043e\u0440\u043d\u0430\u044f \u0430\u0432\u0442\u043e\u0440\u0438\u0437\u0430\u0446\u0438\u044f \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f", 24, "desk_rt_two_factor_auth_not_supported"),
    RESTARTING("\u0423\u0434\u0430\u043b\u0451\u043d\u043d\u044b\u0439 \u043a\u043b\u0438\u0435\u043d\u0442 \u043f\u0435\u0440\u0435\u0437\u0430\u043f\u0443\u0441\u043a\u0430\u0435\u0442\u0441\u044f", 13, "desk_rt_restarting"),
    USER_CLOSE("\u0421\u0435\u0430\u043d\u0441 \u0437\u0430\u0432\u0435\u0440\u0448\u0451\u043d \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u0435\u043c", 4, "desk_rt_user_close"),
    PROTOCOL_ERROR("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u043e\u0442\u043e\u043a\u043e\u043b\u0430", 25, "desk_rt_protocol_error"),
    INTERNAL_ERROR("\u0412\u043d\u0443\u0442\u0440\u0435\u043d\u043d\u044f\u044f \u043e\u0448\u0438\u0431\u043a\u0430", 26, "desk_rt_internal_error"),
    FREEZE("Freeze", -1, "freeze");

    private final String displayName;
    private final int errorCode;
    private final String protocolKey;

    private AnyDeskError(String displayName, int errorCode, String protocolKey) {
        this.displayName = displayName;
        this.errorCode = errorCode;
        this.protocolKey = protocolKey;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getProtocolKey() {
        return this.protocolKey;
    }

    public static AnyDeskError fromDisplayName(String displayName) {
        for (AnyDeskError error : AnyDeskError.values()) {
            if (!error.displayName.equals(displayName)) continue;
            return error;
        }
        return NONE;
    }

    public static AnyDeskError fromProtocolKey(String protocolKey) {
        for (AnyDeskError error : AnyDeskError.values()) {
            if (!error.protocolKey.equals(protocolKey)) continue;
            return error;
        }
        return NONE;
    }
}

