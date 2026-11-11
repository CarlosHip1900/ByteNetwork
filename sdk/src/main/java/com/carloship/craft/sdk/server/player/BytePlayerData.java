package com.carloship.craft.sdk.server.player;

import com.carloship.craft.sdk.annotations.database.MongoId;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.leveling.Leveling;
import com.carloship.craft.sdk.server.leveling.XpCurve;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import net.kyori.adventure.text.Component;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BytePlayerData extends AbstractPlayer {

    @MongoId
    @BsonId
    private UUID uuid;
    private String username;

    private long lastLogin;
    private long lastLogout;
    private long firstJoin;

    private String groupIndex;
    private String packageGroupIndex;

    private long packageGroupTimestamp;

    private int bit;

    private String skinId;

    private boolean onlineMode;
    private Leveling leveling;

    private String email;
    private String hashedPassword;


    @JsonIgnore
    @BsonIgnore
    @NonNull
    public Group getGroup(){
        Group gp = Groups.fromId(groupIndex);
        return gp != null ? gp : Groups.MEMBER;
    }

    @JsonIgnore @BsonIgnore
    public Component getTag(){
        return getGroup().withName(getUsername());
    }

    public void addBit(int amount) {
        bit += amount;
    }

    public void removeBit(int amount) {
        bit = Math.max(0, bit - amount);
    }


    public static BytePlayerData create(UUID uuid, String name, boolean account){
        return new BytePlayerData(
                uuid,
                name,
                0,0,0,
                Groups.MEMBER.getId(),
                null, 0,
                0,
                null,
                account,
                new Leveling(XpCurve.LINEAR),
                null, null
        );
    }
}
