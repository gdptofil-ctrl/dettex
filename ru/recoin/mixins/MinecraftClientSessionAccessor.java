/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_276
 *  net.minecraft.class_310
 *  net.minecraft.class_320
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package ru.recoin.mixins;

import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_320;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_310.class})
public interface MinecraftClientSessionAccessor {
    @Mutable
    @Accessor(value="field_1726")
    public void recoin$setSession(class_320 var1);

    @Mutable
    @Accessor(value="field_1752")
    public void recoin$setItemUseCooldown(int var1);

    @Mutable
    @Accessor(value="field_1689")
    public void recoin$setFramebuffer(class_276 var1);

    @Invoker(value="method_1536")
    public boolean recoin$doAttack();

    @Invoker(value="method_1583")
    public void recoin$doItemUse();

    @Accessor(value="field_1771")
    public int recoin$getAttackCooldown();

    @Accessor(value="field_1771")
    public void recoin$setAttackCooldown(int var1);
}

