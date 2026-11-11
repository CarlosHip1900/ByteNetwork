package com.carloship.craft.sheepwars.player;

import com.carloship.craft.sdk.annotations.database.MongoId;
import com.carloship.craft.sdk.server.leveling.Leveling;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SheepWarPlayerData extends AbstractPlayer {

    @MongoId
    @BsonId
    private UUID uuid;

    private String username;
    private Map<String, Map<String, Long>> stats;

    private double coins;
    private Leveling leveling;

    public void addCoins(double b) {
        coins += b;
    }

    public void removeCoins(double b) {
        coins = Math.max(0, coins - b);
    }
}
