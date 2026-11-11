package com.carloship.craft.sdk.server.commands;

import com.carloship.craft.sdk.server.SDKCommandExecutor;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.utils.entity.EntityFinder;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode", "gm");

        ArgumentEnum<GameMode> gamemode = ArgumentType.Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        gamemode.setCallback((sender, exception) -> sender.sendMessage(DMM("<red>Could not find that game mode.")));

        ArgumentEntity targets = ArgumentType.Entity("targets").onlyPlayers(true);

        setDefaultExecutor(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                String commandName = context.getCommandName();

                player.sendErrorMessage("Usage: /" + commandName + " <gamemode> [targets]");
            }
        });

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                if (!Group.hasPermission(player.getGroup(), Groups.MODERATOR)) {
                    player.sendNotPermission();
                    return;
                }

                GameMode mode = context.get(gamemode);

                executeSelf(player, mode);
            }
        }, gamemode);

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                if (!Group.hasPermission(player.getGroup(), Groups.MODERATOR)) {
                    player.sendNotPermission();
                    return;
                }

                EntityFinder finder = context.get(targets);
                GameMode mode = context.get(gamemode);

                executeOthers(player, mode, finder.find(player));
            }
        }, gamemode, targets);
    }

    private void executeOthers(BytePlayerCraft sender, GameMode mode, List<Entity> entities) {
        if (entities.isEmpty()) {
            sender.sendErrorMessage("Could not find any entities whose game mode could be changed.");
        } else for (Entity entity : entities) {
            if (entity instanceof BytePlayerCraft target) {
                if (target == sender) {
                    executeSelf(sender, mode);
                } else {
                    target.setGameMode(mode);

                    String gamemodeName = extractGamemodeName(mode);
                    Component playerName = target.getDisplayName() == null ? target.getName() : target.getDisplayName();

                    target.sendSuccessMessage("Your game mode has been changed to " + gamemodeName);
                    sender.sendMessage(DMM("<green>You changed the game mode of player ").append(playerName).append(DMM("<green> to " + gamemodeName)));
                }
            }
        }
    }

    private void executeSelf(BytePlayerCraft sender, GameMode mode) {
        sender.setGameMode(mode);
        sender.sendSuccessMessage("Your game mode has been changed to " + extractGamemodeName(mode));
    }

    private String extractGamemodeName(GameMode mode) {
        return switch (mode) {
            case SPECTATOR -> "Spectator";
            case SURVIVAL -> "Survival";
            case ADVENTURE -> "Adventure";
            case CREATIVE -> "Creative";
        };
    }
}