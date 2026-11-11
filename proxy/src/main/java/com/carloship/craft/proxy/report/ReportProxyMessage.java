package com.carloship.craft.proxy.report;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.report.ReportNotificationPacket;
import com.carloship.craft.sdk.server.report.ReportPackets;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class ReportProxyMessage {

    public static final MinecraftChannelIdentifier CHANNEL_IDENTIFIER = MinecraftChannelIdentifier.from(ReportPackets.CHANNEL_IDENTIFIER);

    public static void broadcastReportToServers(ReportNotificationPacket packet) {
        for (RegisteredServer server : Main.proxy().getAllServers()) {
            
            if (server.getPlayersConnected().isEmpty()) continue;

            server.sendPluginMessage(CHANNEL_IDENTIFIER, out -> {
                out.writeLong(packet.reportId().getMostSignificantBits());
                out.writeLong(packet.reportId().getLeastSignificantBits());
                out.writeUTF(packet.targetName());
                out.writeUTF(packet.reporterName());
                out.writeUTF(packet.reason());
                out.writeUTF(packet.serverId());
            });
        }
    }
}
