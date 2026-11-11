package com.carloship.craft.proxy.commands.infra;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.proxy.ProxyHelper;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class PullPlayerCommand implements SimpleCommand {

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

        if (args.length == 0) {
            player.sendMessage(DMM("<red>Use: /pullplayer <player>"));
            return;
        }

        if (args.length == 1) {
            String playerName = args[0];
            Main.proxy().getPlayer(playerName).ifPresentOrElse(target -> {
                Component targetTag = playerModule.fetchEntitySync(target.getUniqueId()).getTag();

                player.getCurrentServer().ifPresentOrElse(targetServer -> {
                    player.sendMessage(DMM("<gray>Pulling the player ").append(targetTag).append(DMM("<gray> to this server...")));

                    target.createConnectionRequest(targetServer.getServer()).connect()
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    player.sendMessage(DMM("<red>Ops.. we couldn't pull ").append(targetTag).append(DMM("<red> to this server")));
                                    log.error("Error try connect in server", throwable);
                                    return;
                                }

                                if (result.isSuccessful()) {
                                    player.sendMessage(DMM("<gray>You pulled the player ").append(targetTag).append(DMM("<gray> to your server")));
                                    target.sendMessage(DMM("<gray>The player ").append(targetTag).append(DMM("<gray> connected you to his server")));
                                    return;
                                }

                                switch (result.getStatus()) {
                                    case ALREADY_CONNECTED -> ProxyHelper.sendTeleportRequest(target, player, false);
                                    case CONNECTION_CANCELLED ->
                                            player.sendMessage(DMM("<red>The player ").append(targetTag).append(DMM("<red> connection was canceled")));
                                    case CONNECTION_IN_PROGRESS ->
                                            player.sendMessage(DMM("<red>The player ").append(targetTag).append(DMM("<red> connection still already in progress")));
                                    case SERVER_DISCONNECTED ->
                                            player.sendMessage(DMM("<red>The server was disconnected"));
                                    case SUCCESS -> player.sendMessage(DMM("<gray>Connected..."));
                                }

                                player.sendMessage(DMM("<red>Unable to connect to the player's server ").append(targetTag));
                            });

                }, () -> player.sendMessage(DMM("<red>Unable to connect to the player's server ").append(targetTag)));

            }, () -> player.sendMessage(DMM("<red>Player not found...")));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        BytePlayerModule playerModule = Main.getInstance().getPlayerModule();

        if (!(source instanceof Player player)) return List.of();

        BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
        if (!Group.hasPermission(playerNetwork.getGroup(), Groups.ADMIN)) return List.of();

        return switch (invocation.arguments().length) {
            case 0 -> Main.getOnlinePlayerNames();
            default -> List.of();
        };
    }
}
