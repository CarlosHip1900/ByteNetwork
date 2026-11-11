package com.carloship.craft.sdk.server.punish.data;

import com.carloship.craft.sdk.annotations.database.MongoId;
import com.carloship.craft.sdk.server.punish.PunishmentTable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunishEntry {

    @MongoId
    @BsonId
    private UUID id;

    private UUID userUuid;
    private String username;
    private String reason;
    private long timestamp;

    private UUID reportId;

    private UUID reporterUuid;
    private String reporterUsername;

    private UUID authorUuid;
    private String authorUsername;

    private String proveLink;
    private String serverId;

    private long durationTime;
    private boolean passed;

    private PunishType type;

    private UUID revokeId;

    @JsonIgnore
    @BsonIgnore
    public boolean isActive() {
        if (isPassed()) return false;
        if (isRevoked()) return false;

        if (durationTime <= 0) return true;

        return System.currentTimeMillis() < (timestamp + durationTime);
    }

    @JsonIgnore
    @BsonIgnore
    public long getRemainingTime() {
        if (durationTime <= 0) return -1;

        return Math.max((timestamp + durationTime) - System.currentTimeMillis(), 0);
    }

    @JsonIgnore
    @BsonIgnore
    public PunishmentTable getPunishmentType() {
        return PunishmentTable.fromId(reason);
    }

    @JsonIgnore
    @BsonIgnore
    public boolean isRevoked(){
        return revokeId != null;
    }

    @JsonIgnore
    @BsonIgnore
    public String getPrefix(){
        return getId().toString().substring(0, 5).toUpperCase();
    }

}