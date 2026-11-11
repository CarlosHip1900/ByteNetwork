package com.carloship.craft.proxy.whitelist;

import com.carloship.craft.sdk.server.group.Group;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class ProxyWhitelist {

    private final String networkType;

    private boolean enabled;

    private final Set<String> allowedPlayers = new HashSet<>();

    private final Set<Group> allowedGroups = new HashSet<>();

    public boolean canJoin(String playerName, Group playerGroup) {

        if (!enabled) {
            return true;
        }

        if (allowedPlayers.contains(playerName)) {
            return true;
        }

        return allowedGroups.contains(playerGroup);
    }
}