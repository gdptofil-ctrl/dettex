/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.api.feature.draggable;

import net.minecraft.class_332;
import ru.recoin.api.feature.draggable.Draggable;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.common.QuickImports;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.core.Main;
import ru.recoin.implement.events.container.SetScreenEvent;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.features.modules.render.Hud;

public abstract class AbstractDraggable
implements Draggable,
QuickImports,
QuickLogger {
    private String name;
    private int x;
    private int y;
    private int width;
    private int height;
    private int preferredX;
    private int preferredY;
    private float elementScale = 1.0f;
    private boolean dragging;
    private boolean canDrag;
    private float dragVisualX;
    private float dragVisualY;
    public final Animation scaleAnimation = new DecelerateAnimation().setValue(1.0).setMs(450);
    private float animatedElementScale = 1.0f;
    private long lastScaleAnimTime = 0L;

    public AbstractDraggable(String name, int x, int y, int width, int height, boolean canDrag) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.preferredX = x;
        this.preferredY = y;
        this.width = width;
        this.height = height;
        this.canDrag = canDrag;
    }

    @Override
    public boolean visible() {
        return true;
    }

    @Override
    public void tick() {
    }

    @Override
    public void packet(PacketEvent e) {
    }

    public void attack(AttackEvent e) {
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        Hud hud = Hud.getInstance();
        float scale = this.getRenderedScale();
        int windowWidth = window.getScaledWidth();
        int windowHeight = window.getScaledHeight();
        int radius = 3;
        float extraWidth = (float)this.width * (scale - 1.0f) / 2.0f;
        float extraHeight = (float)this.height * (scale - 1.0f) / 2.0f;
        float visualLeft = (float)mouseX + this.dragVisualX;
        float visualTop = (float)mouseY + this.dragVisualY;
        float mouseDragX = visualLeft + extraWidth;
        float mouseDragY = visualTop + extraHeight;
        if (this.canDragX()) {
            this.x = Math.round(Math.max(-extraWidth, Math.min(mouseDragX, (float)(windowWidth - this.width) + extraWidth)));
        }
        if (this.canDragY()) {
            this.y = Math.round(Math.max(-extraHeight, Math.min(mouseDragY, (float)(windowHeight - this.height) + extraHeight)));
        }
        for (AbstractDraggable drag : Main.getInstance().getDraggableRepository().draggable()) {
            if (!drag.canDraw(hud, drag) || !drag.canDrag || drag == this) continue;
            int x1 = drag.x + drag.width + radius;
            int x2 = drag.x - this.width - radius;
            int y1 = drag.y + drag.height + radius;
            int y2 = drag.y - this.height - radius;
            int y3 = drag.y;
            if (this.canDragX() && Math.abs((float)x1 - mouseDragX) <= (float)radius) {
                this.drawRect(context, (float)x1 - 0.5f, 0.0f, 0.5f, windowHeight);
                this.x = x1;
            }
            if (this.canDragX() && Math.abs((float)x2 - mouseDragX) <= (float)radius) {
                this.drawRect(context, (float)(x2 + this.width) + 0.5f, 0.0f, 0.5f, windowHeight);
                this.x = x2;
            }
            if (this.canDragY() && Math.abs((float)y1 - mouseDragY) <= (float)radius) {
                this.drawRect(context, 0.0f, (float)y1 - 0.5f, windowWidth, 0.5f);
                this.y = y1;
            }
            if (this.canDragY() && Math.abs((float)y2 - mouseDragY) <= (float)radius) {
                this.drawRect(context, 0.0f, (float)(y2 + this.height) + 0.5f, windowWidth, 0.5f);
                this.y = y2;
            }
            if (!this.canDragY() || !(Math.abs((float)y3 - mouseDragY) <= (float)radius)) continue;
            this.drawRect(context, 0.0f, (float)y3 - 0.5f, windowWidth, 0.5f);
            this.y = y3;
        }
        if (this.canDragX() && Math.abs(this.x + (this.width - windowWidth) / 2) <= radius) {
            this.drawRect(context, (float)windowWidth / 2.0f - 0.25f, 0.0f, 0.5f, windowHeight);
            this.x = (windowWidth - this.width) / 2;
        }
        if (this.canDragY() && Math.abs(this.y + (this.height - windowHeight) / 2) <= radius) {
            this.drawRect(context, 0.0f, (float)windowHeight / 2.0f - 0.25f, windowWidth, 0.5f);
            this.y = (windowHeight - this.height) / 2;
        }
        this.preferredX = this.x;
        this.preferredY = this.y;
    }

    @Override
    public void setScreen(SetScreenEvent e) {
        if (PlayerIntersectionUtil.isChat(e.getScreen())) {
            this.dragging = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered(mouseX, mouseY) && button == 0 && (this.canDragX() || this.canDragY())) {
            this.dragging = true;
            float scale = this.getRenderedScale();
            float extraWidth = (float)this.width * (scale - 1.0f) / 2.0f;
            float extraHeight = (float)this.height * (scale - 1.0f) / 2.0f;
            this.dragVisualX = (float)this.x - extraWidth - (float)mouseX;
            this.dragVisualY = (float)this.y - extraHeight - (float)mouseY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        return true;
    }

    public abstract void drawDraggable(class_332 var1);

    public boolean canDragX() {
        return this.canDrag;
    }

    public boolean canDragY() {
        return this.canDrag;
    }

    public void drawRect(float x, float y, float width, float height) {
        Render2DUtil.drawQuad(x, y, width, height, ColorUtil.getText(0.5f));
    }

    public void drawRect(class_332 context, float x, float y, float width, float height) {
        int left = Math.round(x);
        int top = Math.round(y);
        int right = left + Math.max(1, Math.round(width));
        int bottom = top + Math.max(1, Math.round(height));
        context.method_25294(left, top, right, bottom, -855638017);
    }

    public void stopAnimation() {
        this.scaleAnimation.setDirection(Direction.BACKWARDS);
    }

    public void startAnimation() {
        this.scaleAnimation.setDirection(Direction.FORWARDS);
    }

    public void validPosition() {
        this.x = this.preferredX;
        this.y = this.preferredY;
        if (!window.isAvailable()) {
            return;
        }
        float scale = this.getRenderedScale();
        float extraWidth = (float)this.width * (scale - 1.0f) / 2.0f;
        float extraHeight = (float)this.height * (scale - 1.0f) / 2.0f;
        this.x = Math.round(Math.max(-extraWidth, Math.min((float)this.x, (float)(window.getScaledWidth() - this.width) + extraWidth)));
        this.y = Math.round(Math.max(-extraHeight, Math.min((float)this.y, (float)(window.getScaledHeight() - this.height) + extraHeight)));
    }

    public void setX(int x) {
        this.x = x;
        this.preferredX = x;
    }

    public void setY(int y) {
        this.y = y;
        this.preferredY = y;
    }

    public int getPreferredX() {
        return this.preferredX;
    }

    public int getPreferredY() {
        return this.preferredY;
    }

    public void rescalePosition(int oldWidth, int oldHeight, int newWidth, int newHeight) {
        if (oldWidth <= 0 || oldHeight <= 0 || newWidth <= 0 || newHeight <= 0) {
            this.validPosition();
            return;
        }
        this.x = Math.round((float)this.x * ((float)newWidth / (float)oldWidth));
        this.y = Math.round((float)this.y * ((float)newHeight / (float)oldHeight));
        this.validPosition();
    }

    public boolean isHovered(double mouseX, double mouseY) {
        float scale = this.getRenderedScale();
        double extraWidth = (float)this.width * (scale - 1.0f) / 2.0f;
        double extraHeight = (float)this.height * (scale - 1.0f) / 2.0f;
        double left = (double)this.x - extraWidth;
        double top = (double)this.y - extraHeight;
        return mouseX >= left && mouseX <= left + (double)((float)this.width * scale) && mouseY >= top && mouseY <= top + (double)((float)this.height * scale);
    }

    public boolean isCloseAnimationFinished() {
        return this.scaleAnimation.isFinished(Direction.BACKWARDS);
    }

    public float getRenderedScale() {
        long time = System.currentTimeMillis();
        if (this.lastScaleAnimTime == 0L) {
            this.lastScaleAnimTime = time;
            this.animatedElementScale = this.elementScale;
        } else {
            long dt = Math.min(50L, time - this.lastScaleAnimTime);
            this.lastScaleAnimTime = time;
            float factor = (float)(1.0 - Math.exp(-0.018 * (double)dt));
            this.animatedElementScale += (this.elementScale - this.animatedElementScale) * factor;
        }
        return Hud.getInstance().getRenderedHudScale() * this.animatedElementScale;
    }

    public void setElementScale(float scale) {
        this.elementScale = Math.max(0.5f, Math.min(2.0f, (float)Math.round(scale * 20.0f) / 20.0f));
        this.validPosition();
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public boolean canDraw(Hud hud, AbstractDraggable draggable) {
        return hud.isState() && hud.interfaceSettings.isSelected(draggable.getName()) && this.visible();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPreferredX(int preferredX) {
        this.preferredX = preferredX;
    }

    public void setPreferredY(int preferredY) {
        this.preferredY = preferredY;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public void setCanDrag(boolean canDrag) {
        this.canDrag = canDrag;
    }

    public void setDragVisualX(float dragVisualX) {
        this.dragVisualX = dragVisualX;
    }

    public void setDragVisualY(float dragVisualY) {
        this.dragVisualY = dragVisualY;
    }

    public void setAnimatedElementScale(float animatedElementScale) {
        this.animatedElementScale = animatedElementScale;
    }

    public void setLastScaleAnimTime(long lastScaleAnimTime) {
        this.lastScaleAnimTime = lastScaleAnimTime;
    }

    public String getName() {
        return this.name;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public float getElementScale() {
        return this.elementScale;
    }

    public boolean isCanDrag() {
        return this.canDrag;
    }

    public float getDragVisualX() {
        return this.dragVisualX;
    }

    public float getDragVisualY() {
        return this.dragVisualY;
    }

    public Animation getScaleAnimation() {
        return this.scaleAnimation;
    }

    public float getAnimatedElementScale() {
        return this.animatedElementScale;
    }

    public long getLastScaleAnimTime() {
        return this.lastScaleAnimTime;
    }
}

