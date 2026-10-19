/*
 * Decompiled with CFR 0.152.
 */
package com.adl.nativeprotect;

import com.adl.nativeprotect.Native;
import java.util.HashMap;
import java.util.Map;

public class User {
    private static final User instance = new User();
    private final Map<String, String> cache = new HashMap<String, String>();
    private boolean nativeFailed = false;

    public static User getInstance() {
        return instance;
    }

    private User() {
        try {
            this.cache.put("username", this.getUsername());
            this.cache.put("hwid", this.getHwid());
            this.cache.put("role", this.getRole());
            this.cache.put("uid", this.getUid());
            this.cache.put("subTime", this.getSubsTime());
        }
        catch (UnsatisfiedLinkError e) {
            this.nativeFailed = true;
            this.cache.put("username", "DettexClient");
            this.cache.put("hwid", "hwid-1231294809786-2348786");
            this.cache.put("role", "Admin");
            this.cache.put("uid", "777");
            this.cache.put("subTime", "2025-24-05");
        }
        this.cache.put("avatar", "");
    }

    @Native
    private native String getUsername();

    @Native
    private native String getHwid();

    @Native
    private native String getRole();

    @Native
    private native String getUid();

    @Native
    private native String getSubsTime();

    public String profile(String profile) {
        return this.cache.getOrDefault(profile, "");
    }
}

