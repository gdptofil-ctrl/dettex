/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_3481
 *  net.minecraft.class_3532
 *  net.minecraft.class_7923
 */
package ru.recoin.common.util.other;

import java.util.List;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3481;
import net.minecraft.class_3532;
import net.minecraft.class_7923;
import ru.recoin.api.feature.module.Module;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.other.BaritoneCompat;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public final class BaritoneUndetectController
implements QuickImports {
    public static final BaritoneUndetectController INSTANCE = new BaritoneUndetectController();
    private Angle funtimeAngle;
    private long straightSince;
    private long sprintDelay = 150L;
    private boolean sprinting;
    private Boolean previousSprint;
    private Boolean previousBreak;
    private Boolean previousPlace;
    private Boolean previousFreeLook;
    private Boolean previousBlockFreeLook;
    private Boolean previousAntiCheat;
    private Boolean previousAvoidance;
    private Object previousMaxFallHeight;
    private Boolean previousParkour;
    private boolean parkourSuppressed;

    private BaritoneUndetectController() {
    }

    public void applySettings(Object baritone) {
        if (baritone == null) {
            return;
        }
        this.previousSprint = BaritoneCompat.getBooleanSetting("allowSprint");
        this.previousBreak = BaritoneCompat.getBooleanSetting("allowBreak");
        this.previousPlace = BaritoneCompat.getBooleanSetting("allowPlace");
        this.previousFreeLook = BaritoneCompat.getBooleanSetting("freeLook");
        this.previousBlockFreeLook = BaritoneCompat.getBooleanSetting("blockFreeLook");
        this.previousAntiCheat = BaritoneCompat.getBooleanSetting("antiCheatCompatibility");
        this.previousAvoidance = BaritoneCompat.getBooleanSetting("avoidance");
        this.previousParkour = BaritoneCompat.getBooleanSetting("allowParkour");
        this.parkourSuppressed = false;
        BaritoneCompat.setBooleanSetting("allowBreak", false);
        BaritoneCompat.setBooleanSetting("allowPlace", false);
        BaritoneCompat.setBooleanSetting("allowSprint", true);
        BaritoneCompat.setBooleanSetting("freeLook", true);
        BaritoneCompat.setBooleanSetting("blockFreeLook", true);
        BaritoneCompat.setBooleanSetting("antiCheatCompatibility", true);
        BaritoneCompat.setSetting("maxFallHeightNoWater", 3);
        BaritoneCompat.setBooleanSetting("avoidance", true);
        BaritoneCompat.setSetting("randomLooking", 0.0);
        BaritoneCompat.setSetting("randomLooking113", 0.0);
        BaritoneUndetectController.configureAvoidance(true);
        this.straightSince = 0L;
        this.sprinting = false;
        this.funtimeAngle = null;
    }

    public void restoreSettings(Object baritone, Module module) {
        if (baritone != null) {
            BaritoneCompat.cancelPathing(baritone);
            if (this.previousSprint != null) {
                BaritoneCompat.setBooleanSetting("allowSprint", this.previousSprint);
            }
            if (this.previousBreak != null) {
                BaritoneCompat.setBooleanSetting("allowBreak", this.previousBreak);
            }
            if (this.previousPlace != null) {
                BaritoneCompat.setBooleanSetting("allowPlace", this.previousPlace);
            }
            if (this.previousFreeLook != null) {
                BaritoneCompat.setBooleanSetting("freeLook", this.previousFreeLook);
            }
            if (this.previousBlockFreeLook != null) {
                BaritoneCompat.setBooleanSetting("blockFreeLook", this.previousBlockFreeLook);
            }
            if (this.previousAntiCheat != null) {
                BaritoneCompat.setBooleanSetting("antiCheatCompatibility", this.previousAntiCheat);
            }
            if (this.previousAvoidance != null) {
                BaritoneCompat.setBooleanSetting("avoidance", this.previousAvoidance);
            }
            if (this.previousParkour != null) {
                BaritoneCompat.setBooleanSetting("allowParkour", this.previousParkour);
            }
            BaritoneCompat.setSetting("maxFallHeightNoWater", 3);
            BaritoneUndetectController.configureAvoidance(false);
        }
        if (BaritoneUndetectController.mc.field_1724 != null) {
            BaritoneUndetectController.mc.field_1724.method_5728(false);
        }
        if (BaritoneUndetectController.mc.field_1690 != null) {
            BaritoneUndetectController.mc.field_1690.field_1867.method_23481(false);
        }
        this.straightSince = 0L;
        this.sprinting = false;
        this.funtimeAngle = null;
        if (module != null) {
            RotationController.INSTANCE.release(module);
        }
    }

    public void onTick(Object baritone, Module module) {
        boolean stairSection;
        float deltaPitch;
        float deltaYaw;
        float totalDelta;
        if (baritone == null || BaritoneUndetectController.mc.field_1724 == null || BaritoneUndetectController.mc.field_1687 == null) {
            this.resetSprint(baritone);
            return;
        }
        if (BaritoneUndetectController.mc.field_1755 != null) {
            this.resetSprint(baritone);
            return;
        }
        if (!BaritoneCompat.isPathing(baritone)) {
            this.restoreParkour();
            this.resetSprint(baritone);
            if (this.funtimeAngle != null && module != null) {
                RotationController.INSTANCE.release(module);
                this.funtimeAngle = null;
            }
            return;
        }
        List<class_2338> path = BaritoneCompat.getPathPositions(baritone);
        int currentIndex = BaritoneCompat.getPathPositionIndex(baritone);
        if (path.isEmpty() || currentIndex < 0) {
            this.resetSprint(baritone);
            return;
        }
        int lookIndex = Math.min(currentIndex + 1, path.size() - 1);
        class_2338 lookNode = path.get(lookIndex);
        class_243 targetPoint = lookNode.method_46558().method_1031(0.0, 0.25, 0.0);
        Angle targetAngle = AngleUtil.calculateAngle(targetPoint);
        if (this.funtimeAngle == null) {
            this.funtimeAngle = new Angle(BaritoneUndetectController.mc.field_1724.method_36454(), BaritoneUndetectController.mc.field_1724.method_36455());
        }
        float turnSpeed = (totalDelta = (float)Math.hypot(deltaYaw = class_3532.method_15393((float)(targetAngle.getYaw() - this.funtimeAngle.getYaw())), deltaPitch = targetAngle.getPitch() - this.funtimeAngle.getPitch())) > 50.0f ? 15.0f + (float)(Math.random() * 4.0) : (totalDelta > 20.0f ? 10.0f + (float)(Math.random() * 3.5) : (totalDelta > 5.0f ? 6.0f + (float)(Math.random() * 2.5) : Math.max(1.8f, totalDelta * 0.45f + (float)(Math.random() * (double)1.2f))));
        double time = (double)System.currentTimeMillis() * 0.001;
        float swayYaw = (float)(Math.sin(time * 3.1) * 0.35 + Math.cos(time * 1.7) * 0.2);
        float swayPitch = (float)(Math.cos(time * 2.7) * 0.2 + Math.sin(time * 1.9) * 0.12);
        if (totalDelta < 2.0f) {
            swayYaw *= 0.2f;
            swayPitch *= 0.2f;
        }
        float stepYaw = class_3532.method_15363((float)deltaYaw, (float)(-turnSpeed), (float)turnSpeed) + swayYaw;
        float stepPitch = class_3532.method_15363((float)deltaPitch, (float)(-turnSpeed * 0.6f), (float)(turnSpeed * 0.6f)) + swayPitch;
        float newYaw = this.funtimeAngle.getYaw() + stepYaw;
        float newPitch = class_3532.method_15363((float)(this.funtimeAngle.getPitch() + stepPitch), (float)-89.0f, (float)89.0f);
        this.funtimeAngle = new Angle(newYaw, newPitch).adjustSensitivity();
        if (module != null) {
            RotationController.INSTANCE.rotateTo(this.funtimeAngle, 1, RotationConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_2, module);
        }
        BaritoneCompat.lookAt(baritone, this.funtimeAngle.getYaw(), this.funtimeAngle.getPitch());
        BaritoneUndetectController.mc.field_1724.method_36456(this.funtimeAngle.getYaw());
        BaritoneUndetectController.mc.field_1724.method_36457(this.funtimeAngle.getPitch());
        float yawDiff = Math.abs(class_3532.method_15393((float)(targetAngle.getYaw() - this.funtimeAngle.getYaw())));
        boolean bl = stairSection = BaritoneUndetectController.hasUpcomingHeightChange(path, currentIndex) || BaritoneUndetectController.hasUpcomingStairSection(path, currentIndex);
        if (stairSection && !this.parkourSuppressed) {
            this.previousParkour = BaritoneCompat.getBooleanSetting("allowParkour");
            BaritoneCompat.setBooleanSetting("allowParkour", false);
            this.parkourSuppressed = true;
        } else if (!stairSection) {
            this.restoreParkour();
        }
        boolean canSprint = BaritoneUndetectController.mc.field_1724.method_7344().method_7586() > 6 && !BaritoneUndetectController.mc.field_1724.method_5715() && !stairSection;
        BaritoneCompat.setBooleanSetting("allowSprint", canSprint);
        if (canSprint && yawDiff <= 40.0f) {
            if (BaritoneUndetectController.mc.field_1690 != null) {
                BaritoneUndetectController.mc.field_1690.field_1867.method_23481(true);
            }
            BaritoneUndetectController.mc.field_1724.method_5728(true);
            this.sprinting = true;
        } else if (yawDiff > 65.0f || !canSprint) {
            this.resetSprint(baritone);
        }
        if (BaritoneUndetectController.mc.field_1690 != null && stairSection) {
            BaritoneUndetectController.mc.field_1690.field_1903.method_23481(false);
        }
    }

    public void lookAtDirectly(Object baritone, class_243 point, Module module) {
        if (point == null || BaritoneUndetectController.mc.field_1724 == null) {
            return;
        }
        Angle targetAngle = AngleUtil.calculateAngle(point);
        if (this.funtimeAngle == null) {
            this.funtimeAngle = new Angle(BaritoneUndetectController.mc.field_1724.method_36454(), BaritoneUndetectController.mc.field_1724.method_36455());
        }
        float deltaYaw = class_3532.method_15393((float)(targetAngle.getYaw() - this.funtimeAngle.getYaw()));
        float deltaPitch = targetAngle.getPitch() - this.funtimeAngle.getPitch();
        float turnSpeed = 16.0f + (float)(Math.random() * 5.0);
        float stepYaw = class_3532.method_15363((float)deltaYaw, (float)(-turnSpeed), (float)turnSpeed);
        float stepPitch = class_3532.method_15363((float)deltaPitch, (float)(-turnSpeed * 0.7f), (float)(turnSpeed * 0.7f));
        this.funtimeAngle = new Angle(this.funtimeAngle.getYaw() + stepYaw, class_3532.method_15363((float)(this.funtimeAngle.getPitch() + stepPitch), (float)-89.0f, (float)89.0f)).adjustSensitivity();
        if (module != null) {
            RotationController.INSTANCE.rotateTo(this.funtimeAngle, 1, RotationConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_2, module);
        }
        if (baritone != null) {
            BaritoneCompat.lookAt(baritone, this.funtimeAngle.getYaw(), this.funtimeAngle.getPitch());
        }
        BaritoneUndetectController.mc.field_1724.method_36456(this.funtimeAngle.getYaw());
        BaritoneUndetectController.mc.field_1724.method_36457(this.funtimeAngle.getPitch());
    }

    private void resetSprint(Object baritone) {
        this.straightSince = 0L;
        if (this.sprinting) {
            if (BaritoneUndetectController.mc.field_1690 != null) {
                BaritoneUndetectController.mc.field_1690.field_1867.method_23481(false);
            }
            if (BaritoneUndetectController.mc.field_1724 != null) {
                BaritoneUndetectController.mc.field_1724.method_5728(false);
            }
            this.sprinting = false;
        }
    }

    private void restoreParkour() {
        if (!this.parkourSuppressed) {
            return;
        }
        if (this.previousParkour != null) {
            BaritoneCompat.setBooleanSetting("allowParkour", this.previousParkour);
        }
        this.parkourSuppressed = false;
    }

    public static int calculateStraightDistance(List<class_2338> path, int currentIndex) {
        if (path == null || currentIndex < 0 || currentIndex >= path.size() - 1) {
            return 0;
        }
        class_2338 current = path.get(currentIndex);
        class_2338 next = path.get(currentIndex + 1);
        int dx = next.method_10263() - current.method_10263();
        int dz = next.method_10260() - current.method_10260();
        if (dx == 0 && dz == 0) {
            return 0;
        }
        int straight = 1;
        for (int i = currentIndex + 1; i < path.size() - 1; ++i) {
            class_2338 p1 = path.get(i);
            class_2338 p2 = path.get(i + 1);
            int stepX = p2.method_10263() - p1.method_10263();
            int stepZ = p2.method_10260() - p1.method_10260();
            if (stepX != dx || stepZ != dz || p2.method_10264() != p1.method_10264()) break;
            ++straight;
        }
        return straight;
    }

    public static boolean hasUpcomingHeightChange(List<class_2338> path, int currentIndex) {
        if (path == null || currentIndex < 0 || currentIndex >= path.size()) {
            return false;
        }
        int currentY = path.get(currentIndex).method_10264();
        int end = Math.min(path.size(), currentIndex + 4);
        for (int i = currentIndex + 1; i < end; ++i) {
            if (path.get(i).method_10264() == currentY) continue;
            return true;
        }
        return false;
    }

    public static boolean hasUpcomingStairSection(List<class_2338> path, int currentIndex) {
        if (BaritoneUndetectController.mc.field_1687 == null || BaritoneUndetectController.mc.field_1724 == null) {
            return false;
        }
        if (BaritoneUndetectController.isStairAround(BaritoneUndetectController.mc.field_1724.method_24515())) {
            return true;
        }
        if (path == null || currentIndex < 0 || currentIndex >= path.size()) {
            return false;
        }
        int end = Math.min(path.size(), currentIndex + 7);
        for (int i = currentIndex; i < end; ++i) {
            if (!BaritoneUndetectController.isStairAround(path.get(i))) continue;
            return true;
        }
        return false;
    }

    private static boolean isStairAround(class_2338 pos) {
        return BaritoneUndetectController.mc.field_1687.method_8320(pos).method_26164(class_3481.field_15459) || BaritoneUndetectController.mc.field_1687.method_8320(pos.method_10074()).method_26164(class_3481.field_15459) || BaritoneUndetectController.mc.field_1687.method_8320(pos.method_10084()).method_26164(class_3481.field_15459);
    }

    public static void configureAvoidance(boolean add) {
        for (class_2248 block : class_7923.field_41175) {
            if (!block.method_9564().method_26164(class_3481.field_26983) && block != class_2246.field_37571 && block != class_2246.field_28108 && block != class_2246.field_16999 && block != class_2246.field_10343) continue;
            if (add) {
                BaritoneCompat.addToListSetting("blocksToAvoid", block);
                continue;
            }
            BaritoneCompat.removeFromListSetting("blocksToAvoid", block);
        }
    }

    public Angle getFuntimeAngle() {
        return this.funtimeAngle;
    }
}

