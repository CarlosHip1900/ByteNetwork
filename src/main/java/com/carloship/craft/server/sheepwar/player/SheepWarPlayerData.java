package com.carloship.craft.server.sheepwar.player;

import com.carloship.craft.sdk.annotations.database.MongoId;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayer;
import lombok.*;
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

}
