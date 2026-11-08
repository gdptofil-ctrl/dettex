package ru.recoin.implement.features.modules.misc;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1802;
import net.minecraft.class_2596;
import net.minecraft.class_2663;
import net.minecraft.class_310;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.events.packet.PacketEvent;
import ru.recoin.implement.events.player.AttackEvent;
import ru.recoin.implement.events.player.DeathScreenEvent;
import ru.recoin.implement.events.player.TickEvent;

/** Plays a sound when you hit with a mace, pop a totem, get a kill or die. Heard only by you. */
public final class EventSounds
extends Module {
    private static final String[] MACE = ClientSoundPack.names("mace");
    private static final String[] TOTEM = ClientSoundPack.names("totem");
    private static final String[] KILL = ClientSoundPack.names("kill");
    private static final String[] DEATH = ClientSoundPack.names("death");
    private static final long KILL_WINDOW_MS = 4000L;

    private final SelectSetting maceSound = new SelectSetting("Mace Sound", "Sound on a mace hit").value(MACE).selected(MACE[0]);
    private final BooleanSetting maceSmashOnly = new BooleanSetting("Smash Only", "Only on falling smash hits").setValue(false);
    private final GroupSetting maceGroup = new GroupSetting("Mace Hit", "Mace hit sound").setValue(true).settings(this.maceSound, this.maceSmashOnly);

    private final SelectSetting totemSound = new SelectSetting("Totem Sound", "Sound when you pop a totem").value(TOTEM).selected(TOTEM[0]);
    private final BooleanSetting totemSelf = new BooleanSetting("Own Totem Too", "Also when your own totem pops").setValue(false);
    private final GroupSetting totemGroup = new GroupSetting("Totem Pop", "Totem pop sound").setValue(true).settings(this.totemSound, this.totemSelf);

    private final SelectSetting killSound = new SelectSetting("Kill Sound", "Sound when you get a kill").value(KILL).selected(KILL[0]);
    private final GroupSetting killGroup = new GroupSetting("Kill", "Kill sound").setValue(true).settings(this.killSound);

    private final SelectSetting deathSound = new SelectSetting("Death Sound", "Sound when you die").value(DEATH).selected(DEATH[0]);
    private final GroupSetting deathGroup = new GroupSetting("Death", "Death sound").setValue(true).settings(this.deathSound);

    private final ValueSetting volume = new ValueSetting("Volume", "Event sounds volume").setValue(80.0f).range(0, 100);

    private int lastAttackedId = -1;
    private long lastAttackTime;
    private boolean deathPlayed;

    public EventSounds() {
        super("EventSounds", "Event Sounds", ModuleCategory.MISC);
        this.setup(this.maceGroup, this.totemGroup, this.killGroup, this.deathGroup, this.volume);
    }

    private float vol() {
        return this.volume.getValue() / 100.0f;
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        class_310 mc = class_310.method_1551();
        class_746 player = mc.field_1724;
        if (player == null) {
            return;
        }
        class_1297 target = event.getTarget();
        if (event.isPre()) {
            if (target instanceof class_1309) {
                this.lastAttackedId = target.method_5628();
                this.lastAttackTime = System.currentTimeMillis();
            }
            return;
        }
        if (this.maceGroup.isValue() && player.method_6047().method_31574(class_1802.field_49814) && (!this.maceSmashOnly.isValue() || player.field_6017 > 1.5)) {
            ClientSoundPack.play("mace", this.maceSound.getSelected(), this.vol());
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!event.isReceive()) {
            return;
        }
        class_2596<?> packet = event.getPacket();
        if (!(packet instanceof class_2663)) {
            return;
        }
        class_2663 status = (class_2663)packet;
        byte code = status.method_11470();
        if (code != 35 && code != 3) {
            return;
        }
        // packets arrive on the network thread: resolve the entity and play on the client thread
        class_310 mc = class_310.method_1551();
        mc.execute(() -> this.handleStatus(status, code));
    }

    private void handleStatus(class_2663 status, byte code) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        class_1297 entity = status.method_11469(mc.field_1687);
        if (entity == null) {
            return;
        }
        boolean self = entity == mc.field_1724;
        if (code == 35) {
            if (this.totemGroup.isValue() && (!self || this.totemSelf.isValue())) {
                ClientSoundPack.play("totem", this.totemSound.getSelected(), this.vol());
            }
            return;
        }
        if (!self && this.killGroup.isValue() && entity.method_5628() == this.lastAttackedId && System.currentTimeMillis() - this.lastAttackTime < KILL_WINDOW_MS) {
            this.lastAttackedId = -1;
            ClientSoundPack.play("kill", this.killSound.getSelected(), this.vol());
        }
    }

    @EventHandler
    public void onDeathScreen(DeathScreenEvent event) {
        if (this.deathPlayed) {
            return;
        }
        this.deathPlayed = true;
        if (this.deathGroup.isValue()) {
            ClientSoundPack.play("death", this.deathSound.getSelected(), this.vol());
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_746 player = class_310.method_1551().field_1724;
        if (player != null && !player.method_29504() && player.method_6032() > 0.0f) {
            this.deathPlayed = false;
        }
    }
}
