package com.carloship.craft.sdk;

import com.carloship.craft.sdk.cluster.NetworkModule;
import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.module.ModuleHolder;
import com.carloship.craft.sdk.module.ModuleService;
import com.carloship.craft.sdk.module.ModuleServiceRegistry;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.DefaultBlockRegister;
import com.carloship.craft.sdk.server.chataction.ChatActionDispatcher;
import com.carloship.craft.sdk.server.commands.FeedCommand;
import com.carloship.craft.sdk.server.commands.FlyCommand;
import com.carloship.craft.sdk.server.commands.GamemodeCommand;
import com.carloship.craft.sdk.server.commands.HealCommand;
import com.carloship.craft.sdk.server.common.GameHotBarManager;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceFactory;
import com.carloship.craft.sdk.server.instance.ServerInstanceManager;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.instance.command.CreateInstanceCommand;
import com.carloship.craft.sdk.server.instance.command.DestroyInstanceCommand;
import com.carloship.craft.sdk.server.instance.command.GotoInstanceCommand;
import com.carloship.craft.sdk.server.instance.content.InstanceProfileManager;
import com.carloship.craft.sdk.server.instance.event.ServerInstanceCreateEvent;
import com.carloship.craft.sdk.server.instance.event.ServerInstanceDestroyEvent;
import com.carloship.craft.sdk.server.instance.event.player.PlayerInstanceJoinEvent;
import com.carloship.craft.sdk.server.instance.event.player.PlayerInstanceQuitEvent;
import com.carloship.craft.sdk.server.instance.event.player.PlayerSwitchInstanceEvent;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.server.player.loader.PlayerDataLoaderManager;
import com.carloship.craft.sdk.server.player.loader.PlayerDataLoaderProcessor;
import com.carloship.craft.sdk.server.player.loader.PlayerLoaderManager;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardAnimation;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardManager;
import com.carloship.craft.sdk.server.skin.SkinCache;
import com.carloship.craft.sdk.server.skin.SkinCommand;
import com.carloship.craft.sdk.server.skin.SkinManager;
import com.carloship.craft.sdk.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
@Getter
public abstract class SDKServer<P extends BytePlayerCraft> {

    public static SDKServer<?> INSTANCE;

    private final MinecraftServer minecraftServer;
    private final SchedulerManager schedulerManager;

    private final int port;
    private final String host;

    private final ModuleService moduleService;
    private final ModuleHolder moduleHolder;
    private final ModuleServiceRegistry moduleServiceRegistry;

    private final ServerInstanceManager<P> instanceManager;

    private final ScoreBoardManager scoreBoardManager;

    private final InstanceProfileManager<P> instanceProfileManager;

    private final PlayerDataLoaderProcessor playerDataProcessor;
    private final PlayerDataLoaderManager playerDataLoadManager;
    private final PlayerLoaderManager playerLoaderManager;
    private final GameHotBarManager hotBarManager;

    private BytePlayerModule bytePlayerModule;
    private NetworkModule networkMinestomModule;

    private final ConcurrentHashMap<String, Long> onlinePlayers = new ConcurrentHashMap<>();

    @Setter
    private SDKServerType serverType;

    public SDKServer(MinecraftServer server, int port, String host) {
        this.port = port;
        this.host = host;

        String redisURL = SDKEnvironment.REDIS_URL.getString("redis://localhost:6379");
        String mongoURL = SDKEnvironment.MONGO_URL.getString("mongodb://localhost:27017");

        this.moduleService = new ModuleService(null, redisURL, mongoURL);
        this.moduleHolder = moduleService.getHolder();
        this.moduleServiceRegistry = moduleService.getRegistry();

        this.minecraftServer = server;
        this.schedulerManager = MinecraftServer.getSchedulerManager();

        this.instanceManager = new ServerInstanceManager<>(this);

        this.scoreBoardManager = new ScoreBoardManager();
        this.instanceProfileManager = new InstanceProfileManager<>(this);

        this.playerLoaderManager = new PlayerLoaderManager();
        this.playerDataProcessor = playerLoaderManager.getPlayerDataProcessor();
        this.playerDataLoadManager = playerLoaderManager.getPlayerDataLoadManager();

        this.hotBarManager = new GameHotBarManager();
    }

    public abstract void onStart();

    public abstract void onShutdown();

    @NotNull
    public abstract NetworkServer getServerPayload();

    public abstract List<Class<? extends SDKModule>> serverModules();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public abstract Class<? extends ServerInstanceMode> getEnumModes();

    protected abstract P createPlayer(PlayerConnection playerConnection, GameProfile gameProfile);

    protected abstract ServerInstanceFactory<P> getServerFactory();

    private void registryModules() {
        moduleHolder.registerModule(BytePlayerModule.class);
        moduleHolder.registerModule(NetworkModule.class);
        serverModules().forEach(moduleHolder::registerModule);
    }

    private void scheduleServer() {
        schedulerManager.scheduleTask(playerDataProcessor::tick, TaskSchedule.nextTick(), TaskSchedule.tick(10));
        schedulerManager.submitTask(() -> {
            scoreBoardManager.updateTitles();
            return TaskSchedule.tick(5);
        });

        schedulerManager.submitTask(() -> {
            scoreBoardManager.updateLines();
            return TaskSchedule.tick(10);
        });

        schedulerManager.scheduleTask(() -> {
            getInstanceManager().getRunningInstances()
                    .forEach(instance -> instance.getHologramManager().renderAll());
        }, TaskSchedule.nextTick(), TaskSchedule.seconds(1));

        schedulerManager.scheduleTask(
                () -> onlinePlayers.putAll(networkMinestomModule.getRepository().getOnlinePlayersAllGames()),
                TaskSchedule.nextTick(),
                TaskSchedule.seconds(5)
        );
    }

    public void startServer() {
        MinecraftServer.setBrandName("ByteBuilder");
        MinecraftServer.getExceptionManager().setExceptionHandler(e -> log.error("Internal error ", e));
        MinecraftServer.getConnectionManager().setPlayerProvider(this::createPlayer);

        moduleServiceRegistry.register(NetworkServer.class, getServerPayload());

        registryModules();

        moduleHolder.startModules();

        bytePlayerModule = extractModule(BytePlayerModule.class);

        networkMinestomModule = extractModule(NetworkModule.class);
        networkMinestomModule.register();

        onStart();

        DefaultBlockRegister.initRegisters();

        playerLoaderManager.registryRefreshListeners(this);

        MinecraftServer.getCommandManager().register(
                new FlyCommand(),
                new GamemodeCommand(),
                new HealCommand(),
                new FeedCommand(),
                new SkinCommand(getBytePlayerModule()),
                new CreateInstanceCommand(this, getEnumModes()),
                new DestroyInstanceCommand(this, getEnumModes()),
                new GotoInstanceCommand(this, getEnumModes())
        );

        MinecraftServer.getGlobalEventHandler()
                .addChild(InstanceProfileManager.EVENT_NODE)
                .addChild(SkinManager.EVENT_NODE);

        registerInstanceEvents();

        SDKEventListener.init(this);
        ChatActionDispatcher.init();
        //PunishHook.init(this);
        SkinCache.loadAllSkins();

        scheduleServer();

        minecraftServer.start(host, port);

        instanceManager.createInstance(getMainMode());

        DefaultBlockRegister.initRegisters();
    }

    public void stopServer() {
        schedulerManager.buildShutdownTask(() -> {
            networkMinestomModule.closeServer();
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> networkMinestomModule.transferPlayerToGame(player.getUuid(), "lobby"));
            networkMinestomModule.unregister();
            onShutdown();
        });
    }

    @SuppressWarnings("unchecked")
    public static <P extends BytePlayerCraft> SDKServer<P> getTypedInstance() {
        return (SDKServer<P>) INSTANCE;
    }

    public ServerInstance<P> factoryServer(ServerInstanceMode mode, boolean reply) {
        return getServerFactory().createServer(mode, this, reply);
    }

    public ServerInstanceMode getMainMode() {
        return getServerFactory().getMainMode();
    }

    public ServerInstance<P> getPlayerInstance(BytePlayerCraft player) {
        return getServerInstance(player.getCurrentServerInstanceId());
    }

    public ServerInstance<P> getServerInstance(UUID uuid) {
        return getInstanceManager().getServerInstance(uuid);
    }

    public <M extends SDKModule> M extractModule(Class<M> mClass) {
        return moduleHolder.getModule(mClass);
    }

    public void registerScoreboardAnimation(ScoreBoardAnimation animation) {
        scoreBoardManager.registerAnimation(animation);
    }

    public void proxiedJoinPlayer(UUID uuid, String name) {
        networkMinestomModule.trackPlayerJoin(uuid, name);
    }

    public void proxiedLeavePlayer(UUID uuid) {
        networkMinestomModule.trackPlayerLeave(uuid);
    }


    public int getOnlineBytePlayerData() {
        return onlinePlayers.values().stream().mapToInt(Long::intValue).sum();
    }

    public long countPlayersInServer(String gameId) {
        return onlinePlayers.getOrDefault(gameId, -1L);
    }

    public Component getDefaultChatFormatter(P serverPlayer, PlayerChatEvent event) {
        int level = serverPlayer.getPlayerData().getLeveling().getLevel();
        Component playerTag = serverPlayer.getNametag();

        Component startMessage = DMM("<gray>[" + level + "]");
        startMessage = startMessage.appendSpace().append(playerTag).append(DMM("<gray>: "));

        return startMessage.append(DMM(event.getRawMessage()));
    }

    public static String resolveServerId(String prefix) {
        Path idFile = Path.of(".server-id");

        if (Files.exists(idFile)) {
            try {
                String saved = Files.readString(idFile).trim();
                if (!saved.isBlank()) return saved;
            } catch (IOException e) {
                log.warn("Failed to read .server-id, generating new one");
            }
        }

        String generated = prefix + "-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            Files.writeString(idFile, generated);
        } catch (IOException e) {
            log.warn("Failed to persist .server-id");
        }

        return generated;
    }


    private void registerInstanceEvents() {
        EventNode<Event> instanceSystemNode = EventNode.all("sdk-instance-system");

        instanceSystemNode
                .addListener(PlayerSpawnEvent.class, event -> {
                    if (!(event.getPlayer() instanceof BytePlayerCraft p)) return;
                    @SuppressWarnings("unchecked")
                    P player = (P) p;

                    UUID currentInstance = player.getCurrentServerInstanceId();
                    ServerInstance<P> serverInstance = getInstanceManager().getServerInstance(currentInstance);

                    if (serverInstance == null) {
                        player.kick(TextUtils.INTERNAL_ERROR);
                        log.error("Cannot find instance for player in spawn event");
                        return;
                    }

                    boolean firstLogin = event.isFirstSpawn();

                    EventDispatcher.call(new PlayerInstanceJoinEvent<>(serverInstance, player, firstLogin));

                    var tabList = serverInstance.getTabList();

                    if (tabList != null){
                        player.sendPlayerListHeaderAndFooter(tabList.left(), tabList.right());
                    }
                })
                .addListener(PlayerSwitchInstanceEvent.class, rawEvent -> {
                    @SuppressWarnings("unchecked")
                    PlayerSwitchInstanceEvent<BytePlayerCraft> event = (PlayerSwitchInstanceEvent<BytePlayerCraft>) rawEvent;

                    var lobbyPlayer = event.player();
                    var oldInstance = event.oldInstance();
                    var newInstance = event.newInstance();

                    if (oldInstance != null) {
                        EventDispatcher.call(new PlayerInstanceQuitEvent<>(oldInstance, lobbyPlayer));
                    }

                    lobbyPlayer.setCurrentInstance(newInstance);

                    Pos pos = event.newInstancePos() != null ? event.newInstancePos() : newInstance.getSpawnPoint();

                    lobbyPlayer.setInstance(newInstance.getInstance(), pos)
                            .exceptionally(ex -> {
                                lobbyPlayer.sendErrorMessage("An error occurred while sending you to another lobby!");
                                log.error("Error transferring player {} to instance {}", lobbyPlayer.getUsername(), newInstance.getServerId(), ex);
                                return null;
                            });
                })
                .addListener(PlayerInstanceJoinEvent.class, rawEvent -> {
                    @SuppressWarnings("unchecked")
                    PlayerInstanceJoinEvent<P> event = (PlayerInstanceJoinEvent<P>) rawEvent;

                    var player = event.player();
                    var serverInstance = event.instance();

                    serverInstance.loadPlayer(player, event.firstLogin());
                })
                .addListener(PlayerInstanceQuitEvent.class, rawEvent -> {
                    @SuppressWarnings("unchecked")
                    PlayerInstanceQuitEvent<P> event = (PlayerInstanceQuitEvent<P>) rawEvent;

                    var player = event.player();
                    var instance = event.instance();

                    instance.getHologramManager().removeViewer(player);
                    player.hidePlayers();
                    instance.onPlayerLeave(player);
                    getInstanceProfileManager().onInstanceQuit(player, instance);
                })
                .addListener(ServerInstanceDestroyEvent.class, rawEvent -> {
                    @SuppressWarnings("unchecked")
                    ServerInstanceDestroyEvent<BytePlayerCraft> event = (ServerInstanceDestroyEvent<BytePlayerCraft>) rawEvent;

                    var serverInstance = event.instance();

                    for (BytePlayerCraft player : serverInstance.getPlayers()) {
                        if (event.fallbackInstance() == null) {
                            player.kick(TextUtils.INTERNAL_ERROR);
                            continue;
                        }

                        EventDispatcher.call(new PlayerSwitchInstanceEvent<>(player, serverInstance, event.fallbackInstance(), null));
                    }
                })
                .addListener(PlayerMoveEvent.class, event -> {
                    dispatchToInstance(event.getPlayer(), (instance, player) -> instance.onPlayerMove(player, event));
                })
                .addListener(PlayerUseItemEvent.class, event -> {
                    dispatchToInstance(event.getPlayer(), (instance, player) -> instance.onPlayerInteract(player, event.getItemStack(), null));
                })
                .addListener(PlayerEntityInteractEvent.class, event -> {
                    dispatchToInstance(event.getPlayer(), (instance, player) -> instance.onPlayerInteract(player, null, event.getTarget()));
                })
                .addListener(ServerInstanceCreateEvent.class, event -> {
                    var instance = event.instance();
                    var score = instance.getScoreboard();
                    if (score != null) {
                        registerScoreboardAnimation(instance.getScoreboard().animation());
                        log.info("Registering {}={} scoreboard", instance.getInstanceMode().getId(), instance.getId());
                    } else {
                        log.error("Scoreboard from {}={} is null", instance.getInstanceMode().getId(), instance.getId());
                    }
                });

        MinecraftServer.getGlobalEventHandler().addChild(instanceSystemNode);
    }

    @SuppressWarnings("unchecked")
    private <T extends BytePlayerCraft> void dispatchToInstance(Player player, BiConsumer<ServerInstance<T>, T> action) {
        if (player instanceof BytePlayerCraft sdkPlayer) {
            ServerInstance<T> instance = (ServerInstance<T>) getPlayerInstance((P) sdkPlayer);
            if (instance != null) {
                action.accept(instance, (T) sdkPlayer);
            }
        }
    }

}
