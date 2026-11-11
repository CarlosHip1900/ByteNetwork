package com.carloship.craft.sdk.server.chataction;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerChatEvent;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.util.List;

public class ChatActionDispatcher {

    public static void init(){
        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, event -> {
            if (!(event.getPlayer() instanceof BytePlayerCraft player)) return;

            if (ChatActionCache.handleChat(player, event.getRawMessage())) {
                event.setCancelled(true);
            }
        });
    }


    public static ChatAction sendAction(BytePlayerCraft player, List<Component> message) {

        player.sendMessage(Component.empty());
        message.forEach(player::sendMessage);
        player.sendMessage(Component.empty());

        ChatAction action = new ChatAction(player);
        ChatActionCache.register(player, action);

        return action;
    }

}
