/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_304
 *  net.minecraft.class_3675
 *  net.minecraft.class_463
 *  net.minecraft.class_471
 *  net.minecraft.class_497
 *  net.minecraft.class_498
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 */
package ru.recoin.common.util.entity;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import net.minecraft.class_1041;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_463;
import net.minecraft.class_471;
import net.minecraft.class_497;
import net.minecraft.class_498;
import net.minecraft.class_638;
import net.minecraft.class_746;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.task.scripts.Script;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.movement.ScreenWalk;
import ru.recoin.implement.screens.menu.MenuScreen;

public final class PlayerInventoryComponent
implements QuickImports {
    public static final List<class_304> moveKeys = List.of(PlayerInventoryComponent.mc.field_1690.field_1894, PlayerInventoryComponent.mc.field_1690.field_1881, PlayerInventoryComponent.mc.field_1690.field_1913, PlayerInventoryComponent.mc.field_1690.field_1849, PlayerInventoryComponent.mc.field_1690.field_1903);
    public static final Script script = new Script();
    public static final Script postScript = new Script();
    private static final Queue<Runnable> inventoryActions = new ArrayDeque<Runnable>();
    public static boolean canMove = true;
    private static boolean automatedJump;
    private static boolean inventoryActionRunning;
    private static int inventoryLockTicks;
    private static class_746 contextPlayer;
    private static class_638 contextWorld;

    public static void tick() {
        PlayerInventoryComponent.checkContext();
        PlayerInventoryComponent.updateScript(script);
        PlayerInventoryComponent.updateInventoryActions();
        ScreenWalkInventorySync.runNextPendingAction();
    }

    public static void postMotion() {
        PlayerInventoryComponent.checkContext();
        PlayerInventoryComponent.updateScript(postScript);
    }

    public static void input(InputEvent e) {
        if (!canMove) {
            e.inputNonePreservingSneak();
            if (automatedJump) {
                e.setJumping(true);
            }
        }
    }

    public static void setAutomatedJump(boolean jumping) {
        automatedJump = jumping;
    }

    public static void addTask(Runnable task) {
        if (PlayerInventoryComponent.shouldQueueInventoryAction()) {
            inventoryActions.offer(() -> PlayerInventoryComponent.addTask(task));
            return;
        }
        inventoryActionRunning = true;
        if (script.isFinished() && MovingUtil.hasPlayerMovement()) {
            switch (ServerUtil.server) {
                case "FunTime": {
                    script.cleanup().addTickStep(0, () -> {
                        PlayerInventoryComponent.disableMoveKeys();
                        PlayerInventoryComponent.rotateToCamera();
                    }).addTickStep(1, () -> {
                        task.run();
                        PlayerInventoryComponent.enableMoveKeys();
                    });
                    inventoryActionRunning = false;
                    PlayerInventoryComponent.lockInventoryActions(1);
                    return;
                }
                case "SpookyTime": 
                case "CopyTime": {
                    script.cleanup().addTickStep(0, () -> {
                        PlayerInventoryComponent.disableMoveKeys();
                        PlayerInventoryComponent.rotateToCamera();
                    }).addTickStep(1, task::run).addTickStep(2, PlayerInventoryComponent::enableMoveKeys);
                    inventoryActionRunning = false;
                    PlayerInventoryComponent.lockInventoryActions(1);
                    return;
                }
            }
        }
        script.addTickStep(0, PlayerInventoryComponent::rotateToCamera);
        postScript.cleanup().addTickStep(0, () -> {
            task.run();
            PlayerInventoryUtil.closeScreen(true);
        });
        inventoryActionRunning = false;
        PlayerInventoryComponent.lockInventoryActions(1);
    }

    public static void addFunTimeTask(Runnable task) {
        if (PlayerInventoryComponent.shouldQueueInventoryAction()) {
            inventoryActions.offer(() -> PlayerInventoryComponent.addFunTimeTask(task));
            return;
        }
        inventoryActionRunning = true;
        script.cleanup().addTickStep(0, () -> {
            PlayerInventoryComponent.disableMoveKeys();
            PlayerInventoryComponent.rotateToCamera();
        }).addTickStep(1, () -> {
            task.run();
            PlayerInventoryComponent.enableMoveKeys();
        });
        inventoryActionRunning = false;
        PlayerInventoryComponent.lockInventoryActions(2);
    }

    public static void runInventoryAction(Runnable action) {
        if (PlayerInventoryComponent.shouldQueueInventoryAction()) {
            inventoryActions.offer(action);
            return;
        }
        boolean alreadyRunning = inventoryActionRunning;
        inventoryActionRunning = true;
        try {
            action.run();
        }
        finally {
            inventoryActionRunning = alreadyRunning;
            PlayerInventoryComponent.lockInventoryActions(1);
        }
    }

    public static void lockInventoryActions(int ticks) {
        PlayerInventoryComponent.checkContext();
        inventoryLockTicks = Math.max(inventoryLockTicks, ticks);
    }

    public static void unlockInventoryActions() {
        inventoryLockTicks = 0;
    }

    public static boolean isInventoryActionRunning() {
        return inventoryActionRunning;
    }

    public static boolean isInventoryActionBusy() {
        PlayerInventoryComponent.checkContext();
        return inventoryActionRunning || inventoryLockTicks > 0 || !script.isFinished() || !postScript.isFinished();
    }

    private static void checkContext() {
        if (contextPlayer == PlayerInventoryComponent.mc.field_1724 && contextWorld == PlayerInventoryComponent.mc.field_1687) {
            return;
        }
        contextPlayer = PlayerInventoryComponent.mc.field_1724;
        contextWorld = PlayerInventoryComponent.mc.field_1687;
        script.cleanup();
        postScript.cleanup();
        inventoryActions.clear();
        ScreenWalkInventorySync.clearPendingOperations();
        inventoryLockTicks = 0;
        inventoryActionRunning = false;
        canMove = true;
        automatedJump = false;
        ScreenWalk screenWalk = ScreenWalk.getInstance();
        if (screenWalk != null) {
            screenWalk.setExternalInventorySwapBlocked(false);
        }
    }

    private static boolean shouldQueueInventoryAction() {
        return !inventoryActionRunning && PlayerInventoryComponent.isInventoryActionBusy();
    }

    private static void updateInventoryActions() {
        if (inventoryLockTicks > 0) {
            --inventoryLockTicks;
        }
        if (PlayerInventoryComponent.isInventoryActionBusy() || inventoryActions.isEmpty()) {
            return;
        }
        PlayerInventoryComponent.runInventoryAction(inventoryActions.poll());
    }

    private static void updateScript(Script targetScript) {
        if (targetScript.isFinished()) {
            return;
        }
        inventoryActionRunning = true;
        try {
            targetScript.update();
        }
        finally {
            inventoryActionRunning = false;
        }
    }

    public static void rotateToCamera() {
        if (Aura.getInstance() != null && Aura.getInstance().isState() && Aura.getInstance().getTarget() != null) {
            return;
        }
        Module module = new Module("InventoryComponent", "Inventory Component", ModuleCategory.PLAYER);
        module.state = true;
        RotationController.INSTANCE.rotateTo(AngleUtil.cameraAngle(), RotationConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_3, module);
    }

    public static void disableMoveKeys() {
        canMove = false;
        PlayerInventoryComponent.unPressMoveKeys();
    }

    public static void enableMoveKeys() {
        PlayerInventoryUtil.closeScreen(true);
        canMove = true;
        PlayerInventoryComponent.updateMoveKeys();
    }

    public static void unPressMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.method_23481(false));
    }

    public static void updateMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.method_23481(class_3675.method_15987((class_1041)mc.method_22683(), (int)keyBinding.method_1429().method_1444())));
    }

    public static boolean shouldSkipExecution() {
        return PlayerInventoryComponent.mc.field_1755 != null && !PlayerIntersectionUtil.isChat(PlayerInventoryComponent.mc.field_1755) && !(PlayerInventoryComponent.mc.field_1755 instanceof class_498) && !(PlayerInventoryComponent.mc.field_1755 instanceof class_471) && !(PlayerInventoryComponent.mc.field_1755 instanceof class_463) && !(PlayerInventoryComponent.mc.field_1755 instanceof class_497) && !(PlayerInventoryComponent.mc.field_1755 instanceof MenuScreen);
    }

    private PlayerInventoryComponent() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

