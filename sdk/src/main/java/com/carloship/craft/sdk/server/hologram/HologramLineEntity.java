package com.carloship.craft.sdk.server.hologram;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;

@Getter
public final class HologramLineEntity extends Entity {

    private final HologramLine template;

    public HologramLineEntity(HologramLine template) {
        super(EntityType.ARMOR_STAND);

        this.template = template;

        setInvisible(true);
        setAutoViewable(false);

        editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setMarker(true);
            meta.setSmall(true);
            meta.setHasNoGravity(true);
            meta.setCustomNameVisible(true);
        });
    }

    public void render(Player player) {

        Component component = template.getRenderer().apply(player);

        set(DataComponents.CUSTOM_NAME, component);
    }

    public void renderViewers() {
        getViewers().forEach(this::render);
    }

    public void onLeftClick(Player player) {
        template.getLeftClick().accept(player);
    }

    public void onRightClick(Player player) {
        template.getRightClick().accept(player);
    }
}