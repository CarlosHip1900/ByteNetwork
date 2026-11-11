package com.carloship.craft.sdk.server.instance.command;

import com.carloship.craft.sdk.server.SDKCommandExecutor;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.SDKServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class DestroyInstanceCommand<M extends Enum<M> & ServerInstanceMode, P extends BytePlayerCraft> extends Command {

    public DestroyInstanceCommand(SDKServer<P> root, Class<M> enumClass) {
        super("destroyinstance", "shutdowninstance", "delinstance");

        ArgumentEnum<@NonNull M> modes = ArgumentType.Enum("mode", enumClass).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft p, @NotNull CommandContext context) {
                @SuppressWarnings("unchecked")
                P player = (P) p;

                if (Group.has(player, Groups.ADMIN)) {
                    player.sendNotPermission();
                    return;
                }

                ServerInstanceMode mode = context.get(modes);
                int last = root.getInstanceManager().getInstancesFrom(mode).size();

                var instance = root.getInstanceManager().getInstanceFromAndById(mode, last);
                if (instance == null) {
                    player.sendErrorMessage("Could not find instance " + last + " in mode " + mode.getId());
                    return;
                }

                player.sendInfoMessage("Deleting instance " + instance.getServerId().toString() + "...");
                root.getInstanceManager().destroyInstance(instance);
            }
        }, modes);
    }
}
