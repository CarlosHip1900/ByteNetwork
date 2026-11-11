package com.carloship.craft.masterlobby;

import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.SDKServerType;
import com.carloship.craft.sdk.server.hologram.HologramLine;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.npc.impl.FakePlayer;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardAnimation;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardTemplate;
import com.carloship.craft.sdk.server.skin.SkinsDefault;
import com.carloship.craft.sdk.utils.Pair;
import com.carloship.craft.sdk.utils.TextUtils;
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

public class MasterInstance extends com.carloship.craft.sdk.server.instance.ServerInstance<BytePlayerCraft> {

    public static final Pos DEFAULT_SPAWN = new Pos(0, 70, 0, 0f, 0f);

    public MasterInstance(SDKServer<BytePlayerCraft> root) {
        super(root, SDKServerType.MASTER_LOBBY, "spawn");
    }

    @Override
    public void onStart(SDKServer<BytePlayerCraft> root) {

    }

    @Override
    public void onStop(SDKServer<BytePlayerCraft> root) {

    }

    @Override
    public void registryContent() {
        addNPC(FakePlayer.builder()
                .skin(SkinsDefault.SHEEP_WARS_NPC)
                .position(new Pos(0.5, 66, 50.5))
                .line(HologramLine.of("<#CB416B><bold>SHEEP WARS"))
                .line(HologramLine.of("<aqua>0.1-ALPHA <gray>[26.2]"))
                .line(HologramLine.of("<white>" + SDKServerAPI.getOnlinePlayers(SDKServerType.SHEEP_WARS) + " playing now!"))
                .line(HologramLine.of("<gradient:#FFD300:#DAA520><bold>CLICK TO PLAY"))
                .leftClick(p -> SDKServerAPI.sendPlayerToGame((BytePlayerCraft) p.interacted(), SDKServerType.SHEEP_WARS))
                .rightClick(p -> SDKServerAPI.sendPlayerToGame((BytePlayerCraft) p.interacted(), SDKServerType.SHEEP_WARS))
                .build());

        addNPC(FakePlayer.builder()
                .skin(SkinsDefault.RANK_UP_NPC)
                .position(new Pos(4, 66, 49, -180, 0))
                .line(HologramLine.of("<light_purple><bold>RANK UP"))
                .line(HologramLine.of("<aqua>0.1-ALPHA <gray>[26.2]"))
                .line(HologramLine.of("<white>" + SDKServerAPI.getOnlinePlayers(SDKServerType.RANK_UP) + " playing now!"))
                .line(HologramLine.of("<gradient:#FFD300:#DAA520><bold>CLICK TO PLAY"))
                .build());
    }

    @Override
    public void onPlayerJoin(BytePlayerCraft player, boolean firstLogin) {

    }

    @Override
    public void onPlayerLeave(BytePlayerCraft player) {

    }

    @Override
    public void onPlayerMove(BytePlayerCraft player, PlayerMoveEvent event) {

    }

    @Override
    public void onPlayerInteract(BytePlayerCraft player, @Nullable ItemStack itemStack, @Nullable Entity entity) {

    }

    @Override
    public void onTick() {

    }

    @Override
    public ScoreBoardTemplate<BytePlayerCraft> getScoreboard() {
        return SCORE_BOARD;
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
    protected Class<BytePlayerCraft> getPlayerClass() {
        return BytePlayerCraft.class;
    }

    @Override
    public @NotNull Pos getSpawnPoint() {
        return DEFAULT_SPAWN;
    }


    private static final ScoreBoardTemplate<BytePlayerCraft> SCORE_BOARD = new ScoreBoardTemplate<>(
            new ScoreBoardAnimation(
                    List.of(
                            DMM("<bold><#00a896>BYTE NETWORK")
                    )
            ),

            p -> List.of(
                    Component.empty(),
                    DMM("<white>Group: ").append(p.getGroup().getTag()),
                    DMM("<white>Level: <green>" + p.getPlayerData().getLeveling().getLevel()),
                    Component.empty(),
                    DMM("<white>Bit: <aqua>\uD83D\uDC7E " + TextUtils.formatNumberBrazil(p.getPlayerData().getBit())),
                    Component.empty(),
                    DMM("<dark_gray>" + SDKServerAPI.SHOP_LINK_WEBSITE_ID)
            )
    );
}
