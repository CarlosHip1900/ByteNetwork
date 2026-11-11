package com.carloship.craft.proxy.report;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.report.ReportCooldown;
import com.carloship.craft.sdk.server.report.ReportModule;
import com.carloship.craft.sdk.server.report.ReportNotificationPacket;
import com.carloship.craft.sdk.server.report.data.ReportEntry;
import com.carloship.craft.sdk.server.report.data.ReportStatus;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class ReportListener {

    private final ReportModule reportModule = Main.getInstance().getReportModule();

    @Subscribe
    public EventTask onReport(ReportPlayerEvent event) {
        Player player = event.player();
        String targetName = event.targetName();

        if (ReportCooldown.isPlayerInCooldown(player.getUniqueId())) {
            long remaining = ReportCooldown.getRemainingTimeSeconds(player.getUniqueId());
            return EventTask.async(() -> player.sendMessage(DMM("<red>Please wait <white>" + remaining + " seconds <red>before submitting another report.")));
        }

        Optional<Player> targetOptional = Main.proxy().getPlayer(targetName);

        if (targetOptional.isEmpty()) {
            return EventTask.async(() -> player.sendMessage(DMM("<red>Player " + targetName + " could not be found or is offline.")));
        }

        Player target = targetOptional.get();

        if (target.getUniqueId().equals(player.getUniqueId())) {
            return EventTask.async(() -> player.sendMessage(DMM("<red>You cannot report yourself.")));
        }

        String serverId = target.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("Unknown");

        ReportCooldown.applyCooldown(player.getUniqueId());

        player.sendMessage(DMM("<gray>Processing your report... Please wait."));

        ReportEntry report = new ReportEntry(
                UUID.randomUUID(),
                target.getUniqueId(),
                target.getUsername(),
                player.getUniqueId(),
                player.getUsername(),
                System.currentTimeMillis(),
                event.reason(),
                serverId,
                ReportStatus.OPEN,
                List.of(),
                1,
                null,
                0L,
                null,
                null
        );

        return EventTask.async(() -> reportModule.createReport(report).thenRun(() -> {
            player.sendMessage(DMM("<green>Your report against <white>" + target.getUsername() + " <green>has been successfully submitted!"));

            ReportNotificationPacket notificationPacket = new ReportNotificationPacket(
                    report.getId(),
                    report.getUsername(),
                    report.getReporterUsername(),
                    report.getReason(),
                    report.getServerId()
            );

            ReportProxyMessage.broadcastReportToServers(notificationPacket);

        }).exceptionally(ex -> {
            player.sendMessage(DMM("<red>An internal error occurred while saving your report. Please try again later."));
            return null;
        }));
    }

    @Subscribe
    public void onReceive(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(ReportProxyMessage.CHANNEL_IDENTIFIER)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        ByteArrayInputStream bais = new ByteArrayInputStream(event.getData());
        DataInputStream dis = new DataInputStream(bais);

        try {
            String channel = dis.readUTF();
            UUID mainUuid = UUID.fromString(dis.readUTF());
            String targetName = dis.readUTF();

            Player player = Main.proxy().getPlayer(mainUuid).orElse(null);

            if (player == null) return;

            if (channel.equalsIgnoreCase("report_player")){
                String reason = dis.readUTF();
                Main.fireEvent(new ReportPlayerEvent(player, targetName, reason));
            }


        } catch (Exception e){
            log.error("Error unpack message", e);
        }
    }
}
