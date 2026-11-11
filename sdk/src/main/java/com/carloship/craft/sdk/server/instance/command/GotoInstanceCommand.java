package com.carloship.craft.sdk.server.instance.command;

import com.carloship.craft.sdk.server.SDKCommandExecutor;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.SDKServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.event.EventDispatcher;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.instance.event.player.PlayerSwitchInstanceEvent;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class GotoInstanceCommand<M extends Enum<M> & ServerInstanceMode, P extends BytePlayerCraft> extends Command {

    public GotoInstanceCommand(SDKServer<P> root, Class<M> enumClass) {
        super("gotoinstance");

        ArgumentEnum<@NonNull M> modes = ArgumentType.Enum("mode", enumClass).setFormat(ArgumentEnum.Format.LOWER_CASED);
        ArgumentInteger id = ArgumentType.Integer("id");

        id.setSuggestionCallback((sender, context, suggestion) -> {
            M mode = context.get(modes);

            root.getInstanceManager()
                    .getRunningInstances(mode)
                    .forEach(instance ->
                            suggestion.addEntry(
                                    new SuggestionEntry(String.valueOf(instance.getId()))
                            ));
        });

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft p, @NotNull CommandContext context) {
                @SuppressWarnings("unchecked")
                P player = (P) p;

                if (!Group.has(p, Groups.ADMIN)) {
                    player.sendNotPermission();
                    return;
                }

                ServerInstanceMode mode = context.get(modes);
                int serverId = context.get(id);

                ServerInstance<P> instance = root.getInstanceManager().getInstanceFromAndById(mode, serverId);
                if (instance == null) {
                    player.sendErrorMessage("Could not find instance " + serverId + " in mode " + mode.getId());
                    return;
                }

                ServerInstance<P> currentInstance = root.getPlayerInstance(player);
                if (currentInstance == null) {
                    player.sendErrorMessage("Could not retrieve your current instance.");
                    return;
                }

                if (currentInstance.equals(instance)) {
                    player.sendErrorMessage("You are already in this instance...");
                    return;
                }

                player.sendInfoMessage("Joining " + instance.getServerId().toString() + "...");
                EventDispatcher.call(new PlayerSwitchInstanceEvent<P>(player, currentInstance, instance, null));
            }
        }, modes, id);
    }
}
