package com.carloship.craft.sheepwars.instances.room;

import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.sdk.server.arena.ArenaEngine;
import com.carloship.craft.sheepwars.player.SheepWarPlayer;

public class SheepWarArenaEngine extends ArenaEngine<SheepWarPlayer> {

    protected SheepWarArenaEngine(SDKServer<SheepWarPlayer> server) {
        super(server);
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onTick() {

    }
}
