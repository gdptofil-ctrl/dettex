/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 *  net.minecraft.class_2663
 */
package ru.recoin.implement.features.modules.misc;

import net.minecraft.class_2596;
import net.minecraft.class_2663;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.common.util.other.Instance;
import ru.recoin.core.Main;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.AttackEvent;

public final class Sounds
extends Module {
    public final SelectSetting type = new SelectSetting("Type", "Module toggle sound pack").value("Type 1", "Type 2").selected("Type 1");
    public final ValueSetting volume = new ValueSetting("Volume", "Volume of client sounds").setValue(100.0f).range(0.0f, 100.0f).step(1.0f);
    public final BooleanSetting interfaceEnabled = new BooleanSetting("Interface Sounds", "Play click sounds in client menus").setValue(true);
    public final ValueSetting interfaceVolume = new ValueSetting("Interface Volume", "Volume of menu click sounds").setValue(20.0f).range(0.0f, 100.0f).step(1.0f).visible(this.interfaceEnabled::isValue);
    public final SelectSetting interfacePitch = new SelectSetting("Interface Pitch", "Pitch of menu click sounds").value("Low", "Medium", "High").selected("Medium").visible(this.interfaceEnabled::isValue);
    public final TextSetting totemSound = new TextSetting("Totem Pop Sound File", "Selected WAV file for totem pops").setText("");
    public final TextSetting hitSound = new TextSetting("Hit Sound File", "Selected WAV file for attacks").setText("hit.wav");
    public final ButtonSetting chooseTotemSound = new ButtonSetting("Totem Pop Sound", "Choose a WAV file for totem pops").setButtonName("Select").setRunnable(() -> SoundManager.chooseCustomSound(this.totemSound));
    public final ButtonSetting chooseHitSound = new ButtonSetting("Hit Sound", "Choose a WAV file for attacks").setButtonName("Select").setRunnable(() -> SoundManager.chooseCustomSound(this.hitSound));
    public final ButtonSetting clearTotemSound = new ButtonSetting("Reset Totem Sound", "Restore vanilla totem sound").setButtonName("Reset").setRunnable(() -> this.totemSound.setText(""));
    public final ButtonSetting clearHitSound = new ButtonSetting("Clear Hit Sound", "Restore default hit sound").setButtonName("Clear").setRunnable(() -> this.hitSound.setText(""));

    public Sounds() {
        super("Sounds", ModuleCategory.MISC);
        this.setup(this.type, this.volume, this.interfaceEnabled, this.interfaceVolume, this.interfacePitch, this.totemSound, this.hitSound, this.chooseTotemSound, this.chooseHitSound, this.clearTotemSound, this.clearHitSound);
        this.totemSound.setVisible(() -> false);
        this.hitSound.setVisible(() -> false);
        this.state = true;
        Main.getInstance().getEventManager().register(this);
        this.getAnimation().setDirection(Direction.FORWARDS);
        this.getAnimation().complete();
    }

    public static Sounds getInstance() {
        return Instance.get(Sounds.class);
    }

    public boolean type2() {
        return this.type.isSelected("Type 2");
    }

    public float volume() {
        return this.volume.getValue() / 100.0f;
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (!event.isPre()) {
            SoundManager.playCustomHitSound();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2663 status;
        class_2596<?> class_25962 = event.getPacket();
        if (class_25962 instanceof class_2663 && (status = (class_2663)class_25962).method_11470() == 35) {
            SoundManager.playCustomTotemSound();
        }
    }
}

