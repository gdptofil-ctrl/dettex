package ru.recoin.implement.features.modules.render.cosmetics.sync;

import java.util.UUID;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_5250;
import ru.recoin.implement.features.modules.render.cosmetics.Cosmetics;

/** Dettex logo glyph (font recoin:badge, char U+E000) shown before the names of Dettex users. */
public final class DettexBadge {
    private static final class_2583 STYLE = class_2583.field_24360
            .method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"recoin", (String)"badge")))
            .method_36139(0xFFFFFF);

    private DettexBadge() {
    }

    /** name with the badge in front when this player is a Dettex user, otherwise name unchanged. */
    public static class_2561 decorate(UUID uuid, class_2561 name) {
        Cosmetics cosmetics = Cosmetics.getInstance();
        if (name == null || uuid == null || cosmetics == null || !cosmetics.showBadge(uuid)) {
            return name;
        }
        class_5250 out = class_2561.method_43473();
        out.method_10852((class_2561)class_2561.method_43470((String)"").method_10862(STYLE));
        out.method_27693(" ");
        out.method_10852(name);
        return out;
    }
}
