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

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class BitCommand implements SimpleCommand {

    private static final ProxyServer SERVER = Main.proxy();
    private static final BytePlayerModule PLAYER_MODULE = Main.getInstance().getPlayerModule();

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtils.COMMAND_ONLY_PLAYERS);
            return;
        }

        BytePlayerData senderNetwork = PLAYER_MODULE.fetchEntitySync(player.getUniqueId());
        if (senderNetwork == null) return;

        if (!Group.hasPermission(senderNetwork.getGroup(), Groups.ADMIN)) {
            player.sendMessage(TextUtils.noPermission());
            return;
        }

        if (args.length == 3) {
            String subCmd = args[0];
            String targetName = args[1];

            int amount = 0;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(DMM("<red>Please enter numbers only"));
                return;
            }

            if (amount <= 0) {
                player.sendMessage(DMM("<red>The amount must be greater than 0"));
                return;
            }

            int finalAmount = amount;
            SERVER.getPlayer(targetName).ifPresentOrElse(target -> {

                BytePlayerData targetNetwork = PLAYER_MODULE.fetchEntitySync(target.getUniqueId());
                if (targetNetwork == null) {
                    player.sendMessage(DMM("<red>Could not retrieve this player's information"));
                    return;
                }

                PLAYER_MODULE.modifyAndSendFromProxyAsync(targetNetwork, tC -> {
                    if (subCmd.equalsIgnoreCase("add")) {
                        tC.addBit(finalAmount);
                        player.sendMessage(DMM("<gray>You added <aqua>" + finalAmount + " bit <gray>to player ").append(tC.getTag()));
                    }

                    if (subCmd.equalsIgnoreCase("set")) {
                        tC.setBit(finalAmount);
                        player.sendMessage(DMM("<gray>You set the player's bit balance to <aqua>" + finalAmount + " bit <gray>for player ").append(tC.getTag()));
                    }

                    if (subCmd.equalsIgnoreCase("remove")) {
                        if (tC.getBit() <= 0) {
                            player.sendMessage(DMM("<gray>Player ").append(tC.getTag()).append(DMM(" <gray>does not have any bits to remove")));
                            return;
                        }

                        tC.removeBit(finalAmount);
                        player.sendMessage(DMM("<gray>You removed <aqua>" + finalAmount + " bit <gray>from player ").append(targetNetwork.getTag()));
                    }

                    PLAYER_MODULE.publishRefreshProxyToServer(tC.getUuid(), tC.getUsername());
                });

            }, () -> {
                player.sendMessage(DMM("<red>This player is not online on any of our servers"));
            });

        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player player)) {
            return List.of("<!>");
        }

        BytePlayerData senderNetwork = PLAYER_MODULE.fetchEntitySync(player.getUniqueId());
        if (senderNetwork == null) {
            return List.of("<!>");
        }

        if (!Group.hasPermission(senderNetwork.getGroup(), Groups.ADMIN)) {
            return List.of("<!>");
        }

        return switch (args.length) {
            case 1 -> List.of("add", "remove", "set");
            case 2 -> Main.getOnlinePlayerNames();
            case 3 -> List.of("1", "10", "100", "1000", "10000");
            default -> List.of();
        };
    }
}