/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_1304
 *  net.minecraft.class_1306
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.draggables;

import java.util.Objects;
import net.minecraft.class_10799;
import net.minecraft.class_1304;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.Hud;

public class Armor
extends AbstractDraggable {
    public Armor() {
        super("Armor", 0, 0, 82, 22, false);
    }

    @Override
    public boolean visible() {
        for (class_1799 stack : EntityCompat.getArmorItems((class_1309)Armor.mc.field_1724)) {
            if (stack.method_7960()) continue;
            return true;
        }
        return false;
    }

    @Override
    public void drawDraggable(class_332 context) {
        int handX;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        int n = handX = Objects.requireNonNull(Armor.mc.field_1724).method_6068().equals((Object)class_1306.field_6183) ? 100 : -187;
        if (Hud.getInstance().interfaceSettings.isSelected("HotBar")) {
            this.setX(context.method_51421() / 2 + handX);
            this.setY(context.method_51443() - 27);
            blur.render(ShapeProperties.create(matrix, (float)this.getX() - 0.5f, (float)this.getY() - 0.5f, this.getWidth() + 1, 23.0).round(5.0f).thickness(0.0f).softness(5.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
            this.drawArmorStacks(context, 3, 3);
        } else {
            this.setX(context.method_51421() / 2 + handX);
            this.setY(context.method_51443() - 19);
            int width = 41;
            context.method_44379(this.getX() - 3, this.getY() - 3, this.getX() - 3 + width, this.getY() - 3 + 22);
            context.method_70846(class_10799.field_56883, class_329.field_45310, 182, 22, 0, 0, this.getX() - 3, this.getY() - 3, 182, 22);
            context.method_44380();
            context.method_44379(this.getX() + width - 3, this.getY() - 3, this.getX() + width - 3 + width, this.getY() - 3 + 22);
            context.method_70846(class_10799.field_56883, class_329.field_45310, 182, 22, 0, 0, this.getX() - 103, this.getY() - 3, 182, 22);
            context.method_44380();
            this.drawArmorStacks(context, 0, 0);
        }
    }

    public void drawArmorStacks(class_332 context, int offsetX, int offsetY) {
        class_1304[] armorSlots;
        if (Armor.mc.field_1724 == null) {
            return;
        }
        for (class_1304 slot : armorSlots = new class_1304[]{class_1304.field_6166, class_1304.field_6172, class_1304.field_6174, class_1304.field_6169}) {
            class_1799 itemStack = Armor.mc.field_1724.method_6118(slot);
            context.method_51428(itemStack, this.getX() + offsetX, this.getY() + offsetY, 1);
            context.method_51431(Armor.mc.field_1772, itemStack, this.getX() + offsetX, this.getY() + offsetY);
            offsetX += 20;
        }
    }
}

