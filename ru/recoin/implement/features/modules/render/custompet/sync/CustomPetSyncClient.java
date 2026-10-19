/*
 * INTERNET DISABLED FOR SECURITY
 */
package ru.recoin.implement.features.modules.render.custompet.sync;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import ru.recoin.implement.features.modules.render.custompet.CustomPetVariant;
import ru.recoin.implement.features.modules.render.custompet.sync.CustomPetRemoteState;

public final class CustomPetSyncClient {
    private final Gson gson = new Gson();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final StringBuilder packetBuffer = new StringBuilder();
    private final ConcurrentMap<String, CustomPetRemoteState> remoteStates = new ConcurrentHashMap<String, CustomPetRemoteState>();
    private volatile boolean manualClose;
    private volatile String identityKey = "";
    private volatile String profileUsername = "";
    private volatile String minecraftUsername = "";
    private volatile String world = "";
    private volatile CustomPetVariant variant = CustomPetVariant.NITWIT;
    private volatile int robotType;
    private volatile String petKind = "frog";
    private volatile boolean active;
    private volatile double x;
    private volatile double y;
    private volatile double z;
    private volatile float yaw;
    private volatile boolean moving;
    private volatile boolean umbrella;
    private volatile boolean airborne;
    private volatile double animationSpeed = 1.0;

    public void setLocalState(String profileUsername, String minecraftUsername, String world, CustomPetVariant variant, int robotType, String petKind, boolean active, double x, double y, double z, float yaw, boolean moving, boolean umbrella, boolean airborne, double animationSpeed) {
        this.profileUsername = profileUsername == null ? "" : profileUsername;
        this.minecraftUsername = minecraftUsername == null ? "" : minecraftUsername;
        this.world = world == null ? "" : world;
        this.variant = variant;
        this.robotType = robotType;
        this.petKind = petKind == null ? "frog" : petKind;
        this.active = active;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.moving = moving;
        this.umbrella = umbrella;
        this.airborne = airborne;
        this.animationSpeed = animationSpeed;
    }

    public synchronized void connect(String string, int n) {
        // INTERNET DISABLED
    }

    public boolean isConnected() {
        return false;
    }

    public synchronized void disconnect(String string) {
        this.connected.set(false);
        this.connecting.set(false);
        this.remoteStates.clear();
    }

    public void pushState() {
        // INTERNET DISABLED
    }

    public boolean isConnecting() {
        return false;
    }

    public Map<String, CustomPetRemoteState> snapshotRemoteStates() {
        return Map.of();
    }

    public void setIdentity(String identityKey, String profileUsername, String minecraftUsername) {
        this.identityKey = identityKey == null ? "" : identityKey;
        this.profileUsername = profileUsername == null ? "" : profileUsername;
        this.minecraftUsername = minecraftUsername == null ? "" : minecraftUsername;
    }

    public void setWorld(String world) {
        this.world = world == null ? "" : world;
    }

    public void setVariant(CustomPetVariant variant) {
        this.variant = variant;
    }

    public void setRobotType(int robotType) {
        this.robotType = robotType;
    }

    public void setPetKind(String petKind) {
        this.petKind = petKind == null ? "frog" : petKind;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public void setUmbrella(boolean umbrella) {
        this.umbrella = umbrella;
    }

    public void setAirborne(boolean airborne) {
        this.airborne = airborne;
    }

    public void setAnimationSpeed(double animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
}
