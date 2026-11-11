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

import java.util.Arrays;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class StaffChatCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        BytePlayerModule playerModule = Main.getInstance().getPlayerModule();

        if (!(source instanceof Player player)) {
            source.sendMessage(TextUtils.COMMAND_ONLY_PLAYERS);
            return;
        }

        BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
        if (!Group.hasPermission(playerNetwork.getGroup(), Groups.HELPER)) {
            player.sendMessage(TextUtils.noPermission());
            return;
        }

        String[] args = invocation.arguments();

        if (args.length > 0) {
            String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
            Main.allPlayers().forEach(online -> {
                BytePlayerData network = playerModule.fetchEntitySync(online.getUniqueId());
                if (!Group.hasPermission(network.getGroup(), Groups.HELPER)) return;
                online.sendMessage(DMM("<light_purple><bold>(SC)<reset>").appendSpace().append(playerNetwork.getTag()).append(DMM("<dark_gray>: <gray>" + message)));
            });
        }
    }


}
