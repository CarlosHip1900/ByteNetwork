package com.carloship.craft.proxy.punish;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.punish.PunishmentTable;
import com.carloship.craft.sdk.server.punish.data.PunishEntry;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Optional;
import java.util.UUID;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class PunishMessageListener {

    public static final ChannelIdentifier CHANNEL_IDENTIFIER = MinecraftChannelIdentifier.from("staff:punish");
    
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CHANNEL_IDENTIFIER)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        ByteArrayInputStream bais = new ByteArrayInputStream(event.getData());
        DataInputStream dis = new DataInputStream(bais);

        try {
            String subChannel = dis.readUTF();

            if (subChannel.equalsIgnoreCase("kick_by_ban")) {
                UUID targetUuid = UUID.fromString(dis.readUTF());

                Optional<Player> targetOpt = Main.proxy().getPlayer(targetUuid);
                targetOpt.ifPresent(this::processProxyBanKick);
            }
        } catch (Exception e) {
            PunishMessageListener.log.error("[Proxy-Punish] Failed to decode plugin message payload: ", e);
        }

    }

    private void processProxyBanKick(Player target) {
        Main.getInstance().getPunishModule().getLastPlayerPunish(target.getUniqueId())
                .thenAccept(lastPunishment -> {
                    if (lastPunishment == null) return;

                    if (lastPunishment.isActive() && !lastPunishment.isPassed()) {

                        Main.proxy().getScheduler().buildTask(this, () -> {
                            Component kickReason = createBanMessage(lastPunishment);
                            target.disconnect(kickReason);
                        }).schedule();
                    }
                }).exceptionally(ex -> {
                    System.err.println("[Proxy-Punish] Error querying ban for player " + target.getUsername() + ": " + ex.getMessage());
                    return null;
                });
    }

    public static Component createBanMessage(PunishEntry object) {
        long rt = object.getRemainingTime();

        Component firstLine;
        if (rt == -1) {
            firstLine = DMM("<red>You have been permanently banned from this server");
        } else {
            firstLine = DMM("<red>You have been temporarily banned from this server for <white>"
                    + TextUtils.formateDateDMY_HMS(rt));
        }

        return Component.empty()
                .append(DMM("<red><b>BYTE NETWORK</b></red>")).appendNewline().appendNewline()
                .append(firstLine).appendNewline().appendNewline()
                .append(DMM("<gray>Reason: <white>" + PunishmentTable.fromId(object.getReason()).getPrefix())).appendNewline()
                .append(DMM("<gray>ID: <white>#" + object.getPrefix())).appendNewline()
                .append(DMM("<gray>Use the punishment ID to submit an appeal"));
    }

}
