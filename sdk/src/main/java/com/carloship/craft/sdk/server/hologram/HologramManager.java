package com.carloship.craft.sdk.server.hologram;

import com.carloship.craft.sdk.server.instance.content.InstanceFeatureManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HologramManager implements InstanceFeatureManager {

    public static final Tag<HologramLineEntity> LINE_TAG = Tag.Transient("hologram-line");
    private static final EventNode<EntityEvent> NODE = EventNode.type("hologram-events", EventFilter.ENTITY);

    private final Instance instance;
    private final List<Hologram> holograms = new ArrayList<>();

    static {
        NODE.addListener(EntityAttackEvent.class, event -> {

            if (!(event.getEntity() instanceof Player player))
                return;

            HologramLineEntity hologram = event.getTarget().getTag(LINE_TAG);

            if (hologram == null)
                return;

            hologram.onLeftClick(player);
        });

        NODE.addListener(PlayerEntityInteractEvent.class, event -> {

            if (event.getHand() == PlayerHand.OFF)
                return;

            HologramLineEntity line = event.getTarget().getTag(LINE_TAG);

            if (line == null)
                return;

            line.onRightClick(event.getPlayer());
        });

        if (NODE.getParent() == null) {
            MinecraftServer.getGlobalEventHandler().addChild(NODE);
        }
    }

    public HologramManager(Instance instance) {
        this.instance = instance;
    }

    public void register(Hologram hologram) {
        holograms.add(Objects.requireNonNull(hologram, "hologram"));
    }

    public void unregister(Hologram hologram) {
        holograms.remove(hologram);
        hologram.destroy();
    }

    @Override
    public void spawnAll() {
        holograms.forEach(h -> h.spawn(instance));
    }

    @Override
    public void destroyAll() {
        holograms.forEach(Hologram::destroy);
        holograms.clear();
    }

    public void addViewer(Player player) {
        holograms.forEach(h -> h.addViewer(player));
    }

    public void removeViewer(Player player) {
        holograms.forEach(h -> h.removeViewer(player));
    }

    public void renderAll() {
        holograms.forEach(Hologram::render);
    }

    public List<Hologram> getHolograms() {
        return Collections.unmodifiableList(holograms);
    }
}
