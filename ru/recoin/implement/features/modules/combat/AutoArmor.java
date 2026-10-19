/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10192
 *  net.minecraft.class_124
 *  net.minecraft.class_1304
 *  net.minecraft.class_1304$class_1305
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_2378
 *  net.minecraft.class_2561
 *  net.minecraft.class_5134
 *  net.minecraft.class_6880
 *  net.minecraft.class_7924
 *  net.minecraft.class_9285
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.class_10192;
import net.minecraft.class_124;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_2378;
import net.minecraft.class_2561;
import net.minecraft.class_5134;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.common.util.entity.PlayerInventoryComponent;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.entity.ScreenWalkInventorySync;
import ru.recoin.implement.events.player.TickEvent;
import ru.recoin.implement.features.draggables.Notifications;
import ru.recoin.implement.features.modules.render.Hud;

public class AutoArmor
extends Module {
    public AutoArmor() {
        super("AutoArmor", "Auto Armor", ModuleCategory.PLAYER);
        this.setup(new Setting[0]);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (PlayerInventoryUtil.isServerScreen()) {
            return;
        }
        if (!PlayerInventoryComponent.script.isFinished()) {
            return;
        }
        if (PlayerInventoryComponent.isInventoryActionBusy()) {
            return;
        }
        ArrayList<Runnable> list = new ArrayList<Runnable>();
        for (class_1304 equipment : class_1304.values()) {
            class_1799 equipStack = AutoArmor.mc.field_1724.method_6118(equipment);
            if (equipment.method_5925() != class_1304.class_1305.field_6178 || equipment.equals((Object)class_1304.field_6174) && equipStack.method_7909().equals(class_1802.field_8833)) continue;
            int armorSlot = 8 - equipment.method_5927();
            class_1735 slot = PlayerInventoryUtil.getSlot(s -> {
                class_1799 stack = s.method_7677();
                class_10192 component = (class_10192)stack.method_58694(class_9334.field_54196);
                return s.field_7874 != armorSlot && !this.isBroken(stack) && !this.hasCurseOfBinding(stack) && component != null && component.comp_3174().equals((Object)equipment);
            }, Comparator.comparingDouble(s -> this.calculateArmorValue(s.method_7677())));
            if (slot != null && this.isBetter(slot.method_7677(), equipStack)) {
                list.add(() -> PlayerInventoryUtil.moveItem(slot, armorSlot));
                continue;
            }
            if (!this.isBroken(equipStack)) continue;
            Hud hud = Hud.getInstance();
            if (slot != null) {
                list.add(() -> PlayerInventoryUtil.moveItem(slot, armorSlot));
                if (!hud.state || !hud.notificationSettings.isSelected("Auto Armor")) continue;
                Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)("\u0420\u2014\u0420\u00b0\u0420\u0458\u0420\u00b5\u0420\u0405\u0420\u0451\u0420\u00bb - " + String.valueOf(class_124.field_1060) + this.equipName(equipment) + String.valueOf(class_124.field_1070) + " \u0420\u0405\u0420\u00b0 ")).method_10852(equipStack.method_7964()), 3000L);
                continue;
            }
            if (PlayerInventoryUtil.getSlot(class_1802.field_8162, s -> s.field_7874 >= 9) == null) continue;
            list.add(() -> PlayerInventoryUtil.clickSlot(armorSlot, 0, class_1713.field_7794, false));
            if (!hud.state || !hud.notificationSettings.isSelected("Auto Armor")) continue;
            Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)"\u0420\u2014\u0420\u00b0\u0421\u0403\u0420\u00b5\u0420\u2116\u0420\u0406\u0420\u0451\u0420\u00bb - ").method_10852(equipStack.method_7964()), 3000L);
        }
        if (!list.isEmpty()) {
            ScreenWalkInventorySync.run(() -> {
                list.forEach(Runnable::run);
                PlayerInventoryUtil.updateSlots();
            });
        }
    }

    private float calculateArmorValue(class_1799 stack) {
        class_2378 enchantments = AutoArmor.mc.field_1687.method_30349().method_30530(class_7924.field_41265);
        class_10192 component = (class_10192)stack.method_58694(class_9334.field_54196);
        class_9285 modifiers = (class_9285)stack.method_58695(class_9334.field_49636, (Object)class_9285.field_49326);
        class_1304 slot = component != null ? component.comp_3174() : class_1304.field_6173;
        float protection = (float)(modifiers.method_57481(class_5134.field_23724, 0.0, slot) + modifiers.method_57481(class_5134.field_23725, 0.0, slot) + (double)class_1890.method_8225((class_6880)((class_6880)enchantments.method_10223(class_1893.field_9111.method_29177()).orElseThrow()), (class_1799)stack));
        float unbreaking = class_1890.method_8225((class_6880)((class_6880)enchantments.method_10223(class_1893.field_9119.method_29177()).orElseThrow()), (class_1799)stack);
        float mending = class_1890.method_8225((class_6880)((class_6880)enchantments.method_10223(class_1893.field_9101.method_29177()).orElseThrow()), (class_1799)stack);
        return protection + unbreaking * 0.1f + mending * 0.2f;
    }

    private boolean hasCurseOfBinding(class_1799 stack) {
        class_6880 curseEntry = AutoArmor.mc.field_1687.method_30349().method_30530(class_7924.field_41265).method_10223(class_1893.field_9113.method_29177()).orElse(null);
        if (curseEntry == null) {
            return false;
        }
        return class_1890.method_8225((class_6880)curseEntry, (class_1799)stack) > 0;
    }

    private boolean isBroken(class_1799 stack) {
        if (stack.method_7936() <= 0) {
            return false;
        }
        return (double)stack.method_7919() / (double)stack.method_7936() > 0.98;
    }

    private boolean isBetter(class_1799 newArmor, class_1799 currentArmor) {
        if (currentArmor.method_7960()) {
            return true;
        }
        if (newArmor.method_58694(class_9334.field_54196) == null || currentArmor.method_58694(class_9334.field_54196) == null) {
            return false;
        }
        return this.calculateArmorValue(newArmor) > this.calculateArmorValue(currentArmor);
    }

    private String equipName(class_1304 equipmentSlot) {
        return switch (equipmentSlot) {
            case class_1304.field_6166 -> "\u0420\u2018\u0420\u0455\u0421\u201a\u0420\u0451\u0420\u0405\u0420\u0454\u0420\u0451";
            case class_1304.field_6172 -> "\u0420\u045f\u0420\u0455\u0420\u0405\u0420\u0455\u0420\u00b6\u0420\u0451";
            case class_1304.field_6174 -> "\u0420\u045c\u0420\u00b0\u0420\u0456\u0421\u0402\u0421\u0453\u0420\u0491\u0420\u0405\u0420\u0451\u0420\u0454";
            case class_1304.field_6169 -> "\u0420\u0401\u0420\u00bb\u0420\u00b5\u0420\u0458";
            default -> "\u0420\u201d\u0420\u2022\u0420\u2014\u0420\u2014 \u0420\u040e\u0420\u2019\u0420\u0098\u0420\u045c\u0420\u00ac\u0420\u0407 \u0420\u040e\u0420\u0408\u0420\u0459\u0420\u0452\u0420\u0452\u0420\u0452!";
        };
    }
}

