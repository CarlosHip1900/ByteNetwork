package com.carloship.craft.sheepwars.player;

import com.carloship.craft.sdk.server.leveling.Leveling;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SheepWarPlayer extends BytePlayerCraft {

    private SheepWarPlayerData sheepData;

    public SheepWarPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    public Map<String, Long> statsOf(String mode){
        return sheepData.getStats().getOrDefault(mode, new HashMap<>());
    }

    public Map<String, Map<String, Long>> statsAll(){
        return sheepData.getStats();
    };

    public double getCoins() {
        return sheepData.getCoins();
    }

    public void addCoins(double b){
        sheepData.addCoins(b);
    }

    public void setCoins(double b){
        sheepData.setCoins(b);
    }

    public void removeCoins(double b){
        sheepData.removeCoins(b);
    }

    public Leveling getLeveling(){
        return sheepData.getLeveling();
    }

}
