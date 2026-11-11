package com.carloship.craft.sheepwars.player;

import com.carloship.craft.sdk.annotations.SDKModuleInfo;
import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.server.leveling.Leveling;
import com.carloship.craft.sdk.server.leveling.XpCurve;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.player.database.AbstractPlayerDatabase;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayerModule;
import com.carloship.craft.sdk.server.player.loader.PlayerDataLoaderManager;
import com.carloship.craft.sheepwars.SheepWarsAPI;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
@SDKModuleInfo(
        name = "SheepWarPlayerModule",
        id = "sheep_war_player_module"
)
public class SheepWarPlayerModule extends AbstractPlayerModule<SheepWarPlayerData> {

    private final AbstractPlayerDatabase<SheepWarPlayerData> database;

    public SheepWarPlayerModule(ModuleDependencyPacket packet) {
        super(packet);
        this.database = new AbstractPlayerDatabase<>(SheepWarPlayerData.class, "byte_network", "byte_player_games", "data:games:sheep_wars:", packet);
    }

    @Override
    protected String pubSubChannel() {
        return "data:games:sheep_wars:";
    }

    @Override
    public AbstractPlayerDatabase<SheepWarPlayerData> getDatabase() {
        return database;
    }

    @Override
    public void createModule() {

    }

    @Override
    public void disableModule() {

    }

    public static void hook(){
        PlayerDataLoaderManager.addLoader(bytePlayer -> SheepWarsAPI.sheepPlayerModule()
                .fetchOrCreateEntity(
                        bytePlayer.getUuid(),
                        () -> createPlayer(bytePlayer)
                )
                .whenComplete((data, throwable) -> {
                    if (throwable != null){
                        log.error("Error when tried get/fetch user sheep war data", throwable);
                        return;
                    }

                    log.info("SheepWarsProfile {} loaded...", bytePlayer.getUsername());
                    if (data.getLeveling() == null){
                        data.setLeveling(new Leveling(XpCurve.LINEAR));
                    }

                    ((SheepWarPlayer) bytePlayer).setSheepData(data);
                })
        );
    }

    private static SheepWarPlayerData createPlayer(BytePlayerCraft player){
        return new SheepWarPlayerData(player.getUuid(), player.getUsername(), new HashMap<>(), 0.0, new Leveling(XpCurve.LINEAR));
    }
}
