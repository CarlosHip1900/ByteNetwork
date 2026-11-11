package com.carloship.craft.server.sheepwar.room;

import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardAnimation;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardTemplate;
import com.carloship.craft.sdk.utils.Pair;
import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.server.sheepwar.SheepWarsInstanceMode;
import com.carloship.craft.server.sheepwar.player.SheepWarPlayer;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class SheepWarRoom extends ServerInstance<SheepWarPlayer> {

    public static final int MAX_PLAYERS = 16;

    private final SheepWarArenaEngine engine;

    public SheepWarRoom(SDKServer<SheepWarPlayer> root) {
        super(root, SheepWarsInstanceMode.ROOM, "");
        this.engine = new SheepWarArenaEngine(root);
    }

    @Override
    public void onStart(SDKServer<SheepWarPlayer> root) {
        engine.startEngine();
    }

    @Override
    public void onStop(SDKServer<SheepWarPlayer> root) {
        engine.stopEngine();
    }

    @Override
    public void registryContent() {

    }

    @Override
    public void onPlayerJoin(SheepWarPlayer player, boolean firstLogin) {
        broadcastInstance(player.getNametag().append(DMM("<gray>juntou-se a sala")));
    }

    @Override
    public void onPlayerLeave(SheepWarPlayer player) {
        if (!engine.isRunning()){
            broadcastInstance(player.getNametag().append(DMM("<gray>deixou a partida")));
        }
    }

    @Override
    public void onPlayerMove(SheepWarPlayer player, PlayerMoveEvent event) {

    }

    @Override
    public void onPlayerInteract(SheepWarPlayer player, @Nullable ItemStack itemStack, @Nullable Entity entity) {

    }

    @Override
    public void onTick() {
        if (getPlayerCount() >= MAX_PLAYERS && !engine.isRunning()) {
            engine.startEngine();
        }
    }

    @Override
    public ScoreBoardTemplate<SheepWarPlayer> getScoreboard() {
        return new ScoreBoardTemplate<>(
                new ScoreBoardAnimation(
                        List.of()
                ),
                List.of()
        );
    }

    @Override
    public Pair<Component, Component> getTabList() {
        return null;
    }

    @Override
    public @NotNull BiFunction<@NonNull ServerInstanceMode, BytePlayerCraft, @NonNull Component> nametagFunction() {
        return null;
    }

    @Override
    protected Class<SheepWarPlayer> getPlayerClass() {
        return SheepWarPlayer.class;
    }

    @Override
    public @NotNull Pos getSpawnPoint() {
        return null;
    }
}
