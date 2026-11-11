package com.carloship.craft.proxy.commands;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;
import static com.carloship.craft.sdk.utils.TextUtils.USE;

@RequiredArgsConstructor
public class TellCommand implements SimpleCommand {

    private final ProxyServer server = Main.proxy();

    private final BytePlayerModule playerModule;

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player source)) return;

        BytePlayerData sourcePlayer = playerModule.fetchEntitySync(source.getUniqueId());
        if (sourcePlayer == null) {
            source.sendMessage(DMM("<red>Could not retrieve your player information"));
            return;
        }

        var args = invocation.arguments();

        if (args.length <= 1) {
            source.sendMessage(USE("/tell <player> [message]"));
            return;
        }

        String targetName = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        server.getPlayer(targetName).ifPresentOrElse(onlineTarget -> {
            BytePlayerData targetNetwork = playerModule.fetchEntitySync(onlineTarget.getUniqueId());
            if (targetNetwork == null) {
                source.sendMessage(DMM("<red>Could not retrieve this player's information"));
                return;
            }

            source.sendMessage(DMM("<dark_green>To ").append(targetNetwork.getTag()).append(DMM("<dark_gray>: <gray>" + message)));

            onlineTarget.sendMessage(DMM("<dark_green>From ").append(sourcePlayer.getTag()).append(DMM("<dark_gray>: <gray>" + message)));

        }, () -> {
            source.sendMessage(DMM("<red>This player is not online on our network!"));
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        var args = invocation.arguments();

        if (args.length == 1) {
            return Main.getOnlinePlayerNames();
        }

        return List.of();
    }
}