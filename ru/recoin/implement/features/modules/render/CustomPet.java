/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1297$class_5529
 *  net.minecraft.class_1657
 *  net.minecraft.class_1937
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.render;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_3532;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.render.custompet.CustomPetVariant;
import ru.recoin.implement.features.modules.render.custompet.control.CustomPetFollowerController;
import ru.recoin.implement.features.modules.render.custompet.entity.CustomPetEntity;
import ru.recoin.implement.features.modules.render.custompet.sync.CustomPetRemoteState;
import ru.recoin.implement.features.modules.render.custompet.sync.CustomPetSyncClient;

public class CustomPet
extends Module {
    private static final long CONNECT_RETRY_MS = 5000L;
    private static final long SHARED_STATE_PUSH_MS = 80L;
    private static final long SHARED_HEARTBEAT_MS = 1000L;
    private static final long HIDDEN_STATE_PUSH_MS = 1000L;
    private static final double POSITION_SYNC_THRESHOLD_SQR = 0.0036;
    private static final float YAW_SYNC_THRESHOLD = 2.0f;
    private static final double ANIMATION_SYNC_THRESHOLD = 0.1;
    private static CustomPet instance;
    private static final String KIND_FROG = "\u0416\u0430\u0431\u0430";
    private static final String KIND_ROBOT = "\u0420\u043e\u0431\u043e\u0442";
    private static final String KIND_OWL = "\u0421\u043e\u0432\u0430";
    private static final String[] ROBOT_TYPE_NAMES;
    private final SelectSetting petKind = new SelectSetting("\u041f\u0438\u0442\u043e\u043c\u0435\u0446", "\u0422\u0438\u043f \u043f\u0438\u0442\u043e\u043c\u0446\u0430: \u0436\u0430\u0431\u0430, \u0440\u043e\u0431\u043e\u0442 \u0438\u043b\u0438 \u0441\u043e\u0432\u0430").value("\u0416\u0430\u0431\u0430", "\u0420\u043e\u0431\u043e\u0442", "\u0421\u043e\u0432\u0430").selected("\u0416\u0430\u0431\u0430");
    private final SelectSetting robotType = new SelectSetting("\u0422\u0438\u043f \u0440\u043e\u0431\u043e\u0442\u0430", "\u0412\u044b\u0431\u043e\u0440 \u043c\u043e\u0434\u0435\u043b\u0438 \u0440\u043e\u0431\u043e\u0442\u0430-\u043f\u0438\u0442\u043e\u043c\u0446\u0430").value(ROBOT_TYPE_NAMES).selected(ROBOT_TYPE_NAMES[0]);
    private final SelectSetting petVariant = new SelectSetting("\u0412\u0438\u0434 \u0436\u0430\u0431\u044b", "\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0432\u0430\u0440\u0438\u0430\u043d\u0442 \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u043e\u0439 \u0436\u0430\u0431\u044b").value(CustomPetVariant.settingValues()).selected(CustomPetVariant.NITWIT.getSettingValue());
    private final BooleanSetting sharePetToOthers = new BooleanSetting("\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u0438\u0442\u043e\u043c\u0446\u0430 \u0434\u0440\u0443\u0433\u0438\u043c \u043a\u043b\u0438\u0435\u043d\u0442\u0430\u043c", "\u0415\u0441\u043b\u0438 \u0432\u043a\u043b\u044e\u0447\u0435\u043d\u043e, \u0434\u0440\u0443\u0433\u0438\u0435 \u043a\u043b\u0438\u0435\u043d\u0442\u044b \u0431\u0443\u0434\u0443\u0442 \u0432\u0438\u0434\u0435\u0442\u044c \u0432\u0430\u0448\u0443 \u0436\u0430\u0431\u0443").setValue(true);
    private final CustomPetFollowerController localController = new CustomPetFollowerController();
    private final Map<String, CustomPetEntity> remotePets = new HashMap<String, CustomPetEntity>();
    private final CustomPetSyncClient syncClient = new CustomPetSyncClient();
    private boolean petPathBroken;
    private long nextConnectAttemptAt;
    private long nextSyncPushAt;
    private String lastIdentityKey = "";
    private CustomPetVariant lastPublishedVariant = CustomPetVariant.NITWIT;
    private String lastPublishedPetKind = "frog";
    private boolean lastPublishedShareState = true;
    private boolean hasPublishedPose;
    private double lastPublishedX;
    private double lastPublishedY;
    private double lastPublishedZ;
    private float lastPublishedYaw;
    private boolean lastPublishedMoving;
    private boolean lastPublishedUmbrella;
    private boolean lastPublishedAirborne;
    private double lastPublishedAnimationSpeed = 1.0;

    public CustomPet() {
        super("CustomPet", "Custom Pet", ModuleCategory.RENDER);
        instance = this;
        this.petVariant.visible(() -> KIND_FROG.equals(this.petKind.getSelected()));
        this.robotType.visible(() -> KIND_ROBOT.equals(this.petKind.getSelected()));
        this.setup(this.petKind, this.petVariant, this.robotType, this.sharePetToOthers);
    }

    public static CustomPet getInstance() {
        return instance;
    }

    @Override
    public void deactivate() {
        this.resetAll();
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (CustomPet.mc.field_1724 == null || CustomPet.mc.field_1687 == null) {
            this.resetAll();
            return;
        }
        if (this.petPathBroken) {
            return;
        }
        try {
            CustomPetVariant customPetVariant = this.getSelectedVariant();
            boolean isOwl = this.isOwlSelected();
            this.localController.tick((class_1657)CustomPet.mc.field_1724, customPetVariant, isOwl);
            CustomPetEntity customPetEntity = this.localController.getPet();
            if (customPetEntity != null) {
                customPetEntity.setRobotType(this.getSelectedRobotType());
            }
            try {
                this.tickSync(customPetVariant);
                this.tickRemotePets();
            }
            catch (Throwable throwable) {}
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void resetAll() {
        this.localController.reset();
        this.clearRemotePets();
        this.syncClient.disconnect("reset");
        this.nextConnectAttemptAt = 0L;
        this.nextSyncPushAt = 0L;
        this.lastIdentityKey = "";
        this.lastPublishedVariant = this.getSelectedVariant();
        this.lastPublishedShareState = this.sharePetToOthers.isValue();
        this.hasPublishedPose = false;
        this.lastPublishedX = 0.0;
        this.lastPublishedY = 0.0;
        this.lastPublishedZ = 0.0;
        this.lastPublishedYaw = 0.0f;
        this.lastPublishedMoving = false;
        this.lastPublishedUmbrella = false;
        this.lastPublishedAirborne = false;
        this.lastPublishedAnimationSpeed = 1.0;
    }

    private void tickSync(CustomPetVariant customPetVariant) {
        boolean shouldPush;
        String profileUsername = this.resolveProfileUsername();
        String minecraftUsername = this.resolveMinecraftUsername();
        if (profileUsername.isBlank() || minecraftUsername.isBlank()) {
            this.syncClient.disconnect("missing_identity");
            this.clearRemotePets();
            return;
        }
        CustomPetEntity customPetEntity = this.activeLocalPet();
        if (customPetEntity == null || customPetEntity.method_31481()) {
            return;
        }
        boolean share = this.sharePetToOthers.isValue();
        String petKindStr = this.getSelectedPetKind();
        String identityKey = this.buildIdentityKey(profileUsername, minecraftUsername);
        boolean poseChanged = this.hasSignificantPoseChange(customPetEntity);
        this.syncClient.setLocalState(profileUsername, minecraftUsername, this.currentWorldId(), customPetVariant, this.getSelectedRobotType(), petKindStr, share, customPetEntity.method_23317(), customPetEntity.method_23318(), customPetEntity.method_23321(), customPetEntity.method_36454(), customPetEntity.isPetMoving(), customPetEntity.shouldUseUmbrella(), customPetEntity.isAirborneMode(), customPetEntity.getCurrentAnimationSpeed());
        long now = System.currentTimeMillis();
        if (!this.syncClient.isConnected() && !this.syncClient.isConnecting() && now >= this.nextConnectAttemptAt) {
            this.syncClient.connect(this.resolveSyncHost(), this.resolveSyncPort());
            this.nextConnectAttemptAt = now + 5000L;
        }
        boolean bl = shouldPush = now >= this.nextSyncPushAt || !identityKey.equals(this.lastIdentityKey) || customPetVariant != this.lastPublishedVariant || !petKindStr.equals(this.lastPublishedPetKind) || share != this.lastPublishedShareState || share && poseChanged;
        if (shouldPush) {
            this.syncClient.pushState();
            this.lastIdentityKey = identityKey;
            this.lastPublishedVariant = customPetVariant;
            this.lastPublishedPetKind = petKindStr;
            this.lastPublishedShareState = share;
            this.capturePublishedPose(customPetEntity);
            this.nextSyncPushAt = now + (share && poseChanged ? 80L : 1000L);
        }
    }

    private void tickRemotePets() {
        Map<String, CustomPetRemoteState> map = this.syncClient.snapshotRemoteStates();
        String localIdentity = this.buildIdentityKey(this.resolveProfileUsername(), this.resolveMinecraftUsername());
        HashSet<String> activeKeys = new HashSet<String>();
        for (CustomPetRemoteState state : map.values()) {
            class_1657 player;
            if (!state.active() || state.identityKey().equalsIgnoreCase(localIdentity) || (player = this.findPlayerByName(state.minecraftUsername())) == null || player == CustomPet.mc.field_1724 || player.method_31481()) continue;
            CustomPetEntity entity = this.ensureRemotePet(state);
            entity.applyNetworkState(state);
            activeKeys.add(state.identityKey());
        }
        this.removeInactiveRemotePets(activeKeys);
    }

    private int getSelectedRobotType() {
        String string = this.robotType.getSelected();
        for (int i = 0; i < ROBOT_TYPE_NAMES.length; ++i) {
            if (!ROBOT_TYPE_NAMES[i].equals(string)) continue;
            return i;
        }
        return 0;
    }

    private String getSelectedPetKind() {
        if (this.isOwlSelected()) {
            return "owl";
        }
        return KIND_ROBOT.equals(this.petKind.getSelected()) ? "robot" : "frog";
    }

    private String resolveSyncHost() {
        return this.decodeHiddenValue("SHc1CSgv8cLs5b8=", "ZWlGbQ==");
    }

    private void clearRemotePets() {
        for (CustomPetEntity customPetEntity : this.remotePets.values()) {
            this.removeRemotePetEntity(customPetEntity);
        }
        this.remotePets.clear();
    }

    private String currentWorldId() {
        try {
            if (CustomPet.mc.field_1687 != null) {
                return CustomPet.mc.field_1687.method_27983().method_29177().toString();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "";
    }

    private void capturePublishedPose(CustomPetEntity customPetEntity) {
        this.hasPublishedPose = true;
        this.lastPublishedX = customPetEntity.method_23317();
        this.lastPublishedY = customPetEntity.method_23318();
        this.lastPublishedZ = customPetEntity.method_23321();
        this.lastPublishedYaw = customPetEntity.method_36454();
        this.lastPublishedMoving = customPetEntity.isPetMoving();
        this.lastPublishedUmbrella = customPetEntity.shouldUseUmbrella();
        this.lastPublishedAirborne = customPetEntity.isAirborneMode();
        this.lastPublishedAnimationSpeed = customPetEntity.getCurrentAnimationSpeed();
    }

    private class_1657 findPlayerByName(String string) {
        if (string == null || string.isBlank() || CustomPet.mc.field_1687 == null) {
            return null;
        }
        for (class_1657 playerEntity : CustomPet.mc.field_1687.method_18456()) {
            if (playerEntity == null || playerEntity.method_31481() || playerEntity.method_7334().name() == null || !playerEntity.method_7334().name().equalsIgnoreCase(string)) continue;
            return playerEntity;
        }
        return null;
    }

    private boolean hasSignificantPoseChange(CustomPetEntity customPetEntity) {
        double dz;
        double dy;
        if (!this.hasPublishedPose) {
            return true;
        }
        double dx = customPetEntity.method_23317() - this.lastPublishedX;
        if (dx * dx + (dy = customPetEntity.method_23318() - this.lastPublishedY) * dy + (dz = customPetEntity.method_23321() - this.lastPublishedZ) * dz >= 0.0036) {
            return true;
        }
        float yawDiff = Math.abs(class_3532.method_15393((float)(customPetEntity.method_36454() - this.lastPublishedYaw)));
        if (yawDiff >= 2.0f) {
            return true;
        }
        if (customPetEntity.isPetMoving() != this.lastPublishedMoving || customPetEntity.shouldUseUmbrella() != this.lastPublishedUmbrella || customPetEntity.isAirborneMode() != this.lastPublishedAirborne) {
            return true;
        }
        return Math.abs(customPetEntity.getCurrentAnimationSpeed() - this.lastPublishedAnimationSpeed) >= 0.1;
    }

    private CustomPetEntity ensureRemotePet(CustomPetRemoteState customPetRemoteState) {
        boolean isOwl = customPetRemoteState.isOwl();
        CustomPetEntity existing = this.remotePets.get(customPetRemoteState.identityKey());
        if (existing != null && existing.method_73183() == CustomPet.mc.field_1687 && !existing.method_31481() && existing.isOwl() == isOwl) {
            existing.setPetVariant(customPetRemoteState.variant());
            return existing;
        }
        if (existing != null) {
            this.removeRemotePetEntity(existing);
        }
        CustomPetEntity newPet = new CustomPetEntity((class_1937)CustomPet.mc.field_1687);
        newPet.setOwl(isOwl);
        newPet.setPetVariant(customPetRemoteState.variant());
        newPet.applyNetworkState(customPetRemoteState);
        newPet.snapTo(customPetRemoteState.position(), customPetRemoteState.yaw());
        CustomPet.mc.field_1687.method_53875((class_1297)newPet);
        this.remotePets.put(customPetRemoteState.identityKey(), newPet);
        return newPet;
    }

    private String resolveMinecraftUsername() {
        if (CustomPet.mc.field_1724 == null) {
            return "";
        }
        String string = CustomPet.mc.field_1724.method_7334().name();
        return string == null ? "" : string.trim();
    }

    private CustomPetEntity activeLocalPet() {
        return this.localController.getPet();
    }

    private boolean isOwlSelected() {
        return KIND_OWL.equals(this.petKind.getSelected());
    }

    private String buildIdentityKey(String string, String string2) {
        if (string == null || string2 == null) {
            return "";
        }
        String s1 = string.trim();
        String s2 = string2.trim();
        if (s1.isEmpty() || s2.isEmpty()) {
            return "";
        }
        return s1 + "|" + s2;
    }

    private String resolveProfileUsername() {
        return this.resolveMinecraftUsername();
    }

    private int resolveSyncPort() {
        String string = this.decodeHiddenValue("YkgyLR8=", "TkpC");
        return Integer.parseInt(string);
    }

    private CustomPetVariant getSelectedVariant() {
        if (KIND_ROBOT.equals(this.petKind.getSelected())) {
            return CustomPetVariant.ROBOT;
        }
        return CustomPetVariant.fromSettingValue(this.petVariant.getSelected());
    }

    private void removeRemotePetEntity(CustomPetEntity customPetEntity) {
        if (customPetEntity == null || customPetEntity.method_31481()) {
            return;
        }
        if (CustomPet.mc.field_1687 != null) {
            CustomPet.mc.field_1687.method_2945(customPetEntity.method_5628(), class_1297.class_5529.field_26999);
        }
        customPetEntity.method_31472();
    }

    private void removeInactiveRemotePets(Set<String> set) {
        this.remotePets.entrySet().removeIf(entry -> {
            if (set.contains(entry.getKey())) {
                return false;
            }
            this.removeRemotePetEntity((CustomPetEntity)entry.getValue());
            return true;
        });
    }

    private String decodeHiddenValue(String string, String string2) {
        byte[] byArray = Base64.getDecoder().decode(string);
        byte[] byArray2 = Base64.getDecoder().decode(string2);
        byte[] byArray3 = new byte[byArray.length];
        for (int i = 0; i < byArray.length; ++i) {
            byArray3[i] = (byte)(byArray[i] ^ byArray2[i % byArray2.length] ^ i * 17 + 31);
        }
        return new String(byArray3, StandardCharsets.UTF_8);
    }

    static {
        ROBOT_TYPE_NAMES = new String[]{"\u0420\u043e\u0431\u043e\u0442 1", "\u0420\u043e\u0431\u043e\u0442 2", "\u0420\u043e\u0431\u043e\u0442 3", "\u0420\u043e\u0431\u043e\u0442 4"};
    }
}

