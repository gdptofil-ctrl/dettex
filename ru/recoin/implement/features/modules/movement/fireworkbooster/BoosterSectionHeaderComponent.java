/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.implement.features.modules.movement.fireworkbooster;

import net.minecraft.class_332;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.movement.fireworkbooster.BoosterSectionHeaderSetting;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;

public final class BoosterSectionHeaderComponent
extends AbstractSettingComponent {
    public BoosterSectionHeaderComponent(BoosterSectionHeaderSetting setting) {
        super(setting);
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        this.height = 22.0f;
        Fonts.getMenuSize(11, Fonts.Type.INTER_MEDIUM).drawString(MatrixStackCompat.from(context.method_51448()), this.getSetting().getName(), this.x + 20.0f, this.y + 7.0f, -1315861);
    }
}

