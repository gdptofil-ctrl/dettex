/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.module;

import com.adl.nativeprotect.Native;
import java.util.ArrayList;
import java.util.List;
import ru.recoin.api.feature.module.Module;
import ru.recoin.implement.features.modules.combat.AimAssist;
import ru.recoin.implement.features.modules.combat.AimBot;
import ru.recoin.implement.features.modules.combat.AntiBot;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.AutoArmor;
import ru.recoin.implement.features.modules.combat.AutoCrystal;
import ru.recoin.implement.features.modules.combat.AutoExplosion;
import ru.recoin.implement.features.modules.combat.AutoPotion;
import ru.recoin.implement.features.modules.combat.AutoSwap;
import ru.recoin.implement.features.modules.combat.AutoTotem;
import ru.recoin.implement.features.modules.combat.FastBow;
import ru.recoin.implement.features.modules.combat.HitBoxModule;
import ru.recoin.implement.features.modules.combat.KillAuraModule;
import ru.recoin.implement.features.modules.combat.MaceHelper;
import ru.recoin.implement.features.modules.combat.NoFriendDamage;
import ru.recoin.implement.features.modules.combat.NoInteract;
import ru.recoin.implement.features.modules.combat.NoServerRotation;
import ru.recoin.implement.features.modules.combat.PacketCriticals;
import ru.recoin.implement.features.modules.combat.ShiftTap;
import ru.recoin.implement.features.modules.combat.SpearBoost;
import ru.recoin.implement.features.modules.combat.TargetPearl;
import ru.recoin.implement.features.modules.combat.TriggerBot;
import ru.recoin.implement.features.modules.combat.Velocity;
import ru.recoin.implement.features.modules.combat.WebTrap;
import ru.recoin.implement.features.modules.misc.ActionTracker;
import ru.recoin.implement.features.modules.misc.AnchorFarming;
import ru.recoin.implement.features.modules.misc.AncientFarmer;
import ru.recoin.implement.features.modules.misc.AnyDesk;
import ru.recoin.implement.features.modules.misc.AppleFarm;
import ru.recoin.implement.features.modules.misc.AuctionRelist;
import ru.recoin.implement.features.modules.misc.AutoAccept;
import ru.recoin.implement.features.modules.misc.AutoAuth;
import ru.recoin.implement.features.modules.misc.AutoDuel;
import ru.recoin.implement.features.modules.misc.AutoEnd;
import ru.recoin.implement.features.modules.misc.AutoInventory;
import ru.recoin.implement.features.modules.misc.AutoLeave;
import ru.recoin.implement.features.modules.misc.AutoMessage;
import ru.recoin.implement.features.modules.misc.AutoSell;
import ru.recoin.implement.features.modules.misc.AutoTrade;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.implement.features.modules.misc.AutoWarden;
import ru.recoin.implement.features.modules.misc.BaseFinder;
import ru.recoin.implement.features.modules.misc.ChatHelper;
import ru.recoin.implement.features.modules.misc.ClanUpgrade;
import ru.recoin.implement.features.modules.misc.ClickAction;
import ru.recoin.implement.features.modules.misc.ClickGuiModule;
import ru.recoin.implement.features.modules.misc.ContainerStealer;
import ru.recoin.implement.features.modules.misc.CreeperFarm;
import ru.recoin.implement.features.modules.misc.ElytraHelper;
import ru.recoin.implement.features.modules.misc.FreeCam;
import ru.recoin.implement.features.modules.misc.HealthBypass;
import ru.recoin.implement.features.modules.misc.InventoryManager;
import ru.recoin.implement.features.modules.misc.ItemScroller;
import ru.recoin.implement.features.modules.misc.KeyFinder;
import ru.recoin.implement.features.modules.misc.LeaveTracker;
import ru.recoin.implement.features.modules.misc.LockSlot;
import ru.recoin.implement.features.modules.misc.PvPSafe;
import ru.recoin.implement.features.modules.misc.RPSpoofer;
import ru.recoin.implement.features.modules.misc.Unhook;
import ru.recoin.implement.features.modules.misc.ServerHelper;
import ru.recoin.implement.features.modules.misc.ServerJoiner;
import ru.recoin.implement.features.modules.misc.Sounds;
import ru.recoin.implement.features.modules.misc.TelegramAPI;
import ru.recoin.implement.features.modules.movement.AbilitiesFly;
import ru.recoin.implement.features.modules.movement.FlyModule;
import ru.recoin.implement.features.modules.movement.AirJump;
import ru.recoin.implement.features.modules.movement.AirStuck;
import ru.recoin.implement.features.modules.movement.AutoSprint;
import ru.recoin.implement.features.modules.movement.Blink;
import ru.recoin.implement.features.modules.movement.DragonFly;
import ru.recoin.implement.features.modules.movement.ElytraFly;
import ru.recoin.implement.features.modules.movement.ElytraMotion;
import ru.recoin.implement.features.modules.movement.ElytraTarget;
import ru.recoin.implement.features.modules.movement.FireWorkBooster;
import ru.recoin.implement.features.modules.movement.GrimGlide;
import ru.recoin.implement.features.modules.movement.Jesus;
import ru.recoin.implement.features.modules.movement.LongJump;
import ru.recoin.implement.features.modules.movement.NoClip;
import ru.recoin.implement.features.modules.movement.NoFall;
import ru.recoin.implement.features.modules.movement.NoSlow;
import ru.recoin.implement.features.modules.movement.NoWeb;
import ru.recoin.implement.features.modules.movement.PitBike;
import ru.recoin.implement.features.modules.movement.ScreenWalk;
import ru.recoin.implement.features.modules.movement.Speed;
import ru.recoin.implement.features.modules.movement.Spider;
import ru.recoin.implement.features.modules.movement.Strafe;
import ru.recoin.implement.features.modules.movement.Timer;
import ru.recoin.implement.features.modules.movement.WaterSpeed;
import ru.recoin.implement.features.modules.particles.AmbientParticles;
import ru.recoin.implement.features.modules.player.AncientXRay;
import ru.recoin.implement.features.modules.player.AntiAFK;
import ru.recoin.implement.features.modules.player.AutoBuy;
import ru.recoin.implement.features.modules.player.AutoFarm;
import ru.recoin.implement.features.modules.player.AutoFish;
import ru.recoin.implement.features.modules.player.AutoJump;
import ru.recoin.implement.features.modules.player.AutoRespawn;
import ru.recoin.implement.features.modules.player.AutoTool;
import ru.recoin.implement.features.modules.player.AutoUse;
import ru.recoin.implement.features.modules.player.EnderChestPlus;
import ru.recoin.implement.features.modules.player.FakePlayer;
import ru.recoin.implement.features.modules.player.FastBreak;
import ru.recoin.implement.features.modules.player.FastExp;
import ru.recoin.implement.features.modules.player.GodMode;
import ru.recoin.implement.features.modules.player.ItemHelper;
import ru.recoin.implement.features.modules.player.NameProtect;
import ru.recoin.implement.features.modules.player.NoDelay;
import ru.recoin.implement.features.modules.player.NoPush;
import ru.recoin.implement.features.modules.player.Nuker;
import ru.recoin.implement.features.modules.player.OpenWalls;
import ru.recoin.implement.features.modules.player.TapeMouse;
import ru.recoin.implement.features.modules.pve.SwordWarm;
import ru.recoin.implement.features.modules.render.Animations;
import ru.recoin.implement.features.modules.render.Arrows;
import ru.recoin.implement.features.modules.render.AspectRatio;
import ru.recoin.implement.features.modules.render.Atmosphere;
import ru.recoin.implement.features.modules.render.AttackEffect;
import ru.recoin.implement.features.modules.render.AuctionHelper;
import ru.recoin.implement.features.modules.render.BlockOverlay;
import ru.recoin.implement.features.modules.render.Box;
import ru.recoin.implement.features.modules.render.CameraTweaks;
import ru.recoin.implement.features.modules.render.Contrast;
import ru.recoin.implement.features.modules.render.CooldownHotbar;
import ru.recoin.implement.features.modules.render.CrossHair;
import ru.recoin.implement.features.modules.render.EntityESP;
import ru.recoin.implement.features.modules.render.FullBright;
import ru.recoin.implement.features.modules.render.HandsShader;
import ru.recoin.implement.features.modules.render.HealthHelper;
import ru.recoin.implement.features.modules.render.HitBubble;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.ItemSearcher;
import ru.recoin.implement.features.modules.render.Items2D;
import ru.recoin.implement.features.modules.render.JumpCircle;
import ru.recoin.implement.features.modules.render.KillEffect;
import ru.recoin.implement.features.modules.render.MineHelper;
import ru.recoin.implement.features.modules.render.MotionBlur;
import ru.recoin.implement.features.modules.misc.EventSounds;
import ru.recoin.implement.features.modules.render.NoRender;
import ru.recoin.implement.features.modules.render.Optimization;
import ru.recoin.implement.features.modules.render.Holiday;
import ru.recoin.implement.features.modules.render.MusicHud;
import ru.recoin.implement.features.modules.misc.minigames.MineQuiz;
import ru.recoin.implement.features.modules.misc.Shrift;
import ru.recoin.implement.features.modules.render.hitcolor.HitColor;
import ru.recoin.implement.features.modules.render.hitcolor.HitColorHitbox;
import ru.recoin.implement.features.modules.misc.minigames.Upgrader;
import ru.recoin.implement.features.modules.render.OpponentCooldowns;
import ru.recoin.implement.features.modules.render.PenisESP;
import ru.recoin.implement.features.modules.render.PotionTracker;
import ru.recoin.implement.features.modules.render.ProjectilePrediction;
import ru.recoin.implement.features.modules.render.Rain;
import ru.recoin.implement.features.modules.render.SeeInvisible;
import ru.recoin.implement.features.modules.render.ShaderESP;
import ru.recoin.implement.features.modules.render.ShulkerPreview;
import ru.recoin.implement.features.modules.render.SkeletonESP;
import ru.recoin.implement.features.modules.render.SwingAnimations;
import ru.recoin.implement.features.modules.render.TargetESP;
import ru.recoin.implement.features.modules.render.Trails;
import ru.recoin.implement.features.modules.render.ViewModel;
import ru.recoin.implement.features.modules.render.WardenHelper;
import ru.recoin.implement.features.modules.render.WetWorld;
import ru.recoin.implement.features.modules.render.Weather;
import ru.recoin.implement.features.modules.render.Winter;
import ru.recoin.implement.features.modules.render.WorldLightning;
import ru.recoin.implement.features.modules.render.WorldTweaks;
import ru.recoin.implement.features.modules.render.XRay;
import ru.recoin.implement.features.modules.render.cosmetics.Cosmetics;

@Native
public class ModuleRepository {
    private final List<Module> modules = new ArrayList<Module>();

    @Native
    public void setup() {
        AutoInventory autoInventory = new AutoInventory();
        this.register(new ClickGuiModule(), new ServerHelper(), new RPSpoofer(), new AutoMessage(), new AutoDuel(), autoInventory, new InventoryManager(autoInventory), new ActionTracker(), new WaterSpeed(), new NoFall(), new LongJump(), new AirJump(), new Jesus(), new NoClip(), new ElytraFly(), new ElytraMotion(), new FastBow(), new LeaveTracker(), new EnderChestPlus(), new AnyDesk(), new ClickAction(), new KeyFinder(), new Unhook(), new PvPSafe(), new ChatHelper(), new Sounds(), new ItemScroller(), new LockSlot(), new AppleFarm(), new Hud(), new CooldownHotbar(), new AuctionHelper(), new AutoSell(), new TelegramAPI(), new AuctionRelist(), new ProjectilePrediction(), new PotionTracker(), new OpponentCooldowns(), new HealthHelper(), new HealthBypass(), new BlockOverlay(), new Box(), new JumpCircle(), new ShulkerPreview(), new AmbientParticles(), new Trails(), new WorldLightning(), new XRay(), new MineHelper(), new ItemSearcher(), new Items2D(), new FullBright(), new KillAuraModule(), new Aura(), new AimAssist(), new AimBot(), new AutoSwap(), new SpearBoost(), new ShiftTap(), new MaceHelper(), new Velocity(), new NoFriendDamage(), new NoServerRotation(), new HitBoxModule(), new AntiBot(), new AutoCrystal(), new AutoExplosion(), new WebTrap(), new PacketCriticals(), new AutoSprint(), new AirStuck(), new Speed(), new Timer(), new PitBike(), new Strafe(), new GrimGlide(), new NoPush(), new ElytraHelper(), new ElytraTarget(), new ClanUpgrade(), new NoDelay(), new AntiAFK(), new AutoRespawn(), new AutoJump(), new AutoFish(), new SwordWarm(), new FakePlayer(), new GodMode(), new FastExp(), new NoSlow(), new NoWeb(), new ScreenWalk(), new Blink(), new AutoTool(), new AncientXRay(), new Nuker(), new FastBreak(), new AspectRatio(), new CameraTweaks(), new SwingAnimations(), new Animations(), new ViewModel(), new HandsShader(), new EntityESP(), new SkeletonESP(), new PenisESP(), new ShaderESP(), new MotionBlur(), new Contrast(), new WetWorld(), new Winter(), new Atmosphere(), new Rain(), new Weather(), new Cosmetics(), new Optimization(), new Holiday(), new EventSounds(), new MusicHud(), new MineQuiz(), new Upgrader(), new Shrift(), new HitColor(), new HitColorHitbox(), new ru.recoin.implement.features.modules.render.SmoothCamera(), new ru.recoin.implement.features.modules.render.Shader(), new ru.recoin.implement.features.modules.misc.bob.Bob(), new ru.recoin.implement.features.modules.misc.browser.WebBrowser(), new TargetESP(), new HitBubble(), new AttackEffect(), new KillEffect(), new AutoBuy(), new AutoTrade(), new BaseFinder(), new CreeperFarm(), new AncientFarmer(), new AnchorFarming(), new AutoEnd(), new WardenHelper(), new AutoTotem(), new AutoPotion(), new FreeCam(), new TriggerBot(), new ContainerStealer(), new AutoAccept(), new AutoAuth(), new Arrows(), new AutoLeave(), new WorldTweaks(), new NoRender(), new TargetPearl(), new NameProtect(), new FlyModule(), new DragonFly(), new SeeInvisible(), new AutoArmor(), new AutoUse(), new ItemHelper(), new AutoFarm(), new OpenWalls(), new TapeMouse(), new NoInteract(), new CrossHair(), new FireWorkBooster(), new Spider(), new ServerJoiner());
        // AutoWarden links against Baritone at class-load time. Referencing the class directly
        // makes the JVM verify (and load) it while entering setup(), which used to abort the
        // whole client with NoClassDefFoundError. Load it by name so a missing Baritone only
        // disables this one module.
        try {
            Module autoWarden = (Module)Class.forName("ru.recoin.implement.features.modules.misc.AutoWarden")
                    .getDeclaredConstructor().newInstance();
            this.register(autoWarden);
        } catch (Throwable missing) {
            LoggerUtil.info("[Modules] AutoWarden disabled: " + missing);
        }
    }

    public void register(Module ... module) {
        this.modules.addAll(List.of(module));
    }

    public List<Module> modules() {
        return this.modules;
    }
}

