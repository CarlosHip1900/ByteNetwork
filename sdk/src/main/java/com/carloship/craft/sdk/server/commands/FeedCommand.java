package com.carloship.craft.sdk.server.commands;

import com.carloship.craft.sdk.server.SDKCommandExecutor;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import org.jetbrains.annotations.NotNull;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class FeedCommand extends Command {

    public FeedCommand() {
        super("feed");

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                if (!Group.hasPermission(player.getGroup(), Groups.MODERATOR)) {
                    player.sendNotPermission();
                    return;
                }

                player.setFood(20);
                player.sendSuccessMessage("You have satisfied your hunger!");
            }
        });

        ArgumentWord targetArgument = SDKCommandExecutor.getArgumentWord();

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                if (!Group.hasPermission(player.getGroup(), Groups.MODERATOR)) {
                    player.sendNotPermission();
                    return;
                }

                String targetName = context.get(targetArgument);
                if (targetName.isBlank()) {
                    player.sendErrorMessage("You cannot enter an empty player name.");
                    return;
                }

                BytePlayerCraft target = (BytePlayerCraft) MinecraftServer.getConnectionManager().findOnlinePlayer(targetName);
                if (target == null) {
                    player.sendErrorMessage("Could not find that player...");
                    return;
                }

                target.setFood(20);
                player.sendMessage(DMM("<green>You have satisfied the hunger of player ").append(target.getNametag()));
                target.sendMessage(DMM("<green>Your hunger has been satisfied by player ").append(player.getNametag()));
            }
        }, targetArgument);
    }

}
