package com.carloship.craft.sheepwars.instances.lobby;

import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.server.hologram.HologramLine;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.npc.impl.FakePlayer;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardAnimation;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardTemplate;
import com.carloship.craft.sdk.utils.Pair;
import com.carloship.craft.sdk.utils.TextUtils;
import com.carloship.craft.sheepwars.SheepWarsInstanceMode;
import com.carloship.craft.sheepwars.player.SheepWarPlayer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class SheepWarLobby extends ServerInstance<SheepWarPlayer> {

    public static final Pos DEFAULT_SPAWN = new Pos(0, 65, 0);

    public SheepWarLobby(SDKServer<SheepWarPlayer> root) {
        super(root, SheepWarsInstanceMode.LOBBY, "spawn");
    }

    @Override
    public void onStart(SDKServer<SheepWarPlayer> root) {

    }

    @Override
    public void onStop(SDKServer<SheepWarPlayer> root) {

    }

    @Override
    public void registryContent() {
        addNPC(FakePlayer.builder()
                .skin(NPC_SKIN)
                .position(new Pos(-17.5, 58, 45.5, -180, 0))
                .line(HologramLine.of("<gold><bold>BATTLE"))
                .line(HologramLine.of("<gray>24x24"))
                .line(HologramLine.of("<white>0 playing now!"))
                .line(HologramLine.of("<gradient:#FFD300:#DAA520><bold>CLICK TO PLAY!"))
                .build());

        addNPC(FakePlayer.builder()
                .skin(NPC_SKIN)
                .position(new Pos(-11.5, 58, 44.5, -180, 0))
                .line(HologramLine.of("<light_purple><bold>GROUP"))
                .line(HologramLine.of("<gray>8x8"))
                .line(HologramLine.of("<white>0 playing now!"))
                .line(HologramLine.of("<gradient:#FFD300:#DAA520><bold>CLICK TO PLAY!"))
                .build());

        addNPC(FakePlayer.builder()
                .skin(NPC_SKIN)
                .position(new Pos(-23.5, 58, 44.5, -180, 0))
                .line(HologramLine.of("<yellow><bold>MEGA"))
                .line(HologramLine.of("<gray>16x16"))
                .line(HologramLine.of("<white>0 playing now!"))
                .line(HologramLine.of("<gradient:#FFD300:#DAA520><bold>CLICK TO PLAY!"))
                .build());
    }

    @Override
    public void onPlayerJoin(SheepWarPlayer player, boolean firstLogin) {

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
        return SCORE_BOARD;
    }

    @Override
    public Pair<Component, Component> getTabList() {
        return SDKServerAPI.DEFAULT_TAB_LIST;
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
        return DEFAULT_SPAWN;
    }

    private static final ScoreBoardTemplate<SheepWarPlayer> SCORE_BOARD = new ScoreBoardTemplate<>(
            new ScoreBoardAnimation(
                    List.of(
                            DMM("<bold><#00a896>SHEEP WARS")
                    )
            ),

            p -> List.of(
                    Component.empty(),
                    DMM("<white>Level: <gray>[" + p.getLeveling().getLevel() + "]"),
                    Component.empty(),
                    DMM("<white>Coins: <gold>" + TextUtils.formatNumberBrazil(p.getCoins())),
                    DMM("<white>Bit: <aqua>\uD83D\uDC7E " + TextUtils.formatNumberBrazil(p.getPlayerData().getBit())),
                    Component.empty(),
                    DMM("<dark_gray>" + SDKServerAPI.SHOP_LINK_WEBSITE_ID)
            )
    );

    private static final PlayerSkin NPC_SKIN = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTY3OTA5NzgxMjYyNCwKICAicHJvZmlsZUlkIiA6ICI3ZDJhY2YzOGQ3YTQ0YjU0YTliMGNkYTZhNzk1YmNmYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJCb3VuY2luZXNzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzMwZjUwMzk0YzZkN2RiYzAzZWE1OWZkZjUwNDAyMGRjNWQ2NTQ4ZjlkM2JjOWRjYWM4OTZiYjVjYTA4NTg3YSIKICAgIH0KICB9Cn0=",
            "Tun6IJWyj09dpd1dMbzm5LKdIIBDCAvuTc08YhqXvDeqsikTCbrsx9pmbUl40f/eSdH4pyVQRNZh9otFwqaJw0Q4atdbAOEegIVPHRr2y3OQuVDQr7TsAD//YT8tLwK30f13exNgmFdC3FeJbujunYBoJe9HTOyW8SmZBVWCjxw9bW5On/dRCG5qzPW6IQnqLKpdzoDR9Xrw2K1KvRHNv4ys7H81K/1uP4e/dFRheIely58dscrSoKH5dPVvLdy8FqEtjcX/M9+OHfoM/2a1xDGhYttwJmX01UROSRArKD8XPhe8EpvaulPQB8u2Qagz90u84Se5KwFfWlImDIr/WhloIgIcymGBbjFB84WGpl1Adp/if0aQ4TW++KZzifio9ZJZSXidDZ55oaJi5CGC6wtLID8VEptrRO2UYDFYLVFwxw8Us1HB5fzNIweoDLlnxcxzenp9mhpwk0vtI1yT7wmYHnoR2JpcCwUv8pttBDD71imUDpsnQoONvPQwrN9oMjG0mjjJuPIr1+V6WxuKtXbpESv15MJbNGlZRaHfPI4tiuBlhIjSB9ZBzU+J7xpwkCQTumiNsa324monEj24Wv1c3w9b7mfw5oDprQjDTMDMkbdDOtxqzbGVM4HUQvn3bcilpwM8G2CY3F3CQ5K+XOZV0+dMHJUEsqId80OP5xQ="
    );
}
