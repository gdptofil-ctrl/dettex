/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1667
 *  net.minecraft.class_1675
 *  net.minecraft.class_1676
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_1937
 *  net.minecraft.class_238
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_3966
 *  net.minecraft.class_638
 */
package ru.recoin.common.util.entity;

import net.minecraft.class_1297;
import net.minecraft.class_1667;
import net.minecraft.class_1675;
import net.minecraft.class_1676;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_638;
import ru.recoin.common.QuickImports;

public class SimulatedArrow
implements QuickImports {
    public final class_638 world;
    public class_243 pos;
    public class_243 velocity;
    public final boolean collideEntities;
    public boolean inGround = false;

    public SimulatedArrow(class_638 world, class_243 pos, class_243 velocity) {
        this(world, pos, velocity, true);
    }

    public SimulatedArrow(class_638 world, class_243 pos, class_243 velocity, boolean collideEntities) {
        this.world = world;
        this.pos = pos;
        this.velocity = velocity;
        this.collideEntities = collideEntities;
    }

    public class_239 tick() {
        if (this.inGround) {
            return null;
        }
        class_243 newPos = this.pos.method_1019(this.velocity);
        double drag = this.isTouchingWater() ? 0.6 : 0.99;
        this.velocity = this.velocity.method_1021(drag);
        this.velocity = new class_243(this.velocity.field_1352, this.velocity.field_1351 - (double)0.05f, this.velocity.field_1350);
        class_239 hitResult = this.updateCollision(this.pos, newPos);
        if (hitResult != null) {
            this.pos = hitResult.method_17784();
            this.inGround = true;
            return hitResult;
        }
        this.pos = newPos;
        return null;
    }

    private class_239 updateCollision(class_243 pos, class_243 newPos) {
        double size;
        class_3966 entityHitResult;
        class_638 world = this.world;
        class_1667 arrowEntity = new class_1667((class_1937)this.world, this.pos.field_1352, this.pos.field_1351, this.pos.field_1350, new class_1799((class_1935)class_1802.field_8107), null);
        class_3965 blockHitResult = world.method_17742(new class_3959(pos, newPos, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)arrowEntity));
        if (this.collideEntities && (entityHitResult = class_1675.method_18077((class_1937)this.world, (class_1676)arrowEntity, (class_243)pos, (class_243)newPos, (class_238)new class_238(-(size = 0.45), -size, -size, size, size, size).method_997(pos).method_18804(newPos.method_1020(pos)).method_1014(1.0), entity -> {
            if (!entity.method_7325() && entity.method_5805() && (entity.method_5863() || entity != SimulatedArrow.mc.field_1724 && entity == arrowEntity)) {
                return !arrowEntity.method_5794(entity);
            }
            return false;
        })) != null && entityHitResult.method_17783() != class_239.class_240.field_1333) {
            return entityHitResult;
        }
        if (blockHitResult != null && blockHitResult.method_17783() != class_239.class_240.field_1333) {
            return blockHitResult;
        }
        return null;
    }

    private boolean isTouchingWater() {
        return false;
    }
}

