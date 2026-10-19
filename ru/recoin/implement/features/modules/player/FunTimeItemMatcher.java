/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1320
 *  net.minecraft.class_1322$class_1323
 *  net.minecraft.class_1799
 *  net.minecraft.class_1844
 *  net.minecraft.class_2561
 *  net.minecraft.class_5134
 *  net.minecraft.class_6880
 *  net.minecraft.class_9285
 *  net.minecraft.class_9285$class_9287
 *  net.minecraft.class_9304
 *  net.minecraft.class_9334
 */
package ru.recoin.implement.features.modules.player;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1320;
import net.minecraft.class_1322;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_2561;
import net.minecraft.class_5134;
import net.minecraft.class_6880;
import net.minecraft.class_9285;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import ru.recoin.implement.features.modules.player.AutoBuy;

final class FunTimeItemMatcher {
    private static final Map<String, List<AttributeRule>> ATTRIBUTES = Map.ofEntries(Map.entry("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23716, 4.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 3.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23725, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23724, 2.0, class_1322.class_1323.field_6328))), Map.entry("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0420\u0430\u0437\u0434\u043e\u0440\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 4.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23719, 0.10000001817743467, class_1322.class_1323.field_6330), new AttributeRule((class_6880<class_1320>)class_5134.field_23723, 0.10000001817743467, class_1322.class_1323.field_6330), new AttributeRule((class_6880<class_1320>)class_5134.field_23724, -3.0, class_1322.class_1323.field_6328))), Map.entry("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0422\u0438\u0440\u0430\u043d\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23724, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, -4.0, class_1322.class_1323.field_6328))), Map.entry("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u042f\u0440\u043e\u0441\u0442\u0438", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 5.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, -4.0, class_1322.class_1323.field_6328))), Map.entry("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0412\u0438\u0445\u0440\u044f", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23716, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23719, 0.1500000347540806, class_1322.class_1323.field_6330), new AttributeRule((class_6880<class_1320>)class_5134.field_23723, 0.1500000347540806, class_1322.class_1323.field_6330))), Map.entry("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041c\u0440\u0430\u043a\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23724, 1.5, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, 1.5, class_1322.class_1323.field_6328))), Map.entry("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0414\u0435\u043c\u043e\u043d\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 2.5, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23723, 0.10000001817743467, class_1322.class_1323.field_6330))), Map.entry("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 7.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, -4.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23719, 0.10000001817743467, class_1322.class_1323.field_6330))), Map.entry("\u0421\u0444\u0435\u0440\u0430 \u0425\u0430\u043e\u0441\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23716, -4.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23724, 1.5, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 2.5, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23719, 0.07000004566537438, class_1322.class_1323.field_6330), new AttributeRule((class_6880<class_1320>)class_5134.field_23723, 0.13000000024761527, class_1322.class_1323.field_6330), new AttributeRule((class_6880<class_1320>)class_5134.field_49078, 0.09000001839414021, class_1322.class_1323.field_6330))), Map.entry("\u0421\u0444\u0435\u0440\u0430 \u0421\u0430\u0442\u0438\u0440\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23728, -0.10000000768432185, class_1322.class_1323.field_6330), new AttributeRule((class_6880<class_1320>)class_5134.field_23723, 0.1500000347540806, class_1322.class_1323.field_6330))), Map.entry("\u0421\u0444\u0435\u0440\u0430 \u0411\u0435\u0441\u0442\u0438\u0438", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23724, 1.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, 4.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23719, 0.10000001817743467, class_1322.class_1323.field_6330), new AttributeRule((class_6880<class_1320>)class_5134.field_23723, 0.10000001817743467, class_1322.class_1323.field_6330))), Map.entry("\u0421\u0444\u0435\u0440\u0430 \u0410\u0440\u0435\u0441\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 6.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23724, -2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, -2.0, class_1322.class_1323.field_6328))), Map.entry("\u0421\u0444\u0435\u0440\u0430 \u0413\u0438\u0434\u0440\u044b", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23716, 4.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23724, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_51576, 0.5, class_1322.class_1323.field_6330), new AttributeRule((class_6880<class_1320>)class_5134.field_51583, 0.5, class_1322.class_1323.field_6330))), Map.entry("\u0421\u0444\u0435\u0440\u0430 \u0418\u043a\u0430\u0440\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23721, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, 2.0, class_1322.class_1323.field_6328))), Map.entry("\u0421\u0444\u0435\u0440\u0430 \u042d\u0440\u0438\u0434\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23726, 1.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23716, 2.0, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_47758, 1.0, class_1322.class_1323.field_6328))), Map.entry("\u0421\u0444\u0435\u0440\u0430 \u0422\u0438\u0442\u0430\u043d\u0430", List.of(new AttributeRule((class_6880<class_1320>)class_5134.field_23724, 2.5, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23725, 2.5, class_1322.class_1323.field_6328), new AttributeRule((class_6880<class_1320>)class_5134.field_23719, -0.15000007945819008, class_1322.class_1323.field_6330))));
    private static final Map<String, List<TextRule>> DESCRIPTIONS = Map.ofEntries(Map.entry("\u0422\u0440\u0430\u043f\u043a\u0430", List.of(new TextRule("\u041a\u0430\u0441\u0442: \u041d\u0435\u0440\u0443\u0448\u0438\u043c\u0430\u044f \u043a\u043b\u0435\u0442\u043a\u0430", 0, false))), Map.entry("\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c", List.of(new TextRule("\u041a\u0430\u0441\u0442: \u0421\u0432\u0435\u0442\u043e\u0432\u0430\u044f \u0432\u0441\u043f\u044b\u0448\u043a\u0430", 0, false))), Map.entry("\u0421\u043d\u0435\u0436\u043e\u043a \u0437\u0430\u043c\u043e\u0440\u043e\u0437\u043a\u0430", List.of(new TextRule("\u041a\u0430\u0441\u0442: \u041b\u0435\u0434\u044f\u043d\u0430\u044f \u0441\u0444\u0435\u0440\u0430", 0, false))), Map.entry("\u0411\u043e\u0436\u044c\u044f \u0430\u0443\u0440\u0430", List.of(new TextRule("\u041a\u0430\u0441\u0442: \u0411\u043e\u0436\u0435\u0441\u0442\u0432\u0435\u043d\u043d\u0430\u044f \u0430\u0443\u0440\u0430", 0, false))), Map.entry("\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f", List.of(new TextRule("\u041a\u0430\u0441\u0442: \u0417\u0432\u0443\u043a\u043e\u0432\u0430\u044f \u0432\u043e\u043b\u043d\u0430", 0, false))), Map.entry("\u041f\u043b\u0430\u0441\u0442", List.of(new TextRule("\u041a\u0430\u0441\u0442: \u041d\u0435\u0440\u0443\u0448\u0438\u043c\u0430\u044f \u0441\u0442\u0435\u043d\u0430", 0, false))), Map.entry("\u041c\u0435\u0447 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new TextRule("[\u2605] \u041e\u0440\u0438\u0433\u0438\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", 0, false), new TextRule("\u041e\u043f\u044b\u0442\u043d\u044b\u0439", 3, false), new TextRule("\u0412\u0430\u043c\u043f\u0438\u0440\u0438\u0437\u043c", 2, false), new TextRule("\u041e\u043a\u0438\u0441\u043b\u0435\u043d\u0438\u0435", 2, false), new TextRule("\u042f\u0434", 3, false), new TextRule("\u0414\u0435\u0442\u0435\u043a\u0446\u0438\u044f", 3, false))), Map.entry("\u0411\u0443\u043b\u0430\u0432\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new TextRule("[\u2605] \u041e\u0440\u0438\u0433\u0438\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", 0, false), new TextRule("\u041e\u043f\u044b\u0442\u043d\u044b\u0439", 3, false), new TextRule("\u0412\u0430\u043c\u043f\u0438\u0440\u0438\u0437\u043c", 2, false), new TextRule("\u041e\u043a\u0438\u0441\u043b\u0435\u043d\u0438\u0435", 2, false), new TextRule("\u042f\u0434", 3, false), new TextRule("\u0414\u0435\u0442\u0435\u043a\u0446\u0438\u044f", 3, false))), Map.entry("\u0422\u0440\u0435\u0437\u0443\u0431\u0435\u0446 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new TextRule("[\u2605] \u041e\u0440\u0438\u0433\u0438\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", 0, false), new TextRule("\u0421\u043a\u0430\u0443\u0442", 3, false), new TextRule("\u0421\u0442\u0443\u043f\u043e\u0440", 3, false), new TextRule("\u041f\u0440\u0438\u0442\u044f\u0436\u0435\u043d\u0438\u0435", 2, false), new TextRule("\u0412\u043e\u0437\u0432\u0440\u0430\u0449\u0435\u043d\u0438\u0435", 0, false), new TextRule("\u041f\u043e\u0434\u0440\u044b\u0432\u043d\u0438\u043a", 0, false))), Map.entry("\u041a\u0438\u0440\u043a\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new TextRule("[\u2605] \u041e\u0440\u0438\u0433\u0438\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", 0, false), new TextRule("\u0411\u0443\u043b\u044c\u0434\u043e\u0437\u0435\u0440", 2, false), new TextRule("\u041e\u043f\u044b\u0442\u043d\u044b\u0439", 3, false), new TextRule("\u041c\u0430\u0433\u043d\u0438\u0442", 0, false), new TextRule("\u0410\u0432\u0442\u043e-\u041f\u043b\u0430\u0432\u043a\u0430", 0, false), new TextRule("\u041f\u0430\u0443\u0442\u0438\u043d\u0430", 0, false), new TextRule("\u041f\u0438\u043d\u0433\u0435\u0440", 0, false))), Map.entry("\u0410\u0440\u0431\u0430\u043b\u0435\u0442 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new TextRule("[\u2605] \u041e\u0440\u0438\u0433\u0438\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", 0, false))), Map.entry("\u041e\u0442\u043c\u044b\u0447\u043a\u0430 \u043a \u0421\u0444\u0435\u0440\u0430\u043c", List.of(new TextRule("\u0421 \u0421\u0444\u0435\u0440\u0430\u043c\u0438", 0, false))), Map.entry("\u0411\u043e\u0436\u044c\u0435 \u043a\u0430\u0441\u0430\u043d\u0438\u0435", List.of(new TextRule("\u041c\u043e\u0436\u0435\u0442 \u0434\u043e\u0431\u044b\u0442\u044c \u0441\u043f\u0430\u0432\u043d\u0435\u0440", 0, false))), Map.entry("\u041c\u043e\u0449\u043d\u044b\u0439 \u0443\u0434\u0430\u0440", List.of(new TextRule("\u041c\u043e\u0436\u0435\u0442 \u0440\u0430\u0437\u0440\u0443\u0448\u0438\u0442\u044c \u0431\u0435\u0434\u0440\u043e\u043a", 0, false))), Map.entry("\u041e\u0431\u044b\u0447\u043d\u044b\u0439 \u043c\u0438\u0441\u0442", List.of(new TextRule("\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u043b\u0443\u0442\u0430: \u041e\u0431\u044b\u0447\u043d\u044b\u0439", 0, false))), Map.entry("\u0411\u043e\u0433\u0430\u0442\u044b\u0439 \u043c\u0438\u0441\u0442", List.of(new TextRule("\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u043b\u0443\u0442\u0430: \u0411\u043e\u0433\u0430\u0442\u044b\u0439", 0, false))), Map.entry("\u041b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u044b\u0439 \u043c\u0438\u0441\u0442", List.of(new TextRule("\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u043b\u0443\u0442\u0430: \u041b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u044b\u0439", 0, false))), Map.entry("\u041d\u0435\u0438\u0437\u0431\u0435\u0436\u043d\u044b\u0439 \u0441\u043a\u0438\u043d", List.of(new TextRule("\u043f\u043e\u043b\u0443\u0447\u0430\u0435\u0442\u0435 \u041d\u0435\u0438\u0437\u0431\u0435\u0436\u043d\u044b\u0439 \u0441\u043a\u0438\u043d", 0, false))), Map.entry("\u0414\u0440\u0430\u043a\u043e\u043d\u0438\u0439 \u0441\u043a\u0438\u043d", List.of(new TextRule("\u043f\u043e\u043b\u0443\u0447\u0430\u0435\u0442\u0435 \u0414\u0440\u0430\u043a\u043e\u043d\u0438\u0439 \u0441\u043a\u0438\u043d", 0, false))), Map.entry("\u041f\u0440\u043e\u0433\u0440\u0443\u0437\u0447\u0438\u043a \u0447\u0430\u043d\u043a\u043e\u0432 [1x1]", List.of(new TextRule("\u043f\u0440\u043e\u0433\u0440\u0443\u0436\u0430\u0435\u043c\u043e\u0439 \u043e\u0431\u043b\u0430\u0441\u0442\u0438 (1x1)", 0, false))), Map.entry("\u041f\u0440\u043e\u0433\u0440\u0443\u0437\u0447\u0438\u043a \u0447\u0430\u043d\u043a\u043e\u0432 [3x3]", List.of(new TextRule("\u043f\u0440\u043e\u0433\u0440\u0443\u0436\u0430\u0435\u043c\u043e\u0439 \u043e\u0431\u043b\u0430\u0441\u0442\u0438 (3x3)", 0, false))), Map.entry("\u041f\u0440\u043e\u0433\u0440\u0443\u0437\u0447\u0438\u043a \u0447\u0430\u043d\u043a\u043e\u0432 [5x5]", List.of(new TextRule("\u043f\u0440\u043e\u0433\u0440\u0443\u0436\u0430\u0435\u043c\u043e\u0439 \u043e\u0431\u043b\u0430\u0441\u0442\u0438 (5x5)", 0, false))), Map.entry("\u0420\u0435\u0433\u0438\u043e\u043d 25x25", List.of(new TextRule("\u0420\u0430\u0437\u043c\u0435\u0440: 25x25 \u0431\u043b\u043e\u043a\u043e\u0432", 0, false))), Map.entry("\u0410\u0438\u0440\u0434\u0440\u043e\u043f", List.of(new TextRule("\u043f\u0440\u0438\u0437\u044b\u0432\u0430 \u0410\u0438\u0440\u0434\u0440\u043e\u043f\u0430", 0, false))), Map.entry("\u0411\u043b\u043e\u043a \u0434\u0430\u043c\u0430\u0433\u0435\u0440", List.of(new TextRule("\u041a\u0430\u0441\u0442: \u041d\u0430\u043d\u0435\u0441\u0435\u043d\u0438\u0435 \u0443\u0440\u043e\u043d\u0430", 0, false))), Map.entry("\u041c\u043e\u0434\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u043f\u043e\u043b\u0451\u0442\u0430", List.of(new TextRule("\u0414\u043e\u0441\u0442\u0443\u043f \u043a /fly", 0, false))), Map.entry("\u041c\u043e\u0434\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u043f\u043e\u0447\u0438\u043d\u043a\u0438", List.of(new TextRule("\u0414\u043e\u0441\u0442\u0443\u043f \u043a /fix", 0, false))), Map.entry("\u041a\u043b\u044e\u0447 \u043e\u0442 \u043a\u0435\u0439\u0441\u0430 \u0441 \u041f\u0440\u0438\u0432\u0438\u043b\u0435\u0433\u0438\u044f\u043c\u0438", List.of(new TextRule("\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442: \u041a\u0435\u0439\u0441 \u0441 \u041f\u0440\u0438\u0432\u0438\u043b\u0435\u0433\u0438\u044f\u043c\u0438", 0, false))), Map.entry("\u041a\u043b\u044e\u0447 \u043e\u0442 \u043a\u0435\u0439\u0441\u0430 \u0441 \u0422\u043e\u043a\u0435\u043d\u0430\u043c\u0438", List.of(new TextRule("\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442: \u041a\u0435\u0439\u0441 \u0441 \u0422\u043e\u043a\u0435\u043d\u0430\u043c\u0438", 0, false))), Map.entry("\u0412\u0430\u0439\u0442", List.of(new TextRule("\u0432 10 \u0440\u0430\u0437 \u0441\u0438\u043b\u044c\u043d\u0435\u0435 \u043e\u0431\u044b\u0447\u043d\u043e\u0433\u043e", 0, false), new TextRule("\u0441\u043f\u043e\u0441\u043e\u0431\u0435\u043d \u0432\u0437\u043e\u0440\u0432\u0430\u0442\u044c \u043e\u0431\u0441\u0438\u0434\u0438\u0430\u043d", 0, true))), Map.entry("\u0411\u043b\u044d\u043a", List.of(new TextRule("\u0441\u043f\u043e\u0441\u043e\u0431\u0435\u043d \u0432\u0437\u043e\u0440\u0432\u0430\u0442\u044c \u043e\u0431\u0441\u0438\u0434\u0438\u0430\u043d", 0, false))));
    private static final Map<String, List<EffectRule>> EFFECTS = Map.ofEntries(Map.entry("\u0425\u043b\u043e\u043f\u0443\u0448\u043a\u0430", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5909, 10, 200), new EffectRule((class_6880<class_1291>)class_1294.field_5904, 5, 0), new EffectRule((class_6880<class_1291>)class_1294.field_5919, 10, 100), new EffectRule((class_6880<class_1291>)class_1294.field_5912, 1, 3600))), Map.entry("\u0421\u0432\u044f\u0442\u0430\u044f \u0432\u043e\u0434\u0430", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5924, 2, 900), new EffectRule((class_6880<class_1291>)class_1294.field_5905, 2, 12000), new EffectRule((class_6880<class_1291>)class_1294.field_5915, 2, 0))), Map.entry("\u0417\u0435\u043b\u044c\u0435 \u0413\u043d\u0435\u0432\u0430", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5910, 5, 600), new EffectRule((class_6880<class_1291>)class_1294.field_5909, 4, 600))), Map.entry("\u0417\u0435\u043b\u044c\u0435 \u041f\u0430\u043b\u043b\u0430\u0434\u0438\u043d\u0430", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5907, 1, 12000), new EffectRule((class_6880<class_1291>)class_1294.field_5918, 1, 12000), new EffectRule((class_6880<class_1291>)class_1294.field_5914, 3, 1200), new EffectRule((class_6880<class_1291>)class_1294.field_5905, 1, 18000))), Map.entry("\u0417\u0435\u043b\u044c\u0435 \u0410\u0441\u0441\u0430\u0441\u0438\u043d\u0430", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5910, 4, 1200), new EffectRule((class_6880<class_1291>)class_1294.field_5904, 3, 6000), new EffectRule((class_6880<class_1291>)class_1294.field_5917, 1, 1200), new EffectRule((class_6880<class_1291>)class_1294.field_5921, 2, 0))), Map.entry("\u0417\u0435\u043b\u044c\u0435 \u0420\u0430\u0434\u0438\u0430\u0446\u0438\u0438", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5899, 2, 0), new EffectRule((class_6880<class_1291>)class_1294.field_5920, 2, 0), new EffectRule((class_6880<class_1291>)class_1294.field_5909, 3, 0), new EffectRule((class_6880<class_1291>)class_1294.field_5903, 5, 0), new EffectRule((class_6880<class_1291>)class_1294.field_5912, 1, 0))), Map.entry("\u0421\u043d\u043e\u0442\u0432\u043e\u0440\u043d\u043e\u0435", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5911, 2, 1800), new EffectRule((class_6880<class_1291>)class_1294.field_5901, 2, 200), new EffectRule((class_6880<class_1291>)class_1294.field_5920, 3, 1800), new EffectRule((class_6880<class_1291>)class_1294.field_5919, 1, 200))), Map.entry("\u041a\u0440\u043e\u0432\u0430\u0432\u0430\u044f \u0441\u0442\u0440\u0435\u043b\u0430", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5911, 3, 60), new EffectRule((class_6880<class_1291>)class_1294.field_5919, 1, 40), new EffectRule((class_6880<class_1291>)class_1294.field_5901, 1, 40), new EffectRule((class_6880<class_1291>)class_1294.field_5916, 1, 100))), Map.entry("\u0421\u0442\u0440\u0435\u043b\u0430 \u043e\u0431\u043b\u0435\u0434\u0435\u043d\u0435\u043d\u0438\u044f", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5909, 10, 100), new EffectRule((class_6880<class_1291>)class_1294.field_5901, 3, 40))), Map.entry("\u041c\u0443\u0447\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0441\u0442\u0440\u0435\u043b\u0430", List.of(new EffectRule((class_6880<class_1291>)class_1294.field_5909, 3, 100), new EffectRule((class_6880<class_1291>)class_1294.field_5920, 3, 100), new EffectRule((class_6880<class_1291>)class_1294.field_5899, 3, 100))));
    private static final Map<String, List<EnchantRule>> ENCHANTMENTS = Map.ofEntries(Map.entry("\u0428\u043b\u0435\u043c \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new EnchantRule("minecraft:protection", 5, false), new EnchantRule("minecraft:fire_protection", 5, false), new EnchantRule("minecraft:blast_protection", 5, false), new EnchantRule("minecraft:projectile_protection", 5, false), new EnchantRule("minecraft:unbreaking", 5, false), new EnchantRule("minecraft:mending", 1, false), new EnchantRule("minecraft:aqua_affinity", 1, false), new EnchantRule("minecraft:respiration", 3, false), new EnchantRule("minecraft:thorns", 1, true))), Map.entry("\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new EnchantRule("minecraft:protection", 5, false), new EnchantRule("minecraft:fire_protection", 5, false), new EnchantRule("minecraft:blast_protection", 5, false), new EnchantRule("minecraft:projectile_protection", 5, false), new EnchantRule("minecraft:unbreaking", 5, false), new EnchantRule("minecraft:mending", 1, false), new EnchantRule("minecraft:thorns", 1, true))), Map.entry("\u041f\u043e\u043d\u043e\u0436\u0438 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new EnchantRule("minecraft:protection", 5, false), new EnchantRule("minecraft:fire_protection", 5, false), new EnchantRule("minecraft:blast_protection", 5, false), new EnchantRule("minecraft:projectile_protection", 5, false), new EnchantRule("minecraft:unbreaking", 5, false), new EnchantRule("minecraft:mending", 1, false), new EnchantRule("minecraft:thorns", 1, true))), Map.entry("\u0411\u043e\u0442\u0438\u043d\u043a\u0438 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", List.of(new EnchantRule("minecraft:protection", 5, false), new EnchantRule("minecraft:fire_protection", 5, false), new EnchantRule("minecraft:blast_protection", 5, false), new EnchantRule("minecraft:projectile_protection", 5, false), new EnchantRule("minecraft:unbreaking", 5, false), new EnchantRule("minecraft:mending", 1, false), new EnchantRule("minecraft:feather_falling", 4, false), new EnchantRule("minecraft:soul_speed", 3, false), new EnchantRule("minecraft:depth_strider", 3, false), new EnchantRule("minecraft:thorns", 1, true))));
    private static final Set<String> CUSTOM_ITEMS = Set.of("\u0428\u043b\u0435\u043c \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u041f\u043e\u043d\u043e\u0436\u0438 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u0411\u043e\u0442\u0438\u043d\u043a\u0438 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u041c\u0435\u0447 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u0411\u0443\u043b\u0430\u0432\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u0422\u0440\u0435\u0437\u0443\u0431\u0435\u0446 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u041a\u0438\u0440\u043a\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u0410\u0440\u0431\u0430\u043b\u0435\u0442 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u041b\u0443\u043a \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u042d\u043b\u0438\u0442\u0440\u044b \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u0423\u0434\u043e\u0447\u043a\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0420\u0430\u0437\u0434\u043e\u0440\u0430", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0422\u0438\u0440\u0430\u043d\u0430", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u042f\u0440\u043e\u0441\u0442\u0438", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0412\u0438\u0445\u0440\u044f", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041c\u0440\u0430\u043a\u0430", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u0414\u0435\u043c\u043e\u043d\u0430", "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d \u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f", "\u0421\u0444\u0435\u0440\u0430 \u0425\u0430\u043e\u0441\u0430", "\u0421\u0444\u0435\u0440\u0430 \u0421\u0430\u0442\u0438\u0440\u0430", "\u0421\u0444\u0435\u0440\u0430 \u0411\u0435\u0441\u0442\u0438\u0438", "\u0421\u0444\u0435\u0440\u0430 \u0410\u0440\u0435\u0441\u0430", "\u0421\u0444\u0435\u0440\u0430 \u0413\u0438\u0434\u0440\u044b", "\u0421\u0444\u0435\u0440\u0430 \u0418\u043a\u0430\u0440\u0430", "\u0421\u0444\u0435\u0440\u0430 \u042d\u0440\u0438\u0434\u0430", "\u0421\u0444\u0435\u0440\u0430 \u0422\u0438\u0442\u0430\u043d\u0430", "\u0411\u043e\u0436\u044c\u0435 \u043a\u0430\u0441\u0430\u043d\u0438\u0435", "\u041c\u043e\u0449\u043d\u044b\u0439 \u0443\u0434\u0430\u0440", "\u0411\u043b\u043e\u043a \u0434\u0430\u043c\u0430\u0433\u0435\u0440", "\u041c\u043e\u0434\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u043f\u043e\u043b\u0451\u0442\u0430", "\u041c\u043e\u0434\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u043f\u043e\u0447\u0438\u043d\u043a\u0438", "\u041e\u0442\u043c\u044b\u0447\u043a\u0430 \u043a \u0421\u0444\u0435\u0440\u0430\u043c", "\u0422\u0440\u0430\u043f\u043a\u0430", "\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c", "\u0421\u043d\u0435\u0436\u043e\u043a \u0437\u0430\u043c\u043e\u0440\u043e\u0437\u043a\u0430", "\u0411\u043e\u0436\u044c\u044f \u0430\u0443\u0440\u0430", "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f", "\u041f\u043b\u0430\u0441\u0442", "\u0412\u0430\u0439\u0442", "\u0411\u043b\u044d\u043a");

    private FunTimeItemMatcher() {
    }

    static boolean matches(AutoBuy.CatalogItem item, class_1799 stack, List<class_2561> tooltip) {
        List<EnchantRule> enchantments;
        List<EffectRule> effects;
        List<TextRule> descriptions;
        if (stack.method_7909() != item.item()) {
            return false;
        }
        String name = item.name();
        if (CUSTOM_ITEMS.contains(name) && !FunTimeItemMatcher.verifyCustomItem(stack, tooltip, name)) {
            return false;
        }
        List<AttributeRule> attributes = ATTRIBUTES.get(name);
        if (attributes != null) {
            class_9285 component = (class_9285)stack.method_58695(class_9334.field_49636, (Object)class_9285.field_49326);
            for (AttributeRule rule : attributes) {
                if (!component.comp_2393().stream().noneMatch(entry -> rule.matches((class_9285.class_9287)entry))) continue;
                return false;
            }
        }
        if ((descriptions = DESCRIPTIONS.get(name)) != null) {
            StringBuilder tooltipText = new StringBuilder();
            for (int index = 1; index < tooltip.size(); ++index) {
                tooltipText.append(' ').append(tooltip.get(index).getString());
            }
            String text = tooltipText.toString().toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
            for (TextRule textRule : descriptions) {
                if (textRule.matches(text)) continue;
                return false;
            }
        }
        if ((effects = EFFECTS.get(name)) != null) {
            class_1844 component = (class_1844)stack.method_58694(class_9334.field_49651);
            if (component == null) {
                return false;
            }
            for (EffectRule effectRule : effects) {
                boolean found = false;
                for (class_1293 effect : component.method_57397()) {
                    if (!effectRule.matches(effect)) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                return false;
            }
        }
        if ((enchantments = ENCHANTMENTS.get(name)) != null) {
            class_9304 component = (class_9304)stack.method_58695(class_9334.field_49633, (Object)class_9304.field_49385);
            for (EnchantRule rule : enchantments) {
                if (rule.matches(component)) continue;
                return false;
            }
        }
        if (name.equals("\u041b\u0443\u043a \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f") || name.equals("\u042d\u043b\u0438\u0442\u0440\u044b \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f") || name.equals("\u0423\u0434\u043e\u0447\u043a\u0430 \u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f")) {
            return stack.method_7964().getString().toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').contains(name.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435'));
        }
        return true;
    }

    private static boolean verifyCustomItem(class_1799 stack, List<class_2561> tooltip, String name) {
        String displayName = stack.method_7964().getString();
        if (!displayName.contains("\u2726") && !displayName.contains("\u2605")) {
            return false;
        }
        String normalizedDisplay = displayName.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
        if (!normalizedDisplay.contains(name.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435'))) {
            return false;
        }
        boolean hasOriginalMarker = false;
        for (int i = 1; i < tooltip.size(); ++i) {
            String line = tooltip.get(i).getString();
            if (!line.contains("[\u2605] \u041e\u0440\u0438\u0433\u0438\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442") && !line.contains("[\u2726] \u041e\u0440\u0438\u0433\u0438\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442")) continue;
            hasOriginalMarker = true;
            break;
        }
        if (!hasOriginalMarker) {
            return false;
        }
        int components = stack.method_57353().method_57835() - (stack.method_57826(class_9334.field_49607) ? 1 : 0);
        return components >= 8;
    }

    private record AttributeRule(class_6880<class_1320> attribute, double value, class_1322.class_1323 operation) {
        boolean matches(class_9285.class_9287 entry) {
            return entry.comp_2395().equals(this.attribute) && Math.abs(entry.comp_2396().comp_2449() - this.value) < 0.001 && entry.comp_2396().comp_2450() == this.operation;
        }
    }

    private record TextRule(String text, int level, boolean denied) {
        boolean matches(String tooltip) {
            String value;
            String needle = this.text.toLowerCase(Locale.ROOT).replace('\u0451', '\u0435');
            int position = tooltip.indexOf(needle);
            if (this.denied) {
                return position < 0;
            }
            if (position < 0) {
                return false;
            }
            if (this.level <= 1) {
                return true;
            }
            String after = tooltip.substring(position + needle.length()).trim();
            Matcher matcher = Pattern.compile("^(\\d+|[ivx]+)", 2).matcher(after);
            if (!matcher.find()) {
                return false;
            }
            int found = switch (value = matcher.group(1).toUpperCase(Locale.ROOT)) {
                case "I" -> {
                    int var10_9;
                    yield var10_9 = 1;
                }
                case "II" -> {
                    int var10_9;
                    yield var10_9 = 2;
                }
                case "III" -> {
                    int var10_9;
                    yield var10_9 = 3;
                }
                case "IV" -> {
                    int var10_9;
                    yield var10_9 = 4;
                }
                case "V" -> {
                    int var10_9;
                    yield var10_9 = 5;
                }
                default -> {
                    int var10_9;
                    try {
                        yield var10_9 = Integer.parseInt(value);
                    }
                    catch (NumberFormatException exception) {
                        yield var10_9 = 0;
                    }
                }
            };
            return found >= this.level;
        }
    }

    private record EffectRule(class_6880<class_1291> effect, int level, int duration) {
        boolean matches(class_1293 current) {
            return current.method_5579().equals(this.effect) && current.method_5578() + 1 == this.level && current.method_5584() >= this.duration;
        }
    }

    private record EnchantRule(String id, int level, boolean denied) {
        boolean matches(class_9304 component) {
            int found = 0;
            for (Object2IntMap.Entry entry : component.method_57539()) {
                String key = ((class_6880<?>)entry.getKey()).method_40230().map(value -> value.method_29177().toString()).orElse("");
                if (!key.equals(this.id)) continue;
                found = entry.getIntValue();
                break;
            }
            return this.denied ? found < this.level : found >= this.level;
        }
    }
}

