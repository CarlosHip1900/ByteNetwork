package com.carloship.craft.sdk.server.player;

import com.carloship.craft.sdk.annotations.SDKModuleInfo;
import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.server.player.database.AbstractPlayerDatabase;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayerModule;

@SDKModuleInfo(
        name = "BytePlayerModule",
        id = "byte_player_module"
)
public class BytePlayerModule extends AbstractPlayerModule<BytePlayerData> {

    private final AbstractPlayerDatabase<BytePlayerData> database;

    public BytePlayerModule(ModuleDependencyPacket packet) {
        super(packet);
        this.database = new AbstractPlayerDatabase<>(BytePlayerData.class, "byte-network", "byte_player","data:byte_player", packet);
    }

    @Override
    public void createModule() {

    }

    @Override
    public void disableModule() {

    }

    @Override
    protected String pubSubChannel() {
        return "player:main:";
    }

    @Override
    public AbstractPlayerDatabase<BytePlayerData> getDatabase() {
        return database;
    }
}
