/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_287
 *  net.minecraft.class_4587$class_4665
 *  org.joml.Matrix4f
 *  org.joml.Vector4i
 */
package ru.recoin.api.system.draw;

import net.minecraft.class_287;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Vector4i;

public interface DrawEngine {
    public void quad(Matrix4f var1, class_287 var2, float var3, float var4, float var5, float var6);

    public void quad(Matrix4f var1, class_287 var2, float var3, float var4, float var5, float var6, int var7);

    public void quadTexture(class_4587.class_4665 var1, class_287 var2, float var3, float var4, float var5, float var6, Vector4i var7);

    public void quad(Matrix4f var1, float var2, float var3, float var4, float var5, int var6);
}

