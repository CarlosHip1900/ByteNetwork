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
public class GotoPlayerCommand implements SimpleCommand {

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
            player.sendMessage(DMM("<red>Use: /gotoplayer <player>"));
            return;
        }

        if (args.length == 1) {
            String playerName = args[0];
            Main.proxy().getPlayer(playerName).ifPresentOrElse(target -> {
                Component targetTag = playerModule.fetchEntitySync(target.getUniqueId()).getTag();

                target.getCurrentServer().ifPresentOrElse(targetServer -> {
                    player.sendMessage(DMM("<gray>Connecting with " + targetServer.getServer().getServerInfo().getName() + "..."));
                    player.createConnectionRequest(targetServer.getServer()).connect().whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            player.sendMessage(DMM("<red>Ops.. we couldn't connect to this server..."));
                            log.error("Error try connect in server", throwable);
                            return;
                        }

                        if (result.isSuccessful()) {
                            player.sendMessage(DMM("<gray>Connected..."));
                            return;
                        }

                        switch (result.getStatus()){
                            case ALREADY_CONNECTED -> ProxyHelper.sendTeleportRequest(player, target, true);
                            case SERVER_DISCONNECTED -> player.sendMessage(DMM("<red>The server disconnected from the back-end"));
                            case CONNECTION_CANCELLED -> player.sendMessage(DMM("<red>Back-end has terminated connection to this server"));
                            case CONNECTION_IN_PROGRESS -> player.sendMessage(DMM("<red>The connection is already in progress..."));
                            case SUCCESS -> player.sendMessage(DMM("<gray>Success..."));
                        }

                        player.sendMessage(DMM("<red>We couldn't connect you to the player server ").append(targetTag));
                    });
                }, () -> player.sendMessage(DMM("<red>We couldn't connect you to the player server ").append(targetTag)));
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
