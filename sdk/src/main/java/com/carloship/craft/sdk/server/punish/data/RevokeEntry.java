package com.carloship.craft.sdk.server.punish.data;

import com.carloship.craft.sdk.annotations.database.MongoId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokeEntry {

    @MongoId
    @BsonId
    private UUID id;

    private String revokeReason;
    private long timestamp;

    private UUID reviewerUuid;
    private String reviewerUsername;

    private PunishEntry punishObject;

}