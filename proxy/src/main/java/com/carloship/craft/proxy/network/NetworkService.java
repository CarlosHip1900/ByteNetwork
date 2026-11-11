package com.carloship.craft.proxy.network;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.proxy.player.event.TransferPlayerEvent;
import com.carloship.craft.sdk.cluster.NetworkProxyModule;
import com.carloship.craft.sdk.cluster.payload.ServerRegisterPayload;
import com.carloship.craft.sdk.cluster.payload.ServerUnregisterPayload;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NetworkService {

    public void initModule(NetworkProxyModule module, ProxyServer proxyServer) {
        proxyServer.getAllServers().forEach(server -> proxyServer.unregisterServer(server.getServerInfo()));

        module.setTransferConsumer((uuid, targetServerId) -> {
            proxyServer.getPlayer(uuid).ifPresent(player ->
                    proxyServer.getServer(targetServerId).ifPresent(server ->
                            Main.fireEvent(new TransferPlayerEvent(player, server, targetServerId))
                    )
            );
        });

        module.addExecutorPayload(ServerRegisterPayload.class, payload -> {
            if (!(payload instanceof ServerRegisterPayload server)) return;

            if (proxyServer.getServer(server.serverId()).isPresent()) return;

            ServerInfo info = new ServerInfo(
                    server.serverId(),
                    new InetSocketAddress(server.host(), server.port())
            );

            proxyServer.registerServer(info);

            log.info("[Proxy] Registered server {} on Velocity", server.serverId());
        });

        module.addExecutorPayload(ServerUnregisterPayload.class, payload -> {
            if (!(payload instanceof ServerUnregisterPayload server)) return;

            proxyServer.getServer(server.serverId()).ifPresent(registered -> {
                proxyServer.unregisterServer(registered.getServerInfo());
                log.info("[Proxy] Unregistered server {} from Velocity", server.serverId());
            });
        });
    }
}