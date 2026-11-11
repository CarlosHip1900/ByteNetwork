package com.carloship.craft.proxy.revoke;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.proxy.punish.PunishMessageListener;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.UUID;

@Slf4j
public class RevokeMessageListener {

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(PunishMessageListener.CHANNEL_IDENTIFIER)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        ByteArrayInputStream bais = new ByteArrayInputStream(event.getData());
        DataInputStream dis = new DataInputStream(bais);

        try {
            String subChannel = dis.readUTF();

            if (subChannel.equalsIgnoreCase("init_revoke")) {
                String targetName = dis.readUTF();
                UUID senderUuid = UUID.fromString(dis.readUTF());
                String reason = dis.readUTF();

                Main.proxy().getPlayer(senderUuid).ifPresent(sender -> {
                    BytePlayerData sourceNetwork = Main.getInstance().getPlayerModule().fetchEntitySync(sender.getUniqueId());
                    RevokeHandler.handle(targetName, sender, sourceNetwork, reason);
                });
            }
        } catch (Exception e) {
            log.error("[Proxy-Punish] Failed to decode plugin message payload: ", e);
        }

    }
}
