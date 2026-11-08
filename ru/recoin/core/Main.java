/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ModInitializer
 *  net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 */
package ru.recoin.core;

import com.adl.nativeprotect.Native;
import java.io.File;
import java.lang.reflect.Method;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.maksaidev.atmosphere.weather.WeatherModule;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import ru.recoin.api.event.EventManager;
import ru.recoin.api.feature.draggable.DraggableRepository;
import ru.recoin.api.feature.module.ModuleProvider;
import ru.recoin.api.feature.module.ModuleRepository;
import ru.recoin.api.feature.module.ModuleSwitcher;
import ru.recoin.api.file.DirectoryCreator;
import ru.recoin.api.file.FileController;
import ru.recoin.api.file.FileRepository;
import ru.recoin.api.file.exception.FileProcessingException;
import ru.recoin.api.mods.geckolib.GeckoLib;
import ru.recoin.api.mods.geckolib.GeckoLibClient;
import ru.recoin.api.repository.box.BoxESPRepository;
import ru.recoin.api.repository.macro.MacroRepository;
import ru.recoin.api.repository.rct.RCTRepository;
import ru.recoin.api.repository.way.WayRepository;
import ru.recoin.api.system.discord.DiscordManager;
import ru.recoin.api.system.sound.SoundManager;
import ru.recoin.common.util.logger.LoggerUtil;
import ru.recoin.common.util.logger.RecoinFileLogger;
import ru.recoin.common.util.render.ChinaHatFeatureRenderer;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.client.ClientInfo;
import ru.recoin.core.client.ClientInfoProvider;
import ru.recoin.core.listener.ListenerRepository;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.commands.CommandDispatcher;
import ru.recoin.implement.features.commands.manager.CommandRepository;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackPerpetrator;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeRegistry;
import ru.recoin.implement.features.modules.movement.pitbike.PitBikeRenderer;
import ru.recoin.implement.features.modules.render.WorldTweaks;
import ru.recoin.implement.screens.menu.MenuScreen;

public class Main
implements ModInitializer {
    static Main instance;
    private static final boolean IRIS_LOADED;
    private static boolean irisApiInitialized;
    private static Object irisApi;
    private static Method irisPackInUse;
    private EventManager eventManager = new EventManager();
    private ModuleRepository moduleRepository;
    private ModuleSwitcher moduleSwitcher;
    private CommandRepository commandRepository;
    private CommandDispatcher commandDispatcher;
    private BoxESPRepository boxESPRepository = new BoxESPRepository(this.eventManager);
    private MacroRepository macroRepository = new MacroRepository(this.eventManager);
    private WayRepository wayRepository = new WayRepository(this.eventManager);
    private RCTRepository RCTRepository = new RCTRepository(this.eventManager);
    private WorldRenderContext shaderPackRenderContext;
    private ModuleProvider moduleProvider;
    private DraggableRepository draggableRepository;
    private DiscordManager discordManager;
    private FileRepository fileRepository;
    private FileController fileController;
    private ScissorManager scissorManager = new ScissorManager();
    private ClientInfoProvider clientInfoProvider;
    private ListenerRepository listenerRepository;
    private AttackPerpetrator attackPerpetrator = new AttackPerpetrator();
    private boolean initialized;

    public void onInitialize() {
        instance = this;
        ru.recoin.core.license.LicenseGate.get().start();   // online licence (HWID-bound, server-signed)
        PitBikeRegistry.initialize();
        EntityRendererRegistry.register(PitBikeRegistry.TYPE, PitBikeRenderer::new);
        this.initClientInfoProvider();
        this.initModules();
        this.initDraggable();
        this.initFileManager();
        this.initCommands();
        this.initListeners();
        this.initRenderCallbacks();
        WeatherModule.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> WeatherModule.tick(client));
        this.initDiscordRPC();
        SoundManager.init();
        MenuScreen.INSTANCE.initialize();
        new GeckoLib().onInitialize();
        new GeckoLibClient().onInitializeClient();
        this.initialized = true;
    }

    private void initDraggable() {
        this.draggableRepository = new DraggableRepository();
        this.draggableRepository.setup();
    }

    @Native
    private void initModules() {
        this.moduleRepository = new ModuleRepository();
        this.moduleRepository.setup();
        this.moduleProvider = new ModuleProvider(this.moduleRepository.modules());
        this.moduleSwitcher = new ModuleSwitcher(this.moduleRepository.modules(), this.eventManager);
    }

    private void initCommands() {
        this.commandRepository = new CommandRepository();
        this.commandDispatcher = new CommandDispatcher(this.eventManager);
    }

    private void initDiscordRPC() {
        this.discordManager = new DiscordManager();
        this.discordManager.init();
    }

    @Native
    private void initClientInfoProvider() {
        File clientDirectory = new File(class_310.method_1551().field_1697, "dettexclient");
        File filesDirectory = new File(clientDirectory, "files");
        File moduleFilesDirectory = new File(filesDirectory, "config");
        String role = "PLAYER";
        String days = "";
        try {
            Object gate = Class.forName("ru.recoin.core.license.LicenseGate").getMethod("get").invoke(null);
            if (gate != null) {
                Object state = gate.getClass().getField("state").get(gate);
                if (state != null) {
                    try {
                        Object rt = state.getClass().getField("rankText").get(state);
                        if (rt != null) role = String.valueOf(rt);
                    } catch (Throwable ignored) {}
                    try {
                        int r = state.getClass().getField("rank").getInt(state);
                        if (role.equals("PLAYER")) {
                            if (r == 3) role = "ADMIN";
                            else if (r == 2) role = "ADMINISTRATOR";
                            else if (r == 1) role = "MEDIA";
                        }
                    } catch (Throwable ignored) {}
                    try {
                        int dl = state.getClass().getField("daysLeft").getInt(state);
                        if (dl >= 0) days = String.valueOf(dl);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {
        }
        this.clientInfoProvider = new ClientInfo("DettexClient" + (days.isEmpty() ? "" : " | " + days + "d"), "RAKE", role.toUpperCase(), clientDirectory, filesDirectory, moduleFilesDirectory);
    }

    @Native
    private void initFileManager() {
        DirectoryCreator directoryCreator = new DirectoryCreator();
        directoryCreator.createDirectories(this.clientInfoProvider.clientDir(), this.clientInfoProvider.filesDir(), this.clientInfoProvider.configsDir());
        this.fileRepository = new FileRepository();
        this.fileRepository.setup(this);
        this.fileController = new FileController(this.fileRepository.getClientFiles(), this.clientInfoProvider.filesDir(), this.clientInfoProvider.configsDir());
        try {
            this.fileController.loadFiles();
        }
        catch (FileProcessingException e) {
            LoggerUtil.error("Error occurred while loading files: " + e.getMessage() + " " + String.valueOf(e.getCause()));
        }
    }

    private void initListeners() {
        this.listenerRepository = new ListenerRepository();
        this.listenerRepository.setup();
    }

    private void initRenderCallbacks() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
            class_310 client = class_310.method_1551();
            WorldTweaks tweaks = WorldTweaks.getInstance();
            if (tweaks != null && tweaks.fogBlurEnabled() && client.field_1773 != null) {
                float fov = LegacyRenderSystem.getLastFov();
                if (fov <= 0.0f) {
                    fov = ((Integer)client.field_1690.method_41808().method_41753()).floatValue();
                }
                tweaks.renderFogBlur(client.field_1773.method_22973(fov));
            }
            if (!Main.isShaderPackActive()) {
                this.renderWorldFeatures(context);
            }
        });
        WorldRenderEvents.END_MAIN.register(context -> {
            if (Main.isShaderPackActive()) {
                this.shaderPackRenderContext = context;
            }
        });
    }

    private static boolean isShaderPackActive() {
        if (!IRIS_LOADED) {
            return false;
        }
        if (!irisApiInitialized) {
            irisApiInitialized = true;
            try {
                Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApi = apiClass.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                irisPackInUse = apiClass.getMethod("isShaderPackInUse", new Class[0]);
            }
            catch (ReflectiveOperationException ignored) {
                return false;
            }
        }
        if (irisPackInUse == null) {
            return false;
        }
        try {
            return (Boolean)irisPackInUse.invoke(irisApi, new Object[0]);
        }
        catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private void renderWorldFeatures(WorldRenderContext context) {
        class_310 client = class_310.method_1551();
        class_4587 matrices = context.matrices();
        if (matrices == null) {
            return;
        }
        float tickProgress = client.method_61966().method_60637(false);
        float cachedFov = LegacyRenderSystem.getLastFov();
        float fov = cachedFov > 0.0f ? cachedFov : ((Integer)client.field_1690.method_41808().method_41753()).floatValue();
        Render3DUtil.setLastProjMat(client.field_1773.method_22973(fov));
        Render3DUtil.setLastWorldSpaceMatrix(matrices.method_23760());
        ChinaHatFeatureRenderer.renderPending();
        WorldRenderEvent event = new WorldRenderEvent(matrices, tickProgress);
        EventManager.callEvent(event);
        Render3DUtil.onWorldRender(event);
    }

    public void renderWorldFeaturesAfterShaderPack() {
        WorldRenderContext context = this.shaderPackRenderContext;
        if (context == null) {
            return;
        }
        this.shaderPackRenderContext = null;
        this.renderWorldFeatures(context);
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public ModuleRepository getModuleRepository() {
        return this.moduleRepository;
    }

    public ModuleSwitcher getModuleSwitcher() {
        return this.moduleSwitcher;
    }

    public CommandRepository getCommandRepository() {
        return this.commandRepository;
    }

    public CommandDispatcher getCommandDispatcher() {
        return this.commandDispatcher;
    }

    public BoxESPRepository getBoxESPRepository() {
        return this.boxESPRepository;
    }

    public MacroRepository getMacroRepository() {
        return this.macroRepository;
    }

    public WayRepository getWayRepository() {
        return this.wayRepository;
    }

    public RCTRepository getRCTRepository() {
        return this.RCTRepository;
    }

    public WorldRenderContext getShaderPackRenderContext() {
        return this.shaderPackRenderContext;
    }

    public ModuleProvider getModuleProvider() {
        return this.moduleProvider;
    }

    public DraggableRepository getDraggableRepository() {
        return this.draggableRepository;
    }

    public DiscordManager getDiscordManager() {
        return this.discordManager;
    }

    public FileRepository getFileRepository() {
        return this.fileRepository;
    }

    public FileController getFileController() {
        return this.fileController;
    }

    public ScissorManager getScissorManager() {
        return this.scissorManager;
    }

    public ClientInfoProvider getClientInfoProvider() {
        return this.clientInfoProvider;
    }

    public ListenerRepository getListenerRepository() {
        return this.listenerRepository;
    }

    public AttackPerpetrator getAttackPerpetrator() {
        return this.attackPerpetrator;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void setModuleRepository(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public void setModuleSwitcher(ModuleSwitcher moduleSwitcher) {
        this.moduleSwitcher = moduleSwitcher;
    }

    public void setCommandRepository(CommandRepository commandRepository) {
        this.commandRepository = commandRepository;
    }

    public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    public void setBoxESPRepository(BoxESPRepository boxESPRepository) {
        this.boxESPRepository = boxESPRepository;
    }

    public void setMacroRepository(MacroRepository macroRepository) {
        this.macroRepository = macroRepository;
    }

    public void setWayRepository(WayRepository wayRepository) {
        this.wayRepository = wayRepository;
    }

    public void setRCTRepository(RCTRepository RCTRepository2) {
        this.RCTRepository = RCTRepository2;
    }

    public void setShaderPackRenderContext(WorldRenderContext shaderPackRenderContext) {
        this.shaderPackRenderContext = shaderPackRenderContext;
    }

    public void setModuleProvider(ModuleProvider moduleProvider) {
        this.moduleProvider = moduleProvider;
    }

    public void setDraggableRepository(DraggableRepository draggableRepository) {
        this.draggableRepository = draggableRepository;
    }

    public void setDiscordManager(DiscordManager discordManager) {
        this.discordManager = discordManager;
    }

    public void setFileRepository(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void setFileController(FileController fileController) {
        this.fileController = fileController;
    }

    public void setScissorManager(ScissorManager scissorManager) {
        this.scissorManager = scissorManager;
    }

    public void setClientInfoProvider(ClientInfoProvider clientInfoProvider) {
        this.clientInfoProvider = clientInfoProvider;
    }

    public void setListenerRepository(ListenerRepository listenerRepository) {
        this.listenerRepository = listenerRepository;
    }

    public void setAttackPerpetrator(AttackPerpetrator attackPerpetrator) {
        this.attackPerpetrator = attackPerpetrator;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public static Main getInstance() {
        return instance;
    }

    static {
        RecoinFileLogger.install();
        IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");
    }
}

