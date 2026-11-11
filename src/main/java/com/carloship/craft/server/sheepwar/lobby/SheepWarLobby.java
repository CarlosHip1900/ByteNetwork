package com.carloship.craft.server.sheepwar.lobby;

import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardTemplate;
import com.carloship.craft.sdk.utils.Pair;
import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.server.sheepwar.SheepWarsInstanceMode;
import com.carloship.craft.server.sheepwar.player.SheepWarPlayer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class SheepWarLobby extends ServerInstance<SheepWarPlayer> {

    public SheepWarLobby(SDKServer<SheepWarPlayer> root) {
        super(root, SheepWarsInstanceMode.LOBBY, "world-build/sheep-war");
    }

    @Override
    public void onStart(SDKServer<SheepWarPlayer> root) {

    }

    @Override
    public void onStop(SDKServer<SheepWarPlayer> root) {

    }

    @Override
    public void registryContent() {

    }

    @Override
    public void onPlayerJoin(SheepWarPlayer player, boolean firstLogin) {
        player.setGameMode(GameMode.CREATIVE);
    }

    @Override
    public void onPlayerLeave(SheepWarPlayer player) {

    }

    @Override
    public void onPlayerMove(SheepWarPlayer player, PlayerMoveEvent event) {

    }

    @Override
    public void onPlayerInteract(SheepWarPlayer player, @Nullable ItemStack itemStack, @Nullable Entity entity) {

    }

    @Override
    public void onTick() {

    }

    @Override
    public ScoreBoardTemplate<SheepWarPlayer> getScoreboard() {
        return null;
    }

    @Override
    public Pair<Component, Component> getTabList() {
        return null;
    }

    @Override
    public @NotNull BiFunction<@NonNull ServerInstanceMode, BytePlayerCraft, @NonNull Component> nametagFunction() {
        return (serverInstanceMode, player) -> {
            int level = player.getPlayerData().getLeveling().getLevel();
            Component playerTag = player.getNametag();

            Component startMessage = DMM("<gray>[" + level + "]");

            return startMessage.appendSpace().append(playerTag);
        };
    }

    @Override
    protected Class<SheepWarPlayer> getPlayerClass() {
        return SheepWarPlayer.class;
    }

    @Override
    public @NotNull Pos getSpawnPoint() {
        return new Pos(0, 65, 0);
    }
}
