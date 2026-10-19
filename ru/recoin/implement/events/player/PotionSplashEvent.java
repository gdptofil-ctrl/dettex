/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1686
 *  net.minecraft.class_1799
 *  net.minecraft.class_239
 *  net.minecraft.class_243
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_1686;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_243;
import ru.recoin.api.event.events.Event;

public class PotionSplashEvent
implements Event {
    private final class_1686 potion;
    private final class_1799 stack;
    private final class_243 impactPos;
    private final class_239 hitResult;

    public class_1686 getPotion() {
        return this.potion;
    }

    public class_1799 getStack() {
        return this.stack;
    }

    public class_243 getImpactPos() {
        return this.impactPos;
    }

    public class_239 getHitResult() {
        return this.hitResult;
    }

    public PotionSplashEvent(class_1686 potion, class_1799 stack, class_243 impactPos, class_239 hitResult) {
        this.potion = potion;
        this.stack = stack;
        this.impactPos = impactPos;
        this.hitResult = hitResult;
    }
}

