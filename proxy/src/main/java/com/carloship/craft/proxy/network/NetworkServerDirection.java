package com.carloship.craft.proxy.network;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NetworkServerDirection {

    private static final ConcurrentHashMap<UUID, String> DIRECTIONS = new ConcurrentHashMap<>();

    public static void directPlayer(UUID uuid, String gameId) {
        DIRECTIONS.put(uuid, gameId);
    }

    public static String fromPlayer(UUID uuid){
        return DIRECTIONS.get(uuid);
    }

    public static void undirectPlayer(UUID uuid){
        DIRECTIONS.remove(uuid);
    }

}
