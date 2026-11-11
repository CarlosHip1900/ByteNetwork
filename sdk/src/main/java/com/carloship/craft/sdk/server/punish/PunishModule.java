package com.carloship.craft.sdk.server.punish;

import com.carloship.craft.sdk.annotations.SDKModuleInfo;
import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.module.ModulePriority;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.punish.data.PunishEntry;
import com.carloship.craft.sdk.server.punish.data.RevokeEntry;
import com.carloship.craft.sdk.server.punish.pubsub.PunishPubSub;
import com.carloship.craft.sdk.server.punish.pubsub.RefreshPunishPlayerPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@SDKModuleInfo(
        id = "punish",
        name = "Punish",
        priority = ModulePriority.NORMAL,
        createDefault = false
)
public class PunishModule implements SDKModule {

    private final PunishDatabase database;
    private final PunishPubSub punishPubSub;

    public PunishModule(ModuleDependencyPacket packet) {
        this.database = new PunishDatabase(packet.getRedis(), packet.getObjectMapper(), packet.getMongo());
        this.punishPubSub = new PunishPubSub(this, packet.getObjectMapper(), packet.getPubSubAsyncCommands());
    }

    @Override
    public void createModule() {

    }

    @Override
    public void disableModule() {

    }

    public void registerRevokeListener(BiConsumer<UUID, RefreshPunishPlayerPacket> listener) {
        punishPubSub.registerRevokeListener(listener);
    }

    public CompletableFuture<Long> sendRefreshPacket(RefreshPunishPlayerPacket payload) {
        return punishPubSub.sendRefreshMessage(payload);
    }

    public void uncachePlayerMute(UUID uuid) {
        database.uncachePlayerMute(uuid);
    }

    public void uncachePlayer(UUID uuid) {
        database.uncachePlayer(uuid);
    }

    public PunishEntry getCachedMute(UUID uuid) {
        return database.getCachedMute(uuid);
    }

    public PunishEntry getLastPlayerPunishSync(UUID uuid) {
        return getLastPlayerPunish(uuid).join();
    }

    public CompletableFuture<PunishEntry> getLastPlayerPunish(UUID playerUuid) {
        return database.getLastPlayerPunish(playerUuid);
    }

    public CompletableFuture<Boolean> isBanned(UUID uuid) {
        return database.isBanned(uuid);
    }

    public CompletableFuture<Optional<PunishEntry>> getActiveBan(UUID uuid) {
        return database.getActiveBan(uuid);
    }


    public CompletableFuture<Optional<PunishEntry>> getActiveMute(UUID uuid) {
        return database.getActiveMute(uuid);
    }

    public CompletableFuture<Boolean> isMuted(UUID uuid) {
        return database.isMuted(uuid);
    }

    public CompletableFuture<Boolean> savePunish(PunishEntry punish) {
        log.info("Saving punishment...");
        return database.savePunish(punish);
    }


    public CompletableFuture<RevokeEntry> getPlayerRevoke(UUID playerUuid, UUID revokeUuid) {
        return database.getPlayerRevoke(playerUuid, revokeUuid);
    }

    public CompletableFuture<Boolean> saveRevoke(RevokeEntry revoke) {
        return database.saveRevoke(revoke);
    }

    public CompletableFuture<List<PunishEntry>> getAllPlayerHistory(UUID playerUuid) {
        return database.getAllPlayerHistory(playerUuid);
    }

    public CompletableFuture<Long> countPunishments(UUID uuid, PunishmentTable table) {
        return database.countPunishments(uuid, table);
    }
}
