package com.carloship.craft;

import com.carloship.craft.sdk.utils.EnumQuery;
import com.carloship.craft.server.SDKEnvironment;
import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.server.SDKServerType;
import com.carloship.craft.server.lobby.MasterLobbyServer;
import com.carloship.craft.server.sheepwar.SheepWarServer;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;

@Slf4j
public class Main {

    static void main() throws InterruptedException {
        boolean proxied = SDKEnvironment.PROXY.getBoolean(false);

        String proxyKey = proxied ? SDKEnvironment.PROXY_SECRET_KEY.getString() : null;
        if (proxied && (proxyKey == null || proxyKey.isBlank())) {
            log.error("Velocity secret key is missing or invalid....");
            closeApp();
            return;
        }

        Auth VelocityAuth = proxied ? new Auth.Velocity(proxyKey) : new Auth.Offline();

        MinecraftServer minecraftServer = MinecraftServer.init(VelocityAuth);

        String host = SDKEnvironment.SERVER_HOST.getString("127.0.0.1");
        int port = SDKEnvironment.SERVER_PORT.getInt(25550);

        SDKServerType type = SDKEnvironment.SERVER_TYPE.getEnum(EnumQuery.of(SDKServerType.values()));

        if (type == SDKServerType.UNKNOWN || type == null){
            log.error("Not found correct server type... {}", type);
            closeApp();
            return;
        }

        SDKServer<?> server = factoryServer(type, minecraftServer, port, host);

        if (server == null) {
            log.error("Isn't possible find which type will be that server...");
            closeApp();
            return;
        }

        SDKServer.INSTANCE = server;

        try {
            server.startServer();
            log.info("[{}]/[{}] => Server {}:{} -> Starting...", type.name(), proxied ? "PROXY_CONN" : "NO_PROXY", host, port);
        } catch (Throwable ex) {
            server.stopServer();
            log.error("Failed to start the server.", ex);
            closeApp();
            return;
        }

        Thread.currentThread().join();
    }

    private static void closeApp() {
        Runtime.getRuntime().exit(1);
    }

    private static SDKServer<?> factoryServer(SDKServerType type, MinecraftServer server, int port, String host) {
        return switch(type){
            case SHEEP_WARS -> new SheepWarServer(server, port, host);
            case MASTER_LOBBY -> new MasterLobbyServer(server, port, host);
            case UNKNOWN -> null;
        };
    }

}