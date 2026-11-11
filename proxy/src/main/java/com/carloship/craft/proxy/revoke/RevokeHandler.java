package com.carloship.craft.proxy.revoke;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.server.punish.PunishModule;
import com.carloship.craft.sdk.server.punish.data.PunishEntry;
import com.carloship.craft.sdk.server.punish.data.RevokeEntry;
import com.carloship.craft.sdk.server.punish.pubsub.RefreshPunishPlayerPacket;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class RevokeHandler {

    private static final PunishModule PUNISH_MODULE = Main.getInstance().getPunishModule();
    private static final ProxyServer SERVER = Main.proxy();
    private static final BytePlayerModule PLAYER_MODULE = Main.getInstance().getPlayerModule();

    public static void handle(String targetName, Player source, BytePlayerData sourceNetwork, String reason) {
        SERVER.getPlayer(targetName).ifPresentOrElse(onlineTarget -> {
            BytePlayerData targetNetwork = PLAYER_MODULE.fetchEntitySync(onlineTarget.getUniqueId());
            revokeOnlinePlayer(source, sourceNetwork, onlineTarget, targetNetwork, reason);
        }, () -> {
            PLAYER_MODULE.fetchEntityAsync(targetName).thenAccept(targetNetwork -> {
                revokeOfflinePlayer(source, targetNetwork, reason);
            }).exceptionally(ex -> {
                source.sendMessage(DMM("<red>Oops... An error occurred while retrieving player information for " + targetName + "."));
                log.error("Error while finding player with name {} in player database", targetName, ex);
                return null;
            });
        });
    }

    private static void revokeOnlinePlayer(Player sender, BytePlayerData senderNetwork, Player target, BytePlayerData targetNetwork, String reason) {
        PUNISH_MODULE.getLastPlayerPunish(target.getUniqueId()).thenCompose(lastPunishment -> {
            if (lastPunishment == null) {
                sender.sendMessage(DMM("<red>Player ").append(targetNetwork.getTag()).append(DMM("<red> does not have any active punishments!")));
                return CompletableFuture.completedFuture(null);
            }

            if (lastPunishment.isRevoked()) {
                sender.sendMessage(DMM("<red>Player ").append(targetNetwork.getTag()).append(DMM(" already has a revoked punishment.")));
                return CompletableFuture.completedFuture(null);
            }

            RevokeEntry object = createRevoke(sender.getUniqueId(), sender.getUsername(), reason, lastPunishment);
            lastPunishment.setPassed(true);
            lastPunishment.setRevokeId(object.getId());

            return PUNISH_MODULE.savePunish(lastPunishment)
                    .thenCompose(v1 -> PUNISH_MODULE.sendRefreshPacket(new RefreshPunishPlayerPacket(target.getUniqueId(), true, false)))
                    .thenCompose(v -> PUNISH_MODULE.saveRevoke(object))
                    .whenComplete((aBoolean, throwable) -> {
                        if (throwable != null) {
                            log.error("Error while saving player punishment revocation", throwable);
                            sender.sendMessage(DMM("<red>An error occurred while revoking the punishment for player ").append(targetNetwork.getTag()));
                            return;
                        }
                        target.sendMessage(DMM("<green>Player ").append(senderNetwork.getTag()).append(DMM("<green> has just revoked your punishment <white>#" + lastPunishment.getPrefix())));
                        sender.sendMessage(DMM("<green>You have successfully revoked punishment <white>#" + lastPunishment.getPrefix() + "<green> for player ").append(targetNetwork.getTag()));
                    });
        });
    }

    private static void revokeOfflinePlayer(Player sender, BytePlayerData targetNetwork, String reason) {
        PUNISH_MODULE.getLastPlayerPunish(targetNetwork.getUuid()).thenCompose(lastPunishment -> {
            if (lastPunishment == null) {
                sender.sendMessage(DMM("<red>Player ").append(targetNetwork.getTag()).append(DMM(" does not have any active punishments!")));
                return CompletableFuture.completedFuture(null);
            }

            if (lastPunishment.isRevoked()) {
                sender.sendMessage(DMM("<red>Player ").append(targetNetwork.getTag()).append(DMM(" already has a revoked punishment.")));
                return CompletableFuture.completedFuture(null);
            }

            RevokeEntry object = createRevoke(sender.getUniqueId(), sender.getUsername(), reason, lastPunishment);
            lastPunishment.setPassed(true);
            lastPunishment.setRevokeId(object.getId());

            return PUNISH_MODULE.savePunish(lastPunishment)
                    .thenCompose(v -> PUNISH_MODULE.saveRevoke(object))
                    .whenComplete((aBoolean, throwable) -> {
                        if (throwable != null) {
                            log.error("Error while saving player punishment revocation", throwable);
                            sender.sendMessage(DMM("<red>An error occurred while revoking the punishment for player ").append(targetNetwork.getTag()));
                            return;
                        }
                        sender.sendMessage(DMM("<green>You have successfully revoked punishment <white>#" + lastPunishment.getPrefix() + "<green> for player ").append(targetNetwork.getTag()));
                    });
        });
    }

    private static RevokeEntry createRevoke(UUID senderUuid, String senderName, String reason, PunishEntry lastPunishment) {
        return new RevokeEntry(UUID.randomUUID(), reason, System.currentTimeMillis(), senderUuid, senderName, lastPunishment);
    }

}