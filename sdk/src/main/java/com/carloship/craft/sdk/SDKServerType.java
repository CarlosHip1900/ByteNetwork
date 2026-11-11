package com.carloship.craft.sdk;

import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum SDKServerType implements ServerInstanceMode {

    UNKNOWN(null),
    MASTER_LOBBY(SDKServerAPI.MASTER_LOBBY_GAME_ID),
    SHEEP_WARS(SDKServerAPI.SHEEP_WARS_GAME_ID),
    RANK_UP(SDKServerAPI.RANK_UP_GAME_ID);

    private final String gameId;

    @Override
    public String getId() {
        return name().toLowerCase(Locale.ROOT);
    }
}
