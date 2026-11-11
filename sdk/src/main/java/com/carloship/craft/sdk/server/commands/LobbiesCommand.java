package com.carloship.craft.sdk.server.commands;

import com.carloship.craft.sdk.server.SDKCommandExecutor;
import com.carloship.craft.sdk.server.common.LobbiesMenu;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public class LobbiesCommand extends Command {

    public LobbiesCommand(Pos defaultSpawn, ServerInstanceMode type) {
        super("lobby","lobbies");

        setDefaultExecutor(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                new LobbiesMenu(defaultSpawn, type).showPlayer(player);
            }
        });
    }

}
