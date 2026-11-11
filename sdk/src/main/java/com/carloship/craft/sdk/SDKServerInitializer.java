package com.carloship.craft.sdk;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.utils.EnumQuery;
import com.carloship.craft.sdk.utils.Pair;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;

@Slf4j
public class SDKServerInitializer {

    public static <P extends BytePlayerCraft> Pair<MinecraftServer, SDKServer<P>> createServer(ServerFactory<P> factory) {
        boolean proxied = SDKEnvironment.PROXY.getBoolean(true);

        String proxyKey = proxied ? SDKEnvironment.PROXY_SECRET_KEY.getString() : null;
        if (proxied && (proxyKey == null || proxyKey.isBlank())) {
            log.error("Velocity secret key is missing or invalid.");
            closeApp();
            return null;
        }

        Auth velocityAuth = proxied ? new Auth.Velocity(proxyKey) : new Auth.Offline();
        MinecraftServer minecraftServer = MinecraftServer.init(velocityAuth);

        String host = SDKEnvironment.SERVER_HOST.getString("127.0.0.1");
        int port = SDKEnvironment.SERVER_PORT.getInt(25500);

        SDKServerType type = SDKEnvironment.SERVER_TYPE.getEnum(EnumQuery.of(SDKServerType.values()));
        if (type == SDKServerType.UNKNOWN || type == null) {
            log.error("Not found correct server type... {}", type);
            closeApp();
            return null;
        }

        SDKServer<P> server = factory.create(minecraftServer, port, host);
        SDKServer.INSTANCE = server;
        server.setServerType(type);

        try {
            server.startServer();
            log.info("[{}]/[{}] => Server {}:{} -> Starting...", type.name(), proxied ? "PROXIED" : "NO_PROXY", host, port);
        } catch (Throwable ex) {
            log.error("Failed to start the server.", ex);
            server.stopServer();
            closeApp();
            return null;
        }

        return new Pair<>(minecraftServer, server);
    }

    private static void closeApp() {
        Runtime.getRuntime().exit(1);
    }
}