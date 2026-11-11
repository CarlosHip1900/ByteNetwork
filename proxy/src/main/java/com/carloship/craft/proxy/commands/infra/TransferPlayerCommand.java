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
import net.kyori.adventure.text.Component;

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class TransferPlayerCommand implements SimpleCommand {

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
            player.sendMessage(DMM("<red>Usage: /transferplayer <player> <server>"));
            return;
        }

        if (args.length == 2) {
            String playerName = args[0];
            String serverName = args[1];

            Main.proxy().getPlayer(playerName).ifPresentOrElse(target -> {
                Component targetTag = playerModule.fetchEntitySync(target.getUniqueId()).getTag();
                Main.proxy().getServer(serverName).ifPresentOrElse(server -> {
                    player.sendMessage(DMM("<gray>Sending player ").append(targetTag).append(DMM("<gray> to server " + server.getServerInfo().getName())));

                    target.createConnectionRequest(server).connect().whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            player.sendMessage(DMM("<red>Oops... we couldn't send the player ").append(targetTag).append(DMM("<red> to the server...")));
                            return;
                        }

                        if (result.isSuccessful()) {
                            player.sendMessage(DMM("<green>You successfully transferred player ").append(targetTag).append(DMM("<green> to server " + server.getServerInfo().getName())));

                            target.sendMessage(DMM("<green>You have been forcibly transferred to this server..."));
                            return;
                        }

                        player.sendMessage(DMM("<red>Unable to transfer the player to server " + server.getServerInfo().getName()));
                    });
                }, () -> player.sendMessage(DMM("<red>Could not find that server")));
            }, () -> player.sendMessage(DMM("<red>Could not find that player")));
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
            case 1 -> Main.proxy().getAllServers().stream().map(s -> s.getServerInfo().getName()).toList();
            default -> List.of();
        };
    }
}