package com.carloship.craft.proxy;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class ProxyHelper {

    public static final ChannelIdentifier CHANNEL_IDENTIFIER = MinecraftChannelIdentifier.create("byte","player");

    public static void sendTeleportRequest(Player playerToTeleport, Player targetPlayer, boolean errorMsg) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(byteOutput);

        try {
            dataOutput.writeUTF("teleport_to_player");

            dataOutput.writeUTF(playerToTeleport.getUniqueId().toString());

            dataOutput.writeUTF(targetPlayer.getUniqueId().toString());

            byte[] data = byteOutput.toByteArray();
            playerToTeleport.getCurrentServer().ifPresent(serverConnection -> {
                serverConnection.sendPluginMessage(CHANNEL_IDENTIFIER, data);
            });

        } catch (IOException e) {
            log.error("Error while send teleport request", e);
            if (errorMsg){
                playerToTeleport.sendMessage(DMM("<red>Oops... An error occurred while teleporting to the player."));
            }
        }
    }

}
