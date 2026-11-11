package com.carloship.craft.sdk.server.chataction;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatActionCache {

    private static final ConcurrentHashMap<UUID, ChatAction> ACTIONS = new ConcurrentHashMap<>();

    public static void register(BytePlayerCraft player, ChatAction action) {
        ACTIONS.put(player.getUuid(), action);
    }

    public static void remove(BytePlayerCraft player) {
        ACTIONS.remove(player.getUuid());
    }

    public static boolean inAction(BytePlayerCraft player){
        return ACTIONS.containsKey(player.getUuid());
    }

    public static boolean handleChat(BytePlayerCraft player, String message) {
        ChatAction action = ACTIONS.get(player.getUuid());
        if (action != null) {
            action.handle(message);
            return true;
        }

        return false;
    }
}