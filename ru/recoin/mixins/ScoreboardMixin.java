/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import net.minecraft.class_268;
import net.minecraft.class_269;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_269.class})
public class ScoreboardMixin {
    @Inject(method={"method_1157"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRemoveScoreHolderFromTeam(String playerName, class_268 team, CallbackInfo ci) {
        class_269 scoreboard = (class_269)(Object)this;
        class_268 currentTeam = scoreboard.method_1164(playerName);
        if (currentTeam != team) {
            ci.cancel();
        }
    }
}

