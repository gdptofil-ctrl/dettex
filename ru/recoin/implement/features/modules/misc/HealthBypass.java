/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_266
 *  net.minecraft.class_3532
 *  net.minecraft.class_5250
 *  net.minecraft.class_640
 *  net.minecraft.class_8646
 *  net.minecraft.class_9013
 *  net.minecraft.class_9015
 *  net.minecraft.class_9022
 *  net.minecraft.class_9025
 */
package ru.recoin.implement.features.modules.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_266;
import net.minecraft.class_3532;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import net.minecraft.class_8646;
import net.minecraft.class_9013;
import net.minecraft.class_9015;
import net.minecraft.class_9022;
import net.minecraft.class_9025;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;

public class HealthBypass
extends Module {
    private static final Pattern HEALTH_PATTERN = Pattern.compile("(?i)(?:\u2764|hp|\u0445\u043f|\u2665)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:\u2764|hp|\u0445\u043f|\u2665)?");
    private final SelectSetting mode = new SelectSetting("Mode", "\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u044f \u0437\u0434\u043e\u0440\u043e\u0432\u044c\u044f").value("Default", "Scoreboard", "TabList", "NameTag").selected("Scoreboard");

    public static HealthBypass getInstance() {
        return Instance.get(HealthBypass.class);
    }

    public HealthBypass() {
        super("Health Bypass", "Health Bypass", ModuleCategory.MISC);
        this.setup(this.mode);
    }

    public boolean enabled() {
        return this.isState();
    }

    public boolean isScoreboardMode() {
        return this.mode.isSelected("Scoreboard");
    }

    public boolean isScoreboardActive(class_1309 entity) {
        if (!this.enabled() || entity == null) {
            return false;
        }
        if (entity.method_5767()) {
            return false;
        }
        String selectedMode = this.mode.getSelected();
        return selectedMode.equals("Scoreboard");
    }

    public float resolveHealth(class_1309 entity) {
        float health;
        if (entity == null) {
            return -1.0f;
        }
        String selectedMode = this.mode.getSelected();
        if (selectedMode.equals("Default")) {
            return class_3532.method_15363((float)entity.method_6032(), (float)0.0f, (float)entity.method_6063()) + entity.method_6067();
        }
        if (selectedMode.equals("NameTag") && (health = this.getNameTagHealth(entity)) >= 0.0f) {
            return health;
        }
        if (selectedMode.equals("TabList") && (health = this.getTabListHealth(entity)) >= 0.0f) {
            return health;
        }
        if (selectedMode.equals("Scoreboard")) {
            if (entity.method_5767()) {
                return -1.0f;
            }
            health = this.getScoreboardHealth(entity);
            if (health >= 0.0f) {
                return health;
            }
            return -1.0f;
        }
        return class_3532.method_15363((float)entity.method_6032(), (float)0.0f, (float)entity.method_6063()) + entity.method_6067();
    }

    private float getScoreboardHealth(class_1309 entity) {
        class_1657 player;
        block11: {
            block10: {
                if (!(entity instanceof class_1657)) break block10;
                player = (class_1657)entity;
                if (HealthBypass.mc.field_1687 != null) break block11;
            }
            return -1.0f;
        }
        if (player.method_5767()) {
            return -1.0f;
        }
        class_266 objective = HealthBypass.mc.field_1687.method_8428().method_1189(class_8646.field_45158);
        if (objective == null) {
            objective = HealthBypass.mc.field_1687.method_8428().method_1189(class_8646.field_45156);
        }
        if (objective == null) {
            return -1.0f;
        }
        class_9013 score = HealthBypass.mc.field_1687.method_8428().method_55430((class_9015)player, objective);
        if (score == null) {
            return -1.0f;
        }
        class_5250 text = class_9013.method_55398((class_9013)score, (class_9022)objective.method_55380((class_9022)class_9025.field_47566));
        String numeric = ColorUtil.removeFormatting(text.getString());
        if (numeric == null) {
            numeric = "";
        }
        numeric = numeric.replaceAll("[^0-9.\\-]", "");
        try {
            return Float.parseFloat(numeric);
        }
        catch (NumberFormatException ignored) {
            return score.method_55397();
        }
    }

    private float getTabListHealth(class_1309 entity) {
        String name;
        float parsed;
        class_1657 player;
        block6: {
            block5: {
                if (!(entity instanceof class_1657)) break block5;
                player = (class_1657)entity;
                if (mc.method_1562() != null) break block6;
            }
            return -1.0f;
        }
        class_640 entry = mc.method_1562().method_2871(player.method_5667());
        if (entry == null) {
            return -1.0f;
        }
        if (entry.method_2971() != null && (parsed = this.parseHealthFromText(name = ColorUtil.removeFormatting(entry.method_2971().getString()))) >= 0.0f) {
            return parsed;
        }
        return -1.0f;
    }

    private float getNameTagHealth(class_1309 entity) {
        if (entity.method_5797() == null) {
            return -1.0f;
        }
        String name = ColorUtil.removeFormatting(entity.method_5797().getString());
        return this.parseHealthFromText(name);
    }

    private float parseHealthFromText(String text) {
        if (text == null || text.isEmpty()) {
            return -1.0f;
        }
        Matcher matcher = HEALTH_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return -1.0f;
    }
}

