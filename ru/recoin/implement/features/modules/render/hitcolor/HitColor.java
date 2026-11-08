package ru.recoin.implement.features.modules.render.hitcolor;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
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
 * Hit Color: the entity you hit is tinted with your colour for a moment, instead of (or on top of)
 * the vanilla red flash. Colour, strength, duration, sword / no sword, body only or body + armor.
 */
public final class HitColor
extends Module {
    private final ColorSetting color = new ColorSetting("Color", "Tint colour of the hit entity").value(0xFF7A5CFF);
    private final ValueSetting strength = new ValueSetting("Strength %", "How strong the colour is (opacity of the tint)").setValue(70.0f).range(5, 100);
    private final ValueSetting duration = new ValueSetting("Duration ms", "How long the colour fades out").setValue(450.0f).range(100, 1500);
    private final SelectSetting weapon = new SelectSetting("Weapon", "Any hit, only sword hits, or only hits without a sword").value("Any", "Sword", "No Sword").selected("Any");
    private final SelectSetting target = new SelectSetting("Paint", "Only the body or the body with armor").value("Body", "Body + Armor").selected("Body + Armor");
    private final BooleanSetting onlyMine = new BooleanSetting("Only My Hits", "Off: also when others hit them (any damage)").setValue(true);
    private final BooleanSetting playersOnly = new BooleanSetting("Players Only", "Only players, not mobs").setValue(false);
    private final BooleanSetting hideRed = new BooleanSetting("Hide Vanilla Red", "No red flash, only your colour").setValue(true);

    public HitColor() {
        super("HitColor", "Hit Color", ModuleCategory.RENDER);
        this.setup(this.color, this.strength, this.duration, this.weapon, this.target, this.onlyMine, this.playersOnly, this.hideRed);
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        HitTracker.onAttack(event.getEntity());
    }

    private static HitColor active() {
        try {
            HitColor m = Instance.get(HitColor.class);
            return m != null && m.isState() ? m : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Tint for this entity right now (opaque ARGB multiplier) or 0 for none. Called while building render states. */
    public static int tintFor(class_1297 entity) {
        HitColor m = HitColor.active();
        if (m == null || m.playersOnly.isValue() && !(entity instanceof class_1657)) {
            return 0;
        }
        float s = HitTracker.strength(entity, m.weapon.getSelected(), m.onlyMine.isValue(), (long)m.duration.getValue());
        if (s <= 0.0f) {
            return 0;
        }
        return HitTracker.tint(m.color.getColor(), s * m.strength.getValue() / 100.0f);
    }

    public static boolean paintArmor() {
        HitColor m = HitColor.active();
        return m != null && m.target.isSelected("Body + Armor");
    }

    public static boolean hideVanillaRed() {
        HitColor m = HitColor.active();
        return m != null && m.hideRed.isValue();
    }
}
