package com.carloship.craft.sdk.server.player.loader;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.util.concurrent.CompletableFuture;

public interface PlayerDataLoader {

    CompletableFuture<?> loadData(BytePlayerCraft player);

}
