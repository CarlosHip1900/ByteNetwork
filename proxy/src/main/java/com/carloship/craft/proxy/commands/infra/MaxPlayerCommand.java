package com.carloship.craft.proxy.commands.infra;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class MaxPlayerCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        BytePlayerModule playerModule = Main.getInstance().getPlayerModule();

        if (!(source instanceof Player player)) {
            source.sendMessage(TextUtils.COMMAND_ONLY_PLAYERS);
            return;
        }

        BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
        if (!Group.hasPermission(playerNetwork.getGroup(), Groups.ADMIN)) {
            player.sendMessage(TextUtils.noPermission());
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 1) {
            int maxPlayers;

            try {
                maxPlayers = Integer.parseInt(args[0]);
            } catch (NumberFormatException e){
                player.sendMessage(DMM("<red>You should input valid number..."));
                return;
            }

            Main.getInstance().getSettings().setMaxPlayers(maxPlayers);
            player.sendMessage(DMM("<green>You've adjusted the max number of players on the network to <white>" + maxPlayers));
        }

    }
}