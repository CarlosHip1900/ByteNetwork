package com.carloship.craft.sdk.server.npc;

import com.carloship.craft.sdk.server.hologram.Hologram;
import com.carloship.craft.sdk.server.hologram.HologramManager;
import com.carloship.craft.sdk.server.instance.content.InstanceFeatureManager;
import com.carloship.craft.sdk.server.npc.impl.FakePlayer;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class NPCManager implements InstanceFeatureManager {

    private final Instance instance;

    private final List<FakePlayer> templates = new ArrayList<>();

    public NPCManager(Instance instance) {
        this.instance = instance;
    }

    public void register(FakePlayer fakePlayer) {
        templates.add(fakePlayer);
    }

    public void removerFakePlayer(UUID uuid, HologramManager hologramManager) {
        FakePlayer fakePlayer = templates.stream().filter(fk -> fk.getUUID().equals(uuid)).findAny().orElse(null);
        if (fakePlayer == null) return;

        templates.remove(fakePlayer);

        hologramManager.unregister(fakePlayer.getHologram());
        fakePlayer.destroy();
    }

    public void initHolograms(HologramManager hologramManager) {
        for (FakePlayer fakePlayer : templates) {
            Hologram hologram = fakePlayer.createHologram(instance);

            if (hologram != null) {
                hologramManager.register(hologram);
            }
        }
    }

    @Override
    public void spawnAll() {
        templates.forEach(t -> t.spawn(instance));
    }

    @Override
    public void destroyAll() {
        templates.forEach(FakePlayer::destroy);
    }

}