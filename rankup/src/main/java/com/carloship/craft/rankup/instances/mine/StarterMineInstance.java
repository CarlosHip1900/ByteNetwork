package com.carloship.craft.rankup.instances.mine;

import com.carloship.craft.rankup.RankupAPI;
import com.carloship.craft.rankup.RankupInstanceMode;
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

public class StarterMineInstance extends ServerInstance<RankupPlayer> {

    public StarterMineInstance(SDKServer<RankupPlayer> root) {
        super(root, RankupInstanceMode.START_MINE, "start_mine");
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
        return null;
    }

    @Override
    protected Class<RankupPlayer> getPlayerClass() {
        return RankupPlayer.class;
    }

    @Override
    public @NotNull Pos getSpawnPoint() {
        return null;
    }
}
