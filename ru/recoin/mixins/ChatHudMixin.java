/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_303
 *  net.minecraft.class_338
 *  net.minecraft.class_5250
 *  net.minecraft.class_7469
 *  net.minecraft.class_7591
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.recoin.mixins;

import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_338;
import net.minecraft.class_5250;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.recoin.implement.features.modules.misc.ChatHelper;

@Mixin(value={class_338.class})
public abstract class ChatHudMixin {
    @Shadow
    private List<class_303> field_2061;
    @Unique
    private boolean recoin$replacingSpamMessage;
    @Unique
    private String recoin$lastMessageKey;
    @Unique
    private class_2561 recoin$lastOriginalMessage;
    @Unique
    private int recoin$spamCount;

    @Shadow
    private void method_44813() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void recoin$combineSpam(class_2561 message, class_7469 signature, class_7591 indicator, CallbackInfo ci) {
        if (this.recoin$replacingSpamMessage) {
            return;
        }
        ChatHelper helper = ChatHelper.getInstance();
        if (helper == null || !helper.isAntiSpamEnabled()) {
            this.recoin$resetSpamState();
            return;
        }
        String key = message.getString();
        if (key.equals(this.recoin$lastMessageKey) && this.recoin$lastOriginalMessage != null && !this.field_2061.isEmpty()) {
            ++this.recoin$spamCount;
            this.field_2061.removeFirst();
            this.method_44813();
            class_5250 combined = this.recoin$lastOriginalMessage.method_27661().method_10852((class_2561)class_2561.method_43470((String)(" [x" + this.recoin$spamCount + "]")).method_27692(class_124.field_1080));
            this.recoin$replacingSpamMessage = true;
            try {
                ((class_338)(Object)this).method_44811((class_2561)combined, null, indicator);
            }
            finally {
                this.recoin$replacingSpamMessage = false;
            }
            ci.cancel();
            return;
        }
        this.recoin$lastMessageKey = key;
        this.recoin$lastOriginalMessage = message.method_27661();
        this.recoin$spamCount = 1;
    }

    @Inject(method={"method_1808"}, at={@At(value="HEAD")})
    private void recoin$resetSpamOnClear(boolean clearHistory, CallbackInfo ci) {
        this.recoin$resetSpamState();
    }

    @Unique
    private void recoin$resetSpamState() {
        this.recoin$lastMessageKey = null;
        this.recoin$lastOriginalMessage = null;
        this.recoin$spamCount = 0;
    }

    @ModifyConstant(method={"method_1815"}, constant={@Constant(intValue=100)}, require=0)
    private int recoin$visibleMessageLimit(int original) {
        ChatHelper helper = ChatHelper.getInstance();
        return helper != null && helper.isInfiniteChatEnabled() ? Integer.MAX_VALUE : original;
    }

    @ModifyConstant(method={"method_58744(Lnet/minecraft/class_303;)V"}, constant={@Constant(intValue=100)}, require=0)
    private int recoin$messageLimit(int original) {
        ChatHelper helper = ChatHelper.getInstance();
        return helper != null && helper.isInfiniteChatEnabled() ? Integer.MAX_VALUE : original;
    }
}

