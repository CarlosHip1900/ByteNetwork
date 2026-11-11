package com.carloship.craft.proxy.commands.infra;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.proxy.whitelist.ProxyWhitelistCache;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class WhitelistServerCommand implements SimpleCommand {

    private final BytePlayerModule playerNetworkModule = Main.getInstance().getPlayerModule();
    private final ProxyWhitelistCache whitelistCache = Main.getInstance().getWhitelistCache();

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(TextUtils.COMMAND_ONLY_PLAYERS);
            return;
        }

        BytePlayerData playerNetwork = playerNetworkModule.fetchEntitySync(player.getUniqueId());
        if (playerNetwork == null) {
            player.sendMessage(DMM("<red>Could not retrieve your player data"));
            return;
        }

        if (!Group.hasPermission(playerNetwork.getGroup(), Groups.ADMIN)) {
            player.sendMessage(TextUtils.noPermission());
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 2) {
            String type = args[1];

            if (args[0].equalsIgnoreCase("open")) {
                if (whitelistCache.inWhitelist(type)) {
                    player.sendMessage(DMM("<red>A whitelist is already enabled for the server network <white>" + type));
                    return;
                }

                whitelistCache.enableWhitelist(type);
                player.sendMessage(DMM("<green>You enabled the whitelist for the server network <white>" + type));
            }

            if (args[0].equalsIgnoreCase("close")) {
                if (!whitelistCache.inWhitelist(type)) {
                    player.sendMessage(DMM("<red>There is no whitelist enabled for the server network <white>" + type));
                    return;
                }

                whitelistCache.disableWhitelist(type);
                player.sendMessage(DMM("<red>You disabled the whitelist for the server network <white>" + type));
            }
        }

        if (args.length == 3) {
            String type = args[1];

            if (args[0].equalsIgnoreCase("addplayer")) {
                String playerName = args[2];

                if (whitelistCache.containsPlayer(type, playerName)) {
                    player.sendMessage(DMM("<red>This player is already on the whitelist"));
                    return;
                }

                playerNetworkModule.fetchEntityAsync(playerName).whenComplete((resultPlayer, throwable) -> {
                    if (throwable != null) {
                        player.sendMessage(DMM("<red>Oops... an error occurred while retrieving this player's data"));
                        return;
                    }

                    player.sendMessage(DMM("<green>You added player ").append(resultPlayer.getTag()).append(DMM("<green> to the whitelist for the server network <white>" + type)));

                    whitelistCache.addPlayer(type, playerName);
                });
            }

            if (args[0].equalsIgnoreCase("addgroup")) {
                Group group = Groups.fromId(args[2]);

                if (group == null) {
                    player.sendMessage(DMM("<red>Could not find that group"));
                    return;
                }

                if (whitelistCache.containsGroup(type, group)) {
                    player.sendMessage(DMM("<red>Group ").append(group.getTag()).append(DMM(" is already on the whitelist")));
                    return;
                }

                player.sendMessage(DMM("<green>You added group ").append(group.getTag()).append(DMM("<green> to the whitelist for the server network <white>" + type)));

                whitelistCache.addGroup(type, group);
            }

            if (args[0].equalsIgnoreCase("removeplayer")) {
                String playerName = args[2];

                if (!whitelistCache.containsPlayer(type, playerName)) {
                    player.sendMessage(DMM("<red>This player is not on the whitelist"));
                    return;
                }

                player.sendMessage(DMM("<gray>You removed player " + playerName + " from the whitelist for the server network <white>" + type));

                whitelistCache.removePlayer(type, playerName);
            }

            if (args[0].equalsIgnoreCase("removegroup")) {
                Group group = Groups.fromId(args[2]);

                if (group == null) {
                    player.sendMessage(DMM("<red>Could not find that group"));
                    return;
                }

                if (!whitelistCache.containsGroup(type, group)) {
                    player.sendMessage(DMM("<red>Group ").append(group.getTag()).append(DMM(" is not on the whitelist")));
                    return;
                }

                player.sendMessage(DMM("<gray>You removed group ").append(group.getTag()).append(DMM("<gray> from the whitelist for the server network <white>" + type)));

                whitelistCache.removeGroup(type, group);
            }
        }
    }
}