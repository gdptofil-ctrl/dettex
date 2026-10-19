/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10442
 *  net.minecraft.class_10444
 *  net.minecraft.class_11566
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1937
 *  net.minecraft.class_310
 *  net.minecraft.class_3489
 *  net.minecraft.class_811
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package ru.recoin.mixins;

import net.minecraft.class_10442;
import net.minecraft.class_10444;
import net.minecraft.class_11566;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.recoin.implement.features.modules.render.CustomMace;
import ru.recoin.implement.features.modules.render.CustomSwords;
import ru.recoin.implement.features.modules.render.cosmetics.CosmeticWeaponModels;
import ru.recoin.implement.features.modules.render.cosmetics.Cosmetics;

@Mixin(value={class_10442.class})
public abstract class ItemModelResolverMixin {
    @ModifyVariable(method={"method_65596(Lnet/minecraft/class_10444;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_1937;Lnet/minecraft/class_11566;I)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0, require=1)
    private class_1799 recoin_replaceWeaponModel(class_1799 stack, class_10444 renderState, class_1799 originalStack, class_811 displayContext, class_1937 world, class_11566 itemOwner, int seed) {
        if (stack == null || stack.method_7960()) {
            return stack;
        }
        if (CosmeticWeaponModels.isCosmeticModel(stack)) {
            return stack;
        }
        boolean isMace = stack.method_31574(class_1802.field_49814);
        boolean isSword = ItemModelResolverMixin.isSword(stack);
        boolean isCrossbow = stack.method_31574(class_1802.field_8399);
        if (!(isMace || isSword || isCrossbow)) {
            return stack;
        }
        boolean isSelf = itemOwner == null || itemOwner.method_72393() == null || itemOwner.method_72393() == class_310.method_1551().field_1724;
        Cosmetics cosmetics = Cosmetics.getInstance();
        if (cosmetics != null && cosmetics.isState()) {
            if (isCrossbow && cosmetics.getCrossbowsEnabled().isValue() && (!cosmetics.getCrossbowsSelfOnly().isValue() || isSelf)) {
                return CosmeticWeaponModels.applyCrossbow(stack, cosmetics.getCrossbowModel().getSelected());
            }
            if (isSword && cosmetics.getSwordsEnabled().isValue() && (!cosmetics.getSwordsSelfOnly().isValue() || isSelf)) {
                return CosmeticWeaponModels.apply(stack, cosmetics.getActualSword(), false);
            }
            if (isMace && cosmetics.getMacesEnabled().isValue() && (!cosmetics.getMacesSelfOnly().isValue() || isSelf)) {
                return CosmeticWeaponModels.apply(stack, cosmetics.getActualMace(), true);
            }
        }
        if (isCrossbow) {
            return stack;
        }
        CustomSwords swords = CustomSwords.getInstance();
        if (isSword && swords != null && swords.isState()) {
            if (!swords.isSelfOnly() || isSelf) {
                return CosmeticWeaponModels.apply(stack, swords.getSelectedWeapon(), false);
            }
        }
        CustomMace mace = CustomMace.getInstance();
        if (isMace && mace != null && mace.isState()) {
            if (!mace.isSelfOnly() || isSelf) {
                return CosmeticWeaponModels.apply(stack, mace.getSelectedWeapon(), true);
            }
        }
        return stack;
    }

    private static boolean isSword(class_1799 stack) {
        return stack.method_31573(class_3489.field_42611);
    }
}

