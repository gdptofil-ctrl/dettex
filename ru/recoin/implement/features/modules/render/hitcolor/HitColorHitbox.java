package ru.recoin.implement.features.modules.render.hitcolor;

import net.minecraft.class_1297;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.AttackEvent;

/**
 * Hit Color Hitbox: when hitboxes are shown (vanilla F3+B or the client's Box module), the hitbox of
 * the entity you hit flashes with your colour. Colour, opacity, duration, sword / no sword.
 */
public final class HitColorHitbox
extends Module {
    private final ColorSetting color = new ColorSetting("Color", "Hitbox colour on hit").value(0xFFFF3B6B);
    private final ValueSetting opacity = new ValueSetting("Opacity %", "Opacity of the hit colour").setValue(100.0f).range(10, 100);
    private final ValueSetting duration = new ValueSetting("Duration ms", "How long the colour fades back").setValue(500.0f).range(100, 1500);
    private final SelectSetting weapon = new SelectSetting("Weapon", "Any hit, only sword hits, or only hits without a sword").value("Any", "Sword", "No Sword").selected("Any");
    private final BooleanSetting onlyMine = new BooleanSetting("Only My Hits", "Off: also when others hit them (any damage)").setValue(true);

    public HitColorHitbox() {
        super("HitColorHitbox", "Hit Color Hitbox", ModuleCategory.RENDER);
        this.setup(this.color, this.opacity, this.duration, this.weapon, this.onlyMine);
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        HitTracker.onAttack(event.getEntity());
    }

    /** Hitbox colour for this entity: the normal colour blended to the hit colour while the hit fades. */
    public static int colorFor(class_1297 entity, int normal) {
        HitColorHitbox m;
        try {
            m = Instance.get(HitColorHitbox.class);
        } catch (Throwable ignored) {
            return normal;
        }
        if (m == null || !m.isState()) {
            return normal;
        }
        float s = HitTracker.strength(entity, m.weapon.getSelected(), m.onlyMine.isValue(), (long)m.duration.getValue());
        if (s <= 0.0f) {
            return normal;
        }
        int hit = m.color.getColor();
        int alpha = Math.round(255 * m.opacity.getValue() / 100.0f);
        int na = normal >>> 24;
        int a = Math.round(na + (alpha - na) * s);
        int r = Math.round((normal >> 16 & 0xFF) + ((hit >> 16 & 0xFF) - (normal >> 16 & 0xFF)) * s);
        int g = Math.round((normal >> 8 & 0xFF) + ((hit >> 8 & 0xFF) - (normal >> 8 & 0xFF)) * s);
        int b = Math.round((normal & 0xFF) + ((hit & 0xFF) - (normal & 0xFF)) * s);
        return a << 24 | r << 16 | g << 8 | b;
    }
}
