/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWImage
 *  org.lwjgl.glfw.GLFWImage$Buffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 */
package ru.recoin.common.util.other;

import com.google.common.collect.Maps;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import ru.recoin.common.QuickImports;

public final class BufferUtil
implements QuickImports {
    private static final Map<String, Integer> dynamicIdCounters = Maps.newHashMap();

    public static class_1043 getHeadFromURL(String url) throws IOException {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return new class_1043(() -> "recoin_head", BufferUtil.parseHead(class_1011.method_4309((InputStream)new URL(url).openStream())));
    }

    public static class_1011 parseHead(class_1011 image) {
        int imageHeight;
        if (image == null) {
            return null;
        }
        int imageWidth = 22;
        int imageSrcWidth = image.method_4307();
        int srcHeight = image.method_4323();
        int imageSrcHeight = image.method_4323();
        for (imageHeight = 22; imageWidth < imageSrcWidth || imageHeight < imageSrcHeight; imageWidth *= 2, imageHeight *= 2) {
        }
        class_1011 imgNew = new class_1011(imageWidth, imageHeight, true);
        for (int x = 0; x < imageSrcWidth; ++x) {
            for (int y = 0; y < srcHeight; ++y) {
                imgNew.method_61941(x, y, image.method_61940(x, y));
            }
        }
        image.close();
        return imgNew;
    }

    public static void registerBufferedImageTexture(class_2960 i, BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)bi, "png", baos);
            byte[] bytes = baos.toByteArray();
            BufferUtil.registerTexture(i, bytes);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void registerTexture(class_2960 i, byte[] content) {
        try {
            ByteBuffer data = BufferUtils.createByteBuffer((int)content.length).put(content);
            data.flip();
            class_1043 tex = new class_1043(() -> "recoin_image", class_1011.method_4324((ByteBuffer)data));
            mc.execute(() -> mc.method_1531().method_4616(i, (class_1044)tex));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void setWindowIcon(InputStream img16x16, InputStream img32x32) {
        try (MemoryStack memorystack = MemoryStack.stackPush();){
            GLFWImage.Buffer buffer = GLFWImage.malloc((int)2, (MemoryStack)memorystack);
            List<InputStream> imgList = List.of(img16x16, img32x32);
            ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
            for (int i = 0; i < imgList.size(); ++i) {
                class_1011 nativeImage = class_1011.method_4309((InputStream)imgList.get(i));
                ByteBuffer bytebuffer = MemoryUtil.memAlloc((int)(nativeImage.method_4307() * nativeImage.method_4323() * 4));
                bytebuffer.asIntBuffer().put(nativeImage.method_48463());
                buffer.position(i);
                buffer.width(nativeImage.method_4307());
                buffer.height(nativeImage.method_4323());
                buffer.pixels(bytebuffer);
                buffers.add(bytebuffer);
            }
            if (GLFW.glfwGetPlatform() != 393219) {
                GLFW.glfwSetWindowIcon((long)class_310.method_1551().method_22683().method_4490(), (GLFWImage.Buffer)buffer);
            }
            buffers.forEach(MemoryUtil::memFree);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static class_2960 registerDynamicTexture(String prefix, class_1043 texture) {
        Integer integer = dynamicIdCounters.get(prefix);
        integer = integer == null ? Integer.valueOf(1) : Integer.valueOf(integer + 1);
        dynamicIdCounters.put(prefix, integer);
        class_2960 identifier = class_2960.method_60656((String)String.format(Locale.ROOT, "dynamic/%s_%d", prefix, integer));
        mc.method_1531().method_4616(identifier, (class_1044)texture);
        return identifier;
    }

    private BufferUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

