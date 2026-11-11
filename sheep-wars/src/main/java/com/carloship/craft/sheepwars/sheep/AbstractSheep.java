package com.carloship.craft.sheepwars.sheep;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;

import java.util.UUID;

public abstract class AbstractSheep extends Entity {

    public AbstractSheep() {
        super(EntityType.SHEEP, UUID.randomUUID());
    }

    public abstract void onHitTarget();

    public abstract void onPlayerHit();

    public abstract void onSpawn();

    public abstract void onThrow();

    public abstract ItemStack getItem();

    public abstract String getNamespace();

    public abstract Component getTitle();

}
