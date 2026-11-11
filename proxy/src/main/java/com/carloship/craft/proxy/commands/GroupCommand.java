package com.carloship.craft.proxy.commands;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.*;

@Slf4j
@RequiredArgsConstructor
public class GroupCommand {

    private final BytePlayerModule playerModule;
    private final ProxyServer server;

    public void registryCommands() {
        var cm = server.getCommandManager();
        cm.register(cm.metaBuilder("setgroup").build(), new SetGroup());
    }

    private class SetGroup implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            if (source instanceof Player player) {
                BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
                if (!Group.hasPermission(playerNetwork.getGroup(), Groups.ADMIN)) {
                    player.sendMessage(TextUtils.noPermission());
                    return;
                }
            }

            String[] args = invocation.arguments();

            if (args.length == 0) {
                source.sendMessage(DMM("<use>/setgroup <player> <group>"));
                return;
            }

            if (args.length >= 2) {
                String playerName = args[0];
                String groupId = args[1];

                if (playerName == null || playerName.isBlank()) {
                    source.sendMessage(DMM("<use>/setgroup <player> <group>"));
                    return;
                }

                if (groupId == null || groupId.isBlank()) {
                    source.sendMessage(DMM("<use>/setgroup <player> <group>"));
                    return;
                }

                Group group = Groups.fromId(groupId);
                if (group == null) {
                    source.sendMessage(ERROR(DMM("<gray>Group not found")));
                    return;
                }

                if (source instanceof Player player) {
                    BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
                    if (!Group.hasPermission(playerNetwork.getGroup(), group)) {
                        player.sendMessage(DMM("<red>You cannot assign a group higher than your own ").append(playerNetwork.getGroup().getTag()));
                        return;
                    }

                    if (playerNetwork.getGroup().getId().equalsIgnoreCase(group.getId())) {
                        player.sendMessage(DMM("<red>You cannot assign your own group ").append(playerNetwork.getGroup().getTag()).append(DMM("<red> to another player")));
                        return;
                    }
                }

                server.getPlayer(playerName).ifPresentOrElse(proxiedPlayer -> {
                    BytePlayerData playerNetwork = playerModule.fetchEntitySync(proxiedPlayer.getUniqueId());
                    if (playerNetwork == null) {
                        source.sendMessage(ERROR(DMM("<red>Could not find this player!")));
                        return;
                    }

                    if (playerNetwork.getGroupIndex().equalsIgnoreCase(group.getId())) {
                        source.sendMessage(ERROR(DMM("<red>Player already has this group!")));
                        return;
                    }

                    playerModule.modifyAndSendFromProxy(playerNetwork, pN -> {
                        pN.setGroupIndex(group.getId());
                        source.sendMessage(SP(NamedTextColor.GREEN, "UPDATED!", DMM("<gray>Player " + playerName + "'s group has been updated to ").append(group.getTag())));
                        proxiedPlayer.sendMessage(SP(NamedTextColor.GREEN, "UPDATED!", DMM("<gray>Your group has been updated to ").append(group.getTag())));
                    });

                }, () -> {
                    source.sendMessage(DMM("<gray>Fetching player data..."));
                    playerModule.fetchEntityAsync(playerName).thenAccept(playerNetwork -> {
                        if (playerNetwork == null) {
                            source.sendMessage(ERROR(DMM("<gray>Could not find this user")));
                            return;
                        }

                        if (playerNetwork.getGroupIndex().equalsIgnoreCase(group.getId())) {
                            source.sendMessage(ERROR(DMM("<gray>User already has this group")));
                            return;
                        }

                        playerModule.modifyAndSendFromProxy(playerNetwork, tN -> {
                            tN.setGroupIndex(group.getId());
                            source.sendMessage(SP(NamedTextColor.GREEN, "UPDATED!", DMM("<gray>Player " + playerName + "'s group has been updated to ").append(group.getTag())));
                        });

                    });
                });
            } else {
                source.sendMessage(DMM("<use>/setgroup <player> <group>"));
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
                case 1 -> Arrays.stream(Groups.values()).map(Groups::getId).toList();
                default -> List.of();
            };
        }
    }
}