package com.carloship.craft.sdk.server.hologram;

import com.carloship.craft.sdk.server.instance.content.InstanceContentObject;
import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public final class Hologram implements InstanceContentObject {

    private final UUID uuid = UUID.randomUUID();

    private final Instance instance;
    private final Pos position;

    private final List<HologramLineEntity> entities = new ArrayList<>();

    private boolean spawned;

    public Hologram(Pos position, Instance instance) {
        this.position = position;
        this.instance = instance;
    }

    public static Hologram with(Pos pos, Instance instance){
        return new Hologram(pos, instance);
    }

    public Hologram addLine(HologramLine line) {
        entities.add(line.createEntity());
        return this;
    }

    public Hologram addLine(String text){
        return addLine(HologramLine.of(text));
    }

    @Override
    public Hologram spawn(Instance ignored) {

        if (spawned)
            return this;

        spawned = true;

        double offset = 0;

        var entities = getEntities().reversed();


        for (HologramLineEntity entity : entities) {

            entity.setTag(HologramManager.LINE_TAG, entity);

            entity.setInstance(instance, position.add(0, offset, 0));

            offset += .3;
        }

        return this;
    }

    @Override
    public void destroy() {

        if (!spawned)
            return;

        spawned = false;

        entities.forEach(Entity::remove);

        entities.clear();
    }

    public void render() {
        entities.forEach(HologramLineEntity::renderViewers);
    }

    public void addViewer(Player player) {
        entities.forEach(e -> e.addViewer(player));
    }

    public void removeViewer(Player player) {
        entities.forEach(e -> e.removeViewer(player));
    }
}