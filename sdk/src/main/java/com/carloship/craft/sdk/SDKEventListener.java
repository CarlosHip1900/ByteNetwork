package com.carloship.craft.sdk;

import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.menu.Menu;
import com.carloship.craft.sdk.server.menu.MenuInventory;
import com.carloship.craft.sdk.utils.ServerUtils;
import com.carloship.craft.sdk.utils.TextUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.inventory.AbstractInventory;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class SDKEventListener {

    private static final EventNode<Event> EVENT_NODE = EventNode.all("sdk-events");

    private static final Cache<UUID, Boolean> cooldownCache = Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .build();

    public static <P extends BytePlayerCraft> void init(SDKServer<P> root) {
        ServerInstanceMode MAIN_MODE = root.getMainMode();

        EVENT_NODE.addListener(AsyncPlayerConfigurationEvent.class, event -> {
                    ServerInstance<P> instance = root.getInstanceManager().findBest(MAIN_MODE);
                    if (!(event.getPlayer() instanceof BytePlayerCraft player)) {
                        event.getPlayer().kick(TextUtils.INTERNAL_ERROR);
                        log.error("Player isn't BytePlayerCraft");
                        return;
                    }

                    if (instance == null) {
                        player.kick(TextUtils.INTERNAL_ERROR);
                        log.error("Cannot possible find instance for player in config event");
                        return;
                    }

                    event.setSpawningInstance(instance.getInstance());
                    player.setRespawnPoint(instance.getSpawnPoint());

                    player.setCurrentInstance(instance);

                    root.proxiedJoinPlayer(player.getUuid(), player.getUsername());
                })
                .addListener(PlayerChatEvent.class, event -> {
                    if (!(event.getPlayer() instanceof BytePlayerCraft serverP)) return;
                    if (event.isCancelled()) return;

                    @SuppressWarnings("unchecked")
                    P serverPlayer = (P) serverP;

                    if (cooldownCache.getIfPresent(serverPlayer.getUuid()) != null) {
                        serverPlayer.sendErrorMessage("Please wait before executing this command again.");
                        event.setCancelled(true);
                        return;
                    }

                    cooldownCache.put(serverPlayer.getUuid(), Boolean.TRUE);

/*
                    if (PunishHook.isMuted(serverPlayer.getUuid())) {
                        PunishEntry entry = PunishHook.getMute(serverPlayer.getUuid());
                        serverPlayer.sendMessage("");
                        serverPlayer.sendMessage(DMM("<red>Você está com um silenciamento em ativo"));
                        serverPlayer.sendMessage(DMM("<red>Restam: " + formatTime(entry.getRemainingTime())));
                        serverPlayer.sendMessage(DMM("<red>Pelo motivo: " + entry.getPunishmentType().getPrefix()));
                        serverPlayer.sendMessage("");
                        event.setCancelled(true);
                        return;
                    }
*/


                    ServerInstance<P> server = root.getServerInstance(serverPlayer.getCurrentServerInstanceId());
                    if (server == null) {
                        serverPlayer.sendErrorMessage("Could not retrieve your information.");
                        event.setCancelled(true);
                        return;
                    }

                    var customFormatter = server.getChatFormatter();

                    Component finalMessage;
                    if (customFormatter != null) {
                        finalMessage = customFormatter.apply(serverPlayer, event);
                    } else {
                        finalMessage = root.getDefaultChatFormatter(serverPlayer, event);
                    }

                    event.setFormattedMessage(finalMessage);
                })
                .addListener(PlayerDisconnectEvent.class, event -> {
                    if (!(event.getPlayer() instanceof BytePlayerCraft serverPlayer)) return;

                    root.proxiedLeavePlayer(serverPlayer.getUuid());

                    ServerUtils.removeTeam(serverPlayer);
                })
                .addListener(PlayerSkinInitEvent.class, event -> {
                    if (!(event.getPlayer() instanceof BytePlayerCraft serverPlayer)) return;

                    root.getInstanceProfileManager().onInstanceJoin(serverPlayer, root.getPlayerInstance(serverPlayer));
                })
                //PLUGIN MESSAGE
                .addListener(PlayerPluginMessageEvent.class, event -> {
                    if (!event.getIdentifier().equals("byte:player")) return;

                    ByteArrayInputStream byteInput = new ByteArrayInputStream(event.getMessage());
                    DataInputStream dataInput = new DataInputStream(byteInput);

                    try {
                        String subChannel = dataInput.readUTF();

                        if (subChannel.equals("teleport_to_player")) {
                            UUID playerToTeleportUUID = UUID.fromString(dataInput.readUTF());
                            UUID targetPlayerUUID = UUID.fromString(dataInput.readUTF());

                            Player playerToTeleport = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerToTeleportUUID);
                            Player targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(targetPlayerUUID);

                            if (playerToTeleport != null && targetPlayer != null) {
                                playerToTeleport.teleport(targetPlayer.getPosition());
                            }
                        }
                    } catch (IOException e) {
                        log.error("Error translate proxy message", e);
                    }
                })
                .addListener(InventoryPreClickEvent.class, event -> {
                    Player player = event.getPlayer();
                    AbstractInventory openInv = player.getOpenInventory();
                    if (!(openInv instanceof MenuInventory menuInventory)) {
                        return;
                    }

                    Menu menu = menuInventory.getMenu();

                    AbstractInventory clickedInv = event.getInventory();
                    if (clickedInv == openInv) {
                        event.setCancelled(true);
                        menu.handleWhenClicked(event);
                    }
                });

        MinecraftServer.getGlobalEventHandler().addChild(EVENT_NODE);
    }

}
