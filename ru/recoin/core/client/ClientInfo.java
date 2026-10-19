/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.core.client;

import java.io.File;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.core.client.ClientInfoProvider;
import ru.recoin.core.client.UserProfile;

public record ClientInfo(String clientName, String userName, String role, File clientDir, File filesDir, File configsDir) implements ClientInfoProvider
{
    @Override
    public String getFullInfo() {
        return String.format("Welcome! Client: %s Version: %s Branch: %s", this.clientName, UserProfile.getUsername(), StringUtil.getUserRole());
    }
}

