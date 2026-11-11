package com.carloship.craft.proxy.commands.infra;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class ServerInfoCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        BytePlayerModule playerModule = Main.getInstance().getPlayerModule();

        if (!(source instanceof Player player)) {
            source.sendMessage(TextUtils.COMMAND_ONLY_PLAYERS);
            return;
        }

        BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
        if (!Group.hasPermission(playerNetwork.getGroup(), Groups.ADMIN)) {
            player.sendMessage(TextUtils.noPermission());
            return;
        }

        var args = invocation.arguments();
        if (args.length == 0){
            player.sendMessage(DMM("<red>Use: /serverinfo <server>"));
            return;
        }

        Main.proxy().getServer(args[0]).ifPresentOrElse(server -> {
            server.ping().whenComplete((ping, throwable) -> {
                if (throwable != null){
                    player.sendMessage(DMM("<red>Ops... we couldn't get info from server " + args[0]));
                    log.error("Error get server info", throwable);
                    return;
                }

                Main.getInstance().getNetworkProxyModule().getRepository().getServer(args[0]).ifPresentOrElse(networkServer -> {
                    player.sendMessage(DMM("<green>Server info: " + args[0]));
                    player.sendMessage(DMM("<gray>Connected players: <yellow>" + networkServer.getPlayers() + "<dark_gray>/<gold>" + networkServer.getMaxPlayers()));
                    player.sendMessage(DMM("<gray>Protocol version: <yellow>" + ping.getVersion()));
                    player.sendMessage(DMM("<gray>Server ID: <white>" + networkServer.getServerId()));
                    player.sendMessage(DMM("<gray>Game ID: <white>" + networkServer.getGameId()));
                    player.sendMessage(DMM("<gray>Cluster ID: <white>" + networkServer.getClusterId()));
                    player.sendMessage(DMM("<gray>Server Type: <white>" + networkServer.getServerType()));
                }, () -> player.sendMessage(DMM("<red>We couldn't complete server info...")));

            });
        }, () -> player.sendMessage(DMM("<red>Server not found...")));

    }


    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        BytePlayerModule playerModule = Main.getInstance().getPlayerModule();

        if (!(source instanceof Player player)) return List.of();

        BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
        if (!Group.hasPermission(playerNetwork.getGroup(), Groups.ADMIN)) return List.of();

        if (invocation.arguments().length == 0){
            return Main.proxy().getAllServers().stream().map(s -> s.getServerInfo().getName())
                    .toList();
        }

        return List.of();
    }
}
