package com.carloship.craft.sdk.server.group;

import com.carloship.craft.sdk.utils.ServerUtils;
import com.carloship.craft.sdk.utils.TextUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Team;
import org.bson.codecs.pojo.annotations.BsonIgnore;

@Getter
@AllArgsConstructor
public final class GroupImpl implements Group {

    private String tag;

    private final String order;
    private final String id;
    private final boolean defaultGroup;
    private final boolean premium;
    private final int weight;

    public GroupImpl(String id, String order, int weight, Component tag, boolean defaultGroup, boolean premium) {
        this.id = id;
        this.order = order;
        this.weight = weight;
        this.tag = tag != null ? TextUtils.MM.serialize(tag) : "";
        this.defaultGroup = defaultGroup;
        this.premium = premium;
    }


    @JsonIgnore
    @BsonIgnore
    @Override
    public Component getTag() {
        if (tag == null || tag.isEmpty()) {
            return Component.empty();
        }
        try {
            return TextUtils.MM.deserialize(tag);
        } catch (Exception e) {
            return Component.text(tag);
        }
    }

    @JsonIgnore
    @BsonIgnore
    public Team getTeam(){
        return ServerUtils.generateTeam(order, this);
    }
}
