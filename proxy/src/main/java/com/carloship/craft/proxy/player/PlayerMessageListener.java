package com.carloship.craft.proxy.player;

import com.carloship.craft.proxy.Main;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.UUID;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;


@Slf4j
public class PlayerMessageListener {

    public static final MinecraftChannelIdentifier CHANNEL_IDENTIFIER = MinecraftChannelIdentifier.from("player:actions");

    @Subscribe
    public void onReceive(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CHANNEL_IDENTIFIER)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        ByteArrayInputStream bais = new ByteArrayInputStream(event.getData());
        DataInputStream dis = new DataInputStream(bais);

        try {
            String channel = dis.readUTF();
            UUID mainUuid = UUID.fromString(dis.readUTF());
            UUID targetUuid = UUID.fromString(dis.readUTF());

            Player player = Main.proxy().getPlayer(mainUuid).orElse(null);
            Player target = Main.proxy().getPlayer(targetUuid).orElse(null);

            if (player == null) {
                log.error("Player from player:actions is null!");
                return;
            }

            if (target == null) {
                player.sendMessage(DMM("<red>Player not found..."));
                return;
            }

/*            if (channel.equalsIgnoreCase("add_friend")){
                Main.fireEvent(new FriendAddEvent(player, target));
            }

            if (channel.equalsIgnoreCase("create_party_with")){
                Main.fireEvent(new PartyCreateWithEvent(player, target));
            }

            if (channel.equalsIgnoreCase("block_player")){
                Main.fireEvent(new IgnorePlayerEvent(player, target));
            }*/

        } catch (Exception e){
            log.error("Error unpack message", e);
        }
    }
}
