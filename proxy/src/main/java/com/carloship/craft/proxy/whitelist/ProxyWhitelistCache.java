package com.carloship.craft.proxy.whitelist;

import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyWhitelistCache {

    private final Map<String, ProxyWhitelist> cache = new HashMap<>();

    public ProxyWhitelist fetch(String type) {
        return cache.computeIfAbsent(type, ProxyWhitelist::new);
    }

    public boolean canJoin(String type, String playerName, Group group) {
        return fetch(type).canJoin(playerName, group);
    }

    public void disableWhitelist(String type) {
        fetch(type).setEnabled(false);
    }

    public void enableWhitelist(String type) {
        var list = fetch(type);
        list.setEnabled(true);
        list.getAllowedGroups().addAll(List.of(Groups.MASTER, Groups.ADMIN));
    }

    public boolean inWhitelist(String type){
        return cache.get(type) != null;
    }

    public void addPlayer(String type, String playerName) {
        fetch(type).getAllowedPlayers().add(playerName);
    }

    public boolean containsPlayer(String type, String playerName) {
        return fetch(type).getAllowedPlayers().contains(playerName);
    }

    public void removePlayer(String type, String playerName) {
        fetch(type).getAllowedPlayers().remove(playerName);
    }

    public void addGroup(String type, Group group) {
        fetch(type).getAllowedGroups().add(group);
    }

    public boolean containsGroup(String type, Group group) {
        return fetch(type).getAllowedGroups().contains(group);
    }

    public void removeGroup(String type, Group group) {
        fetch(type).getAllowedGroups().remove(group);
    }
}