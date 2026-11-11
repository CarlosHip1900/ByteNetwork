package com.carloship.craft.rankup.instances.lobby;

import com.carloship.craft.rankup.RankupAPI;
import com.carloship.craft.rankup.player.RankupPlayer;
import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardTemplate;
import com.carloship.craft.sdk.utils.Pair;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class RankupLobbyInstance extends ServerInstance<RankupPlayer> {

    public static final Pos DEFAULT_SPAWN = new Pos(0, 65, 0);

    public RankupLobbyInstance(SDKServer<RankupPlayer> root, ServerInstanceMode instanceMode, String instanceFolder) {
        super(root, instanceMode, instanceFolder);
    }

    @Override
    public void onStart(SDKServer<RankupPlayer> root) {

    }

    @Override
    public void onStop(SDKServer<RankupPlayer> root) {

    }

    @Override
    public void registryContent() {

    }

    @Override
    public void onPlayerJoin(RankupPlayer player, boolean firstLogin) {
        announcePlayerJoin(player);
    }

    @Override
    public void onPlayerLeave(RankupPlayer player) {

    }

    @Override
    public void onPlayerMove(RankupPlayer player, PlayerMoveEvent event) {

    }

    @Override
    public void onPlayerInteract(RankupPlayer player, @Nullable ItemStack itemStack, @Nullable Entity entity) {

    }

    @Override
    public void onTick() {

    }

    @Override
    public ScoreBoardTemplate<RankupPlayer> getScoreboard() {
        return RankupAPI.SCORE_BOARD;
    }

    @Override
    public Pair<Component, Component> getTabList() {
        return SDKServerAPI.DEFAULT_TAB_LIST;
    }

    @Override
    public @NotNull BiFunction<@NonNull ServerInstanceMode, BytePlayerCraft, @NonNull Component> nametagFunction() {
        return (serverInstanceMode, player) -> player.getNametag();
    }

    @Override
    protected Class<RankupPlayer> getPlayerClass() {
        return RankupPlayer.class;
    }

    @Override
    public @NotNull Pos getSpawnPoint() {
        return DEFAULT_SPAWN;
    }
}
