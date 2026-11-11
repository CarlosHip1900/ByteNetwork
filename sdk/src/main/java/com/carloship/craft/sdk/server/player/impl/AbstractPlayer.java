package com.carloship.craft.sdk.server.player.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.UUID;

@Getter
@Setter
public abstract class AbstractPlayer {

    @JsonIgnore @BsonIgnore
    private transient boolean dirty;

    public void markDirty() {
        this.dirty = true;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public abstract UUID getUuid();

    public abstract String getUsername();
}
