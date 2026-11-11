package com.carloship.craft.proxy.commands;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.cluster.NetworkProxyModule;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public interface ServerCommands {

    NetworkProxyModule PROXY_MODULE = Main.getInstance().getNetworkProxyModule();

    class LobbyCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            Player player = (Player) invocation.source();
            PROXY_MODULE.findAvailableServer(SDKServerAPI.MASTER_LOBBY_GAME_ID, SDKServerAPI.PERSISTENT_GAME_TYPE_ID).ifPresentOrElse(server -> {
                PROXY_MODULE.transferPlayer(player.getUniqueId(), server.getServerId());
            }, () -> {
                player.sendMessage(DMM("<red>Ops... we couldn't find this kind server online..."));
            });
        }
    }

    class RankUpCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            Player player = (Player) invocation.source();
            PROXY_MODULE.findAvailableServer("rankup", SDKServerAPI.PERSISTENT_GAME_TYPE_ID).ifPresentOrElse(server -> {
                PROXY_MODULE.transferPlayer(player.getUniqueId(), server.getServerId());
            }, () -> {
                player.sendMessage(DMM("<red>Ops... we couldn't find this kind server online..."));
            });
        }
    }

    class SheepWarCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            Player player = (Player) invocation.source();
            PROXY_MODULE.findAvailableServer(SDKServerAPI.SHEEP_WARS_GAME_ID, SDKServerAPI.MINIGAME_GAME_TYPE_ID).ifPresentOrElse(server -> {
                PROXY_MODULE.transferPlayer(player.getUniqueId(), server.getServerId());
            }, () -> {
                player.sendMessage(DMM("<red>Ops... we couldn't find this kind server online..."));
            });
        }
    }

}
