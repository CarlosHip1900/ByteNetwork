package com.carloship.craft.sdk.server.group;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Team;

public interface Group {

    String getId();

    Component getTag();

    boolean isDefaultGroup();

    boolean isPremium();

    int getWeight();

    Team getTeam();

    default Component withName(String name){
        return getTag().append(Component.text(name));
    }

    default NamedTextColor getColor(){
        return (NamedTextColor) getTag().color();
    }

    static boolean hasPermission(Group user, Group required) {
        int userWeight = user.getWeight();
        int requiredWeight = required.getWeight();

        return userWeight <= requiredWeight;
    }

    static boolean has(BytePlayerCraft player, Group group){
        return hasPermission(player.getGroup(), group);
    }
}
