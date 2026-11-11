package com.carloship.craft.rankup.player;

import com.carloship.craft.rankup.RankupAPI;
import com.carloship.craft.sdk.annotations.SDKModuleInfo;
import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.server.leveling.Leveling;
import com.carloship.craft.sdk.server.leveling.XpCurve;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.player.database.AbstractPlayerDatabase;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayerModule;
import com.carloship.craft.sdk.server.player.loader.PlayerDataLoaderManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SDKModuleInfo(
        name = "RankupPlayerModule",
        id = "rank_up_player_module"
)
public class RankupPlayerModule extends AbstractPlayerModule<RankupPlayerData> {

    private final AbstractPlayerDatabase<RankupPlayerData> database;

    public RankupPlayerModule(ModuleDependencyPacket packet) {
        super(packet);
        this.database = new AbstractPlayerDatabase<>(RankupPlayerData.class, "byte_network", "byte_player_rankup", "data:persistent:rankup:", packet);
    }

    @Override
    protected String pubSubChannel() {
        return "data:persistent:rankup:";
    }

    @Override
    public AbstractPlayerDatabase<RankupPlayerData> getDatabase() {
        return database;
    }

    @Override
    public void createModule() {

    }

    @Override
    public void disableModule() {

    }

    public static void hook() {
        PlayerDataLoaderManager.addLoader(bytePlayer -> RankupAPI.rankupPlayerModule()
                .fetchOrCreateEntity(
                        bytePlayer.getUuid(),
                        () -> createPlayer(bytePlayer)
                )
                .whenComplete((data, throwable) -> {
                    if (throwable != null) {
                        log.error("Error when tried get/fetch user sheep war data", throwable);
                        return;
                    }

                    log.info("Rankup profile {} loaded...", bytePlayer.getUsername());
                    if (data.getLeveling() == null) {
                        data.setLeveling(new Leveling(XpCurve.LINEAR));
                    }

                    ((RankupPlayer) bytePlayer).setRankupPlayerData(data);
                })
        );
    }

    private static RankupPlayerData createPlayer(BytePlayerCraft player) {
        return new RankupPlayerData(player.getUuid(), player.getUsername(), 0.0, new Leveling(XpCurve.LINEAR));
    }
}
