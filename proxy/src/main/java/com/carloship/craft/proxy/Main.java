package com.carloship.craft.proxy;

import com.carloship.craft.proxy.commands.*;
import com.carloship.craft.proxy.commands.infra.*;
import com.carloship.craft.proxy.network.NetworkProxySettings;
import com.carloship.craft.proxy.network.NetworkServerListener;
import com.carloship.craft.proxy.network.NetworkService;
import com.carloship.craft.proxy.player.PlayerEmailService;
import com.carloship.craft.proxy.player.PlayerMessageListener;
import com.carloship.craft.proxy.player.ProxyPlayerListener;
import com.carloship.craft.proxy.punish.PunishMessageListener;
import com.carloship.craft.proxy.report.ReportCommand;
import com.carloship.craft.proxy.report.ReportListener;
import com.carloship.craft.proxy.revoke.RevokeCommand;
import com.carloship.craft.proxy.revoke.RevokeMessageListener;
import com.carloship.craft.proxy.whitelist.ProxyWhitelistCache;
import com.carloship.craft.sdk.SDKEnvironment;
import com.carloship.craft.sdk.cluster.NetworkProxyModule;
import com.carloship.craft.sdk.module.ModuleHolder;
import com.carloship.craft.sdk.module.ModuleService;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.server.punish.PunishModule;
import com.carloship.craft.sdk.server.report.ReportModule;
import com.carloship.craft.sdk.utils.EnvVariable;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Plugin(id = "proxy", name = "Proxy", version = "0.1-SNAPSHOT")
@Getter
public class Main {

    @Getter
    private static Main instance;

    @Getter(AccessLevel.PRIVATE)
    private final ProxyServer server;
    private final Logger logger;

    private final Path dataDirectory;

    private final ModuleService moduleService;
    private final ModuleHolder moduleHolder;

    private NetworkProxyModule networkProxyModule;
    private NetworkProxySettings settings;

    private ProxyWhitelistCache whitelistCache;

    private BytePlayerModule playerModule;

    private PunishModule punishModule;
    private ReportModule reportModule;

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws IOException {
        instance = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        String REDIS_URL = SDKEnvironment.REDIS_URL.getString("redis://localhost:6379");
        String MONGO_URL = SDKEnvironment.MONGO_URL.getString("mongodb://localhost:27017");

        this.moduleService = new ModuleService(null, REDIS_URL, MONGO_URL);
        this.moduleHolder = moduleService.getHolder();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.settings = NetworkProxySettings.createDefault();

        server.getChannelRegistrar().register(ProxyHelper.CHANNEL_IDENTIFIER);
        server.getChannelRegistrar().register(PlayerMessageListener.CHANNEL_IDENTIFIER);

        moduleHolder.registerModule(BytePlayerModule.class);
        moduleHolder.registerModule(PunishModule.class);
        moduleHolder.registerModule(ReportModule.class);
        moduleHolder.registerModule(NetworkProxyModule.class);

        this.networkProxyModule = moduleHolder.getModule(NetworkProxyModule.class);

        new NetworkService().initModule(networkProxyModule, server);

        this.playerModule = moduleHolder.getModule(BytePlayerModule.class);
        this.punishModule = moduleHolder.getModule(PunishModule.class);
        this.reportModule = moduleHolder.getModule(ReportModule.class);

        this.whitelistCache = new ProxyWhitelistCache();

        this.moduleService.enable();

        EventManager eventManager = server.getEventManager();

        eventManager.register(this, new NetworkServerListener(server, networkProxyModule, playerModule, whitelistCache));
        eventManager.register(this, new ProxyPlayerListener(playerModule, punishModule));

        eventManager.register(this, new PlayerMessageListener());
        eventManager.register(this, new PunishMessageListener());
        eventManager.register(this, new RevokeMessageListener());
        eventManager.register(this, new ReportListener());
        eventManager.register(this, new PlayerEmailService());

        CommandManager commandManager = server.getCommandManager();

        new GroupCommand(playerModule, server).registryCommands();

        commandManager.register(commandManager.metaBuilder("bit").build(), new BitCommand());

        commandManager.register(commandManager.metaBuilder("motd").build(), new MotdCommand());
        commandManager.register(commandManager.metaBuilder("setmaxplayer").build(), new MaxPlayerCommand());

        commandManager.register(commandManager.metaBuilder("tell").aliases("msg", "message", "dm").build(), new TellCommand(playerModule));

        commandManager.register(commandManager.metaBuilder("broadcast").aliases("bcast", "bc", "infoall").build(), new BroadcastCommand());
        commandManager.register(commandManager.metaBuilder("gotoplayer").aliases("gp").build(), new GotoPlayerCommand());
        commandManager.register(commandManager.metaBuilder("gotoserver").aliases("gs").build(), new GotoServerCommand());
        commandManager.register(commandManager.metaBuilder("pullplayer").build(), new PullPlayerCommand());
        commandManager.register(commandManager.metaBuilder("staffchat").aliases("sc").build(), new StaffChatCommand());
        commandManager.register(commandManager.metaBuilder("transferplayer").aliases("sendplayer").build(), new TransferPlayerCommand());
        commandManager.register(commandManager.metaBuilder("whitelist").build(), new WhitelistServerCommand());
        commandManager.register(commandManager.metaBuilder("serverinfo").build(), new ServerInfoCommand());

        commandManager.register(commandManager.metaBuilder("revogar").build(), new RevokeCommand());
        commandManager.register(commandManager.metaBuilder("report").aliases("reportar").build(), new ReportCommand());

        commandManager.register(commandManager.metaBuilder("lobby").build(), new ServerCommands.LobbyCommand());
        commandManager.register(commandManager.metaBuilder("rankup").build(), new ServerCommands.RankUpCommand());
        commandManager.register(commandManager.metaBuilder("sheepwar").build(), new ServerCommands.SheepWarCommand());

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        moduleService.disable();
    }

    public static ProxyServer proxy() {
        return getInstance().getServer();
    }

    public static <E> CompletableFuture<E> fireEvent(E event) {
        return proxy().getEventManager().fire(event);
    }

    public static List<String> getOnlinePlayerNames() {
        return proxy().getAllPlayers().stream().map(Player::getUsername).toList();
    }

    public static Collection<Player> allPlayers() {
        return proxy().getAllPlayers();
    }
}