package com.carloship.craft.rankup;

import com.carloship.craft.rankup.player.RankupPlayer;
import com.carloship.craft.rankup.player.RankupPlayerModule;
import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardAnimation;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardTemplate;
import com.carloship.craft.sdk.utils.TextUtils;
import net.kyori.adventure.text.Component;

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class RankupAPI {

    private static final RankupServer INSTANCE = (RankupServer) RankupServer.INSTANCE;

    public static RankupPlayerModule rankupPlayerModule() {
        return INSTANCE.getRankupPlayerModule();
    }

    public static final ScoreBoardTemplate<RankupPlayer> SCORE_BOARD = new ScoreBoardTemplate<>(
            new ScoreBoardAnimation(
                    List.of(
                            DMM("<bold><#00a896>RANK UP")
                    )
            ),

            p -> List.of(
                    Component.empty(),
                    DMM("<white>Level: <gray>[" + p.getLeveling().getLevel() + "]"),
                    DMM("<white>XP: <aqua>" + TextUtils.formatNumberBrazil(p.getLeveling().getXp())),
                    Component.empty(),
                    DMM("<white>Coins: <gold>" + TextUtils.formatNumberBrazil(p.getCoins())),
                    DMM("<white>Bit: <aqua>\uD83D\uDC7E " + TextUtils.formatNumberBrazil(p.getPlayerData().getBit())),
                    Component.empty(),
                    DMM("<dark_gray>" + SDKServerAPI.SHOP_LINK_WEBSITE_ID)
            )
    );

}
