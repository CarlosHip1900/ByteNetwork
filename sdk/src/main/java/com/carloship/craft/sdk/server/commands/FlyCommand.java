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

public class FlyCommand extends Command {

    public FlyCommand() {
        super("fly");

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                if (!Group.hasPermission(player.getGroup(), Groups.VIP)) {
                    player.sendNotPermission();
                    return;
                }

                if (player.isAllowFlying()) {
                    player.sendErrorMessage("You have disabled flight mode.");
                    player.setAllowFlying(false);
                    player.setFlying(false);
                    return;
                }

                player.setAllowFlying(true);
                player.setFlying(true);
                player.sendSuccessMessage("You have enabled flight mode.");
            }
        });

        ArgumentWord targetArgument = SDKCommandExecutor.getArgumentWord();

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                if (!Group.hasPermission(player.getGroup(), Groups.ADMIN)) {
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

                if (target.isAllowFlying()) {
                    player.sendMessage(DMM("<red>You have disabled flight mode for player ").append(target.getNametag()));
                    target.sendMessage(DMM("<red>Your flight mode has been disabled by player ").append(player.getNametag()));
                    target.setAllowFlying(false);
                    target.setFlying(false);
                    return;
                }

                target.setAllowFlying(true);
                target.setFlying(true);
                player.sendMessage(DMM("<green>You have enabled flight mode for player ").append(target.getNametag()));
                target.sendMessage(DMM("<green>Your flight mode has been enabled by player ").append(player.getNametag()));
            }
        }, targetArgument);
    }

}