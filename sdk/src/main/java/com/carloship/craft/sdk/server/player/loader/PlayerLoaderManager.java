package com.carloship.craft.sdk.server.player.loader;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.SDKServer;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

@Getter
public class PlayerLoaderManager {

    private final PlayerDataLoaderProcessor playerDataProcessor;
    private final PlayerDataLoaderManager playerDataLoadManager;

    public PlayerLoaderManager() {
        this.playerDataProcessor = new PlayerDataLoaderProcessor();
        this.playerDataLoadManager = new PlayerDataLoaderManager();
    }

    public void registryRefreshListeners(SDKServer<?> root) {
        BytePlayerModule networkModule = root.getBytePlayerModule();

        playerDataProcessor.registerLoadListener();
        playerDataLoadManager.registerLoadListener(networkModule);

        networkModule.getRefreshListeners().add(playerNetwork -> {
            Player fetchPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerNetwork.getUuid());
            if (fetchPlayer == null) return;
            if (!(fetchPlayer instanceof BytePlayerCraft dataServer)) return;

            dataServer.setPlayerData(playerNetwork);
            root.getInstanceProfileManager().refreshPlayer(dataServer);
        });

    }

}
