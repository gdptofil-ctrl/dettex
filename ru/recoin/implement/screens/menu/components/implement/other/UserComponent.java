/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 */
package ru.recoin.implement.screens.menu.components.implement.other;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import ru.recoin.api.system.discord.DiscordManager;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.core.client.UserProfile;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class UserComponent
extends AbstractComponent {
    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        Matrix4f positionMatrix = matrix.method_23760().method_23761();
        DiscordManager discord = Main.getInstance().getDiscordManager();
        rectangle.render(ShapeProperties.create(matrix, this.x + 5.0f, this.y - 30.0f, 75.0, 25.0).round(4.0f).thickness(2.0f).softness(0.5f).outlineColor(ColorUtil.getOutline()).color(ColorUtil.getGuiRectColor(0.5f)).build());
        Render2DUtil.drawTexture(context, discord.getAvatarId(), this.x + 11.0f, this.y - 25.0f, 15.0f, 7.5f, 0, 15, 21, ColorUtil.getGuiRectColor(1.0f));
        rectangle.render(ShapeProperties.create(matrix, this.x + 21.5f, this.y - 15.5f, 5.0, 5.0).round(2.5f).color(ColorUtil.getGuiRectColor(1.0f)).build());
        rectangle.render(ShapeProperties.create(matrix, this.x + 22.5f, this.y - 14.5f, 3.0, 3.0).round(1.5f).color(-14236020).build());
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(positionMatrix, this.x + 5.5f, this.y - 29.5f, 74.0f, 22.0f);
        Fonts.getMenuSize(12).drawString(matrix, UserProfile.getUsername(), this.x + 30.0f, this.y - 21.0f, -2828575);
        Fonts.getMenuSize(10).drawGradientString(matrix, StringUtil.getUserRole(), this.x + 30.0f, (double)this.y - 14.5, ColorUtil.fade(0), ColorUtil.fade(60));
        scissor.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

