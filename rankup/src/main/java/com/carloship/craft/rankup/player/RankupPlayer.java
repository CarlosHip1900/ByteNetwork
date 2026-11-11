package com.carloship.craft.rankup.player;

import com.carloship.craft.sdk.server.leveling.Leveling;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;

@Getter
@Setter
public class RankupPlayer extends BytePlayerCraft {

    private RankupPlayerData rankupPlayerData;

    public RankupPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    public double getCoins() {
        return rankupPlayerData.getCoins();
    }

    public void addCoins(double b){
        rankupPlayerData.addCoins(b);
    }

    public void setCoins(double b){
        rankupPlayerData.setCoins(b);
    }

    public void removeCoins(double b){
        rankupPlayerData.removeCoins(b);
    }

    public Leveling getLeveling(){
        return rankupPlayerData.getLeveling();
    }

}
