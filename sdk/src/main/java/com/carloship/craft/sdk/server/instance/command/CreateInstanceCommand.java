package com.carloship.craft.sdk.server.instance.command;

import com.carloship.craft.sdk.server.SDKCommandExecutor;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.SDKServer;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.event.EventDispatcher;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceException;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.instance.event.player.PlayerSwitchInstanceEvent;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class CreateInstanceCommand<M extends Enum<M> & ServerInstanceMode, P extends BytePlayerCraft> extends Command {

    public CreateInstanceCommand(SDKServer<P> root, Class<M> enumClass) {
        super("createinstance", "startinstance", "newinstance");

        ArgumentEnum<@NonNull M> modes = ArgumentType.Enum("mode", enumClass).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft p, @NotNull CommandContext context) {
                @SuppressWarnings("unchecked")
                P player = (P) p;

                if (!Group.has(player, Groups.ADMIN)) {
                    player.sendNotPermission();
                    return;
                }

                ServerInstanceMode mode = context.get(modes);

                try {
                    ServerInstance<P> instance = root.getInstanceManager().createInstance(mode);

                    player.sendSuccessMessage("You created a " + mode.getId() + " instance (" + instance.getServerId().toString() + ")");

                    player.sendMessage(DMM("<gray>Click <yellow><bold>HERE</bold> <gray>to join it")
                            .clickEvent(ClickEvent.callback(audience -> {
                                var currentInstance = root.getPlayerInstance(player);
                                if (currentInstance == null) {
                                    player.sendErrorMessage("Could not retrieve your current instance.");
                                    return;
                                }

                                EventDispatcher.call(new PlayerSwitchInstanceEvent<P>(player, currentInstance, instance, null));
                            })));
                } catch (ServerInstanceException e) {
                    player.sendErrorMessage("You have reached the instance limit for this mode.");
                }
            }
        }, modes);
    }
}
