package com.carloship.craft.sdk.server.instance.content;

import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.utils.ServerUtils;
import com.carloship.craft.sdk.SDKServer;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;

public class InstanceProfileManager<P extends BytePlayerCraft> {

    private final SDKServer<P> root;

    public InstanceProfileManager(SDKServer<P> root) {
        this.root = root;
    }

    public void onInstanceJoin(BytePlayerCraft player, ServerInstance<P> instance) {
        ServerUtils.updateTeam(player);

        for (BytePlayerCraft other : instance.getPlayers()) {
            if (other == player) {
                sendAddTabPacket(player, player, instance.nametagFunction());
                continue;
            }

            sendAddTabPacket(player, other, instance.nametagFunction());
            sendAddTabPacket(other, player, instance.nametagFunction());
        }
    }

    public void onInstanceQuit(BytePlayerCraft player, ServerInstance<P> instance) {
        for (BytePlayerCraft other : instance.getPlayers()) {
            if (other == player) continue;

            sendRemoveTabPacket(player, other);
            sendRemoveTabPacket(other, player);
        }

        ServerUtils.removeTeam(player);
    }

    public void refreshPlayer(BytePlayerCraft player) {
        ServerUtils.updateTeam(player);

        ServerInstance<P> instance = root.getPlayerInstance(player);
        if (instance == null) return;

        for (BytePlayerCraft other : instance.getPlayers()) {
            sendRemoveTabPacket(other, player);
            sendAddTabPacket(other, player, instance.nametagFunction());
        }
    }

    private void sendRemoveTabPacket(BytePlayerCraft viewer, BytePlayerCraft target) {
        viewer.sendPacket(new PlayerInfoRemovePacket(List.of(target.getUuid())));
    }

    private void sendAddTabPacket(BytePlayerCraft viewer, BytePlayerCraft target, BiFunction<ServerInstanceMode, BytePlayerCraft, Component> displayFunction) {
        List<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();

        var skin = target.getSkin();
        properties.add(new PlayerInfoUpdatePacket.Property(
                "textures",
                skin.textures(),
                skin.signature()
        ));

        PlayerInfoUpdatePacket.Entry entry = getEntry(target, displayFunction, properties);

        viewer.sendPacket(new PlayerInfoUpdatePacket(
                EnumSet.of(
                        PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                        PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                        PlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
                        PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                ),
                List.of(entry)
        ));
    }

    private PlayerInfoUpdatePacket.@NonNull Entry getEntry(BytePlayerCraft target, BiFunction<ServerInstanceMode, BytePlayerCraft, Component> displayFunction, List<PlayerInfoUpdatePacket.Property> properties) {
        String nameToDisplay = target.getUsername();
        Component tagToDisplay = displayFunction.apply(target.getCurrentServerMode(), target);

        return new PlayerInfoUpdatePacket.Entry(
                target.getUuid(),
                nameToDisplay,
                properties,
                true,
                target.getLatency(),
                target.getGameMode(),
                tagToDisplay,
                null,
                0,
                true
        );
    }

    public static final EventNode<Event> EVENT_NODE = EventNode.all("tag-tablist-packets")
            .addListener(PlayerPacketOutEvent.class, event -> {
                Player viewer = event.getPlayer();

                if (event.getPacket() instanceof PlayerInfoUpdatePacket packet) {
                    var entries = packet.entries();
                    var filteredEntries = new ArrayList<PlayerInfoUpdatePacket.Entry>();

                    for (var entry : entries) {
                        if (entry.uuid().equals(viewer.getUuid())) {
                            filteredEntries.add(entry);
                            continue;
                        }

                        var targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(entry.uuid());
                        if (viewer.getInstance() != null && viewer.getInstance().equals(targetPlayer.getInstance())) {
                            filteredEntries.add(entry);
                        }
                    }

                    if (filteredEntries.isEmpty()) {
                        event.setCancelled(true);
                    }
                }
            });

}