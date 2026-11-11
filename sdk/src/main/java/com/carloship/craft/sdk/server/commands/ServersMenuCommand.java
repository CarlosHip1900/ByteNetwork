package com.carloship.craft.sdk.server.commands;

import com.carloship.craft.sdk.server.SDKCommandExecutor;
import com.carloship.craft.sdk.server.common.ServersMenu;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

public class ServersMenuCommand extends Command {

    public ServersMenuCommand() {
        super("server", "servers");

        setDefaultExecutor(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                new ServersMenu().showPlayer(player);
            }
        });
    }
}
