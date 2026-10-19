/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_636
 *  net.minecraft.class_638
 *  net.minecraft.class_7204
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package ru.recoin.mixins;

import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_7204;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_636.class})
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor(value="field_3715")
    public float getcurrentBreakingProgress();

    @Invoker(value="method_2911")
    public void invokeSyncSelectedSlot();

    @Invoker(value="method_41931")
    public void invokeSendSequencedPacket(class_638 var1, class_7204 var2);
}

