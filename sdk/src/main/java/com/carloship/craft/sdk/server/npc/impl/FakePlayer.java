package com.carloship.craft.sdk.server.npc.impl;

import com.carloship.craft.sdk.server.hologram.Hologram;
import com.carloship.craft.sdk.server.hologram.HologramLine;
import com.carloship.craft.sdk.server.instance.content.InstanceContentObject;
import com.carloship.craft.sdk.server.npc.skin.BaseSkinPlayerNPC;
import com.carloship.craft.sdk.server.skin.SkinManager;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.ResolvableProfile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
@Builder
public final class FakePlayer implements InstanceContentObject {

    private final PlayerSkin skin;

    @Builder.Default
    private final boolean lookAtPlayers = true;

    @Builder.Default
    private final double hologramOffset = 2.3;

    private Pos position;

    @Singular("line")
    private final List<HologramLine> lines;

    private final Consumer<BaseNPC.InteractEvent<EntityAttackEvent>> leftClick;
    private final Consumer<BaseNPC.InteractEvent<PlayerEntityInteractEvent>> rightClick;

    // Runtime objects
    private transient BaseSkinPlayerNPC npc;
    private transient Hologram hologram;
    private transient boolean spawned;

    @Override
    public FakePlayer spawn(Instance instance) {

        if (spawned)
            return this;

        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(position, "position");

        npc = new BaseSkinPlayerNPC(new ResolvableProfile(Objects.requireNonNullElse(skin, SkinManager.DEFAULT_NPC_SKIN)));
        npc.setPos(position);

        if (lookAtPlayers) npc.lookClose();
        if (leftClick != null) npc.setAttack(leftClick);
        if (rightClick != null) npc.setInteract(rightClick);

        npc.spawn(instance);

        spawned = true;
        return this;
    }

    public Hologram createHologram(Instance instance) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }

        Hologram hologram = new Hologram(position.add(0, 1.9, 0), instance);

        lines.forEach(hologram::addLine);

        return hologram;
    }

    @Override
    public void destroy() {

        if (!spawned)
            return;

        if (npc != null) {
            npc.remove();
            npc = null;
        }

        if (hologram != null) {
            hologram.destroy();
            hologram = null;
        }

        spawned = false;
    }

    public UUID getUUID(){
        return getNpc().getUuid();
    }
}