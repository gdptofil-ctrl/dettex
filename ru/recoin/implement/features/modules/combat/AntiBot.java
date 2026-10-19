/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_4844
 */
package ru.recoin.implement.features.modules.combat;

import com.google.common.collect.Lists;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_4844;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.repository.friend.FriendUtils;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

public class AntiBot
extends Module {
    public static final CopyOnWriteArrayList<class_1657> bots = Lists.newCopyOnWriteArrayList();
    private final BooleanSetting removeFromWorld = new BooleanSetting("\u0423\u0434\u0430\u043b\u044f\u0442\u044c \u0438\u0437 \u043c\u0438\u0440\u0430", "").setValue(false);

    public static AntiBot getInstance() {
        return Instance.get(AntiBot.class);
    }

    public static boolean isBot(class_1657 player) {
        return AntiBot.getInstance().isState() && bots.contains(player);
    }

    public void reset() {
        bots.clear();
    }

    private static boolean onServer(String value) {
        return mc.method_1558() != null && AntiBot.mc.method_1558().field_3761.contains(value);
    }

    public AntiBot() {
        super("AntiBot", "Anti Bot", ModuleCategory.COMBAT);
        this.setup(this.removeFromWorld);
    }

    private void checkOfflineUuids() {
        for (class_1657 player2 : AntiBot.mc.field_1687.method_18456()) {
            if (AntiBot.mc.field_1724 == player2 || player2.method_5667().equals(class_4844.method_43344((String)player2.method_5477().getString())) || bots.contains(player2)) continue;
            bots.add(player2);
        }
        if (this.removeFromWorld.isValue()) {
            try {
                AntiBot.mc.field_1687.method_18456().removeIf(bots::contains);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static boolean isEmpty() {
        return bots.isEmpty();
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (AntiBot.mc.field_1687 != null && AntiBot.mc.field_1724 != null) {
            if (AntiBot.onServer("reallyworld")) {
                this.checkReallyWorld();
            } else if (AntiBot.onServer("lonygrief")) {
                this.checkLonyGrief();
            } else {
                this.checkOfflineUuids();
            }
        }
    }

    private boolean hasGlint(class_1799 stack) {
        return stack.method_7960() ? false : stack.method_7958();
    }

    public static boolean isBot(class_1309 entity) {
        boolean bl;
        if (entity instanceof class_1657) {
            class_1657 value = (class_1657)entity;
            bl = bots.contains(value);
        } else {
            bl = false;
        }
        return bl;
    }

    private void checkReallyWorld() {
        for (class_1657 player2 : AntiBot.mc.field_1687.method_18456()) {
            boolean flag5;
            if (AntiBot.mc.field_1724 == player2) continue;
            class_1799 stack = player2.method_6118(class_1304.field_6166);
            class_1799 stack2 = player2.method_6118(class_1304.field_6172);
            class_1799 stack3 = player2.method_6118(class_1304.field_6174);
            class_1799 stack4 = player2.method_6118(class_1304.field_6169);
            class_1799 stack5 = player2.method_6079();
            class_1799 stack6 = player2.method_6047();
            boolean flag = !stack.method_7960() && !stack2.method_7960() && !stack3.method_7960() && !stack4.method_7960();
            boolean flag2 = stack.method_7923() && stack2.method_7923() && stack3.method_7923() && stack4.method_7923();
            boolean flag3 = stack.method_31574(class_1802.field_8370) || stack2.method_31574(class_1802.field_8570) || stack3.method_31574(class_1802.field_8577) || stack4.method_31574(class_1802.field_8267) || stack.method_31574(class_1802.field_8660) || stack2.method_31574(class_1802.field_8396) || stack3.method_31574(class_1802.field_8523) || stack4.method_31574(class_1802.field_8743);
            boolean flag4 = !stack.method_7986() && !stack2.method_7986() && !stack3.method_7986() && !stack4.method_7986();
            boolean bl = flag5 = player2.method_7344().method_7586() == 20;
            if (flag && flag2 && stack5.method_7960() && flag3 && !stack6.method_7960() && flag4 && flag5) {
                if (!bots.contains(player2)) {
                    bots.add(player2);
                }
                return;
            }
            bots.remove(player2);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        bots.clear();
    }

    private void checkLonyGrief() {
        for (class_1657 player2 : AntiBot.mc.field_1687.method_18456()) {
            boolean flag21;
            if (AntiBot.mc.field_1724 == player2) continue;
            class_1799 stack = player2.method_6118(class_1304.field_6166);
            class_1799 stack2 = player2.method_6118(class_1304.field_6172);
            class_1799 stack3 = player2.method_6118(class_1304.field_6174);
            class_1799 stack4 = player2.method_6118(class_1304.field_6169);
            boolean flag = stack.method_7960();
            boolean flag2 = stack2.method_7960();
            boolean flag3 = stack3.method_7960();
            boolean flag4 = stack4.method_7960();
            boolean flag5 = !stack.method_7960();
            boolean flag6 = !stack2.method_7960();
            boolean flag7 = !stack3.method_7960();
            boolean flag8 = !stack4.method_7960();
            boolean flag9 = this.hasGlint(stack);
            boolean flag10 = this.hasGlint(stack2);
            boolean flag11 = this.hasGlint(stack3);
            boolean flag12 = this.hasGlint(stack4);
            boolean flag13 = flag || flag5 && !flag9;
            boolean flag14 = flag2 || flag6 && !flag10;
            boolean flag15 = flag3 || flag7 && !flag11;
            boolean flag16 = flag4 || flag8 && !flag12;
            boolean flag17 = player2.method_6096() == 0;
            boolean flag18 = FriendUtils.isFriend((class_1297)player2);
            boolean flag19 = stack.method_7986() && stack2.method_7986() && stack3.method_7986() && stack4.method_7986();
            boolean flag20 = player2.method_5477().getString().length() == 6;
            boolean bl = flag21 = flag13 && flag14 && flag15 && flag16;
            if (flag20 && !flag18 && !flag17 && !flag19 && flag21) {
                if (!bots.contains(player2)) {
                    bots.add(player2);
                }
                return;
            }
            bots.remove(player2);
        }
    }

    @Override
    public void activate() {
        super.activate();
        bots.clear();
    }
}

