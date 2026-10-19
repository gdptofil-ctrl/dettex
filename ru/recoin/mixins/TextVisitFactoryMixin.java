/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_5223
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package ru.recoin.mixins;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_5223;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.recoin.api.event.EventManager;
import ru.recoin.implement.events.render.TextFactoryEvent;

@Mixin(value={class_5223.class})
public class TextVisitFactoryMixin {
    private static final Charset WINDOWS_1251 = Charset.forName("windows-1251");
    private static final Pattern TOKEN = Pattern.compile("\\S+");

    @ModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/class_5223;method_27473(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z", ordinal=0), method={"method_27472(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z"}, index=0)
    private static String adjustText(String text) {
        TextFactoryEvent event = new TextFactoryEvent(text);
        EventManager.callEvent(event);
        return TextVisitFactoryMixin.repairMixedMojibake(event.getText());
    }

    private static String repairMixedMojibake(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        Matcher matcher = TOKEN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        while (matcher.find()) {
            String token = matcher.group();
            matcher.appendReplacement(result, Matcher.quoteReplacement(TextVisitFactoryMixin.repairToken(token)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String repairToken(String token) {
        if (TextVisitFactoryMixin.mojibakeMarkers(token) < 2) {
            return token;
        }
        try {
            String decoded = new String(token.getBytes(WINDOWS_1251), StandardCharsets.UTF_8);
            if (decoded.indexOf(65533) < 0 && TextVisitFactoryMixin.mojibakeMarkers(decoded) < TextVisitFactoryMixin.mojibakeMarkers(token)) {
                return decoded;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return token;
    }

    private static int mojibakeMarkers(String text) {
        int markers = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c != '\u0420' && c != '\u0421' && c != '\u00c2') continue;
            ++markers;
        }
        return markers;
    }
}

