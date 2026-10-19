/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2374
 *  net.minecraft.class_238
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_265
 *  net.minecraft.class_2879
 *  net.minecraft.class_2960
 *  net.minecraft.class_3489
 *  net.minecraft.class_3532
 *  net.minecraft.class_3965
 *  net.minecraft.class_7923
 */
package ru.recoin.implement.features.modules.movement;

import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_265;
import net.minecraft.class_2879;
import net.minecraft.class_2960;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_7923;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.SimulatedPlayer;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.common.util.task.TaskPriority;
import ru.recoin.common.util.task.scripts.Script;
import ru.recoin.implement.events.keyboard.HotBarScrollEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.HotBarUpdateEvent;
import ru.recoin.implement.events.player.InputEvent;
import ru.recoin.implement.events.player.JumpEvent;
import ru.recoin.implement.events.player.MotionEvent;
import ru.recoin.implement.events.player.PostTickEvent;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.SlothSmoothMode;
import ru.recoin.implement.features.modules.movement.FunTimeSpiderMode;

public class Spider
extends Module {
    private final Script script = new Script();
    private final StopWatch stopWatch = new StopWatch();
    private final transient FunTimeSpiderMode funTime = new FunTimeSpiderMode(this);
    private String customBlockId = "";
    private final SelectSetting mode = new SelectSetting("Mode", "Selects the type of spider").value("Block", "Matrix", "FT Rod", "FunTime", "Water Bucket").selected("Block");
    private final ValueSetting spiderSpeed = new ValueSetting("Speed", "Spider climb speed").range(1.0f, 10.0f).visible(() -> this.mode.isSelected("Matrix") || this.mode.isSelected("FT Rod"));

    public Spider() {
        super("Spider", ModuleCategory.MOVEMENT);
        this.spiderSpeed.setValue(2.0f);
        this.setup(this.mode, this.spiderSpeed);
    }

    @EventHandler
    public void onMotion(MotionEvent e) {
        if (this.mode.isSelected("FunTime") || this.funTime.isRunning()) {
            return;
        }
        if (!Spider.mc.field_1724.field_5976) {
            return;
        }
        long speed = (long)class_3532.method_15363((float)(500.0f - this.spiderSpeed.getValue() / 2.0f * 100.0f), (float)0.0f, (float)500.0f);
        if ((this.mode.isSelected("Matrix") || this.mode.isSelected("FT Rod")) && this.stopWatch.finished(speed)) {
            Spider.mc.field_1724.method_6043();
            Spider.mc.field_1724.method_24830(true);
            e.setOnGround(true);
            if (this.mode.isSelected("FT Rod")) {
                this.placeRodStack();
            }
            this.stopWatch.reset();
        }
    }

    @EventHandler
    public void onInput(InputEvent event) {
        this.funTime.input(event);
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (event.getPlayer() == Spider.mc.field_1724 && this.funTime.isJumpSuppressed()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onPostTick(PostTickEvent event) {
        this.funTime.postTick();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        this.funTime.packet(event);
    }

    @EventHandler
    public void onHotBarUpdate(HotBarUpdateEvent event) {
        if (this.funTime.blocksHotbar()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onHotBarScroll(HotBarScrollEvent event) {
        if (this.funTime.blocksHotbar()) {
            event.cancel();
        }
    }

    @Override
    public void activate() {
        this.funTime.reset();
        if (this.mode.isSelected("FunTime") && this.funTime.warnMissingItem()) {
            this.setState(false);
        }
    }

    @Override
    public void deactivate() {
        this.funTime.reset();
    }

    @Override
    public void setState(boolean enabled) {
        if (!enabled && this.isState() && this.funTime.requestDisable()) {
            return;
        }
        super.setState(enabled);
        if (!enabled) {
            this.funTime.reset();
        }
    }

    void finishFunTimeDisable() {
        super.setState(false);
        this.funTime.reset();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        this.funTime.tick();
        if (this.funTime.isRunning()) {
            return;
        }
        if (!(this.mode.isSelected("Water Bucket") && Spider.mc.field_1724 != null && Spider.mc.field_1761 != null && Spider.mc.field_1724.field_5976 && Spider.mc.field_1724.method_6047().method_31574(class_1802.field_8705))) {
            return;
        }
        Spider.mc.field_1761.method_2919((class_1657)Spider.mc.field_1724, class_1268.field_5808);
        Spider.mc.field_1724.method_6104(class_1268.field_5808);
        class_243 velocity = Spider.mc.field_1724.method_18798();
        Spider.mc.field_1724.method_18800(velocity.field_1352, 0.3, velocity.field_1350);
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() == 0) {
            this.funTime.prepare(this.mode.isSelected("FunTime"));
        }
        if (this.funTime.isRunning()) {
            return;
        }
        if (this.mode.isSelected("Block") && e.getType() == 0) {
            this.handleBlockSpider();
        }
    }

    private void useRakeButton(MotionEvent e, int slot) {
        int lastSlot = Spider.mc.field_1724.method_31548().method_67532();
        Spider.mc.field_1724.method_31548().method_61496(slot);
        e.setPitch(80.0f);
        e.setYaw(Spider.mc.field_1724.method_36454());
        class_239 hitResult = Spider.mc.field_1724.method_5745(4.0, 1.0f, false);
        if (hitResult.method_17783() == class_239.class_240.field_1332) {
            class_3965 r = (class_3965)hitResult;
            Spider.mc.field_1724.field_3944.method_52787((class_2596)new class_2879(class_1268.field_5808));
            Spider.mc.field_1761.method_2896(Spider.mc.field_1724, class_1268.field_5808, r);
        }
        Spider.mc.field_1724.method_31548().method_61496(lastSlot);
        Spider.mc.field_1724.field_6017 = 0.0;
    }

    private int getRakeButtonSlot() {
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = Spider.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || !stack.method_31573(class_3489.field_15551)) continue;
            return i;
        }
        return -1;
    }

    private void placeRodStack() {
        int slotRod = this.getSlotForRod();
        if (slotRod == -1) {
            return;
        }
        int lastSlot = Spider.mc.field_1724.method_31548().method_67532();
        Spider.mc.field_1724.method_31548().method_61496(slotRod);
        class_2350 facing = Spider.mc.field_1724.method_5735();
        class_2338 wallPos = Spider.mc.field_1724.method_24515().method_10093(facing).method_10086(1);
        if (Spider.mc.field_1687.method_8320(wallPos).method_26215()) {
            class_3965 hit = new class_3965(wallPos.method_46558(), facing.method_10153(), wallPos, false);
            Spider.mc.field_1761.method_2896(Spider.mc.field_1724, class_1268.field_5808, hit);
            Spider.mc.field_1724.field_3944.method_52787((class_2596)new class_2879(class_1268.field_5808));
        }
        Spider.mc.field_1724.method_31548().method_61496(lastSlot);
    }

    private int getSlotForRod() {
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = Spider.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() != class_1802.field_27051) continue;
            return i;
        }
        return -1;
    }

    private void handleBlockSpider() {
        class_1268 hand;
        if (!Spider.mc.field_1724.field_5976) {
            return;
        }
        boolean offHand = Spider.mc.field_1724.method_6079().method_7909() instanceof class_1747;
        int slotId = PlayerInventoryUtil.getHotbarSlotId(i -> Spider.mc.field_1724.method_31548().method_5438(i).method_7909() instanceof class_1747);
        if (!offHand && slotId == -1) {
            return;
        }
        class_2338 targetPos = this.getBlockPos();
        class_2338 supportPos = this.findPos();
        if (supportPos.equals((Object)class_2338.field_10980)) {
            return;
        }
        class_2350 facing = class_2350.method_10147((float)(targetPos.method_10263() - supportPos.method_10263()), (float)(targetPos.method_10264() - supportPos.method_10264()), (float)(targetPos.method_10260() - supportPos.method_10260()));
        class_243 hitVec = supportPos.method_46558().method_1031((double)facing.method_10148() * 0.5, (double)facing.method_10164() * 0.5, (double)facing.method_10165() * 0.5);
        Angle angle = AngleUtil.calculateAngle(hitVec);
        Angle.VecRotation vecRotation = new Angle.VecRotation(angle, angle.toVector());
        RotationController.INSTANCE.rotateTo(vecRotation, (class_1309)Spider.mc.field_1724, 1, new RotationConfig(new SlothSmoothMode(), true, true), TaskPriority.HIGH_IMPORTANCE_1, this);
        class_1799 stack = offHand ? Spider.mc.field_1724.method_6079() : Spider.mc.field_1724.method_31548().method_5438(slotId);
        class_1268 class_12682 = hand = offHand ? class_1268.field_5810 : class_1268.field_5808;
        if (this.canPlace(stack)) {
            int prev = Spider.mc.field_1724.method_31548().method_67532();
            if (!offHand) {
                Spider.mc.field_1724.method_31548().method_61496(slotId);
            }
            class_3965 hit = new class_3965(hitVec, facing, supportPos, false);
            Spider.mc.field_1761.method_2896(Spider.mc.field_1724, hand, hit);
            Spider.mc.field_1724.method_6104(hand);
            if (!offHand) {
                Spider.mc.field_1724.method_31548().method_61496(prev);
            }
            if (Spider.mc.field_1724.method_24828()) {
                Spider.mc.field_1724.method_6043();
            }
        }
    }

    private boolean canPlace(class_1799 stack) {
        class_2338 blockPos = this.getBlockPos();
        if (blockPos.method_10264() >= Spider.mc.field_1724.method_31478()) {
            return false;
        }
        class_1792 class_17922 = stack.method_7909();
        if (!(class_17922 instanceof class_1747)) {
            return false;
        }
        class_1747 blockItem = (class_1747)class_17922;
        if (!Spider.mc.field_1687.method_8320(blockPos).method_26215() && !Spider.mc.field_1687.method_8320(blockPos).method_45474()) {
            return false;
        }
        class_265 shape = blockItem.method_7711().method_9564().method_26220((class_1922)Spider.mc.field_1687, blockPos);
        if (shape.method_1110()) {
            return true;
        }
        class_238 box = shape.method_1107().method_996(blockPos);
        return !box.method_994(Spider.mc.field_1724.method_5829());
    }

    private class_2338 findPos() {
        class_2338 blockPos = this.getBlockPos();
        if (Spider.mc.field_1687.method_8320(blockPos).method_51367()) {
            return class_2338.field_10980;
        }
        return Stream.of(blockPos.method_10067(), blockPos.method_10078(), blockPos.method_10072(), blockPos.method_10095()).filter(pos -> Spider.mc.field_1687.method_8320(pos).method_51367()).findFirst().orElse(class_2338.field_10980);
    }

    private class_2338 getBlockPos() {
        return class_2338.method_49638((class_2374)SimulatedPlayer.simulateLocalPlayer((int)1).pos.method_1031(0.0, -0.001, 0.0));
    }

    private int getCustomBlockSlot() {
        class_1792 target = this.getCustomBlockItem();
        if (target == null || target == class_1802.field_8162) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = Spider.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || stack.method_7909() != target) continue;
            return i;
        }
        return -1;
    }

    private void useCustomBlock(MotionEvent e, int slot) {
        int lastSlot = Spider.mc.field_1724.method_31548().method_67532();
        Spider.mc.field_1724.method_31548().method_61496(slot);
        e.setPitch(80.0f);
        e.setYaw(Spider.mc.field_1724.method_36454());
        class_239 hitResult = Spider.mc.field_1724.method_5745(4.0, 1.0f, false);
        if (hitResult.method_17783() == class_239.class_240.field_1332) {
            class_3965 r = (class_3965)hitResult;
            Spider.mc.field_1724.field_3944.method_52787((class_2596)new class_2879(class_1268.field_5808));
            Spider.mc.field_1761.method_2896(Spider.mc.field_1724, class_1268.field_5808, r);
        }
        Spider.mc.field_1724.method_31548().method_61496(lastSlot);
        Spider.mc.field_1724.field_6017 = 0.0;
    }

    public class_1792 getCustomBlockItem() {
        if (this.customBlockId == null || this.customBlockId.isBlank()) {
            return null;
        }
        class_2960 id = class_2960.method_12829((String)(this.customBlockId.contains(":") ? this.customBlockId : "minecraft:" + this.customBlockId));
        if (id == null || !class_7923.field_41178.method_10250(id)) {
            return null;
        }
        return (class_1792)class_7923.field_41178.method_63535(id);
    }

    public void setCustomBlockId(String customBlockId) {
        this.customBlockId = customBlockId;
    }

    public Script getScript() {
        return this.script;
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

    public FunTimeSpiderMode getFunTime() {
        return this.funTime;
    }

    public String getCustomBlockId() {
        return this.customBlockId;
    }

    public SelectSetting getMode() {
        return this.mode;
    }

    public ValueSetting getSpiderSpeed() {
        return this.spiderSpeed;
    }
}

