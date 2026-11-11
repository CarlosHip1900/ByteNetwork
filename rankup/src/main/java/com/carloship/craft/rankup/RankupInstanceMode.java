package com.carloship.craft.rankup;

import com.carloship.craft.sdk.server.instance.ServerInstanceMode;

import java.util.Locale;

public enum RankupInstanceMode implements ServerInstanceMode {

    SPAWN,
    SHOP,
    START_MINE;

    @Override
    public String getId() {
        return name().toLowerCase(Locale.ROOT);
    }
}
